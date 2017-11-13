package de.rfnbrgr.grphoto2

import de.rfnbrgr.grphoto2.testutil.MdnsTestService
import org.junit.Rule
import spock.lang.Specification

class Grphoto2NetworkAutodetectSpec extends Specification {

    Grphoto2 grphoto

    static final PTP_TYPE = '_ptp._tcp.local.'
    static final SERVICE_NAME = 'Test service'
    static final PORT = 1234
    static final TEXT = 'sample text'
    static final HOST = '1.2.3.4'

    @Rule
    MdnsTestService mdnsTestService = new MdnsTestService(InetAddress.getByName(HOST))

    def setup() {
        grphoto = new Grphoto2()
    }

    def 'mdns network auto detection works - synchronous usage'() {
        setup:
        def finder = grphoto.networkAutodetect()

        when:
        finder.start()
        mdnsTestService.registerService(PTP_TYPE, SERVICE_NAME, PORT, TEXT)
        Thread.sleep(1000)

        and:
        def cameras = finder.stopAndReturnCameras()

        then:
        cameras.size() > 0
        def camera = cameras.find { it.model == SERVICE_NAME }
        camera
        camera.path == "ptpip:$HOST:$PORT"
    }

    def 'mdns network auto detection works - asynchronous usage'() {
        setup:
        def finder = grphoto.networkAutodetect()
        def cameras = []
        finder.onDetect { it -> cameras << it }

        when:
        finder.start()
        mdnsTestService.registerService(PTP_TYPE, SERVICE_NAME, PORT, TEXT)
        Thread.sleep(1000)

        then:
        cameras.size() > 0
        def camera = cameras.find { it.model == SERVICE_NAME }
        camera
        camera.path == "ptpip:$HOST:$PORT"
    }

}
