package com.rfxcom.rfxtrx.util.lighting2;

import com.google.common.collect.Lists;
import com.intuso.utilities.listener.Listener;
import com.intuso.utilities.listener.ListenerRegistration;
import com.intuso.utilities.listener.Listeners;

import java.io.IOException;

/**
* Created by tomc on 04/11/14.
*/
public class Lighting2Appliance {

    protected final Lighting2House lighting2House;
    protected final byte unitCode;
    protected final Listeners<Callback> callbacks = new Listeners<Callback>(Lists.<Callback>newCopyOnWriteArrayList());

    private final Lighting2House.Callback houseCallback = new Lighting2House.Callback() {

        @Override
        public void turnedOn(byte unitCode) {
            if (Lighting2Appliance.this.unitCode == unitCode) {
                Lighting2Appliance.this.on = true;
                for (Callback listener : callbacks)
                    listener.turnedOn(Lighting2Appliance.this);
            }
        }

        @Override
        public void turnedOnAll() {
            Lighting2Appliance.this.on = true;
            for (Callback listener : callbacks)
                listener.turnedOn(Lighting2Appliance.this);
        }

        @Override
        public void turnedOff(byte unitCode) {
            if (Lighting2Appliance.this.unitCode == unitCode) {
                Lighting2Appliance.this.on = false;
                for (Callback listener : callbacks)
                    listener.turnedOff(Lighting2Appliance.this);
            }
        }

        @Override
        public void turnedOffAll() {
            Lighting2Appliance.this.on = false;
            for (Callback listener : callbacks)
                listener.turnedOff(Lighting2Appliance.this);
        }

        @Override
        public void setLevel(byte unitCode, byte level) {
            // appliances don't have levels
        }

        @Override
        public void setLevelAll(byte level) {
            // appliances don't have levels
        }
    };
    private final ListenerRegistration listenerRegistration;

    protected boolean on;

    public Lighting2Appliance(Lighting2House lighting2House, byte unitCode) {
        this(lighting2House, unitCode, false);
    }

    public Lighting2Appliance(Lighting2House lighting2House, byte unitCode, boolean on) {
        this.lighting2House = lighting2House;
        this.unitCode = unitCode;
        this.on = on;
        this.listenerRegistration = this.lighting2House.addCallback(houseCallback);
    }

    @Override
    protected void finalize() throws Throwable {
        this.listenerRegistration.removeListener();
        super.finalize();
    }

    public ListenerRegistration addCallback(Callback listener) {
        return callbacks.addListener(listener);
    }

    public byte getUnitCode() {
        return unitCode;
    }

    public boolean isOn() {
        return on;
    }

    public void turnOn() throws IOException {
        lighting2House.turnOn(unitCode);
    }

    public void turnOff() throws IOException {
        lighting2House.turnOff(unitCode);
    }

    public static interface Callback extends Listener {
        void turnedOn(Lighting2Appliance unit);
        void turnedOff(Lighting2Appliance unit);
    }
}