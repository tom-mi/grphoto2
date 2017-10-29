package de.rfnbrgr.grphoto2

import de.rfnbrgr.grphoto2.domain.ConfigFieldType
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneId

import static spock.util.matcher.HamcrestMatchers.closeTo

class CameraConnectionSpec extends Specification {

    final static PATH = 'usb:001,001'

    Grphoto2 grphoto
    CameraConnection connection

    def setup() {
        grphoto = new Grphoto2()
        connection = grphoto.connect(PATH)
    }

    def cleanup() {
        connection.close()
        grphoto.close()
    }

    def 'config can be read'() {
        when:
        def config = connection.readConfig()

        then:
        // This test setup might need to be adapted if the virtual libgphoto2 usb library is expanded or changed
        config.size() == 22

        with(config.getByPath('/main/status/cameramodel')) {
            field.name == 'cameramodel'
            field.path == '/main/status/cameramodel'
            field.label == 'Camera Model'
            field.type == ConfigFieldType.TEXT
            field.choices == []
            value == 'VC'
        }
        with(config.getByPath('/main/settings/autofocus')) {
            field.name == 'autofocus'
            field.path == '/main/settings/autofocus'
            field.label == 'Autofocus'
            field.type == ConfigFieldType.RADIO
            field.choices == ['On', 'Off']
            value == 'On'
        }
        with(config.getByPath('/main/settings/fastfs')) {
            field.type == ConfigFieldType.TOGGLE
            field.choices == []
            value == 1
        }
        with(config.getByPath('/main/other/5003')) {
            field.type == ConfigFieldType.MENU
            field.choices == ['640x480', '1024x768', '2048x1536']
            value == '640x480'
        }
        with(config.getByPath('/main/settings/datetime')) {
            field.type == ConfigFieldType.DATE
            field.choices == []
        }
        def value = config.getByPath('/main/settings/datetime').getValue()
        // This seems to be a bug in libgphoto's and/or vusb's timezone handling
        def expectedValue = LocalDateTime.now(ZoneId.of('UTC')).atZone(ZoneId.systemDefault()).toEpochSecond()
        value closeTo(expectedValue, 10)

        config.each { println it }
    }
}