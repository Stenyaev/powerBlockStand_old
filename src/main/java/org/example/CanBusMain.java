package org.example;

import org.example.can.CanBus;


public class CanBusMain {
    public static void main(String[] args) throws InterruptedException {
        CanBus canBus = new CanBus("/dev/ttyUSB0");

        while (true) {
//            canBus.sendFrame("00000000");
            canBus.sendFrameAll("00000000");

            Thread.sleep(1000);
        }
    }
}