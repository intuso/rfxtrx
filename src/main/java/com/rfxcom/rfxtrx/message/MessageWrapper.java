package com.rfxcom.rfxtrx.message;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 24/04/12
 * Time: 18:28
 * To change this template use File | Settings | File Templates.
 */
public abstract class MessageWrapper {
    
    protected Message message;
    
    public MessageWrapper(Message message) {
        this.message = message;
    }

    public void writeTo(OutputStream out, byte sequenceNumber) throws IOException {
        message.writeTo(out, sequenceNumber);
    }

    @Override
    public String toString() {
        return message.toString();
    }
}
