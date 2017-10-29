package de.rfnbrgr.grphoto2.domain

class GphotoError extends RuntimeException {
    final int errorCode

    GphotoError(String message, int errorCode) {
        super(message)
        this.errorCode = errorCode
    }

}
