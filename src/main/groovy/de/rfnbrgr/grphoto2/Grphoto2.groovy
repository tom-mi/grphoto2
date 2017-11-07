package de.rfnbrgr.grphoto2

import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.domain.CameraNotFoundError
import de.rfnbrgr.grphoto2.domain.DetectedCamera
import de.rfnbrgr.grphoto2.jna.Camera
import de.rfnbrgr.grphoto2.jna.Gphoto2Library
import de.rfnbrgr.grphoto2.util.ListWrapper
import de.rfnbrgr.grphoto2.discovery.NetworkCameraFinder
import de.rfnbrgr.grphoto2.util.PortInfoListWrapper
import de.rfnbrgr.grphoto2.util.PortInfoWrapper
import groovy.util.logging.Slf4j

import java.nio.ByteBuffer

import static de.rfnbrgr.grphoto2.util.GphotoUtil.checkErrorCode

@Slf4j
class Grphoto2 implements Closeable {

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

    List<DetectedCamera> usbAutodetect() {
        def rawList = new PointerByReference()
        checkErrorCode(lib.gp_list_new(rawList))
        rawList.pointer = rawList.value
        try {
            checkErrorCode(lib.gp_camera_autodetect(rawList, context))
            def listWrapper = new ListWrapper(lib, rawList)
            listWrapper.collect { new DetectedCamera(model: it.name, path: it.value) }
        } finally {
            rawList.pointer = null
            lib.gp_list_free(rawList)
        }
    }

    CameraConnection connect(DetectedCamera camera) {
        connect(camera.path, camera.guid)
    }

    CameraConnection connect(String path) {
        connect(path, null)
    }

    CameraConnection connect(String path, String guid) {
        (CameraConnection) withPortList { PortInfoListWrapper portList ->
            def portInfo = portList.find { it.path == path }
            if (!portInfo) {
                portInfo = portList.find { it.path.endsWith(':') && path.startsWith(it.path) }
                if (portInfo != null) {
                    portInfo.path = path
                }
            }
            if (guid) {
                setPtpIpGuid(guid)
            }
            if (!portInfo) {
                throw new CameraNotFoundError("No camera found at path [$path]")
            }
            connectWithPortInfo(portInfo)
        }
    }

    private setPtpIpGuid(String guid) {
        checkErrorCode(lib.gp_setting_set(ByteBuffer.wrap('gphoto'.bytes), ByteBuffer.wrap('model'.bytes), ByteBuffer.wrap('PTP/IP Camera'.bytes)))
        checkErrorCode(lib.gp_setting_set(ByteBuffer.wrap('ptp_ip'.bytes), ByteBuffer.wrap('guid'.bytes), ByteBuffer.wrap(guid.bytes)))
    }

    private CameraConnection connectWithPortInfo(PortInfoWrapper portInfo) {
        Camera.ByReference[] cameraReferenceArray = [new Camera.ByReference()]
        checkErrorCode(lib.gp_camera_new(cameraReferenceArray))
        def camera = cameraReferenceArray[0]
        try {
            checkErrorCode(lib.gp_camera_set_port_info(camera, portInfo.info))
            checkErrorCode(lib.gp_camera_init(camera, context))
            return new CameraConnection(lib, context, camera)
        } catch (Exception e) {
            lib.gp_camera_unref(camera)
            throw e
        }
    }

    private withPortList(Closure closure) {
        def rawPortList = new PointerByReference()
        try {
            checkErrorCode(lib.gp_port_info_list_new(rawPortList))
            // Otherwise, a segfault occurs
            rawPortList.pointer = rawPortList.value
            checkErrorCode(lib.gp_port_info_list_load(rawPortList))
            def portList = new PortInfoListWrapper(lib, rawPortList)
            closure(portList)
        } finally {
            // This is required to avoid sporadic IllegalStateException due to the workaround above
            rawPortList.pointer = null
            lib.gp_port_info_list_free(rawPortList)
        }
    }

    static NetworkCameraFinder networkAutodetect() {
        new NetworkCameraFinder()
    }

    @Override
    void close() throws IOException {
        lib.gp_context_unref(context)
    }

}
