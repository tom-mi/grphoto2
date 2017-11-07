package de.rfnbrgr.grphoto2.discovery

import javax.jmdns.ServiceInfo

class MdnsInfoParser {

    static extractName(ServiceInfo info) {
        extractCanonName(info) ?: info.name
    }

    private static extractCanonName(ServiceInfo info) {
        info.getPropertyString('nicname.canon.com')
    }

    static extractGuid(ServiceInfo info) {
        extractCanonGuid(info) ?: null
    }

    private static extractCanonGuid(ServiceInfo info) {
        def tid = info.getPropertyString('tid.canon.com')
        if (tid == null) {
            return null
        }
        def parts = tid.split('-').collect { part -> part.split(/(?<=\G.{2})/)}

        (parts[0].reverse() + parts[1].reverse() + parts[2].reverse() + parts[3] + parts[4]).join(':').toLowerCase()
    }

}
