package com.bervan.shstat.tokens;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SynonymConverterTest {
    @Test
    void test() {
        SynonymConverter synonymConverter = new SynonymConverter();
        assertEquals(Optional.of("samsung"), synonymConverter.convert("samsung"));
        assertEquals(Optional.of("black"), synonymConverter.convert("czarny"));
        assertEquals(Optional.of("black"), synonymConverter.convert("czarna"));
        assertEquals(Optional.of("black"), synonymConverter.convert("czarne"));
        assertEquals(Optional.of("black"), synonymConverter.convert("czarnych"));
        assertEquals(Optional.of("black"), synonymConverter.convert("black"));
    }

}