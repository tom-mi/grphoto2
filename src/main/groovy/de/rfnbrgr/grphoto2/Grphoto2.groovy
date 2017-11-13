package de.rfnbrgr.grphoto2

import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.domain.CameraNotFoundError
import de.rfnbrgr.grphoto2.domain.DetectedCamera
import de.rfnbrgr.grphoto2.jna.Camera
import de.rfnbrgr.grphoto2.jna.CameraAbilities
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
        (CameraConnection) withPortList { PortInfoListWrapper portList ->
            def index = checkErrorCode(lib.gp_port_info_list_lookup_path(portList.list, camera.path))
            def portInfoRef = new PointerByReference()
            checkErrorCode(lib.gp_port_info_list_get_info(portList.list, index, portInfoRef))
            def portInfo = new PortInfoWrapper(lib, new Gphoto2Library.GPPortInfo(portInfoRef.value))
            //portInfoRef.pointer = portInfoRef.value


            def abilitiesList = new PointerByReference()
            def abilities = new CameraAbilities()
            checkErrorCode(lib.gp_abilities_list_new(abilitiesList))
            abilitiesList.pointer = abilitiesList.value
            checkErrorCode(lib.gp_abilities_list_load(abilitiesList, context))
            def modelIndex = checkErrorCode(lib.gp_abilities_list_lookup_model(abilitiesList, camera.model))
            checkErrorCode(lib.gp_abilities_list_get_abilities(abilitiesList, modelIndex, abilities))

            println '-------'
            println new String(abilities.model)
            println abilities.port
            println abilities.status
            println new String(abilities.library)
            println abilities.device_type
            println '-------'

            def abilitiesByValue = new CameraAbilities.ByValue(abilities.pointer)
            //def abilitiesByValue = //Structure.newInstance(CameraAbilities.ByValue.class, abilities.pointer)
            //           println '-------'
            //           println abilitiesByValue.model
            //           println abilitiesByValue.port
            //           println abilitiesByValue.status
            //           println abilitiesByValue.library
            //           println abilitiesByValue.device_type
            //           println '-------'
            //Structure.newInstance(CameraAbilities.ByValue, abilities.pointer)
//            abilitiesByValue.pointer = abilities.pointer

            println portInfo.path
//            if (!portInfo) {
            //               throw new CameraNotFoundError("No camera found at path [$path]")
            //          }

            if (camera.guid) {
                setPtpIpGuid(camera.guid)
            }
            connectWithPortInfoAndAbilities(portInfo, abilitiesByValue)
        }
    }

    private setPtpIpGuid(String guid) {
//        checkErrorCode(lib.gp_setting_set(byteBuffer('gphoto2'), byteBuffer('model'), byteBuffer('PTP/IP Camera')))
        checkErrorCode(lib.gp_setting_set(byteBuffer('ptp2_ip'), byteBuffer('guid'), byteBuffer(guid)))
    }

    private static ByteBuffer byteBuffer(String s) {
        def bytes = new byte[s.bytes.length + 1]
        bytes[s.bytes.length] = 0
        System.arraycopy(s.bytes, 0, bytes, 0, s.bytes.length)
        ByteBuffer.wrap(bytes)
    }

    private CameraConnection connectWithPortInfoAndAbilities(PortInfoWrapper portInfo, CameraAbilities.ByValue abilities) {
        Camera.ByReference[] cameraReferenceArray = [new Camera.ByReference()]
        checkErrorCode(lib.gp_camera_new(cameraReferenceArray))
        def camera = cameraReferenceArray[0]
        try {
            checkErrorCode(lib.gp_camera_set_abilities(camera, abilities))
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
