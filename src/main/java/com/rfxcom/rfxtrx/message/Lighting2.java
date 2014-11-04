package com.rfxcom.rfxtrx.message;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 24/04/12
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 */
public class Lighting2 extends MessageWrapper {
    
    public final static byte PACKET_TYPE = 0x11;
    
    public enum SubType {
        AC((byte)0x0),
        HomeEasyEU((byte)0x1),
        ANSLUT((byte)0x2);

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

        On((byte)1),
        OnAll((byte)4),
        Off((byte)0),
        OffAll((byte)3),
        Level((byte)2),
        LevelAll((byte)5);

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

    private final static int HOUSE_ID1 = 0, HOUSE_ID2 = 1, HOUSE_ID3 = 2, HOUSE_ID4 = 3, UNITCODE = 4, CMND = 5, LEVEL = 6, RSSI = 7;

    public Lighting2() {
        super(new Message(PACKET_TYPE, (byte)0x0, new byte[8]));
    }

    public Lighting2(Message message) {
        super(message);
    }
    
    public Lighting2(SubType subType, int id, byte unitCode, Command command, byte level) {
        super(new Message(PACKET_TYPE, subType.code, new byte[8]));
        setHouseId(id);
        setUnitCode(unitCode);
        setCommand(command);
        setLevel(level);
    }

    public SubType getSubType() {
        return SubType.valueOf(message.getPacketSubType());
    }

    public void setSubType(SubType subType) {
        message.setPacketSubType(subType.code);
    }
    
    public int getHouseId() {
        // NB id1 is left-hand most 2 bits of the byte
        return (message.getPacketData(HOUSE_ID1) << 30) +
                (message.getPacketData(HOUSE_ID2) << 16) +
                (message.getPacketData(HOUSE_ID3) << 8) +
                message.getPacketData(HOUSE_ID4);
    }
    
    public void setHouseId(int houseIid) {
        // NB id1 is left-hand most 2 bits of the byte
        message.setPacketData(HOUSE_ID1, (byte) (houseIid >> 30));
        message.setPacketData(HOUSE_ID2, (byte) (houseIid >> 16));
        message.setPacketData(HOUSE_ID3, (byte) (houseIid >> 8));
        message.setPacketData(HOUSE_ID4, (byte) (houseIid));
    }

    public byte getUnitCode() {
        return message.getPacketData(UNITCODE);
    }

    public void setUnitCode(byte unitCode) {
        message.setPacketData(UNITCODE, unitCode);
    }

    public Command getCommand() {
        return Command.valueOf(message.getPacketData(CMND));
    }

    public void setCommand(Command command) {
        message.setPacketData(CMND, command.code);
    }

    public byte getLevel() {
        return message.getPacketData(LEVEL);
    }

    public void setLevel(byte level) {
        message.setPacketData(LEVEL, level);
    }

    public double getRSSI() {
        // lower four bytes are RSSI where 0x0 is weak and 0xF is strong
        // this function returns 1 as strong and 0 as weak.
        return ((double)message.getPacketData(RSSI)) / 0xF;
    }
}
