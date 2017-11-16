package de.rfnbrgr.grphoto2

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomjankes.wiremock.WireMockGroovy
import de.rfnbrgr.grphoto2.testutil.SsdpTestService
import org.junit.Rule
import spock.lang.Specification

class Grphoto2SsdpAutodetectSpec extends Specification {

    @Rule
    SsdpTestService ssdpTestService = new SsdpTestService()

    @Rule
    WireMockRule wireMockRule = new WireMockRule(HTTP_PORT)

    def wireMockStub = new WireMockGroovy()

    Grphoto2 grphoto

    final static CANON_ST = 'urn:schemas-canon-com:service:ICPO-SmartPhoneEOSSystemService:1'
    final static HTTP_HOST = '127.0.0.1'
    final static HTTP_PORT = 8080
    final static PATH = '/upnp/CanonDevDesc.xml'
    final static GUID = '01234567-FEED-DEAD-BEEF-001122334455'

    def setup() {
        grphoto = new Grphoto2()
    }

    def 'ssdp network auto detection finds canon camera'() {
        setup:
        ssdpTestService.responsesBySearchTarget[CANON_ST] = canonSsdpResponse("http://$HTTP_HOST:$HTTP_PORT$PATH")
        wireMockStub.stub {
            request {
                method 'GET'
                url PATH
            }
            response {
                status 200
                body this.canonUpnpResponse()
            }
        }

        def finder = grphoto.networkAutodetect()
        finder.withMdns = false

        def cameras = []
        finder.onDetect { it -> cameras << it }

        when:
        finder.start()
        Thread.sleep(5000)

        then:
        cameras.size() > 0
        def camera = cameras.find { it.name == 'Canon EOS 6D' }
        camera
        camera.path =~ /ptpip:\d+.\d+.\d+.\d+:15740/
        camera.model == 'PTP/IP Camera'
        camera.guid == '67:45:23:01:ed:fe:ad:de:be:ef:00:11:22:33:44:55'
    }

    private canonSsdpResponse(String xmlLocation) {
        [
                'HTTP/1.1 200 OK',
                'Cache-Control: max-age=1800',
                'EXT:',
                "Location: $xmlLocation",
                'Server: Camera OS/1.0 UPnP/1.0 Canon Device Discovery/1.0',
                'ST: urn:schemas-canon-com:service:ICPO-SmartPhoneEOSSystemService:1',
                'USN: uuid:00000000-0000-0000-0001-FEEDDEADBEEF::urn:schemas-canon-com:service:ICPO-SmartPhoneEOSSystemService:1',
                '',
                '',
        ].join('\r\n')
    }

    private canonUpnpResponse() {
        """\
<?xml version="1.0"?>
<root xmlns="urn:schemas-upnp-org:device-1-0">
    <specVersion>
        <major>1</major>
        <minor>0</minor>
    </specVersion>
    <URLBase>http://$HTTP_HOST:$HTTP_PORT/upnp/</URLBase>
    <device>
        <deviceType>urn:schemas-upnp-org:device:Basic:1</deviceType>
        <friendlyName>Canon EOS 6D</friendlyName>
        <manufacturer>Canon</manufacturer>
        <manufacturerURL>http://www.canon.com/</manufacturerURL>
        <modelDescription>Canon Digital Camera</modelDescription>
        <modelName>Canon EOS 6D</modelName>
        <serialNumber>12345678</serialNumber>
        <UDN>uuid:00000000-0000-0000-0001-FEEDDEADBEEF</UDN>
        <serviceList>
            <service>
                <serviceType>urn:schemas-canon-com:service:ICPO-SmartPhoneEOSSystemService:1</serviceType>
                <serviceId>urn:schemas-canon-com:serviceId:ICPO-SmartPhoneEOSSystemService-1</serviceId>
                <SCPDURL>CameraSvcDesc.xml</SCPDURL>
                <controlURL>control/CanonCamera/</controlURL>
                <eventSubURL></eventSubURL>
                <ns:X_targetId xmlns:ns="urn:schemas-canon-com:schema-upnp">uuid:$GUID</ns:X_targetId>
                <ns:X_onService xmlns:ns="urn:schemas-canon-com:schema-upnp">0</ns:X_onService>
                <ns:X_deviceUsbId xmlns:ns="urn:schemas-canon-com:schema-upnp">3250</ns:X_deviceUsbId>
                <ns:X_deviceNickname xmlns:ns="urn:schemas-canon-com:schema-upnp">Hoth</ns:X_deviceNickname>
            </service>
        </serviceList>
        <presentationURL>/</presentationURL>
    </device>
</root>
"""
    }

}
