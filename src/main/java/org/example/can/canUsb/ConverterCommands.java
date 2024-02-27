package org.example.can.canUsb;

public interface ConverterCommands {
    /** CAN speed command **/
    final String  CAN_SPEED_125 = "S4";
    final String  CAN_SPEED_250 = "S5";
    final String  CAN_SPEED_500 = "S6"; //default mode
    final String  CAN_SPEED_1000 = "S8";

    /** CAN Control **/
    final String OPEN = "O";
    final String CLOSE = "C";
    final String RESET = "RST";
    final String STATUS = "F";
    final String EXT_SUCCESS = "Z";
    final String STD_SUCCESS = "z";
    final String STD_ID = "t";
    final String EXT_ID = "T";
    /** Operating mode */
    final String NO_ACK = "W"; // mode without confirmation of sending a CAN message
    final String ACK = "w"; // mode with confirmation of sending a CAN message; default mode

}
