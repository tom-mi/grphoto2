package de.rfnbrgr.grphoto2

import de.rfnbrgr.grphoto2.domain.ConfigFieldType
import de.rfnbrgr.grphoto2.domain.GphotoError
import de.rfnbrgr.grphoto2.domain.UpdateError
import spock.lang.Specification
import spock.lang.Unroll

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

        ['gphoto2', '--port', PATH, '--set-config-value', '/main/settings/autofocus=On'].execute().waitFor()
        ['gphoto2', '--port', PATH, '--set-config-value', '/main/settings/fastfs=1'].execute().waitFor()
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
            !field.readOnly
            value == 'VC'
        }
        with(config.getByPath('/main/settings/autofocus')) {
            field.name == 'autofocus'
            field.path == '/main/settings/autofocus'
            field.label == 'Autofocus'
            field.type == ConfigFieldType.RADIO
            field.choices == ['On', 'Off']
            !field.readOnly
            value == 'On'
        }
        with(config.getByPath('/main/settings/fastfs')) {
            field.type == ConfigFieldType.TOGGLE
            field.choices == []
            !field.readOnly
            value == 1
        }
        with(config.getByPath('/main/other/5003')) {
            field.type == ConfigFieldType.MENU
            field.choices == ['640x480', '1024x768', '2048x1536']
            field.readOnly
            value == '640x480'
        }
        with(config.getByPath('/main/settings/datetime')) {
            field.type == ConfigFieldType.DATE
            field.choices == []
            !field.readOnly
        }
        def value = config.getByPath('/main/settings/datetime').getValue()
        // This seems to be a bug in libgphoto's and/or vusb's timezone handling
        def expectedValue = LocalDateTime.now(ZoneId.of('UTC')).atZone(ZoneId.systemDefault()).toEpochSecond()
        value closeTo(expectedValue, 10)

        config.findAll { !it.field.readOnly }.each { println it }
    }

    def 'config can be updated'() {
        when:
        def config = connection.readConfig()

        then:
        config.getByPath('/main/settings/autofocus').value == 'On'

        when:
        def updates = [
                config.getByPath('/main/settings/autofocus').entryForUpdate('Off'),
                config.getByPath('/main/settings/fastfs').entryForUpdate(0),
                config.getByPath('/main/settings/datetime').entryForUpdate(1509329425),
        ]
        connection.updateConfig(updates)

        then:
        noExceptionThrown()

        when:
        def newConfig = connection.readConfig()

        then:
        newConfig.getByPath('/main/settings/autofocus').value == 'Off'
        newConfig.getByPath('/main/settings/fastfs').value == 0
    }

    @Unroll
    def 'config update #path = #newValue fails with #exception - #description'() {
        setup:
        def config = connection.readConfig()

        when:
        def updates = [config.getByPath(path).entryForUpdate(newValue)]
        connection.updateConfig(updates)

        then:
        def ex = thrown(exception)
        println(ex)

        where:
        path                        | newValue || exception   | description
        '/main/status/batterylevel' | '100'    || UpdateError | 'a property known to be readonly cannot be changed'
        '/main/status/manufacturer' | 'ACME'   || GphotoError | 'a property readonly on the camera cannot be changed'
        '/main/settings/autofocus'  | 'auto'   || UpdateError | 'property cannot be changed to invalid choice'
    }

}
