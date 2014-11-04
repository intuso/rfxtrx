package com.rfxcom.rfxtrx.message;

/**
* Created by tomc on 04/11/14.
*/
public enum OperationFrequency {
    F310MHz((byte)0x50),
    F315MHz((byte)0x51),
    F43392MHzRec((byte)0x52),
    F43392MHzTrx((byte)0x53),
    F86800MHz((byte)0x55),
    F86800MHzFSK((byte)0x56),
    F86830MHz((byte)0x57),
    F86830MHzFSK((byte)0x58),
    F86835MHz((byte)0x59),
    F86835MHzFSK((byte)0x5A),
    F86895MHz((byte)0x5B);

    protected byte code;

    OperationFrequency(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static OperationFrequency valueOf(byte b) {
        for(OperationFrequency frequency : values())
            if(frequency.code == b)
                return frequency;
        return null;
    }
}
