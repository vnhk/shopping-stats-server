package com.bervan.shstat.tokens;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RomanNumeralConverterTest {
    @Test
    void test() {
        RomanNumeralConverter converter = new RomanNumeralConverter();
        assertEquals(Optional.empty(), converter.convert("samsung"));
        assertEquals(Optional.empty(), converter.convert("black"));
        assertEquals(Optional.empty(), converter.convert("blackVI"));
        assertEquals(Optional.of("3"), converter.convert("III"));
        assertEquals(Optional.empty(), converter.convert("VIXX"));
    }


}