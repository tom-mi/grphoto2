package de.rfnbrgr.grphoto2.domain

import groovy.transform.Canonical
import groovy.transform.ToString
import jdk.nashorn.internal.ir.annotations.Immutable

@Immutable
@Canonical
@ToString(includePackage = false)
class ConfigEntry {
    ConfigField field
    def value

    ConfigEntry entryForUpdate(newValue) {
        new ConfigEntry(field, newValue)
    }
}
