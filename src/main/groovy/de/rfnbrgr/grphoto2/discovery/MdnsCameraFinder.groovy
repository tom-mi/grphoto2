package de.rfnbrgr.grphoto2.discovery

import de.rfnbrgr.grphoto2.domain.DetectedCamera
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Slf4j

import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

import static MdnsInfoParser.extractGuid
import static MdnsInfoParser.extractName

@Slf4j
class MdnsCameraFinder implements CameraFinder {

    private static final LISTEN_ADDRESSES = [Inet4Address.getByName('0.0.0.0'), Inet6Address.getByName('::')]
    private final jmdnsProcesses = []
    private final MdnsListener listener

    private static class MdnsListener implements ServiceListener {

        List<Closure> callbacks = []

        @Override
        void serviceAdded(ServiceEvent event) {
            log.debug("Service added: $event.info")
        }

        @Override
        void serviceRemoved(ServiceEvent event) {
            log.debug("Service removed: $event.info")
        }

        @Override
        void serviceResolved(ServiceEvent event) {
            log.debug("Service resolved: $event.info")
            try {
                String name = extractName(event.info)
                log.debug("Found service [$name]")

                event.info.inetAddresses.each { address ->
                    String path = 'ptpip:' + address.hostAddress + ':' + event.info.port
                    String guid = extractGuid(event.info)
                    def camera = new DetectedCamera(name, path, guid)
                    log.debug("Found camera $camera")
                    callbacks.each { it(camera) }
                }
            } catch (Exception e) {
                log.error("Error occurred while adding resolved service", e)
            }
        }
    }

    MdnsCameraFinder() {
        log.debug('Starting mdns discovery...')
        listener = new MdnsListener()
    }

    @Override
    void start() {


        LISTEN_ADDRESSES.each { InetAddress address ->
            log.debug("Creating listener on address $address")
            try {
                def jmdns = JmDNS.create(address)
                jmdns.addServiceListener('_ptp._tcp.local.', listener)
                jmdnsProcesses << jmdns
            } catch (Exception e) {
                log.warn("Could not create listener on address $address:", e)
            }
            log.debug("Created listener on address $address")
        }
    }

    @Override
    void onDetect(
            @ClosureParams(value = SimpleType.class, options = 'de.rfnbrgr.grphoto2.domain.DetectedCamera') Closure callback) {
        listener.callbacks << callback
    }

    @Override
    void stop() {
        listener.callbacks.clear()
        log.debug('Starting cleanup thread...')
        Thread.start {
            log.debug('Closing mdns instances')
            jmdnsProcesses.each { it.close() }
            log.debug('Closed mdns instances')
        }
    }

}
