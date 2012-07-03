package com.rfxcom.rfxtrx;

import java.util.EnumSet;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 05/05/12
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */
public class Mode {

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

    public enum OperationMode {
        Undecoded(3, 7),
        RFU1(3, 6),
        RFU2(3, 5),
        RFU3(3, 4),
        RFU4(3, 3),
        RFU5(3, 2),
        RFU6(3, 1),
        RFU7(3, 0),
        RFU8(2, 7),
        RFU9(2, 6),
        ProGuard(4, 5),
        FS20(4, 4),
        LaCrosse(4, 3),
        Hideki(4, 2),
        AD(4, 1),
        Mertik(4, 0),
        Visonic(3, 7),
        ATI(3, 6),
        OregonScientific(3, 5),
        IkeaKoppla(3, 4),
        HomeEasyEU(3, 3),
        AC(3, 2),
        ARC(3, 1),
        X10(3, 0);

        protected int byteIndex, bitIndex;

        OperationMode(int byteIndex, int bitIndex) {
            this.byteIndex = byteIndex;
            this.bitIndex = bitIndex;
        }

        public int getByteIndex() {
            return byteIndex;
        }

        public int getBitIndex() {
            return bitIndex;
        }
    }

    private OperationFrequency operationFrequency;

    private EnumSet<OperationMode> operationModes;
}
