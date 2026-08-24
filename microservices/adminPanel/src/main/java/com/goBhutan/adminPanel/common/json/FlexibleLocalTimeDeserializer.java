package com.goBhutan.adminPanel.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class FlexibleLocalTimeDeserializer extends JsonDeserializer<LocalTime> {

    private static final DateTimeFormatter AM_PM_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("h:mm a")
            .toFormatter(Locale.ENGLISH);

    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            AM_PM_FORMATTER,
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ISO_LOCAL_TIME
    );

    @Override
    public LocalTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String time = value.trim();
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalTime.parse(time, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next accepted time format.
            }
        }

        throw context.weirdStringException(
                value,
                LocalTime.class,
                "Expected time in 24-hour format like 13:00, or AM/PM format like 1:00 PM"
        );
    }
}
