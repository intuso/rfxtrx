package com.rfxcom.rfxtrx.message;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 24/04/12
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 */
public class TemperatureSensors extends MessageWrapper {

    public final static byte PACKET_TYPE = 0x50;

    public enum SubType {
        TEMP1((byte)0x1),
        TEMP2((byte)0x2),
        TEMP3((byte)0x3),
        TEMP4((byte)0x4),
        TEMP5((byte)0x5);

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

    private final static int SENSOR_ID1 = 0, SENSOR_ID2 = 1, TEMP_HIGH_SIGN = 2, TEMP_LOW = 3, BATTERY_RSSI = 4;

    public TemperatureSensors() {
        super(new Message(PACKET_TYPE, (byte)0x0, new byte[5]));
    }

    public TemperatureSensors(Message message) {
        super(message);
    }

    public SubType getSubType() {
        return SubType.valueOf(message.getPacketSubType());
    }
    
    public int getSensorId() {
        return ((message.getPacketData(SENSOR_ID1) & 0xFF) << 8) +
                (message.getPacketData(SENSOR_ID2) & 0xFF);
    }

    public double getTemp() {
        // temp high is first 7 bits, temp low is whole byte
        int times10 = ((message.getPacketData(TEMP_HIGH_SIGN) & 0xFE) << 7) +
                message.getPacketData(TEMP_LOW) & 0xFF;
        double temp = (double)times10 / 10;
        // last bit of temp sign is 1 for negative
        boolean negative = (message.getPacketData(TEMP_HIGH_SIGN) & 0x01) > 0;
        return negative ? 0 - temp : temp;
    }

    public double getBattery() {
        // upper four bytes are battery level where 0x0 is weak and 0x9 is strong
        // this function returns 1 as strong and 0 as weak.
        return ((double)((message.getPacketData(BATTERY_RSSI) & 0xF0) >> 4)) / 0x9;
    }

    public double getRSSI() {
        // lower four bytes are RSSI where 0x0 is weak and 0xF is strong
        // this function returns 1 as strong and 0 as weak.
        return ((double)(message.getPacketData(BATTERY_RSSI) & 0x0F)) / 0xF;
    }
}
