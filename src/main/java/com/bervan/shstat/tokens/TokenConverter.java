package com.bervan.shstat.tokens;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public abstract class TokenConverter {

    public final Optional<String> convert(String text) {
        if (text == null || text.isBlank() || containsOnlySpecialCharacters(text)) {
            return Optional.empty();
        }
        String result = convertInternal(text);
        if (result == null || result.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(result);
    }

    protected abstract String convertInternal(String string);

    protected boolean containsOnlySpecialCharacters(String text) {
        return text.matches("^[^\\w\\s]+$");
    }
}
