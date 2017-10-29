package de.rfnbrgr.grphoto2

import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.domain.CameraNotFoundError
import de.rfnbrgr.grphoto2.domain.Connection
import de.rfnbrgr.grphoto2.domain.DetectedCamera
import de.rfnbrgr.grphoto2.jna.Camera
import de.rfnbrgr.grphoto2.jna.Gphoto2Library
import de.rfnbrgr.grphoto2.util.ListWrapper
import de.rfnbrgr.grphoto2.util.PortInfoListWrapper
import groovy.util.logging.Slf4j

import static de.rfnbrgr.grphoto2.util.GphotoUtil.checkErrorCode

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
        def rawList = new PointerByReference()
        checkErrorCode(lib.gp_list_new(rawList))
        try {
            checkErrorCode(lib.gp_camera_autodetect(rawList.value, context.value))
            def listWrapper = new ListWrapper(lib, rawList)
            listWrapper.collect { new DetectedCamera(model: it.name, path: it.value) }
        } finally {
            lib.gp_list_free(rawList)
        }
    }

    def connect(String path) {
        withPortList { PortInfoListWrapper portList ->
            def portInfo = portList.find { it.path == path }
            if (!portInfo) {
                throw new CameraNotFoundError("No camera found at path $path")
            }

            Camera.ByReference[] cameraReferenceArray = [new Camera.ByReference()]
            checkErrorCode(lib.gp_camera_new(cameraReferenceArray))
            def camera = cameraReferenceArray[0]
            try {
                checkErrorCode(lib.gp_camera_set_port_info(camera, portInfo.info))
                checkErrorCode(lib.gp_camera_init(camera, context))
                return new Connection(lib, camera)
            } catch (Exception e) {
                lib.gp_camera_unref(camera)
                throw e
            }
        }
    }

    private withPortList(Closure closure) {
        def rawPortList = new PointerByReference()
        try {
            checkErrorCode(lib.gp_port_info_list_new(rawPortList))
            rawPortList.pointer = rawPortList.value
            checkErrorCode(lib.gp_port_info_list_load(rawPortList))
            def portList = new PortInfoListWrapper(lib, rawPortList)
            closure(portList)
        } finally {
            lib.gp_port_info_list_free(rawPortList)
        }
    }
}
