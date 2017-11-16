package de.rfnbrgr.grphoto2.discovery

import de.rfnbrgr.grphoto2.util.UdpServer
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Slf4j

@Slf4j
class SsdpCameraFinder implements CameraFinder {

    final List<Closure> callbacks = []
    private final InetAddress SSDP_BROADCAST_ADDRESS = InetAddress.getByName('239.255.255.250')
    private final int SSDP_PORT = 1900

    private final SEARCH_TARGETS = [
            'urn:schemas-canon-com:service:ICPO-SmartPhoneEOSSystemService:1',
    ]

    UdpServer server

    @Override
    void onDetect(
            @ClosureParams(value = SimpleType.class, options = 'de.rfnbrgr.grphoto2.domain.DetectedCamera') Closure callback) {
        callbacks << callback
    }

    def ssdpResponseHandler = { InetAddress address, int port, String data ->
        def camera = SsdpResponseHandler.handleSsdpResponse(address, port, data)
        if (camera) {
            log.debug("Found camera $camera")
            callbacks.each{ callback -> callback(camera) }
        }
    }

    @Override
    void start() {
        server = new UdpServer()
        server.handler = ssdpResponseHandler
        server.start()

        SEARCH_TARGETS.each { sendSsdpSearchRequest(it) }
    }

    private sendSsdpSearchRequest(String searchTarget) {
        def request = [
                'M-SEARCH * HTTP/1.1',
                "Host: $SSDP_BROADCAST_ADDRESS.hostAddress:$SSDP_PORT",
                'MAN: "ssdp:discover"',
                'MX: 3',
                "ST: $searchTarget",
                '',
                '',
        ].join('\r\n')
        server.send(SSDP_BROADCAST_ADDRESS, SSDP_PORT, request)
    }

    @Override
    void stop() {
        server.stop()
    }
}
