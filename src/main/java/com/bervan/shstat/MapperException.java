package com.bervan.shstat;

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

    public MapperException(String message) {
        super(message);
    }

    public boolean isSendErrorMessage() {
        return sendErrorMessage;
    }
}
