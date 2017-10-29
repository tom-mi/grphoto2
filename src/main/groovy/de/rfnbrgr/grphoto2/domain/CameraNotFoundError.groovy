package de.rfnbrgr.grphoto2.domain

class CameraNotFoundError extends Exception {
    CameraNotFoundError(String message) {
        super(message)
    }
}
