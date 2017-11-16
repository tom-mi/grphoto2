package de.rfnbrgr.grphoto2

import de.rfnbrgr.grphoto2.domain.DetectedCamera
import de.rfnbrgr.grphoto2.discovery.NetworkCameraFinder
import spock.lang.Specification

class PtpipManualSpec extends Specification {

    Grphoto2 grphoto
    CameraConnection connection

    def setup() {
        grphoto = new Grphoto2()
        NetworkCameraFinder detector = Grphoto2.networkAutodetect()
        detector.start()
        sleep(5000)
        def detectedCameras = detector.stopAndReturnCameras()
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
