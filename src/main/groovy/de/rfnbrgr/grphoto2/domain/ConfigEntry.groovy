package de.rfnbrgr.grphoto2.domain

import groovy.transform.Immutable
import groovy.transform.ToString

interface ConfigEntry {
    ConfigField getField()
    def getValue()
    ConfigEntry entryForUpdate(newValue)
}

@Immutable
@ToString(includePackage = false)
class StringConfigEntry implements ConfigEntry {
    ConfigField field
    String value

    @Override
    ConfigEntry entryForUpdate(newValue) {
        field.validateUpdate(newValue)
        return new StringConfigEntry(field, newValue as String)
    }
}

@Immutable
@ToString(includePackage = false)
class IntegerConfigEntry implements ConfigEntry {
    ConfigField field
    Integer value

    @Override
    ConfigEntry entryForUpdate(newValue) {
        field.validateUpdate(newValue)
        return new IntegerConfigEntry(field, newValue as Integer)
    }
}

@Immutable
@ToString(includePackage = false)
class FloatConfigEntry implements ConfigEntry {
    ConfigField field
    Float value

    @Override
    ConfigEntry entryForUpdate(newValue) {
        field.validateUpdate(newValue)
        return new FloatConfigEntry(field, newValue as Float)
    }
}
