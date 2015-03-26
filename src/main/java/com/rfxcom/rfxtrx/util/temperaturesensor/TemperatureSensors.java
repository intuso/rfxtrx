package com.rfxcom.rfxtrx.util.temperaturesensor;

import com.google.common.collect.Lists;
import com.intuso.utilities.listener.Listener;
import com.intuso.utilities.listener.ListenerRegistration;
import com.intuso.utilities.listener.Listeners;
import com.rfxcom.rfxtrx.RFXtrx;
import com.rfxcom.rfxtrx.message.MessageListener;
import com.rfxcom.rfxtrx.message.MessageWrapper;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 24/04/12
 * Time: 17:53
 * To change this template use File | Settings | File Templates.
 */
public class TemperatureSensors {

    private final RFXtrx agent;
    private final com.rfxcom.rfxtrx.message.TemperatureSensors.SubType subType;
    private final Listeners<Callback> callbacks = new Listeners<Callback>(Lists.<Callback>newCopyOnWriteArrayList());

    private final MessageListener listener = new MessageListener() {
        @Override
        public void messageReceived(MessageWrapper messageWrapper) {
            if(messageWrapper instanceof com.rfxcom.rfxtrx.message.TemperatureSensors) {
                com.rfxcom.rfxtrx.message.TemperatureSensors temperatureSensorsMessageWrapper = (com.rfxcom.rfxtrx.message.TemperatureSensors)messageWrapper;
                if(temperatureSensorsMessageWrapper.getSubType() == subType) {
                    int sensorId = temperatureSensorsMessageWrapper.getSensorId();
                    double temperature = temperatureSensorsMessageWrapper.getTemp();
                    for(Callback listener : callbacks)
                        listener.newTemperature(sensorId, temperature);
                }
            }
        }
    };
    private final ListenerRegistration listenerRegistration;

    public static TemperatureSensors forTemp1(RFXtrx agent) {
        return new TemperatureSensors(agent, com.rfxcom.rfxtrx.message.TemperatureSensors.SubType.TEMP1);
    }

    public static TemperatureSensors forTemp2(RFXtrx agent) {
        return new TemperatureSensors(agent, com.rfxcom.rfxtrx.message.TemperatureSensors.SubType.TEMP2);
    }

    public static TemperatureSensors forTemp3(RFXtrx agent) {
        return new TemperatureSensors(agent, com.rfxcom.rfxtrx.message.TemperatureSensors.SubType.TEMP3);
    }

    public static TemperatureSensors forTemp4(RFXtrx agent) {
        return new TemperatureSensors(agent, com.rfxcom.rfxtrx.message.TemperatureSensors.SubType.TEMP4);
    }

    public static TemperatureSensors forTemp5(RFXtrx agent) {
        return new TemperatureSensors(agent, com.rfxcom.rfxtrx.message.TemperatureSensors.SubType.TEMP5);
    }

    public TemperatureSensors(RFXtrx agent, com.rfxcom.rfxtrx.message.TemperatureSensors.SubType subType) {
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

    public static interface Callback extends Listener {
        void newTemperature(int sensorId, double temperature);
    }
}
