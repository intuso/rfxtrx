package com.rfxcom.rfxtrx.message;

import com.intuso.utilities.listener.Listener;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 24/04/12
 * Time: 18:34
 * To change this template use File | Settings | File Templates.
 */
public interface MessageListener extends Listener {
    public void messageReceived(MessageWrapper messageWrapper);
}
