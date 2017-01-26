package com.rfxcom.rfxtrx.util.temperaturesensor;

import com.google.common.collect.Lists;
import com.intuso.utilities.listener.ManagedCollection;

/**
 * Created by tomc on 04/11/14.
 */
public class TemperatureSensor {

    private final TemperatureSensors sensors;
    private final int sensorId;
    private final ManagedCollection<Callback> callbacks = new ManagedCollection<Callback>(Lists.<Callback>newCopyOnWriteArrayList());

    private final TemperatureSensors.Callback homeEasyCallback = new TemperatureSensors.Callback() {

        @Override
        public void newTemperature(int sensorId, double temperature) {
            if (TemperatureSensor.this.sensorId == sensorId) {
                for (Callback listener : callbacks)
                    listener.newTemperature(temperature);
            }
        }
    };
    private final ManagedCollection.Registration listenerRegistration;

    public TemperatureSensor(TemperatureSensors sensors, int sensorId) {
        this.sensors = sensors;
        this.sensorId = sensorId;
        this.listenerRegistration = this.sensors.addCallback(homeEasyCallback);
    }

    @Override
    protected void finalize() throws Throwable {
        this.listenerRegistration.remove();
        super.finalize();
    }

    public ManagedCollection.Registration addCallback(Callback listener) {
        return callbacks.add(listener);
    }

    public interface Callback {
        void newTemperature(double temperature);
    }
}
