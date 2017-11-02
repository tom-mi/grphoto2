package de.rfnbrgr.grphoto2

import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.domain.*
import de.rfnbrgr.grphoto2.jna.Camera
import de.rfnbrgr.grphoto2.jna.CameraFilePath
import de.rfnbrgr.grphoto2.jna.Gphoto2Library
import de.rfnbrgr.grphoto2.util.WidgetWrapper
import groovy.transform.Canonical
import groovy.transform.ToString

import static de.rfnbrgr.grphoto2.jna.Gphoto2Library.CameraCaptureType.GP_CAPTURE_IMAGE
import static de.rfnbrgr.grphoto2.jna.Gphoto2Library.CameraWidgetType.GP_WIDGET_SECTION
import static de.rfnbrgr.grphoto2.jna.Gphoto2Library.CameraWidgetType.GP_WIDGET_WINDOW
import static de.rfnbrgr.grphoto2.util.GphotoUtil.checkErrorCode
import static de.rfnbrgr.grphoto2.util.WidgetTypeUtil.mapCameraWidgetType

@Canonical
@ToString(includePackage = false)
class CameraConnection implements Closeable {

    private Gphoto2Library lib
    private PointerByReference context
    private Camera camera

    CameraConnection(Gphoto2Library lib, PointerByReference context, Camera camera) {
        this.lib = lib
        this.context = context
        this.camera = camera
    }

    @Override
    void close() throws IOException {
        lib.gp_camera_exit(camera, context)
        // TODO gp_camara_unref required? It sometimes causes IllegalStateExceptions
    }

    Config readConfig() {
        (Config) withRootWidget { WidgetWrapper rootWidget ->
            new Config(walkWidget(rootWidget))
        }
    }

    private walkWidget(WidgetWrapper widget) {
        def results = []

        if (!(widget.type in [GP_WIDGET_WINDOW, GP_WIDGET_SECTION])) {
            results << mapWidget(widget)
        }

        widget.children.each { child ->
            results += walkWidget(child)
        }

        results
    }

    private static mapWidget(WidgetWrapper widget) {
        def field = mapField(widget)
        def value = ConfigValue.of(widget.value)
        new ConfigEntry(field, value)
    }

    private static mapField(WidgetWrapper widget) {
        def type = mapCameraWidgetType(widget.type)
        def choices = []
        if (type in [ConfigFieldType.RADIO, ConfigFieldType.MENU]) {
            choices = widget.choices.toList()
        }
        new ConfigField(
                name: widget.name,
                path: widget.path,
                label: widget.label,
                type: type,
                choices: choices,
                readOnly: widget.readOnly as Boolean,
                rangeMin: type == ConfigFieldType.RANGE ? widget.range.min : null,
                rangeMax: type == ConfigFieldType.RANGE ? widget.range.max : null,
                rangeIncrement: type == ConfigFieldType.RANGE ? widget.range.increment : null,
        )
    }

    void updateConfig(List<ConfigEntry> entries) {
        withRootWidget { WidgetWrapper rootWidget ->
            entries.each { entry ->
                def widgetToUpdate = rootWidget.getByPath(entry.field.path)
                widgetToUpdate.setValue(entry.value)
            }
            checkErrorCode(lib.gp_camera_set_config(camera, rootWidget.widget, context))
        }
    }

    private withRootWidget(Closure closure) {
        def window = new PointerByReference()
        checkErrorCode(lib.gp_camera_get_config(camera, window, context))
        try {
            window.pointer = window.value
            def rootWidget = new WidgetWrapper(lib, window)
            closure(rootWidget)
        } finally {
            lib.gp_widget_unref(window)
        }
    }

    CameraFile capture_image() {
        def path = new CameraFilePath()
        checkErrorCode(lib.gp_camera_capture(camera, GP_CAPTURE_IMAGE, path, context))
        def folder = new String(path.folder)
        def name = new String(path.name)
        new CameraFile(folder: folder, name: name)
    }

}
