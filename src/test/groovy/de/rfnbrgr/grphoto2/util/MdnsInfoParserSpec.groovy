package de.rfnbrgr.grphoto2.util

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

}
