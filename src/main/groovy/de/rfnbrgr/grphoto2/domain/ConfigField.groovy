package de.rfnbrgr.grphoto2.domain

import groovy.transform.Immutable
import groovy.transform.ToString

@Immutable
@ToString(includePackage = false)
class ConfigField {

    final static RELATIVE_STEP_TOLERANCE = 0.001

    String path
    String name
    String label
    ConfigFieldType type
    List<String> choices
    Boolean readOnly
    Float rangeMin
    Float rangeMax
    Float rangeIncrement

    void validateUpdate(newValue) {
        validateReadability()
        validateChoice(newValue)
        validateRange(newValue)
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

    private validateRange(value) {
        if (type == ConfigFieldType.RANGE) {
            if (value < rangeMin - rangeIncrement * RELATIVE_STEP_TOLERANCE ||
                    value > rangeMax + rangeIncrement * RELATIVE_STEP_TOLERANCE) {
                throw new UpdateError("Value $value is outside range [$rangeMin .. $rangeMax]")
            }
            float distanceToStep = ((value - rangeMin) % rangeIncrement)
            if (distanceToStep > rangeIncrement / 2) {
                distanceToStep -= rangeIncrement
            }
            if (distanceToStep.abs() / rangeIncrement > RELATIVE_STEP_TOLERANCE) {
                throw new UpdateError("Value $value is not a valid step in range [$rangeMin .. $rangeMax] with increment $rangeIncrement")
            }
        }
    }
}
