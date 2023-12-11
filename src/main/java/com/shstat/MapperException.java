package com.shstat;

import org.apache.logging.log4j.message.StringFormattedMessage;

public class MapperException extends RuntimeException {
    private boolean sendErrorMessage = true;

    public MapperException(StringFormattedMessage message) {
        super(message.getFormattedMessage());
    }

    public MapperException(StringFormattedMessage message, boolean sendErrorMessage) {
        super(message.getFormattedMessage());
        this.sendErrorMessage = sendErrorMessage;
    }

    public boolean isSendErrorMessage() {
        return sendErrorMessage;
    }
}
