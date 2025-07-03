package com.bervan.shstat.tokens;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class RomanNumeralConverter extends TokenConverter {
    private static final Map<Character, Integer> romanToIntMap = new HashMap<>();

    static {
        romanToIntMap.put('I', 1);
        romanToIntMap.put('V', 5);
        romanToIntMap.put('X', 10);
        romanToIntMap.put('L', 50);
        romanToIntMap.put('C', 100);
        romanToIntMap.put('D', 500);
        romanToIntMap.put('M', 1000);
    }

    @Override
    protected String convertInternal(String text) {
        String upperText = text.toUpperCase();

        // If the entire input matches a valid Roman numeral, process it
        if (isOnlyRomanNumeral(upperText)) {
            int arabic = romanToArabic(upperText);
            return String.valueOf(arabic);
        }

        // If the input contains only letters but not a valid Roman numeral, return empty
        // Or if it contains letters + other characters, also return empty
        return "";
    }

    /**
     * Checks if the entire string is a valid Roman numeral.
     *
     * @param text Input text to check
     * @return true if text is a valid Roman numeral, false otherwise
     */
    private boolean isOnlyRomanNumeral(String text) {
        Pattern pattern = Pattern.compile("^M{0,4}(CM|CD|D?C{0,3})"
                + "(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$", Pattern.CASE_INSENSITIVE);

        return pattern.matcher(text).matches();
    }

    /**
     * Converts a Roman numeral string to its Arabic integer equivalent.
     *
     * @param roman The Roman numeral string
     * @return Arabic number equivalent
     */
    private int romanToArabic(String roman) {
        int total = 0;
        int prevValue = 0;

        for (int i = roman.length() - 1; i >= 0; i--) {
            char c = roman.charAt(i);
            int value = romanToIntMap.getOrDefault(c, 0);
            if (value < prevValue) {
                total -= value;
            } else {
                total += value;
                prevValue = value;
            }
        }

        return total;
    }
}