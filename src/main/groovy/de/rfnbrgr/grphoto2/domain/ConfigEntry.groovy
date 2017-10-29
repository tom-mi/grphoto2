package de.rfnbrgr.grphoto2.domain

import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString(includePackage = false )
class ConfigEntry {
    final ConfigField field
    String value

    ConfigEntry(ConfigField field, String value) {
        this.field = field
        this.value = value
    }
}
