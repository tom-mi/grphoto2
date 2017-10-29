package de.rfnbrgr.grphoto2

import de.rfnbrgr.grphoto2.domain.CameraNotFoundError
import de.rfnbrgr.grphoto2.domain.CameraConnection
import de.rfnbrgr.grphoto2.domain.DetectedCamera
import spock.lang.Specification

class Grphoto2Spec extends Specification {

    final static PATH = 'usb:001,001'
    final static MODEL = 'Nikon DSC D750'

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
        connection instanceof CameraConnection

        cleanup:
        connection?.close()
    }

    def 'connect to camera - throws exception for invalid path'() {
        when:
        grphoto.connect('usb:42,42')

        then:
        thrown(CameraNotFoundError)
    }

    def 'connect to camera - works with valid DetectedCamera'() {
        when:
        def connection = grphoto.connect(new DetectedCamera(MODEL, PATH))

        then:
        noExceptionThrown()
        connection instanceof CameraConnection

        cleanup:
        connection?.close()
    }

    def 'connect to camera - throws exception for DetectedCamera with invalid path'() {
        when:
        grphoto.connect(new DetectedCamera(model: MODEL, path: 'usb:42,42'))

        then:
        thrown(CameraNotFoundError)
    }
}
