package org.example.can.canUsb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;

public class MsgDeque implements ConverterCommands {
    Logger logger;
    private volatile ArrayDeque<String> canMsgBuffer;

    public MsgDeque() {
        logger =  LoggerFactory.getLogger(this.getClass().getName());
        canMsgBuffer = new ArrayDeque<>();
    }
    public synchronized void sendMsg(String data){
        canMsgBuffer.offerLast(checkMsg(data));
    }

    /**
     * Message is added to the front of the queue
     * @param data - message to converter
     */
    public synchronized void sendMsgFirst(String data){
        canMsgBuffer.offerFirst(checkMsg(data));
    }
    public synchronized String getMsg (){
        return canMsgBuffer.pollFirst();
    }
    private String checkMsg(String data){
        if (data.length() == 25) {
            data = EXT_ID + data + "\r";
        } else if (data.length() == 20) {
            data = STD_ID + data + "\r";
        } else data = data+"\r";
        return data;
    }

    public Integer getCanMsgDequeSize() {
        return canMsgBuffer.size();
    }

}
