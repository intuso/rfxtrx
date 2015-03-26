package com.rfxcom.rfxtrx.message;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by tomc on 25/03/15.
 */
public class Lighting2Test {

    @Test
    public void testLighting2() throws IOException {
        Message message = new Message();
        message.setPacketData(new byte[] {0x00,0x03,0x72,(byte)0xa0,0x02,0x01,0x0f,0x00});
        Lighting2 lighting2 = new Lighting2(message);
        assertEquals(225952, lighting2.getHouseId());
        assertEquals(0x02, lighting2.getUnitCode());
        assertEquals(Lighting2.Command.On, lighting2.getCommand());
        assertEquals(0x0f, lighting2.getLevel());

        lighting2 = new Lighting2(Lighting2.SubType.AC, 225952, (byte)2, Lighting2.Command.On, (byte)0x0f);
        ByteArrayOutputStream out = new ByteArrayOutputStream(8);
        lighting2.writeTo(out, (byte)0);
        byte[] data = out.toByteArray();
        for(int i = 4; i < data.length; i++)
            assertEquals(message.getPacketData(i - 4), data[i]);
    }
}
