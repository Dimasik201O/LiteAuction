package org.dimasik.liteauction.backend.communication.impl;

import org.dimasik.liteauction.backend.communication.AbstractCommunication;

public class Local extends AbstractCommunication {
    public Local() {
        super("local");
    }

    @Override
    public void publishMessage(String channel, String message) {
        super.onMessage(channel, message);
    }
}
