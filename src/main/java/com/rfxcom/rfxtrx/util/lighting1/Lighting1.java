package com.rfxcom.rfxtrx.util.lighting1;

import com.google.common.collect.Lists;
import com.intuso.utilities.listener.ListenerRegistration;
import com.intuso.utilities.listener.Listeners;
import com.rfxcom.rfxtrx.RFXtrx;
import com.rfxcom.rfxtrx.message.MessageListener;
import com.rfxcom.rfxtrx.message.MessageWrapper;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 24/04/12
 * Time: 17:53
 * To change this template use File | Settings | File Templates.
 */
public class Lighting1 {

    private final RFXtrx agent;
    private final com.rfxcom.rfxtrx.message.Lighting1.SubType subType;
    private final Listeners<Callback> callbacks = new Listeners<>(Lists.<Callback>newCopyOnWriteArrayList());

    private final MessageListener listener = new MessageListener() {
        @Override
        public void messageReceived(MessageWrapper messageWrapper) {
            if(messageWrapper instanceof com.rfxcom.rfxtrx.message.Lighting1) {
                com.rfxcom.rfxtrx.message.Lighting1 lighting1MessageWrapper = (com.rfxcom.rfxtrx.message.Lighting1)messageWrapper;
                if(lighting1MessageWrapper.getSubType() == subType) {
                    byte houseCode = lighting1MessageWrapper.getHouseCode();
                    byte unitCode = lighting1MessageWrapper.getUnitCode();
                    switch(lighting1MessageWrapper.getCommand()) {
                        case On:
                            for(Callback listener : callbacks)
                                listener.turnedOn(houseCode, unitCode);
                            break;
                        case OnAll:
                            for(Callback listener : callbacks)
                                listener.turnedOnAll(houseCode);
                            break;
                        case Off:
                            for(Callback listener : callbacks)
                                listener.turnedOff(houseCode, unitCode);
                            break;
                        case OffAll:
                            for(Callback listener : callbacks)
                                listener.turnedOffAll(houseCode);
                            break;
                        case Dim:
                            for(Callback listener : callbacks)
                                listener.dim(houseCode, unitCode);
                            break;
                        case Bright:
                            for(Callback listener : callbacks)
                                listener.bright(houseCode, unitCode);
                            break;
                        case Chime:
                            for(Callback listener : callbacks)
                                listener.chime(houseCode);
                            break;
                    }
                }
            }
        }
    };
    private final ListenerRegistration listenerRegistration;

    public static Lighting1 forX10(RFXtrx agent) {
        return new Lighting1(agent, com.rfxcom.rfxtrx.message.Lighting1.SubType.X10);
    }

    public static Lighting1 forARC(RFXtrx agent) {
        return new Lighting1(agent, com.rfxcom.rfxtrx.message.Lighting1.SubType.ARC);
    }

    public static Lighting1 forELROAB400(RFXtrx agent) {
        return new Lighting1(agent, com.rfxcom.rfxtrx.message.Lighting1.SubType.ELROAB400D);
    }

    public static Lighting1 forWaveman(RFXtrx agent) {
        return new Lighting1(agent, com.rfxcom.rfxtrx.message.Lighting1.SubType.Waveman);
    }

    public static Lighting1 forChaconEMW200(RFXtrx agent) {
        return new Lighting1(agent, com.rfxcom.rfxtrx.message.Lighting1.SubType.ChaconEMW200);
    }

    public static Lighting1 forIMPULS(RFXtrx agent) {
        return new Lighting1(agent, com.rfxcom.rfxtrx.message.Lighting1.SubType.IMPULS);
    }

    public Lighting1(RFXtrx agent, com.rfxcom.rfxtrx.message.Lighting1.SubType subType) {
        this.agent = agent;
        this.subType = subType;
        this.listenerRegistration = this.agent.addListener(listener);
    }

    @Override
    protected void finalize() throws Throwable {
        listenerRegistration.removeListener();
        super.finalize();
    }

    public ListenerRegistration addCallback(Callback listener) {
        return callbacks.addListener(listener);
    }

    private void sendCommand(byte houseCode, byte unitCode, com.rfxcom.rfxtrx.message.Lighting1.Command command) throws IOException {
        agent.sendMessage(new com.rfxcom.rfxtrx.message.Lighting1(subType, houseCode, unitCode, command));
    }

    public void turnOn(byte houseCode, byte unitCode) throws IOException {
        sendCommand(houseCode, unitCode, com.rfxcom.rfxtrx.message.Lighting1.Command.On);
    }

    public void turnOnAll(byte houseCode) throws IOException {
        sendCommand(houseCode, (byte)0x00, com.rfxcom.rfxtrx.message.Lighting1.Command.OnAll);
    }

    public void turnOff(byte houseCode, byte unitCode) throws IOException {
        sendCommand(houseCode, unitCode, com.rfxcom.rfxtrx.message.Lighting1.Command.Off);
    }

    public void turnOffAll(byte houseCode) throws IOException {
        sendCommand(houseCode, (byte)0x00, com.rfxcom.rfxtrx.message.Lighting1.Command.OffAll);
    }

    public void dim(byte houseCode, byte unitCode) throws IOException {
        sendCommand(houseCode, unitCode, com.rfxcom.rfxtrx.message.Lighting1.Command.Dim);
    }

    public void bright(byte houseCode, byte unitCode) throws IOException {
        sendCommand(houseCode, unitCode, com.rfxcom.rfxtrx.message.Lighting1.Command.Bright);
    }

    public void chime(byte houseCode) throws IOException {
        sendCommand(houseCode, (byte)0x00, com.rfxcom.rfxtrx.message.Lighting1.Command.Chime);
    }

    public interface Callback {
        void turnedOn(byte houseCode, byte unitCode);
        void turnedOnAll(byte houseCode);
        void turnedOff(byte houseCode, byte unitCode);
        void turnedOffAll(byte houseCode);
        void dim(byte houseCode, byte unitCode);
        void bright(byte houseCode, byte unitCode);
        void chime(byte houseCode);
    }
}
