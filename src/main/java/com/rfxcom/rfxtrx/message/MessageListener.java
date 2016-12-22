package com.rfxcom.rfxtrx.message;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 24/04/12
 * Time: 18:34
 * To change this template use File | Settings | File Templates.
 */
public interface MessageListener {
    void messageReceived(MessageWrapper messageWrapper);
}
