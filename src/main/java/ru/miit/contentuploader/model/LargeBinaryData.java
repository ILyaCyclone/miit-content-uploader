package ru.miit.contentuploader.model;

import lombok.Data;

@Data
public class LargeBinaryData {
    private long idContentVersion;
    private String filename;

    // null if query-binary-calculated-attributes
    LargeBinaryMetadata metadata;
}
