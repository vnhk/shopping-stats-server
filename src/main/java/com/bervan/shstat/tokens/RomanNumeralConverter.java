package com.bervan.shstat.tokens;


import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
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
        return convertRomanNumeralsInText(text.toUpperCase());
    }

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

    private String convertRomanNumeralsInText(String text) {
        Pattern pattern = Pattern.compile("\\bM{0,4}(CM|CD|D?C{0,3})"
                + "(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})\\b", Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String roman = matcher.group();
            int arabic = romanToArabic(roman.toUpperCase());
            matcher.appendReplacement(result, String.valueOf(arabic));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
