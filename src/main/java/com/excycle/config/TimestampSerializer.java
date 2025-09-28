package com.excycle.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class TimestampSerializer extends JsonSerializer<Long> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void serialize(Long timestamp, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (timestamp == null) {
            gen.writeNull();
            return;
        }

        LocalDateTime dateTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.of("GMT+8")
        );
        gen.writeString(dateTime.format(FORMATTER));
    }
}