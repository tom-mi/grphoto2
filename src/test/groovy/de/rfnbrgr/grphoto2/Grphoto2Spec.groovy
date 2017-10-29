package de.rfnbrgr.grphoto2

import de.rfnbrgr.grphoto2.domain.CameraNotFoundError
import de.rfnbrgr.grphoto2.domain.Connection
import de.rfnbrgr.grphoto2.domain.DetectedCamera
import spock.lang.Specification

class Grphoto2Spec extends Specification {

    final PATH = 'usb:001,001'
    final MODEL = 'Nikon DSC D750'

    Grphoto2 grphoto

    def setup() {
        grphoto = new Grphoto2()
    }

    def 'camera_autodetect works'() {
        when:
        def cameras = grphoto.camera_autodetect()

        then:
        noExceptionThrown()
        cameras.size == 1
        cameras == [new DetectedCamera(model: MODEL, path: PATH)]
    }

    def 'connect co camera - works for valid path'() {
        when:
        def connection = grphoto.connect(PATH)

        then:
        noExceptionThrown()
        connection instanceof Connection

        cleanup:
        connection.close()
    }

    def 'connect to camera - throws exception for invalid path'() {
        when:
        grphoto.connect('usb:42,42')

        then:
        thrown(CameraNotFoundError)
    }
}
