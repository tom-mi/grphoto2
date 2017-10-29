package de.rfnbrgr.grphoto2.domain

class Config extends AbstractList<ConfigEntry> {

    private final List<ConfigEntry> entries
    private final Map<String, ConfigEntry> entriesByPath

    Config(List<ConfigEntry> entries) {
        this.entries = entries
        this.entriesByPath = entries.collectEntries{ [(it.field.path): it] }
    }

    ConfigEntry getByPath(String path) {
        entriesByPath.get(path)
    }

    @Override
    ConfigEntry get(int i) {
        return entries[i]
    }

    @Override
    int size() {
        return entries.size()
    }
}
