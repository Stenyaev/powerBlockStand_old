package org.example.can.canUsb;

import jssc.SerialPort;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jssc.SerialPort.PURGE_TXCLEAR;


public class CanSender implements Runnable, ConverterCommands{
    private SerialPort serialPort;
    private boolean threadTerminated = false;
    private MsgDeque msgDeque;
//    private volatile MsgDeque msgDeque;

    Logger logger;

    public CanSender(SerialPort serialPort, MsgDeque msgDeque) {
        logger =  LoggerFactory.getLogger(this.getClass().getName());
        this.serialPort = serialPort;
        this.msgDeque = msgDeque;

    }


    private void sendFrame(String data) {
        try {
            // Thread.sleep(1);
            serialPort.purgePort(PURGE_TXCLEAR);
            serialPort.writeBytes(data.getBytes());
            logger.info(data);
            Thread.sleep(1);

        } catch (
                InterruptedException | SerialPortException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        String msg ;

        while(!threadTerminated){

            if (msgDeque.getCanMsgDequeSize()!=0){

                msg = msgDeque.getMsg();
                sendFrame(msg);
            }
        }
    }
    public void terminateThread(){
        threadTerminated = true;
    }

}
