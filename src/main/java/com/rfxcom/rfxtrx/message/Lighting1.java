package com.rfxcom.rfxtrx.message;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 24/04/12
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 */
public class Lighting1 extends MessageWrapper {

    public final static byte PACKET_TYPE = 0x10;

    public enum SubType {
        X10((byte)0x0),
        ARC((byte)0x1),
        ELROAB400D((byte)0x2),
        Waveman((byte)0x3),
        ChaconEMW200((byte)0x4),
        IMPULS((byte)0x5);

        private byte code;

        SubType(byte code) {
            this.code = code;
        }

        static SubType valueOf(byte b) {
            for(SubType type : values())
                if(type.code == b)
                    return type;
            return null;
        }
    }

    public enum Command {
        Off((byte)0),
        On((byte)1),
        Dim((byte)2),
        Bright((byte)3),
        OffAll((byte)5),
        OnAll((byte)6),
        Chime((byte)7);

        private byte code;

        Command(byte code) {
            this.code = code;
        }

        static Command valueOf(byte b) {
            for(Command cmd : values())
                if(cmd.code == b)
                    return cmd;
            return null;
        }
    }

    private final static int HOUSE_CODE = 0, UNIT_CODE = 1, CMND = 2, RSSI = 3;

    public Lighting1() {
        super(new Message(PACKET_TYPE, (byte)0x0, new byte[4]));
    }

    public Lighting1(Message message) {
        super(message);
    }

    public Lighting1(SubType subType, byte houseCode, byte unitCode, Command command) {
        super(new Message(PACKET_TYPE, subType.code, new byte[4]));
        setHouseCode(houseCode);
        setUnitCode(unitCode);
        setCommand(command);
    }

    public SubType getSubType() {
        return SubType.valueOf(message.getPacketSubType());
    }

    public void setSubType(SubType subType) {
        message.setPacketSubType(subType.code);
    }

    public byte getHouseCode() {
        return message.getPacketData(HOUSE_CODE);
    }

    public void setHouseCode(byte houseCode) {
        message.setPacketData(HOUSE_CODE, houseCode);
    }

    public byte getUnitCode() {
        return message.getPacketData(UNIT_CODE);
    }

    public void setUnitCode(byte unitCode) {
        message.setPacketData(UNIT_CODE, unitCode);
    }

    public Command getCommand() {
        return Command.valueOf(message.getPacketData(CMND));
    }

    public void setCommand(Command command) {
        message.setPacketData(CMND, command.code);
    }

    public double getRSSI() {
        // lower four bits are RSSI where 0x0 is weak and 0xF is strong
        // this function returns 1 as strong and 0 as weak.
        return ((double)(message.getPacketData(RSSI) & 0x0F)) / 0xF;
    }
}
