package ru.miit.contentuploader.model;

import lombok.Data;

@Data
public class LargeBinaryData {
    private long idContentVersion;
    private String filename;

    // null if query-binary-metadata is false
    private LargeBinaryMetadata metadata;
}
