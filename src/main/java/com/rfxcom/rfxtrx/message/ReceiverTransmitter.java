package com.rfxcom.rfxtrx.message;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 28/04/12
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */
public class ReceiverTransmitter extends MessageWrapper {

    public final static byte PACKET_TYPE = 0x2;

    public enum SubType {
        ERROR((byte)0x0),
        RESPONSE((byte)0x1);
        
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

    public enum TransmitterResponse {
        ACKOK((byte)0x0),
        ACKDelayed((byte)0x1),
        NAKNoLock((byte)0x2),
        NAKACAddress((byte)0x3);        

        private byte code;

        TransmitterResponse(byte code) {
            this.code = code;
        }

        static TransmitterResponse valueOf(byte b) {
            for(TransmitterResponse tr : values())
                if(tr.code == b)
                    return tr;
            return null;
        }
    }
    
    private final static int MSG = 0;

    public ReceiverTransmitter(Message message) {
        super(message);
    }

    public SubType getSubType() {
        return SubType.valueOf(message.getPacketSubType());
    }

    public TransmitterResponse getTransmitterResponse() {
        return TransmitterResponse.valueOf(message.getPacketData(MSG));
    }
}
