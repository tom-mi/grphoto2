package de.rfnbrgr.grphoto2.domain

import groovy.transform.Immutable
import groovy.transform.ToString

@Immutable
@ToString(includePackage = false)
class ConfigEntry {
    final ConfigField field
    final ConfigValue valueWrapper

    def getValue() {
        valueWrapper.value
    }

    ConfigEntry entryForUpdate(newValue) {
        field.validateUpdate(newValue)
        new ConfigEntry(field, valueWrapper.ofSameType(newValue))
    }
}
