package com.rfxcom.rfxtrx.util.lighting1;

import com.google.common.collect.Lists;
import com.intuso.utilities.listener.ManagedCollection;

import java.io.IOException;

/**
* Created by tomc on 04/11/14.
*/
public class Lighting1Appliance {

    protected final Lighting1House lighting1House;
    protected final byte unitCode;
    protected final ManagedCollection<Callback> callbacks = new ManagedCollection<Callback>(Lists.<Callback>newCopyOnWriteArrayList());

    private final Lighting1House.Callback houseCallback = new Lighting1House.Callback() {

        @Override
        public void turnedOn(byte unitCode) {
            if (Lighting1Appliance.this.unitCode == unitCode) {
                Lighting1Appliance.this.on = true;
                for (Callback listener : callbacks)
                    listener.turnedOn(Lighting1Appliance.this);
            }
        }

        @Override
        public void turnedOnAll() {
            Lighting1Appliance.this.on = true;
            for (Callback listener : callbacks)
                listener.turnedOn(Lighting1Appliance.this);
        }

        @Override
        public void turnedOff(byte unitCode) {
            if (Lighting1Appliance.this.unitCode == unitCode) {
                Lighting1Appliance.this.on = false;
                for (Callback listener : callbacks)
                    listener.turnedOff(Lighting1Appliance.this);
            }
        }

        @Override
        public void turnedOffAll() {
            Lighting1Appliance.this.on = false;
            for (Callback listener : callbacks)
                listener.turnedOff(Lighting1Appliance.this);
        }

        @Override
        public void dim(byte unitCode) {
            // appliances don't have levels
        }

        @Override
        public void bright(byte unitCode) {
            // appliances don't have levels
        }

        @Override
        public void chime() {
            // appliances don't have levels
        }
    };
    private final ManagedCollection.Registration listenerRegistration;

    protected boolean on;

    public Lighting1Appliance(Lighting1House lighting1House, byte unitCode) {
        this(lighting1House, unitCode, false);
    }

    public Lighting1Appliance(Lighting1House lighting1House, byte unitCode, boolean on) {
        this.lighting1House = lighting1House;
        this.unitCode = unitCode;
        this.on = on;
        this.listenerRegistration = this.lighting1House.addCallback(houseCallback);
    }

    @Override
    protected void finalize() throws Throwable {
        this.listenerRegistration.remove();
        super.finalize();
    }

    public ManagedCollection.Registration addCallback(Callback listener) {
        return callbacks.add(listener);
    }

    public byte getUnitCode() {
        return unitCode;
    }

    public boolean isOn() {
        return on;
    }

    public void turnOn() throws IOException {
        lighting1House.turnOn(unitCode);
    }

    public void turnOff() throws IOException {
        lighting1House.turnOff(unitCode);
    }

    public interface Callback {
        void turnedOn(Lighting1Appliance unit);
        void turnedOff(Lighting1Appliance unit);
    }
}
