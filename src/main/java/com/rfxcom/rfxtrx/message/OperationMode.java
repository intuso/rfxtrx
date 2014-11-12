package com.rfxcom.rfxtrx.message;

/**
* Created by tomc on 04/11/14.
*/
public enum OperationMode {

    Undecoded(3, 7),
    RFU1(3, 6),
    RFU2(3, 5),
    RFU3(3, 4),
    RFU4(3, 3),
    RFU5(3, 2),
    RFU6(3, 1),
    RFU7(3, 0),
    RFU8(4, 7),
    RFU9(4, 6),
    ProGuard(4, 5),
    FS20(4, 4),
    LaCrosse(4, 3),
    Hideki(4, 2),
    AD(4, 1),
    Mertik(4, 0),
    Visonic(5, 7),
    ATI(5, 6),
    OregonScientific(5, 5),
    IkeaKoppla(5, 4),
    HomeEasyEU(5, 3),
    AC(5, 2),
    ARC(5, 1),
    X10(5, 0);

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
