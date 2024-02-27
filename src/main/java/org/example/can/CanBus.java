package org.example.can;

import org.example.can.canUsb.CanUsb;
import org.example.can.canUsb.CanUsbListener;

import java.util.HashMap;

public class CanBus {
    private static CanUsb canUsb;

    private final byte PROTOCOL = 1;
    private byte monitorAddress ;
    private byte moduleAddress;
    private byte moduleAddressAll;
    private byte productionDate;
    private short serialNumberLowerPart;
    private byte groupAddress;
    private boolean shutDownDCDC;

    private  int idBuilder() {
        return (PROTOCOL << 25) + (monitorAddress << 21) + (moduleAddress << 14) + (productionDate << 9) +
                (serialNumberLowerPart);
    }
    private  int idBuilderAll() {
        return (PROTOCOL << 25) + (monitorAddress << 21) + (moduleAddressAll << 14) + (productionDate << 9) +
                (serialNumberLowerPart);
    }

    public CanBus(String port, byte address) {
        canUsb = new CanUsb(port);
        canUsb.addListener(new PortReader());

        this.moduleAddress = address;
        this.monitorAddress = 1;
        this.productionDate = 0;
        this.serialNumberLowerPart = 0;
        this.groupAddress = 1;
        this.shutDownDCDC = false;
    }

    public CanBus(String port) {
        canUsb = new CanUsb(port);
        canUsb.addListener(new PortReader());

        this.moduleAddressAll = 0;
        this.monitorAddress = 1;
        this.productionDate = 0;
        this.serialNumberLowerPart = 0;
        this.groupAddress = 1;
        this.shutDownDCDC = false;
    }

    public boolean sendFrame(String data) {
        String idHex = Integer.toHexString(idBuilder());
        if (idHex.length() < 8) {
            idHex = "0" + idHex;
        }
        return canUsb.sendFrame("T" + idHex + "3" + data);
    }

    public boolean sendFrameAll(String data) {
        String idHex = Integer.toHexString(idBuilderAll());
        if (idHex.length() < 8) {
            idHex = "0" + idHex;
        }
        return canUsb.sendFrame("T" + idHex + "3" + data);
    }

    public void readMsg(String id, String data) {
        System.out.println("CAN message received: " + id + data);
    }

    private class PortReader implements CanUsbListener {
        @Override
        public void newMessageEvent(HashMap<String, String> msg) {
            readMsg(msg.get("id"), msg.get("frame"));
        }
    }
}
