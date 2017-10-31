package de.rfnbrgr.grphoto2.util

import de.rfnbrgr.grphoto2.domain.DetectedCamera
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Slf4j

import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

import static de.rfnbrgr.grphoto2.util.MdnsEventParser.extractName

@Slf4j
class NetworkCameraFinder {

    private static final LISTEN_ADDRESSES = [Inet4Address.getByName('0.0.0.0'), Inet6Address.getByName('::')]
    private final jmdnsProcesses
    private final CameraFinderListener listener

    private static class CameraFinderListener implements ServiceListener {

        List<DetectedCamera> cameras = []
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
                event.info.niceTextString
                String name = extractName(event.info)
                log.info("Found service [$name]")

                event.info.inetAddresses.each { address ->
                    String path = 'ptpip:' + address.hostAddress + ':' + event.info.port
                    def camera = new DetectedCamera(name, path)
                    log.debug("Found camera $camera")
                    cameras << camera
                    callbacks.each { it(camera) }
                }
            } catch (Exception e) {
                log.error("Error occurred while adding resolved service", e)
            }
        }
    }

    NetworkCameraFinder() {
        log.debug('Starting mdns discovery...')
        listener = new CameraFinderListener()

        jmdnsProcesses = LISTEN_ADDRESSES.collect { InetAddress address ->
            log.debug("Creating listener on address $address")
            def jmdns = null
            try {
                jmdns = JmDNS.create(address)
                jmdns.addServiceListener('_ptp._tcp.local.', listener)
            } catch (Exception e) {
                log.warn("Could not create listener on address $address:", e)
            }
            log.debug("Created listener on address $address")
            jmdns
        }.findResults { it }
    }

    void onDetect(
            @ClosureParams(value = SimpleType.class, options = 'de.rfnbrgr.grphoto2.domain.DetectedCamera') Closure callback) {
        listener.callbacks << callback
    }

    void stop() {
        log.debug('Closing mdns instances')
        jmdnsProcesses.each { it.close() }
        log.debug('Closed mdns instances')
    }

    List<DetectedCamera> stopAndReturnCameras() {
        stop()
        return listener.cameras
    }
}
