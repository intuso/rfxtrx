package com.rfxcom.rfxtrx.util;

import com.rfxcom.rfxtrx.RFXtrx;
import com.rfxcom.rfxtrx.message.Lighting2;
import com.rfxcom.rfxtrx.message.MessageWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 24/04/12
 * Time: 17:53
 * To change this template use File | Settings | File Templates.
 */
public class HomeEasy {
    
    private RFXtrx.MessageListener listener = new RFXtrx.MessageListener() {
        @Override
        public void messageReceived(MessageWrapper messageWrapper) {
            if(messageWrapper instanceof Lighting2) {
                Lighting2 lightingMessageWrapper = (Lighting2)messageWrapper;
                if(lightingMessageWrapper.getSubType() == subType) {
                    int id = lightingMessageWrapper.getId();
                    byte unitCode = lightingMessageWrapper.getUnitCode();
                    switch(lightingMessageWrapper.getCommand()) {
                        case Off:
                            for(UnknownApplianceListener listener : unitListeners)
                                listener.nowOff(id, unitCode);
                            break;
                        case On:
                            for(UnknownApplianceListener listener : unitListeners)
                                listener.nowOn(id, unitCode);
                            break;
                        case Level:
                            for(UnknownApplianceListener listener : unitListeners)
                                listener.newLevel(id, unitCode, lightingMessageWrapper.getLevel());
                            break;
                    }
                }
            }
        }
    };
    
    private RFXtrx agent;

    private Lighting2.SubType subType;
    
    private List<UnknownApplianceListener> unitListeners = new ArrayList<UnknownApplianceListener>();
    
    private HomeEasy(RFXtrx agent, Lighting2.SubType subType) {
        this.agent = agent;
        this.subType = subType;
        agent.addListener(listener);
    }
    
    public static HomeEasy createEU(RFXtrx agent) {
        return new HomeEasy(agent, Lighting2.SubType.HomeEasyEU);
    }

    public static HomeEasy createUK(RFXtrx agent) {
        return new HomeEasy(agent, Lighting2.SubType.AC);
    }

    public void addUnknownApplianceListener(UnknownApplianceListener listener) {
        unitListeners.add(listener);
    }

    public void removeUnknownApplianceListener(UnknownApplianceListener listener) {
        unitListeners.remove(listener);
    }

    public Appliance createAppliance(int id, byte unitCode) {
        return new Appliance(id, unitCode);
    }
    
    private void sendCommand(int id, byte unitCode, Lighting2.Command command, byte level) throws IOException {
        agent.sendMessage(new Lighting2(subType, id, unitCode, command, level));
    }

    public void off(int id, byte unitCode) throws IOException {
        sendCommand(id, unitCode, Lighting2.Command.Off, (byte)0x00);
    }
    
    public void on(int id, byte unitCode) throws IOException {
        sendCommand(id, unitCode, Lighting2.Command.On, (byte)0x00);
    }

    public void level(int id, byte unitCode, byte level) throws IOException {
        sendCommand(id, unitCode, Lighting2.Command.Level, level);
    }

    public void groupOff(int id) throws IOException {
        sendCommand(id, (byte)0x00, Lighting2.Command.GroupOff, (byte)0x00);
    }

    public void groupOn(int id) throws IOException {
        sendCommand(id, (byte)0x00, Lighting2.Command.GroupOn, (byte)0x00);
    }

    public void groupLevel(int id, byte level) throws IOException {
        sendCommand(id, (byte)0x00, Lighting2.Command.GroupLevel, level);
    }
    
    public class Appliance {

        private final int id;
        private final byte unitCode;
        
        boolean on = false;
        byte level = 0;
        
        private List<KnownApplianceListener> listeners = new ArrayList<KnownApplianceListener>();
        
        private Appliance(int i, byte u) {
            this.id = i;
            this.unitCode = u;
            HomeEasy.this.addUnknownApplianceListener(new UnknownApplianceListener() {
                @Override
                public void nowOff(int i, byte u) {
                    if(id == i && unitCode == u) {
                        on = false;
                        for(KnownApplianceListener listener : listeners)
                            listener.nowOff(Appliance.this);
                    }
                }

                @Override
                public void nowOn(int i, byte u) {
                    if(id == i && unitCode == u) {
                        on = true;
                        for(KnownApplianceListener listener : listeners)
                            listener.nowOn(Appliance.this);
                    }
                }

                @Override
                public void newLevel(int id, byte unit, byte level) {
                    // appliances don't have levels
                }
            });
        }

        public void addListener(KnownApplianceListener listener) {
            listeners.add(listener);
        }

        public void removeListener(KnownApplianceListener listener) {
            listeners.remove(listener);
        }

        public int getId() {
            return id;
        }

        public byte getUnitCode() {
            return unitCode;
        }

        public boolean isOn() {
            return on;
        }

        public byte getLevel() {
            return level;
        }
        
        public void turnOff() throws IOException {
            HomeEasy.this.off(id, unitCode);
        }

        public void turnOn() throws IOException {
            HomeEasy.this.on(id, unitCode);
        }

        public void setLevel(byte level) throws IOException {
            HomeEasy.this.level(id, unitCode, level);
        }
    }

    public static interface UnknownApplianceListener {
        void nowOff(int id, byte unitCode);
        void nowOn(int id, byte unitCode);
        void newLevel(int id, byte unitCode, byte level);
    }

    public static interface KnownApplianceListener {
        void nowOff(Appliance unit);
        void nowOn(Appliance unit);
    }
}
