package de.rfnbrgr.grphoto2.discovery

class CanonUtil {
    static guidFromUuid(String uuid) {
        def parts = uuid.split('-').collect { part -> part.split(/(?<=\G.{2})/) }

        (parts[0].reverse() + parts[1].reverse() + parts[2].reverse() + parts[3] + parts[4]).join(':').toLowerCase()
    }
}
