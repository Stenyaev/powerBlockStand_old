package org.example.can.canUsb;

import java.util.HashMap;

public interface CanUsbListener {
    /**
     * Оставлен для совместимости со старым кодом
     * @param msg
     * - iiildddd...dd[CR]
     * - iiiiiiiildddd...dd[CR]
     */
    @Deprecated
    default void newMessageEvent(String msg){

    };
    /**
     *
     * @param msg
     * - key "id": CAN frame identifier
     * - key "frame": CAN frame
     */
    void newMessageEvent(HashMap<String, String> msg);

}
