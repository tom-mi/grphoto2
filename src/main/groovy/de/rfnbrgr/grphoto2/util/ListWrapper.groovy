package de.rfnbrgr.grphoto2.util

import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.jna.Gphoto2Library
import groovy.transform.Canonical

import static GphotoUtil.checkErrorCode

class ListWrapper extends AbstractList<ListItem> {

    final Gphoto2Library lib
    final PointerByReference list

    ListWrapper(Gphoto2Library lib, PointerByReference list) {
        this.lib = lib
        this.list = list
    }

    @Override
    ListItem get(int i) {
        PointerByReference name = new PointerByReference()
        PointerByReference value = new PointerByReference()

        // TODO Replace with non-deprecated calls. However, that currently returns -2 (BAD ARGUMENT).
        checkErrorCode(lib.gp_list_get_name(list.value, i, name))
        checkErrorCode(lib.gp_list_get_value(list.value, i, value))
        new ListItem(name: name.value.getString(0), value: value.value.getString(0))
    }

    @Override
    int size() {
        // TODO Replace with non-deprecated calls. However, that currently returns -2 (BAD ARGUMENT).
        checkErrorCode(lib.gp_list_count(list.value))
    }
}

@Canonical
class ListItem {
    String name
    String value
}
