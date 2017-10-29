package de.rfnbrgr.grphoto2

import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.domain.DetectedCamera
import de.rfnbrgr.grphoto2.jna.Gphoto2Library
import de.rfnbrgr.grphoto2.util.ListWrapper
import groovy.util.logging.Slf4j

import static de.rfnbrgr.grphoto2.util.ErrorUtil.checkErrorCode

@Slf4j
class Grphoto2 {

    private final Gphoto2Library lib
    private final PointerByReference context

    Grphoto2() {
        lib = Gphoto2Library.INSTANCE
        context = lib.gp_context_new()
        lib.gp_context_set_error_func(context, errorFunc, null)
        lib.gp_context_set_message_func(context, messageFunc, null)
    }

    private Gphoto2Library.GPContextErrorFunc errorFunc = new Gphoto2Library.GPContextErrorFunc() {
        @Override
        void apply(Pointer context, Pointer text, Pointer data) {
            log.error("Error callback called: ${text.getString(0)}")
        }
    }

    private Gphoto2Library.GPContextMessageFunc messageFunc = new Gphoto2Library.GPContextMessageFunc() {
        @Override
        void apply(Pointer context, Pointer text, Pointer data) {
            log.info("Messag callback called: ${text.getString(0)}")
        }
    }

    List<DetectedCamera> camera_autodetect() {
        try {
            PointerByReference rawList = new PointerByReference()
            checkErrorCode(lib.gp_list_new(rawList))
            checkErrorCode(lib.gp_camera_autodetect(rawList.value, context.value))
            def listWrapper = new ListWrapper(lib, rawList)
            listWrapper.collect { new DetectedCamera(model: it.name, port: it.value) }
        } finally {
            lib.gp_list_free(rawList)
        }
    }

}
