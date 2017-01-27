package com.rfxcom.rfxtrx.util.lighting2;

import com.google.common.collect.Lists;
import com.intuso.utilities.collection.ManagedCollection;

import java.io.IOException;

/**
 * Created by tomc on 04/11/14.
 */
public class Lighting2House {

    private final Lighting2 lighting2;
    private final int houseId;
    private final ManagedCollection<Callback> callbacks = new ManagedCollection<Callback>(Lists.<Callback>newCopyOnWriteArrayList());

    private final Lighting2.Callback homeEasyCallback = new Lighting2.Callback() {

        @Override
        public void turnedOn(int houseId, byte unitCode) {
            if (Lighting2House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.turnedOn(unitCode);
            }
        }

        @Override
        public void turnedOnAll(int houseId) {
            if (Lighting2House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.turnedOnAll();
            }
        }

        @Override
        public void turnedOff(int houseId, byte unitCode) {
            if (Lighting2House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.turnedOff(unitCode);
            }
        }

        @Override
        public void turnedOffAll(int houseId) {
            if (Lighting2House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.turnedOffAll();
            }
        }

        @Override
        public void setLevel(int houseId, byte unitCode, byte level) {
            if (Lighting2House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.setLevel(unitCode, level);
            }
        }

        @Override
        public void setLevelAll(int houseId, byte level) {
            if (Lighting2House.this.houseId == houseId) {
                for (Callback listener : callbacks)
                    listener.setLevelAll(level);
            }
        }
    };
    private final ManagedCollection.Registration listenerRegistration;

    public Lighting2House(Lighting2 lighting2, int houseId) {
        this.lighting2 = lighting2;
        this.houseId = houseId;
        this.listenerRegistration = this.lighting2.addCallback(homeEasyCallback);
    }

    @Override
    protected void finalize() throws Throwable {
        this.listenerRegistration.remove();
        super.finalize();
    }

    public ManagedCollection.Registration addCallback(Callback listener) {
        return callbacks.add(listener);
    }

    public void turnOn(byte unitCode) throws IOException {
        lighting2.turnOn(houseId, unitCode);
    }

    public void turnOnAll() throws IOException {
        lighting2.turnOnAll(houseId);
    }

    public void turnOff(byte unitCode) throws IOException {
        lighting2.turnOff(houseId, unitCode);
    }

    public void turnOffAll() throws IOException {
        lighting2.turnOffAll(houseId);
    }

    public void setLevel(byte unitCode, byte level) throws IOException {
        lighting2.setLevel(houseId, unitCode, level);
    }

    public void setLevelAll(byte level) throws IOException {
        lighting2.setLevelAll(houseId, level);
    }

    public interface Callback {
        void turnedOn(byte unitCode);
        void turnedOnAll();
        void turnedOff(byte unitCode);
        void turnedOffAll();
        void setLevel(byte unitCode, byte level);
        void setLevelAll(byte level);
    }
}
