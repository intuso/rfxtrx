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
        Off((byte)0),
        On((byte)1),
        Level((byte)2),
        GroupOff((byte)3),
        GroupOn((byte)4),
        GroupLevel((byte)5);

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

    private final static int ID1 = 0, ID2 = 1, ID3 = 2, ID4 = 3, UNITCODE = 4, CMND = 5, LEVEL = 6, RSSI = 7;

    public Lighting2() {
        super(new Message(PACKET_TYPE, (byte)0x0, new byte[8]));
    }

    public Lighting2(Message message) {
        super(message);
    }
    
    public Lighting2(SubType subType, int id, byte unitCode, Command command, byte level) {
        super(new Message(PACKET_TYPE, subType.code, new byte[8]));
        setId(id);
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
    
    public int getId() {
        // NB id1 is left-hand most 2 bits of the byte
        return (message.getPacketData(ID1) << 30) +
                (message.getPacketData(ID2) << 16) +
                (message.getPacketData(ID3) << 8) +
                message.getPacketData(ID4);
    }
    
    public void setId(int id) {
        // NB id1 is left-hand most 2 bits of the byte
        message.setPacketData(ID1, (byte) (id >> 30));
        message.setPacketData(ID2, (byte) (id >> 16));
        message.setPacketData(ID3, (byte) (id >> 8));
        message.setPacketData(ID4, (byte) (id));
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
