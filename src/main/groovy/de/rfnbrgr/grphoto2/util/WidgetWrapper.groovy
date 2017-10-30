package de.rfnbrgr.grphoto2.util

import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.jna.Gphoto2Library
import groovy.transform.Memoized

import static de.rfnbrgr.grphoto2.jna.Gphoto2Library.CameraWidgetType.*
import static de.rfnbrgr.grphoto2.util.GphotoUtil.checkErrorCode

class WidgetWrapper {

    final Gphoto2Library lib
    final PointerByReference widget
    final String parentPath


    WidgetWrapper(Gphoto2Library lib, PointerByReference widget, String parentPath) {
        this.lib = lib
        this.widget = widget
        this.parentPath = parentPath
    }

    WidgetWrapper(Gphoto2Library lib, PointerByReference widget) {
        this(lib, widget, '')
    }

    @Memoized
    String getName() {
        def valuePointer = new PointerByReference()
        checkErrorCode(lib.gp_widget_get_name(widget, valuePointer))
        valuePointer.value.getString(0)
    }

    String getPath() {
        parentPath + '/' + name
    }

    @Memoized
    int getType() {
        def valuePointer = new IntByReference()
        checkErrorCode(lib.gp_widget_get_type(widget, valuePointer))
        valuePointer.value
    }

    @Memoized
    String getLabel() {
        def valuePointer = new PointerByReference()
        checkErrorCode(lib.gp_widget_get_label(widget, valuePointer))
        valuePointer.value.getString(0)
    }

    @Memoized
    def getValue() {
        def valuePointer = new PointerByReference()
        checkErrorCode(lib.gp_widget_get_value(widget, valuePointer.pointer))
        readValueWithCorrectType(valuePointer)
    }

    private readValueWithCorrectType(PointerByReference valuePointer) {
        switch (type) {
            case [GP_WIDGET_TEXT, GP_WIDGET_RADIO, GP_WIDGET_MENU]:
                return valuePointer.pointer.getPointer(0).getString(0)
            case [GP_WIDGET_DATE, GP_WIDGET_TOGGLE]:
                return valuePointer.pointer.getInt(0)
            case [GP_WIDGET_RANGE]:
                // TODO check if that actually works
                return valuePointer.pointer.getFloat(0)
        }

    }

    @Memoized
    WidgetChoicesWrapper getChoices() {
        new WidgetChoicesWrapper(lib, widget)
    }

    @Memoized
    def getReadOnly() {
        def valuePointer = new IntByReference()
        checkErrorCode(lib.gp_widget_get_readonly(widget, valuePointer))
        valuePointer.value
    }

    @Memoized
    WidgetChildren getChildren() {
        new WidgetChildren(lib, widget, path)
    }
}
