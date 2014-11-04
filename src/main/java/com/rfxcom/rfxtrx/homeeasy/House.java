package com.rfxcom.rfxtrx.homeeasy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomc on 04/11/14.
 */
public class House {

    private final HomeEasy homeEasy;
    private final int houseId;
    private final List<Callback> callbacks = new ArrayList<Callback>();

    private final HomeEasy.Callback homeEasyCallback = new HomeEasy.Callback() {

        @Override
        public void turnedOn(int houseId, byte unitCode) {
            if (House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.turnedOn(unitCode);
            }
        }

        @Override
        public void turnedOnAll(int houseId) {
            if (House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.turnedOnAll();
            }
        }

        @Override
        public void turnedOff(int houseId, byte unitCode) {
            if (House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.turnedOff(unitCode);
            }
        }

        @Override
        public void turnedOffAll(int houseId) {
            if (House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.turnedOffAll();
            }
        }

        @Override
        public void setLevel(int houseId, byte unitCode, byte level) {
            if (House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.setLevel(unitCode, level);
            }
        }

        @Override
        public void setLevelAll(int houseId, byte level) {
            if (House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.setLevelAll(level);
            }
        }
    };

    public House(HomeEasy homeEasy, int houseId) {
        this.homeEasy = homeEasy;
        this.houseId = houseId;
        this.homeEasy.addCallback(homeEasyCallback);
    }

    @Override
    protected void finalize() throws Throwable {
        this.homeEasy.removeCallback(homeEasyCallback);
        super.finalize();
    }

    public void addCallback(Callback listener) {
        callbacks.add(listener);
    }

    public void removeCallback(Callback listener) {
        callbacks.remove(listener);
    }

    public void turnOn(byte unitCode) throws IOException {
        homeEasy.turnOn(houseId, unitCode);
    }

    public void turnOnAll() throws IOException {
        homeEasy.turnOnAll(houseId);
    }

    public void turnOff(byte unitCode) throws IOException {
        homeEasy.turnOff(houseId, unitCode);
    }

    public void turnOffAll() throws IOException {
        homeEasy.turnOffAll(houseId);
    }

    public void setLevel(byte unitCode, byte level) throws IOException {
        homeEasy.setLevel(houseId, unitCode, level);
    }

    public void setLevelAll(byte level) throws IOException {
        homeEasy.setLevelAll(houseId, level);
    }

    public static interface Callback {
        void turnedOn(byte unitCode);
        void turnedOnAll();
        void turnedOff(byte unitCode);
        void turnedOffAll();
        public void setLevel(byte unitCode, byte level);
        void setLevelAll(byte level);
    }
}
