package de.rfnbrgr.grphoto2.util

import com.sun.jna.ptr.FloatByReference
import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.Grphoto2
import de.rfnbrgr.grphoto2.jna.Gphoto2Library
import spock.lang.Specification

import static de.rfnbrgr.grphoto2.jna.Gphoto2Library.CameraWidgetType.GP_WIDGET_RANGE
import static de.rfnbrgr.grphoto2.util.GphotoUtil.checkErrorCode

class WidgetRangeWrapperSpec extends Specification {

    def lib = Gphoto2Library.INSTANCE
    Grphoto2 grphoto

    def setup() {
        grphoto = new Grphoto2()
    }

    def cleanup() {
        grphoto.close()
    }

    def 'widget range wrapper works with mocked widget'() {
        // This is necessary as the virtual usb device does not contain a RANGE widget
        setup:
        def widget = new PointerByReference()
        def value = 42f
        def min = 1f
        def max = 100f
        def increment = 2f
        def valuePointer = new FloatByReference(value as float).pointer

        checkErrorCode(lib.gp_widget_new(GP_WIDGET_RANGE, "Test range widget", widget))
        widget.pointer = widget.value
        checkErrorCode(lib.gp_widget_set_range(widget, min, max, increment))
        checkErrorCode(lib.gp_widget_set_value(widget, valuePointer))

        when:
        def wrapper = new WidgetRangeWrapper(lib, widget)

        then:
        wrapper.min == min
        wrapper.max == max
        wrapper.increment == increment

        cleanup:
        lib.gp_widget_unref(widget)
    }
}
