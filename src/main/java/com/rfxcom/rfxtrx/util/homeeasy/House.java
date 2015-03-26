package com.rfxcom.rfxtrx.util.homeeasy;

import com.google.common.collect.Lists;
import com.intuso.utilities.listener.Listener;
import com.intuso.utilities.listener.ListenerRegistration;
import com.intuso.utilities.listener.Listeners;

import java.io.IOException;

/**
 * Created by tomc on 04/11/14.
 */
public class House {

    private final HomeEasy homeEasy;
    private final int houseId;
    private final Listeners<Callback> callbacks = new Listeners<Callback>(Lists.<Callback>newCopyOnWriteArrayList());

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
    private final ListenerRegistration listenerRegistration;

    public House(HomeEasy homeEasy, int houseId) {
        this.homeEasy = homeEasy;
        this.houseId = houseId;
        this.listenerRegistration = this.homeEasy.addCallback(homeEasyCallback);
    }

    @Override
    protected void finalize() throws Throwable {
        this.listenerRegistration.removeListener();
        super.finalize();
    }

    public ListenerRegistration addCallback(Callback listener) {
        return callbacks.addListener(listener);
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

    public static interface Callback extends Listener {
        void turnedOn(byte unitCode);
        void turnedOnAll();
        void turnedOff(byte unitCode);
        void turnedOffAll();
        public void setLevel(byte unitCode, byte level);
        void setLevelAll(byte level);
    }
}
