package de.rfnbrgr.grphoto2

import de.rfnbrgr.grphoto2.domain.DetectedCamera
import spock.lang.Specification

class Grphoto2Spec extends Specification {

    def 'camera_autodetect works'() {
        setup:
        def grphoto = new Grphoto2()

        when:
        def cameras = grphoto.camera_autodetect()

        then:
        noExceptionThrown()
        cameras.size == 1
        cameras == [new DetectedCamera(model: 'Nikon DSC D750', port: 'usb:001,001')]
    }

}
