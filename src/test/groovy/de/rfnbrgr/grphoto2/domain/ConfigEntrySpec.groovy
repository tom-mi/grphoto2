package de.rfnbrgr.grphoto2.domain

import spock.lang.Specification
import spock.lang.Unroll

class ConfigEntrySpec extends Specification {

    def 'validation of range field allows valid value'() {
        when:
        def newEntry = entry.entryForUpdate(7f)

        then:
        newEntry.value == 7f
        newEntry.field == entry.field
    }

    @Unroll
    def 'validation fails for value #newValue outside range'() {
        when:
        entry.entryForUpdate(newValue)

        then:
        def ex = thrown(UpdateError)
        println ex

        where:
        newValue << [0.5f, 10.5f, 13f]
    }

    def 'validation fails for value between valid steps'() {
        when:
        entry.entryForUpdate(5f)

        then:
        def ex = thrown(UpdateError)
        println ex
    }

    @Unroll
    def 'validation succeeds for value #newValue sufficiently close to valid steps'() {
        when:
        def newEntry = entry.entryForUpdate(newValue)

        then:
        newEntry.value == newValue

        where:
        newValue << [4.0001f, 3.9999f, 0.9999f, 10.0001f]
    }

    private static getEntry() {
        def field = new ConfigField('/overall/speed', 'speed', 'Speed', ConfigFieldType.RANGE, [], false, 1f, 10f, 3f)
        new ConfigEntry(field, new FloatValue(4f))
    }
}
