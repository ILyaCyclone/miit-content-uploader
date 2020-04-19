package ru.miit.contentuploader.model;

import lombok.Data;

@Data
public class ContentVersion {
    private long idContentVersion;
    private long idWebMetaterm;

    private long idContent;

    private int idkFormat;
    private int idLang;

    private LargeBinaryData largeBinaryData;
}
