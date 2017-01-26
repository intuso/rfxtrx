package com.rfxcom.rfxtrx.util.lighting2;

import com.google.common.collect.Lists;
import com.intuso.utilities.listener.MemberRegistration;
import com.intuso.utilities.listener.ManagedCollection;

import java.io.IOException;

/**
* Created by tomc on 04/11/14.
*/
public class Lighting2Appliance {

    protected final Lighting2House lighting2House;
    protected final byte unitCode;
    protected final ManagedCollection<Callback> callbacks = new ManagedCollection<Callback>(Lists.<Callback>newCopyOnWriteArrayList());

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
    private final MemberRegistration listenerRegistration;

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

    public MemberRegistration addCallback(Callback listener) {
        return callbacks.add(listener);
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

    public interface Callback {
        void turnedOn(Lighting2Appliance unit);
        void turnedOff(Lighting2Appliance unit);
    }
}
