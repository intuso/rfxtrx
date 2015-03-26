package com.rfxcom.rfxtrx.util.lighting1;

import com.google.common.collect.Lists;
import com.intuso.utilities.listener.Listener;
import com.intuso.utilities.listener.ListenerRegistration;
import com.intuso.utilities.listener.Listeners;

import java.io.IOException;

/**
 * Created by tomc on 04/11/14.
 */
public class House {

    private final Lighting1 lighting1;
    private final byte houseId;
    private final Listeners<Callback> callbacks = new Listeners<Callback>(Lists.<Callback>newCopyOnWriteArrayList());

    private final Lighting1.Callback homeEasyCallback = new Lighting1.Callback() {

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
        public void dim(int houseId, byte unitCode) {
            if (House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.dim(unitCode);
            }
        }

        @Override
        public void bright(int houseId, byte unitCode) {
            if (House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.bright(unitCode);
            }
        }

        @Override
        public void chime(int houseId) {
            if (House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.chime();
            }
        }
    };
    private final ListenerRegistration listenerRegistration;

    public House(Lighting1 lighting1, byte houseId) {
        this.lighting1 = lighting1;
        this.houseId = houseId;
        this.listenerRegistration = this.lighting1.addCallback(homeEasyCallback);
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
        lighting1.turnOn(houseId, unitCode);
    }

    public void turnOnAll() throws IOException {
        lighting1.turnOnAll(houseId);
    }

    public void turnOff(byte unitCode) throws IOException {
        lighting1.turnOff(houseId, unitCode);
    }

    public void turnOffAll() throws IOException {
        lighting1.turnOffAll(houseId);
    }

    public void dim(byte unitCode) throws IOException {
        lighting1.dim(houseId, unitCode);
    }

    public void bright(byte unitCode) throws IOException {
        lighting1.bright(houseId, unitCode);
    }

    public void chime() throws IOException {
        lighting1.chime(houseId);
    }

    public static interface Callback extends Listener {
        void turnedOn(byte unitCode);
        void turnedOnAll();
        void turnedOff(byte unitCode);
        void turnedOffAll();
        void dim(byte unitCode);
        void bright(byte unitCode);
        void chime();
    }
}
