package de.rfnbrgr.grphoto2.util

import de.rfnbrgr.grphoto2.domain.GphotoError
import de.rfnbrgr.grphoto2.jna.Gphoto2Library

class GphotoUtil {

    static int checkErrorCode(int errorCode) {
        if (errorCode < 0) {
            def reason = determineReason(errorCode) ?: "unknown reason"
            def message = "Got nonzero return code $errorCode: $reason"
            throw new GphotoError(message, errorCode)
        }
        errorCode
    }

    private static determineReason(int errorCode) {
        Gphoto2Library.declaredFields
                .findAll { !it.synthetic }
                .findAll { it.name =~ /^GP_ERROR_/ }
                .find{ Gphoto2Library."$it.name" == errorCode }?.name
    }
}
