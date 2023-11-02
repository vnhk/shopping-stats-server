package com.shstat.shstat;

import org.apache.logging.log4j.message.StringFormattedMessage;

public class MapperException extends RuntimeException {
    public MapperException(StringFormattedMessage message) {
        super(message.getFormattedMessage());
    }
}
