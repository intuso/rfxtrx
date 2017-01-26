package com.rfxcom.rfxtrx.util.lighting2;

import com.intuso.utilities.listener.ManagedCollection;

import java.io.IOException;

/**
* Created by tomc on 04/11/14.
*/
public class Lighting2DimmableAppliance extends Lighting2Appliance {

    private final Lighting2House.Callback houseCallback = new Lighting2House.Callback() {

        @Override
        public void turnedOn(byte unitCode) {
            // handled by super class
        }

        @Override
        public void turnedOnAll() {
            // handled by super class
        }

        @Override
        public void turnedOff(byte unitCode) {
            // handled by super class
        }

        @Override
        public void turnedOffAll() {
            // handled by super class
        }

        @Override
        public void setLevel(byte unitCode, byte level) {
            if (Lighting2DimmableAppliance.this.unitCode == unitCode) {
                Lighting2DimmableAppliance.this.level = level;
                for (Lighting2Appliance.Callback listener : callbacks)
                    if(listener instanceof Callback)
                        ((Callback) listener).newLevel(Lighting2DimmableAppliance.this, level);
            }
        }

        @Override
        public void setLevelAll(byte level) {
            if (Lighting2DimmableAppliance.this.unitCode == unitCode) {
                Lighting2DimmableAppliance.this.level = level;
                for (Lighting2Appliance.Callback listener : callbacks)
                    if(listener instanceof Callback)
                        ((Callback) listener).newLevel(Lighting2DimmableAppliance.this, level);
            }
        }
    };
    private final ManagedCollection.Registration listenerRegistration;

    protected byte level;

    public Lighting2DimmableAppliance(Lighting2House lighting2House, byte unitCode) {
        this(lighting2House, unitCode, false, (byte)0);
    }

    public Lighting2DimmableAppliance(Lighting2House lighting2House, byte unitCode, boolean on, byte level) {
        super(lighting2House, unitCode, on);
        this.level = level;
        this.listenerRegistration = this.lighting2House.addCallback(houseCallback);
    }

    @Override
    protected void finalize() throws Throwable {
        this.listenerRegistration.remove();
        super.finalize();
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) throws IOException {
        lighting2House.setLevel(unitCode, level);
    }

    public static interface Callback extends Lighting2Appliance.Callback {
        void newLevel(Lighting2DimmableAppliance appliance, byte level);
    }
}
