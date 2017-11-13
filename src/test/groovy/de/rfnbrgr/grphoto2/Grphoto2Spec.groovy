package de.rfnbrgr.grphoto2

import de.rfnbrgr.grphoto2.domain.CameraNotFoundError
import de.rfnbrgr.grphoto2.domain.DetectedCamera
import spock.lang.Specification

class Grphoto2Spec extends Specification {

    final static PATH = 'usb:001,001'
    final static MODEL = 'Nikon DSC D750'

    Grphoto2 grphoto

    def setup() {
        grphoto = new Grphoto2()
    }

    def cleanup() {
        grphoto.close()
    }

    def 'usb auto detect works'() {
        when:
        def cameras = grphoto.usbAutodetect()

        then:
        noExceptionThrown()
        cameras.size == 1
        cameras == [new DetectedCamera(model: MODEL, path: PATH)]
    }

    def 'connect to camera - throws exception for invalid path'() {
        when:
        grphoto.connect(new DetectedCamera(MODEL, 'usb:42,42', null))

        then:
        thrown(CameraNotFoundError)
    }

    def 'connect to camera - works with valid DetectedCamera'() {
        when:
        def connection = grphoto.connect(new DetectedCamera(MODEL, PATH, null))

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

    def 'connect to camera - times out for DetectedCamera which matches only by prefix'() {
        when:
        def connection = grphoto.connect(new DetectedCamera(model: MODEL, path: 'ptpip:127.0.0.1', guid: '67:45:23:01:ed:fe:ad:de:be:ef:00:11:22:33:44:55'))

        then:
        noExceptionThrown()
        connection instanceof CameraConnection

        cleanup:
        connection?.close()
    }
}
