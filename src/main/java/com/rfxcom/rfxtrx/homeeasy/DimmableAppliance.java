package com.rfxcom.rfxtrx.homeeasy;

import java.io.IOException;

/**
* Created by tomc on 04/11/14.
*/
public class DimmableAppliance extends Appliance {

    private final House.Callback houseCallback = new House.Callback() {

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
            if (DimmableAppliance.this.unitCode == unitCode) {
                DimmableAppliance.this.level = level;
                for (Appliance.Callback listener : callbacks)
                    if(listener instanceof Callback)
                        ((Callback) listener).newLevel(DimmableAppliance.this, level);
            }
        }

        @Override
        public void setLevelAll(byte level) {
            if (DimmableAppliance.this.unitCode == unitCode) {
                DimmableAppliance.this.level = level;
                for (Appliance.Callback listener : callbacks)
                    if(listener instanceof Callback)
                        ((Callback) listener).newLevel(DimmableAppliance.this, level);
            }
        }
    };

    protected byte level;

    public DimmableAppliance(House house, byte unitCode) {
        this(house, unitCode, false, (byte)0);
    }

    public DimmableAppliance(House house, byte unitCode, boolean on, byte level) {
        super(house, unitCode, on);
        this.level = level;
        house.addCallback(houseCallback);
    }

    @Override
    protected void finalize() throws Throwable {
        house.removeCallback(houseCallback);
        super.finalize();
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) throws IOException {
        house.setLevel(unitCode, level);
    }

    public static interface Callback extends Appliance.Callback {
        void newLevel(DimmableAppliance appliance, byte level);
    }
}
