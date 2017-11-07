package de.rfnbrgr.grphoto2.discovery

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

interface CameraFinder {

    void onDetect(
            @ClosureParams(value = SimpleType.class, options = 'de.rfnbrgr.grphoto2.domain.DetectedCamera') Closure callback)

    void start()

    void stop()
}