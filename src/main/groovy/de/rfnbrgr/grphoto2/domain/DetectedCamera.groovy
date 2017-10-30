package de.rfnbrgr.grphoto2.domain

import groovy.transform.Immutable
import groovy.transform.ToString

@Immutable
@ToString(includePackage = false)
class DetectedCamera {
    String model
    String path
}
