package de.rfnbrgr.grphoto2.domain

import groovy.transform.Immutable
import groovy.transform.ToString


abstract class ConfigValue<T> {
    abstract T getValue()
    abstract ConfigValue<T> ofSameType(value)

    static ConfigValue of(Object value) {
        switch (value.class) {
            case String: return new StringValue(value as String)
            case Integer: return new IntegerValue(value as Integer)
            case Float: return new FloatValue(value as Float)
            default: throw new IllegalArgumentException("Value must be of type String, Integer or Float, got ${value.class}")
        }
    }
}

@Immutable
@ToString(includePackage = false)
class StringValue extends ConfigValue<String> {

    final String value

    @Override
    StringValue ofSameType(Object value) {
        return new StringValue(value as String)
    }
}

@Immutable
@ToString(includePackage = false)
class IntegerValue extends ConfigValue<Integer> {

    final Integer value
    @Override
    IntegerValue ofSameType(Object value) {
        return new IntegerValue(value as Integer)
    }
}

@Immutable
@ToString(includePackage = false)
class FloatValue extends ConfigValue<Float> {

    final Float value

    @Override
    FloatValue ofSameType(Object value) {
        return new FloatValue(value as Float)
    }
}
