package de.rfnbrgr.grphoto2.discovery

import de.rfnbrgr.grphoto2.domain.DetectedCamera
import groovy.util.logging.Slf4j

@Slf4j
class SsdpResponseHandler {

    static final CANON_PTPIP_PORT = 15740

    static handleSsdpResponse(InetAddress address, int port, String rawResponse) {
        def response = parseRawResponse(rawResponse)
        if (response) {
            switch (response.ST) {
                case 'urn:schemas-canon-com:service:ICPO-SmartPhoneEOSSystemService:1':
                    return handleCanonResponse(address, port, response)
            }
        }
    }

    private static handleCanonResponse(InetAddress address, int port, response) {
        log.debug("Trying to parse response $response with canon handler")
        def location = response.Location
        try {
            if (location) {
                def upnpData = new XmlSlurper().parse(location as String)
                def name = upnpData.device.friendlyName.text() as String
                def uuid = upnpData.device.serviceList.service.'X_targetId'.text().split(':').last()
                def guid = CanonUtil.guidFromUuid(uuid)
                return new DetectedCamera(name: name, model: NetworkCameraFinder.PTP_IP_MODEL,
                        path: "ptpip:$address.hostAddress:$CANON_PTPIP_PORT", guid: guid)
            }
        } catch (Exception e) {
            log.warn('Failed to parse response', e)
        }
    }

    private static parseRawResponse(String response) {
        def lines = response.readLines()*.trim().findAll()
        if (lines.size() < 2) {
            return null
        }
        def header = lines.first()
        lines.tail().collectEntries { line ->
            if (line.contains(':')) {
                def (key, value) = line.split(':', 2)
                [(key): value.trim()]
            } else {
                [:]
            }
        } + [header: header]
    }
}
