package de.rfnbrgr.grphoto2.util

import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.jna.Gphoto2Library
import groovy.transform.Memoized

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
    WidgetChoicesWrapper getChoices() {
        new WidgetChoicesWrapper(lib, widget)
    }

    @Memoized
    WidgetChildren getChildren() {
        new WidgetChildren(lib, widget, path)
    }
}
