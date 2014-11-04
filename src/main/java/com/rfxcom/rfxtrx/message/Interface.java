package com.rfxcom.rfxtrx.message;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 28/04/12
 * Time: 14:07
 * To change this template use File | Settings | File Templates.
 */
public class Interface extends MessageWrapper {
    
    public final static byte PACKET_TYPE = 0x0;

    public enum Command {
        Reset((byte)0x0),
        GetStatus((byte)0x2),
        SetMode((byte)0x3),
        EnableAllModes((byte)0x4),
        EnableUndecoded((byte)0x5),
        Save((byte)0x6),
        DisableX10((byte)0x10),
        DisableARC((byte)0x11),
        DisableAC((byte)0x12),
        DisableHomeEasyEU((byte)0x13),
        DisableIkeaKoppla((byte)0x14),
        DisableOregonScientific((byte)0x15),
        DisableATIRemoteWonder((byte)0x16),
        DisableVisonic((byte)0x17),
        DisableMertik((byte)0x18),
        DisableAD((byte)0x19),
        DisableHideki((byte)0x1A),
        DisableLaCrosse((byte)0x1B),
        DisableFS20((byte)0x1C),
        Select310MHz((byte)0x50),
        Select315MHz((byte)0x51),
        Select86800MHz((byte)0x55),
        Select86800MHzFSK((byte)0x56),
        Select86830MHz((byte)0x57),
        Select86830MHzFSK((byte)0x58),
        Select86835MHz((byte)0x59),
        Select86835MHzFSK((byte)0x5A),
        Select86895MHz((byte)0x5B);

        protected byte code;

        Command(byte code) {
            this.code = code;
        }

        static Command valueOf(byte b) {
            for(Command cmd : values())
                if(cmd.code == b)
                    return cmd;
            return null;
        }
    }

    private final static int CMD = 0, MSG1 = 1;

    public Interface() {
        super(new Message(PACKET_TYPE, (byte) 0x0, new byte[10]));
    }
    
    public Interface(Command command) {
        this();
        message.setPacketData(CMD, command.code);
    }

    public void setFrequency(OperationFrequency frequency) {
        message.setPacketData(MSG1, frequency.getCode());
    }
    
    public void setOperationMode(OperationMode value, boolean set) {
        byte b = message.getPacketData(value.getByteIndex());
        if(set)
            b |= (1 << value.getBitIndex());
        else
            b &= ~(1 << value.getBitIndex());
        message.setPacketData(value.getByteIndex(), b);
    }
}
