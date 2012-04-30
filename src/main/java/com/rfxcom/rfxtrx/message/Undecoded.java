package com.rfxcom.rfxtrx.message;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 28/04/12
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */
public class Undecoded extends MessageWrapper {

    public final static byte PACKET_TYPE = 0x3;

    public enum SubType {
        AC((byte)0x0),
        ARC((byte)0x1),
        ATI((byte)0x2),
        Hideki((byte)0x3),
        LaCrosse((byte)0x4),
        AD((byte)0x5),
        Mertik((byte)0x6),
        Oregon1((byte)0x7),
        Oregon2((byte)0x8),
        Oregon3((byte)0x9),
        ProGuard((byte)0xA),
        Visonic((byte)0xB),
        NEC((byte)0xC),
        FS20((byte)0xD);

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

    private final static int MSG = 0;

    public Undecoded(Message message) {
        super(message);
    }

    public SubType getSubType() {
        return SubType.valueOf(message.getPacketSubType());
    }
}
