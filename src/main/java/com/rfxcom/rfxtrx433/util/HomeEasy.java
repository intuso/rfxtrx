package com.rfxcom.rfxtrx433.util;

import com.rfxcom.rfxtrx433.RFXtrx433;
import com.rfxcom.rfxtrx433.message.MessageListener;
import com.rfxcom.rfxtrx433.message.MessageWrapper;
import com.rfxcom.rfxtrx433.message.transceiver.Lighting2;

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

    private List<MessageListener<Lighting2>> listeners = new ArrayList<MessageListener<Lighting2>>();

    private MessageListener<MessageWrapper> listener = new MessageListener<MessageWrapper>() {
        @Override
        public void messageReceived(MessageWrapper messageWrapper) {
            if(messageWrapper instanceof Lighting2) {
                Lighting2 lightingMessageWrapper = (Lighting2)messageWrapper;
                if(lightingMessageWrapper.getSubType().equals(Lighting2.SubType.HomeEasyEU))
                    for(MessageListener<Lighting2> listener : listeners)
                        listener.messageReceived(lightingMessageWrapper);
            }
        }
    };

    RFXtrx433 agent;
    
    public HomeEasy(RFXtrx433 agent) {
        this.agent = agent;
        agent.addListener(listener);
    }

    private void addListener(MessageListener<Lighting2> listener) {
        listeners.add(listener);
    }

    private void removeListener(MessageListener<Lighting2> listener) {
        listeners.remove(listener);
    }

    public HomeEasyDevice createDevice(int id, byte unitCode) {
        return new HomeEasyDevice(id, unitCode);
    }
    
    private void sendCommand(int id, byte unitCode, Lighting2.Command command, byte level) throws IOException {
        agent.sendMessage(new Lighting2(Lighting2.SubType.HomeEasyEU, id, unitCode, command, level));
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
    
    public class HomeEasyDevice {

        private List<MessageListener<Lighting2>> listeners = new ArrayList<MessageListener<Lighting2>>();

        private MessageListener<Lighting2> listener = new MessageListener<Lighting2>() {
            @Override
            public void messageReceived(Lighting2 messageWrapper) {
                if(messageWrapper.getId() == id && messageWrapper.getUnitCode() == unitCode)
                    for(MessageListener<Lighting2> listener : listeners)
                        listener.messageReceived(messageWrapper);
            }
        };

        private int id;
        private byte unitCode;
        
        private HomeEasyDevice(int id, byte unitCode) {
            this.id = id;
            this.unitCode = unitCode;
            HomeEasy.this.addListener(listener);
        }

        private void addListener(MessageListener<Lighting2> listener) {
            listeners.add(listener);
        }

        private void removeListener(MessageListener<Lighting2> listener) {
            listeners.remove(listener);
        }
        
        public void off() throws IOException {
            HomeEasy.this.off(id, unitCode);
        }

        public void on() throws IOException {
            HomeEasy.this.on(id, unitCode);
        }

        public void level(byte level) throws IOException {
            HomeEasy.this.level(id, unitCode, level);
        }
    }
}
