package com.rfxcom.rfxtrx.message;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by tomc on 25/03/15.
 */
public class TemperatureSensorsTest {

    @Test
    public void testMessageDecode() {
        Message message = new Message();
        message.setPacketData(new byte[] {0x22,0x01,0x00,(byte)170,0x79});
        TemperatureSensors temperatureSensors = new TemperatureSensors(message);
        assertEquals(Double.valueOf(17.0), Double.valueOf(temperatureSensors.getTemp()));
    }
}
