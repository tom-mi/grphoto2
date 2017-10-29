package de.rfnbrgr.grphoto2.domain

import de.rfnbrgr.grphoto2.jna.Camera
import de.rfnbrgr.grphoto2.jna.Gphoto2Library
import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString(includePackage = false)
class CameraConnection implements Closeable {

    private Gphoto2Library lib
    private Camera camera

    CameraConnection(Gphoto2Library lib, Camera camera) {
        this.lib = lib
        this.camera = camera
    }

    @Override
    void close() throws IOException {
        lib.gp_camera_unref(camera)
    }
}
