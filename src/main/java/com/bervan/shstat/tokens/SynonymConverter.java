package com.bervan.shstat.tokens;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class SynonymConverter extends TokenConverter {
    private static final Map<Pattern, String> REPLACEMENTS = new LinkedHashMap<>();

    static {
        // Black
        REPLACEMENTS.put(Pattern.compile("\\bczarn(y|a|e|i|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "black");
        REPLACEMENTS.put(Pattern.compile("\\bczerni\\b", Pattern.CASE_INSENSITIVE), "black");
        REPLACEMENTS.put(Pattern.compile("\\bczerń\\b", Pattern.CASE_INSENSITIVE), "black");

        // Red
        REPLACEMENTS.put(Pattern.compile("\\bczerwon(y|a|e|i|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "red");
        REPLACEMENTS.put(Pattern.compile("\\bczerwieni\\b", Pattern.CASE_INSENSITIVE), "red");
        REPLACEMENTS.put(Pattern.compile("\\bczerwień\\b", Pattern.CASE_INSENSITIVE), "red");

        // Green
        REPLACEMENTS.put(Pattern.compile("\\bzielon(y|a|e|i|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "green");
        REPLACEMENTS.put(Pattern.compile("\\bzieleni\\b", Pattern.CASE_INSENSITIVE), "green");
        REPLACEMENTS.put(Pattern.compile("\\bzieleń\\b", Pattern.CASE_INSENSITIVE), "green");

        // Blue
        REPLACEMENTS.put(Pattern.compile("\\bniebiesk(i|a|ie|iego|im|ą|ich)?\\b", Pattern.CASE_INSENSITIVE), "blue");
        REPLACEMENTS.put(Pattern.compile("\\bniebieskości\\b", Pattern.CASE_INSENSITIVE), "blue");

        // White
        REPLACEMENTS.put(Pattern.compile("\\bbiał(y|a|e|i|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "white");
        REPLACEMENTS.put(Pattern.compile("\\bbieli\\b", Pattern.CASE_INSENSITIVE), "white");
        REPLACEMENTS.put(Pattern.compile("\\bbiel\\b", Pattern.CASE_INSENSITIVE), "white");

        // Silver
        REPLACEMENTS.put(Pattern.compile("\\bsrebrn(y|a|e|i|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "silver");
        REPLACEMENTS.put(Pattern.compile("\\bsrebra\\b", Pattern.CASE_INSENSITIVE), "silver");
        REPLACEMENTS.put(Pattern.compile("\\bsrebro\\b", Pattern.CASE_INSENSITIVE), "silver");

        // Additional colors
        REPLACEMENTS.put(Pattern.compile("\\bszar(y|a|e|i|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "gray");
        REPLACEMENTS.put(Pattern.compile("\\bzłot(y|a|e|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "gold");
        REPLACEMENTS.put(Pattern.compile("\\bróżow(y|a|e|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "pink");
        REPLACEMENTS.put(Pattern.compile("\\bgranatow(y|a|e|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "navy");
        REPLACEMENTS.put(Pattern.compile("\\bbeżow(y|a|e|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "beige");
        REPLACEMENTS.put(Pattern.compile("\\bbłękitn(y|a|e|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "light blue");
        REPLACEMENTS.put(Pattern.compile("\\bantracytow(y|a|e|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "anthracite");
        REPLACEMENTS.put(Pattern.compile("\\boliwkow(y|a|e|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "olive");
        REPLACEMENTS.put(Pattern.compile("\\bkhaki\\b", Pattern.CASE_INSENSITIVE), "khaki");
        REPLACEMENTS.put(Pattern.compile("\\bkremow(y|a|e|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "cream");
        REPLACEMENTS.put(Pattern.compile("\\bmiedzi(an(y|a|e|ych|ego|emu|ej|ą)?)?\\b", Pattern.CASE_INSENSITIVE), "copper");
        REPLACEMENTS.put(Pattern.compile("\\bmiętow(y|a|e|ych|ego|emu|ej|ą)?\\b", Pattern.CASE_INSENSITIVE), "mint");

        // Series replacement
        REPLACEMENTS.put(Pattern.compile("\\b(series|seria|serii|serię)\\s*(VII|VIII|IX|X|I{1,3}|IV|V?I{0,3}|[1-9]|10)\\b", Pattern.CASE_INSENSITIVE), "s$2");
    }

    @Override
    protected String convertInternal(String text) {
        return replaceSynonyms(text);
    }

    private String replaceSynonyms(String input) {
        String result = input;
        for (Map.Entry<Pattern, String> entry : REPLACEMENTS.entrySet()) {
            result = entry.getKey().matcher(result).replaceAll(entry.getValue());
        }
        return result;
    }
}