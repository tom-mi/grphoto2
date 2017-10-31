package de.rfnbrgr.grphoto2.util

import javax.jmdns.ServiceInfo

class MdnsInfoParser {

    static extractName(ServiceInfo info) {
        extractCanonName(info) ?: info.name
    }

    private static extractCanonName(ServiceInfo info) {
        info.getPropertyString('nicname.canon.com')
    }

}
