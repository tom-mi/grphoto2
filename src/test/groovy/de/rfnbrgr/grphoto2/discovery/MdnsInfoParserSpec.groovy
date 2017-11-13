package de.rfnbrgr.grphoto2.discovery

import spock.lang.Specification
import spock.lang.Unroll

import javax.jmdns.ServiceInfo

class MdnsInfoParserSpec extends Specification {

    @Unroll
    def 'test extractName - [#expectedName]'() {
        setup:
        def info = [
                getPropertyString: { name -> properties.getOrDefault(name, null) },
                getName          : { -> name },
        ] as ServiceInfo

        expect:
        MdnsInfoParser.extractName(info) == expectedName

        where:
        name             | properties                            || expectedName
        'Generic camera' | [:]                                   || 'Generic camera'
        'XYZ'            | ['nicname.canon.com': 'Canon EOS 6D'] || 'Canon EOS 6D'
    }

    @Unroll
    def 'test extractGuid for canon - [#value]'() {
        setup:
        def properties = ['tid.canon.com': value]
        def info = [
                getPropertyString: { name -> properties.getOrDefault(name, null) },
        ] as ServiceInfo

        expect:
        MdnsInfoParser.extractGuid(info) == expectedGuid

        where:
        value                                   | expectedGuid
        '01234567-FEED-DEAD-BEEF-001122334455' | '67:45:23:01:ed:fe:ad:de:be:ef:00:11:22:33:44:55'
        '00000000-0000-0000-0000-000000000000' | '00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00'
        'FEDCBA98-7654-3210-FEDC-BA9876543210' | '98:ba:dc:fe:54:76:10:32:fe:dc:ba:98:76:54:32:10'
    }

    def 'test extractGuid for other'() {
        setup:
        def properties = [:]
        def info = [
                getPropertyString: { name -> properties.getOrDefault(name, null) },
        ] as ServiceInfo

        expect:
        MdnsInfoParser.extractGuid(info) == null
    }

}
