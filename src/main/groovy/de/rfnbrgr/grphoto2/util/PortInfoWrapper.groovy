package de.rfnbrgr.grphoto2.util

import com.sun.jna.ptr.PointerByReference
import de.rfnbrgr.grphoto2.jna.Gphoto2Library

import static GphotoUtil.checkErrorCode

class PortInfoWrapper {

    final Gphoto2Library lib
    final Gphoto2Library.GPPortInfo info

    PortInfoWrapper(Gphoto2Library lib, Gphoto2Library.GPPortInfo info) {
        this.lib = lib
        this.info = info
    }

    String getPath() {
        def valuePointer = new PointerByReference()
        checkErrorCode(lib.gp_port_info_get_path(info, valuePointer))
        return valuePointer.value.getString(0)
    }

    void setPath(String path) {
        checkErrorCode(lib.gp_port_info_set_path(info, path))
    }
}
