package com.home.expenseautomator.util;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static String extractRegex(String text, String regexPattern) throws IOException {
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(text);
        String result = null;
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }
}
