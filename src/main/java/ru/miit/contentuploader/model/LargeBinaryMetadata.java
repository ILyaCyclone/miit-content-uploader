package ru.miit.contentuploader.model;

import lombok.Data;

import java.time.Instant;

@Data
public class LargeBinaryMetadata {
    private final Instant lastModified;
    private final String hash;
    private final Integer width;
    private final Integer height;
}
