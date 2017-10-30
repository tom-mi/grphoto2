package de.rfnbrgr.grphoto2

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
        def file = connection.capture_image()

        then:
        noExceptionThrown()
        file
        println file
    }

}
