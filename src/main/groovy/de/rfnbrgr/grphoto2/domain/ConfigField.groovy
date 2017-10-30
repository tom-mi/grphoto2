package de.rfnbrgr.grphoto2.domain

import groovy.transform.Canonical
import groovy.transform.ToString
import jdk.nashorn.internal.ir.annotations.Immutable

@Immutable
@Canonical
@ToString(includePackage = false)
class ConfigField {

    String path
    String name
    String label
    ConfigFieldType type
    List<String> choices
    Boolean readOnly

    void validateUpdate(newValue) {
        validateReadability()
        validateChoice(newValue)
    }

    private validateReadability() {
        if (readOnly) {
            throw new UpdateError("Cannot update a readOnly field")
        }
    }

    private validateChoice(value) {
        if (type in [ConfigFieldType.MENU, ConfigFieldType.RADIO]) {
            if (!(value in choices)) {
                throw new UpdateError("Cannot update field with choices $choices to invalid value $value")
            }
        }
    }
}
