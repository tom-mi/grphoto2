package de.rfnbrgr.grphoto2.domain

import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString(includePackage = false)
class DetectedCamera {
    String model
    String path
}
