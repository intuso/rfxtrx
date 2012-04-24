package com.rfxcom.rfxtrx433.message;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 24/04/12
 * Time: 17:11
 * To change this template use File | Settings | File Templates.
 */
public class Message {
    private byte packetType;
    private byte packetSubType;
    private byte[] packetData;

    public Message() {}

    public Message(byte packetType, byte packetSubType, byte[] packetData) {
        this.packetType = packetType;
        this.packetSubType = packetSubType;
        this.packetData = packetData;
    }

    public byte getPacketType() {
        return packetType;
    }

    public void setPacketType(byte packetType) {
        this.packetType = packetType;
    }

    public byte getPacketSubType() {
        return packetSubType;
    }

    public void setPacketSubType(byte packetSubType) {
        this.packetSubType = packetSubType;
    }

    public byte[] getPacketData() {
        return packetData;
    }

    public byte getPacketData(int byteIndex) {
        return packetData[byteIndex];
    }

    public void setPacketData(byte[] packetData) {
        this.packetData = packetData;
    }

    public void setPacketData(int byteIndex, byte data) {
        packetData[byteIndex] = data;
    }
    
    public void writeTo(OutputStream out, byte sequenceNumber) throws IOException {
        out.write((byte)(3 + packetData.length));
        out.write(new byte[] {packetType, packetSubType, sequenceNumber});
        out.write(packetData);
        out.flush();
    }
}
