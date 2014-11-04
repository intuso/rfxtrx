package com.rfxcom.rfxtrx.message;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 28/04/12
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */
public class InterfaceResponse extends MessageWrapper {

    public final static byte PACKET_TYPE = 0x1;

    private final static int CMD = 0, MSG1 = 1, MSG2 = 2;

    public InterfaceResponse(Message message) {
        super(message);
    }

    public Interface.Command getCommand() {
        return Interface.Command.valueOf(message.getPacketData(CMD));
    }

    public OperationFrequency getFrequency() {
        return OperationFrequency.valueOf(message.getPacketData(MSG1));
    }

    public byte getFirmwareVersion() {
        return message.getPacketData(MSG2);
    }
    
    public boolean isOperationModeSet(OperationMode value) {
        return (message.getPacketData(value.getByteIndex()) & (1 << value.getBitIndex())) > 0;
    }
}
