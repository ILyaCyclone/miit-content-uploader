package ru.miit.contentuploader.model;

import lombok.Data;

@Data
public class Content {
    private long idContent;
    private long idWebMetaterm;
    private int idkContent;

    private ContentVersion contentVersion;
}
