package de.rfnbrgr.grphoto2

import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.domain.Config
import de.rfnbrgr.grphoto2.domain.ConfigEntry
import de.rfnbrgr.grphoto2.domain.ConfigField
import de.rfnbrgr.grphoto2.domain.ConfigFieldType
import de.rfnbrgr.grphoto2.jna.Camera
import de.rfnbrgr.grphoto2.jna.Gphoto2Library
import de.rfnbrgr.grphoto2.util.WidgetWrapper
import groovy.transform.Canonical
import groovy.transform.ToString

import static de.rfnbrgr.grphoto2.jna.Gphoto2Library.CameraWidgetType.GP_WIDGET_SECTION
import static de.rfnbrgr.grphoto2.jna.Gphoto2Library.CameraWidgetType.GP_WIDGET_WINDOW
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
        lib.gp_camera_unref(camera)
    }

    Config readConfig() {
        def window = new PointerByReference()
        lib.gp_camera_get_config(camera, window, context)

        window.pointer = window.value
        def rootWidget = new WidgetWrapper(lib, window)

        new Config(walkWidget(rootWidget))
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
        new ConfigEntry(field, widget.value)
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
        )
    }

}
