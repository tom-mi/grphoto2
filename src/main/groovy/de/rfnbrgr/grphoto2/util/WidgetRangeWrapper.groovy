package de.rfnbrgr.grphoto2.util

import com.sun.jna.ptr.FloatByReference
import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.jna.Gphoto2Library

class WidgetRangeWrapper {
    final Gphoto2Library lib
    final float min
    final float max
    final float increment

    WidgetRangeWrapper(Gphoto2Library lib, PointerByReference widget) {
        this.lib = lib

        def minRef = new FloatByReference()
        def maxRef = new FloatByReference()
        def incrementRef = new FloatByReference()

        lib.gp_widget_get_range(widget, minRef, maxRef, incrementRef)
        min = minRef.value
        max = maxRef.value
        increment = incrementRef.value
    }

}
