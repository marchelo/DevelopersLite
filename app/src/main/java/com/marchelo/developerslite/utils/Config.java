package com.marchelo.developerslite.utils;

import java.text.DateFormat;

/**
 * @author Oleg Green
 * @since 22.05.16
 */
public class Config {
    public static DateFormat getDateFormat() {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    }
}