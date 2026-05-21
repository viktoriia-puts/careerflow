package com.careerflow.jobsearch.service;

import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Service
public class TextCleaningService {

    private static final Charset WINDOWS_1252 = Charset.forName("Windows-1252");

    public String cleanJobText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String cleaned = value;

        cleaned = replaceKnownMojibake(cleaned);

        if (looksLikeMojibake(cleaned)) {
            String repaired = repairWindows1252Mojibake(cleaned);

            if (countMojibakeMarkers(repaired) < countMojibakeMarkers(cleaned)) {
                cleaned = repaired;
            }
        }

        cleaned = replaceKnownMojibake(cleaned);

        return cleaned
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n\\s+", "\n")
                .trim();
    }

    private String replaceKnownMojibake(String value) {
        return value
                .replace("Ã¤", "\u00E4")
                .replace("Ã¶", "\u00F6")
                .replace("Ã¼", "\u00FC")
                .replace("Ã„", "\u00C4")
                .replace("Ã–", "\u00D6")
                .replace("Ãœ", "\u00DC")
                .replace("Ã\u009c", "\u00DC")
                .replace("ÃŸ", "\u00DF")
                .replace("Ã\u009f", "\u00DF")
                .replace("Ã©", "\u00E9")
                .replace("Ã¨", "\u00E8")
                .replace("Ã¡", "\u00E1")
                .replace("Ã ", "\u00E0")
                .replace("Ã´", "\u00F4")
                .replace("Ã§", "\u00E7")
                .replace("â\u0080\u0093", "\u2013")
                .replace("â\u0080\u0094", "\u2014")
                .replace("â\u0080\u0098", "\u2018")
                .replace("â\u0080\u0099", "\u2019")
                .replace("â\u0080\u009c", "\u201C")
                .replace("â\u0080\u009d", "\u201D")
                .replace("â\u0080\u00a6", "\u2026")
                .replace("â\u0082¬", "\u20AC")
                .replace("â\u0080¯", " ")
                .replace("â", "\u2013")
                .replace("â", "\u2014")
                .replace("â", "\u2018")
                .replace("â", "\u2019")
                .replace("â", "\u201C")
                .replace("â", "\u201D")
                .replace("â¦", "\u2026")
                .replace("â¬", "\u20AC")
                .replace("â¯", " ")
                .replace("Â ", " ")
                .replace("Â", " ");
    }

    private boolean looksLikeMojibake(String value) {
        return value.contains("Ã")
                || value.contains("Â")
                || value.contains("â")
                || value.contains("\u0080")
                || value.contains("\u0093")
                || value.contains("\u009c")
                || value.contains("\u009f");
    }

    private int countMojibakeMarkers(String value) {
        int count = 0;
        String markers = "ÃÂâ\u0080\u0093\u009c\u009f";

        for (int i = 0; i < value.length(); i++) {
            if (markers.indexOf(value.charAt(i)) >= 0) {
                count++;
            }
        }

        return count;
    }

    private String repairWindows1252Mojibake(String value) {
        try {
            return new String(value.getBytes(WINDOWS_1252), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}