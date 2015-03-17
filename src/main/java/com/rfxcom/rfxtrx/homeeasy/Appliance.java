package com.rfxcom.rfxtrx.homeeasy;

import com.intuso.utilities.listener.ListenerRegistration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
* Created by tomc on 04/11/14.
*/
public class Appliance {

    protected final House house;
    protected final byte unitCode;
    protected final List<Callback> callbacks = new ArrayList<Callback>();

    private final House.Callback houseCallback = new House.Callback() {

        @Override
        public void turnedOn(byte unitCode) {
            if (Appliance.this.unitCode == unitCode) {
                Appliance.this.on = true;
                for (Callback listener : callbacks)
                    listener.turnedOn(Appliance.this);
            }
        }

        @Override
        public void turnedOnAll() {
            Appliance.this.on = true;
            for (Callback listener : callbacks)
                listener.turnedOn(Appliance.this);
        }

        @Override
        public void turnedOff(byte unitCode) {
            if (Appliance.this.unitCode == unitCode) {
                Appliance.this.on = false;
                for (Callback listener : callbacks)
                    listener.turnedOff(Appliance.this);
            }
        }

        @Override
        public void turnedOffAll() {
            Appliance.this.on = false;
            for (Callback listener : callbacks)
                listener.turnedOff(Appliance.this);
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

    public Appliance(House house, byte unitCode) {
        this(house, unitCode, false);
    }

    public Appliance(House house, byte unitCode, boolean on) {
        this.house = house;
        this.unitCode = unitCode;
        this.on = on;
        this.listenerRegistration = this.house.addCallback(houseCallback);
    }

    @Override
    protected void finalize() throws Throwable {
        this.listenerRegistration.removeListener();
        super.finalize();
    }

    public void addCallback(Callback listener) {
        callbacks.add(listener);
    }

    public void removeCallback(Callback listener) {
        callbacks.remove(listener);
    }

    public byte getUnitCode() {
        return unitCode;
    }

    public boolean isOn() {
        return on;
    }

    public void turnOn() throws IOException {
        house.turnOn(unitCode);
    }

    public void turnOff() throws IOException {
        house.turnOff(unitCode);
    }

    public static interface Callback {
        void turnedOn(Appliance unit);
        void turnedOff(Appliance unit);
    }
}
