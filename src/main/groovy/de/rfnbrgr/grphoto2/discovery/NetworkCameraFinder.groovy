package de.rfnbrgr.grphoto2.discovery

import de.rfnbrgr.grphoto2.domain.DetectedCamera
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Slf4j

@Slf4j
class NetworkCameraFinder {

    private final List<CameraFinder> finders = []
    boolean withMdns = true
    boolean withSsdp = true

    private final List<Closure> callbacks = []
    private final List<DetectedCamera> cameras = []

    void start() {
        if (withMdns) {
            finders << new MdnsCameraFinder()
        }
        if (withSsdp) {
            // todo
        }
        if (finders.size() == 0) {
            throw new IllegalStateException("All finders are disabled. Cannot discover anything.")
        }

        finders.each{ finder ->
            finder.onDetect { camera ->
                callbacks.each { callback ->
                    callback(camera)
                }
                cameras << camera
            }
        }

        finders.each{ it.start() }
    }

    void onDetect(
            @ClosureParams(value = SimpleType.class, options = 'de.rfnbrgr.grphoto2.domain.DetectedCamera') Closure callback) {
        callbacks << callback
    }

    void stop() {
        finders.each{ it.stop() }
    }

    List<DetectedCamera> stopAndReturnCameras() {
        callbacks.clear()
        List<DetectedCamera> result = cameras.clone() as List<DetectedCamera>
        stop()
        return result
    }
}
