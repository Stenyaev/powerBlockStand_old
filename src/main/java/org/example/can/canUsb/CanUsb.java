package org.example.can.canUsb;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;


public class CanUsb implements ConverterCommands{
    private final SerialPort serialPort;
    private
    CanUsbListener listener;
    private final CanSender canSender;
    private boolean canAlive = true;
    public static Integer countPing = 0;
    private MsgDeque msgDeque;

    Logger logger;

    public CanUsb(String sPort)  {
        logger =  LoggerFactory.getLogger(this.getClass().getName());
        serialPort = new SerialPort(sPort);
        msgDeque = new MsgDeque();


        if (!(serialPort.isOpened())) {
            try {
                serialPort.openPort();
                serialPort.setParams(3_000_000, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
                serialPort.addEventListener(new PortReader());
            } catch (SerialPortException e) {
                throw new RuntimeException(e);
            }
        }

        canSender = new CanSender(serialPort,msgDeque);
        Thread canSenderThread = new Thread(canSender, "CanThread");
        canSenderThread.start();
        openCanBus();
//        Timer ping = new Timer(); // запускаем запрос статуса какждую секунду
//        ping.schedule(new TimerTask(){
//
//            @Override
//            public void run() {
//                pingCan();
//            }
//        },0,1000);
    }

    /**
     *
     * @param speed - CAN speed
     * @param ackMode - confirmation mode
     */
    public void setParams(String speed, String ackMode){
        msgDeque.sendMsg(speed);
        msgDeque.sendMsg(OPEN);
        msgDeque.sendMsg(ackMode);

    }
    /**
     *
     * @param command -  firstly send. ASCII command like TITAN,
     */
    public boolean sendCommand(String command) {
        msgDeque.sendMsgFirst(command);
        return canAlive;
    }

    private  void pingCan() {
        if (CanUsb.countPing++>10) canAlive = false;
        else canAlive = true;
        msgDeque.sendMsgFirst(STATUS);
    }

    /**
     *
     * @param data:
     *
     *            Transmitting a Standard CAN Frame     iii8dddddddddddddddd
     * iii: Standard CAN frame (11 bit) identifier in hexadecimal format (000-7FF).
     * l: CAN data length =  8 bytes.
     * dd: Data byte value in hexadecimal format (00-FF). The number of bytes must be equal to the data length field.
     * Example: 00281199FFFFFFFFFFFF  will send a standard CAN frame with ID = 002h, DL = 8, Data = 11 99 FF FF FF FF FF FF.
     *
     *            Transmitting an Extended CAN Frame    iiiiiiii8dddddddddddddddd
     * iiiiiiii: Extended CAN frame (29 bit) identifier in hexadecimal format (00000000- 1FFFFFFF).
     * l: CAN data length = 8 (8 bytes).
     * dd: Data byte value in hexadecimal format (00-FF). The number of bytes must be equal to the data length field.
     * Example: 1FFFFFFF81122334455667788 will send an extended CAN frame with ID = 1FFFFFFFh, DLC = 8, data = 11 22 33 44 55 66 77 88.
     *
     * @return CAN converter state. True - normal operation
     */
    public boolean sendFrame(String data) {
        msgDeque.sendMsg(data);
        return canAlive;
    }

    /**
     *
     * @param id -
     *           iii: Standard CAN frame (11 bit) identifier in hexadecimal format (000-7FF).
     *           iiiiiiii: Extended CAN frame (29 bit) identifier in hexadecimal format (00000000- 1FFFFFFF).
     *
     * @param data -  value in decimal format have transformed to hexadecimal format and have sent to CAN bus
     * @return CAN converter state. True - normal operation
     */

    public boolean sendFrame(String id,Integer data) {

        StringBuilder canMsg = new StringBuilder();
        String dataStr = Integer.toHexString(data).toUpperCase();
        if(id.length()==3){
            canMsg.append(STD_ID);
        }
        else if(id.length()==8){
            canMsg.append(EXT_ID);
        }
        else return false;

        if (dataStr.length()%2!=0){ // должно быть четное число символов
            dataStr = "0"+dataStr;
        }
        canMsg.append(id).append(dataStr.length()/2).append(dataStr);
        msgDeque.sendMsg(canMsg.toString());
        return canAlive;
    }
    /**
     * Настраевает скорость CAN по умолчанию 500К и открывает порт преобразователя
     */
    private void openCanBus() {
        //  sendFrame("C"); // В случае если порт ранее не открывался в микроконтролере не будет события
        msgDeque.sendMsg(CAN_SPEED_500);
        msgDeque.sendMsg(OPEN);
    }

    /**
     * Программный сброс микроконтроллера и открытие порта
     */
    private void resetCanBus() throws InterruptedException {
        sendFrame("RST");
        Thread.sleep(100);
        // openCanBus();
    }

    public void addListener(CanUsbListener listener) {
        this.listener = listener;
    }

    public void readFrame() {
        try {
            while (serialPort.getInputBufferBytesCount()>0) {

                byte[] symbol = serialPort.readBytes(1);

                String symbolStr = new String(symbol, StandardCharsets.US_ASCII);
                String str = "";
                String id = "";
                Integer lenght = 0;
                String lenghtStr = "";
                String frame = "";
                switch (symbolStr) {
                    case EXT_ID: {

                        id = serialPort.readString(8);
                        lenghtStr = serialPort.readString(1);

                        lenght = Integer.parseInt(lenghtStr) * 2;


                        frame = serialPort.readString(lenght);

                        HashMap<String, String> msg = new HashMap<>();
                        msg.put("id", id);
                        msg.put("frame", frame);

                        if (listener != null) {
                            listener.newMessageEvent(id + lenghtStr + frame);
                            listener.newMessageEvent(msg);
                        }

                        countPing = 0;
                        serialPort.readBytes(1);//read [CR] from buffer
                        break;
                    }
                    case STD_ID: {

                        id = serialPort.readString(3);
                        lenghtStr = serialPort.readString(1);
                        lenght = Integer.parseInt(lenghtStr) * 2;
                        frame = serialPort.readString(lenght);

                        HashMap<String, String> msg = new HashMap<>();
                        msg.put("id", id);
                        msg.put("frame", frame);

                        if (listener != null) {
                            listener.newMessageEvent(id + lenghtStr + frame);
                            listener.newMessageEvent(msg);
                        }
                        countPing = 0;
                        break;
                    }
                    case EXT_SUCCESS:
                    case STD_SUCCESS: {
                        str = serialPort.readString(1); //read [CR] from buffer
                        countPing = 0;
                        break;
                    }
                    case "0": {
                        str = serialPort.readString(2);//answer is 0000[CR], first symbol have been read
                        CanUsb.countPing = 0;
                        break;
                    }
                }
            }
        } catch (SerialPortException e) {
            throw new RuntimeException(e);
        } catch (NumberFormatException e) {
            logger.error("CAN_USB: wrong CAN data length");
        }
    }

    public class PortReader implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {

            if (serialPortEvent.isRXCHAR()) {
                readFrame();

            }
        }
    }


}
