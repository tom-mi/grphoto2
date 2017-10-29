package de.rfnbrgr.grphoto2.util

import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.jna.Gphoto2Library

import static de.rfnbrgr.grphoto2.util.GphotoUtil.checkErrorCode

class WidgetChildren extends AbstractList<WidgetWrapper> {

    private final Gphoto2Library lib
    private final PointerByReference widget
    private final String parentPath

    WidgetChildren(Gphoto2Library lib, PointerByReference widget, String parentPath) {
        this.lib = lib
        this.widget = widget
        this.parentPath = parentPath
    }

    @Override
    WidgetWrapper get(int i) {
        def childWidget = new PointerByReference()
        checkErrorCode(lib.gp_widget_get_child(widget, i, childWidget))
        childWidget.pointer = childWidget.value
        new WidgetWrapper(lib, childWidget, parentPath)
    }

    @Override
    int size() {
        checkErrorCode(lib.gp_widget_count_children(widget))
    }
}
