package de.rfnbrgr.grphoto2.util

import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.jna.Gphoto2Library

import static GphotoUtil.checkErrorCode

class PortInfoListWrapper extends AbstractList<PortInfoWrapper> {

    final Gphoto2Library lib
    final PointerByReference list

    PortInfoListWrapper(Gphoto2Library lib, PointerByReference list) {
        super()
        this.lib = lib
        this.list = list
    }

    @Override
    PortInfoWrapper get(int i) {
        def rawInfo = new PointerByReference()
        checkErrorCode(lib.gp_port_info_list_get_info(list, i, rawInfo))
        return new PortInfoWrapper(lib, new Gphoto2Library.GPPortInfo(rawInfo.value))
    }

    @Override
    int size() {
        return checkErrorCode(lib.gp_port_info_list_count(list))
    }
}
