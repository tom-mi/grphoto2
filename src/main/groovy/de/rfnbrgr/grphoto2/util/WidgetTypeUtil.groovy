package de.rfnbrgr.grphoto2.util

import de.rfnbrgr.grphoto2.domain.ConfigFieldType

import static ConfigFieldType.*
import static de.rfnbrgr.grphoto2.jna.Gphoto2Library.CameraWidgetType.*

class WidgetTypeUtil {

    static final MAPPING = [
            (GP_WIDGET_WINDOW) : WINDOW,
            (GP_WIDGET_SECTION): SECTION,
            (GP_WIDGET_TEXT)   : TEXT,
            (GP_WIDGET_RANGE)  : RANGE,
            (GP_WIDGET_TOGGLE) : TOGGLE,
            (GP_WIDGET_RADIO)  : RADIO,
            (GP_WIDGET_MENU)   : MENU,
            (GP_WIDGET_BUTTON) : BUTTON,
            (GP_WIDGET_DATE)   : DATE,
    ]

    static ConfigFieldType mapCameraWidgetType(int cameraWidgetType) {
        MAPPING.getOrDefault(cameraWidgetType, null)
    }

}
