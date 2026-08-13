package com.stationery_ecommerce.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class HelperMethod {

    public static String generateSlug(String input) {
        if (input == null || input.isEmpty()) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

        String slug = pattern.matcher(normalized).replaceAll("").toLowerCase(Locale.ROOT);
        return slug.replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
