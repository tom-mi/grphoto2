package de.rfnbrgr.grphoto2.util

import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.jna.Gphoto2Library

import static de.rfnbrgr.grphoto2.util.GphotoUtil.checkErrorCode

class WidgetChoicesWrapper extends AbstractList<String> {
    final Gphoto2Library lib
    final PointerByReference widget

    WidgetChoicesWrapper(Gphoto2Library lib, PointerByReference widget) {
        this.lib = lib
        this.widget = widget
    }

    @Override
    String get(int i) {
        def valuePointer = new PointerByReference()
        checkErrorCode(lib.gp_widget_get_choice(widget, i, valuePointer))
        valuePointer.value.getString(0)
    }

    @Override
    int size() {
        checkErrorCode(lib.gp_widget_count_choices(widget))
    }
}
