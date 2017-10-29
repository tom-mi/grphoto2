package de.rfnbrgr.grphoto2.domain

import de.rfnbrgr.grphoto2.jna.Camera
import de.rfnbrgr.grphoto2.jna.Gphoto2Library

class Connection implements Closeable {

    private Gphoto2Library lib
    private Camera camera

    Connection(Gphoto2Library lib, Camera camera) {
        this.lib = lib
        this.camera = camera
    }

    @Override
    void close() throws IOException {
        lib.gp_camera_unref(camera)
    }
}
