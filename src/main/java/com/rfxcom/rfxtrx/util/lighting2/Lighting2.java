package com.rfxcom.rfxtrx.util.lighting2;

import com.google.common.collect.Lists;
import com.intuso.utilities.listener.MemberRegistration;
import com.intuso.utilities.listener.ManagedCollection;
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
public class Lighting2 {

    private final RFXtrx agent;
    private final com.rfxcom.rfxtrx.message.Lighting2.SubType subType;
    private final ManagedCollection<Callback> callbacks = new ManagedCollection<>(Lists.<Callback>newCopyOnWriteArrayList());

    private final MessageListener listener = new MessageListener() {
        @Override
        public void messageReceived(MessageWrapper messageWrapper) {
            if(messageWrapper instanceof com.rfxcom.rfxtrx.message.Lighting2) {
                com.rfxcom.rfxtrx.message.Lighting2 lighting2MessageWrapper = (com.rfxcom.rfxtrx.message.Lighting2)messageWrapper;
                if(lighting2MessageWrapper.getSubType() == subType) {
                    int id = lighting2MessageWrapper.getHouseId();
                    byte unitCode = lighting2MessageWrapper.getUnitCode();
                    switch(lighting2MessageWrapper.getCommand()) {
                        case On:
                            for(Callback listener : callbacks)
                                listener.turnedOn(id, unitCode);
                            break;
                        case OnAll:
                            for(Callback listener : callbacks)
                                listener.turnedOnAll(id);
                            break;
                        case Off:
                            for(Callback listener : callbacks)
                                listener.turnedOff(id, unitCode);
                            break;
                        case OffAll:
                            for(Callback listener : callbacks)
                                listener.turnedOffAll(id);
                            break;
                        case Level:
                            for(Callback listener : callbacks)
                                listener.setLevel(id, unitCode, lighting2MessageWrapper.getLevel());
                            break;
                        case LevelAll:
                            for(Callback listener : callbacks)
                                listener.setLevelAll(id, lighting2MessageWrapper.getLevel());
                            break;
                    }
                }
            }
        }
    };
    private final MemberRegistration listenerRegistration;

    public static Lighting2 forAC(RFXtrx agent) {
        return new Lighting2(agent, com.rfxcom.rfxtrx.message.Lighting2.SubType.AC);
    }

    public static Lighting2 forHomeEasyEU(RFXtrx agent) {
        return new Lighting2(agent, com.rfxcom.rfxtrx.message.Lighting2.SubType.HomeEasyEU);
    }

    public static Lighting2 forANSLUT(RFXtrx agent) {
        return new Lighting2(agent, com.rfxcom.rfxtrx.message.Lighting2.SubType.ANSLUT);
    }

    public Lighting2(RFXtrx agent, com.rfxcom.rfxtrx.message.Lighting2.SubType subType) {
        this.agent = agent;
        this.subType = subType;
        this.listenerRegistration = this.agent.addListener(listener);
    }

    @Override
    protected void finalize() throws Throwable {
        listenerRegistration.removeListener();
        super.finalize();
    }

    public MemberRegistration addCallback(Callback listener) {
        return callbacks.add(listener);
    }

    private void sendCommand(int houseId, byte unitCode, com.rfxcom.rfxtrx.message.Lighting2.Command command, byte level) throws IOException {
        agent.sendMessage(new com.rfxcom.rfxtrx.message.Lighting2(subType, houseId, unitCode, command, level));
    }

    public void turnOn(int houseId, byte unitCode) throws IOException {
        sendCommand(houseId, unitCode, com.rfxcom.rfxtrx.message.Lighting2.Command.On, (byte) 0x0F);
    }

    public void turnOnAll(int houseId) throws IOException {
        sendCommand(houseId, (byte)0x00, com.rfxcom.rfxtrx.message.Lighting2.Command.OnAll, (byte)0x0F);
    }

    public void turnOff(int houseId, byte unitCode) throws IOException {
        sendCommand(houseId, unitCode, com.rfxcom.rfxtrx.message.Lighting2.Command.Off, (byte)0x00);
    }

    public void turnOffAll(int houseId) throws IOException {
        sendCommand(houseId, (byte)0x00, com.rfxcom.rfxtrx.message.Lighting2.Command.OffAll, (byte)0x00);
    }

    public void setLevel(int houseId, byte unitCode, byte level) throws IOException {
        sendCommand(houseId, unitCode, com.rfxcom.rfxtrx.message.Lighting2.Command.Level, level);
    }

    public void setLevelAll(int houseId, byte level) throws IOException {
        sendCommand(houseId, (byte)0x00, com.rfxcom.rfxtrx.message.Lighting2.Command.LevelAll, level);
    }

    public interface Callback {
        void turnedOn(int houseId, byte unitCode);
        void turnedOnAll(int houseId);
        void turnedOff(int houseId, byte unitCode);
        void turnedOffAll(int houseId);
        void setLevel(int houseId, byte unitCode, byte level);
        void setLevelAll(int houseId, byte level);
    }
}
