package de.rfnbrgr.grphoto2.domain

import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString(includePackage = false )
class ConfigEntry {
    final ConfigField field
    def value

    ConfigEntry(ConfigField field, value) {
        this.field = field
        this.value = value
    }
}
