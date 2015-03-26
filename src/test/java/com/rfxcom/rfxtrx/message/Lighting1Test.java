package com.rfxcom.rfxtrx.message;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by tomc on 25/03/15.
 */
public class Lighting1Test {

    @Test
    public void testLighting2() throws IOException {
        Message message = new Message();
        message.setPacketData(new byte[] {0x46,0x09,0x00,0x00});
        Lighting1 lighting1 = new Lighting1(message);
        assertEquals(70, lighting1.getHouseCode());
        assertEquals(9, lighting1.getUnitCode());
        assertEquals(Lighting1.Command.Off, lighting1.getCommand());

        lighting1 = new Lighting1(Lighting1.SubType.ARC, (byte)70, (byte)9, Lighting1.Command.Off);
        ByteArrayOutputStream out = new ByteArrayOutputStream(4);
        lighting1.writeTo(out, (byte)0);
        byte[] data = out.toByteArray();
        for(int i = 4; i < data.length; i++)
            assertEquals(message.getPacketData(i - 4), data[i]);
    }
}
