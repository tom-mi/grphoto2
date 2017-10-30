package de.rfnbrgr.grphoto2

import de.rfnbrgr.grphoto2.domain.CaptureType
import spock.lang.Specification

class CaptureManualSpec extends Specification {

    Grphoto2 grphoto
    CameraConnection connection

    def setup() {
        grphoto = new Grphoto2()
        def detectedCameras = grphoto.camera_autodetect()
        assert detectedCameras.size() > 0: 'Please connect a camera!'
        connection = grphoto.connect(detectedCameras.first())
    }

    def cleanup() {
        connection?.close()
        grphoto.close()
    }

    def 'capture works'() {
        when:
        def file = connection.capture(CaptureType.IMAGE)

        then:
        noExceptionThrown()
        file
        println file
    }

}
