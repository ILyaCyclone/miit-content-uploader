package ru.miit.contentuploader.repository;

import ru.miit.contentuploader.model.Content;
import ru.miit.contentuploader.model.ContentVersion;
import ru.miit.contentuploader.model.LargeBinaryData;

import java.io.InputStream;

public interface UploadRepository {
    Content createContent(long idkContent);

    int getIdkFormat(String extension);

    void createInformationContent(long idInfo, long idContent);

    ContentVersion createContentVersion(long idContent, int idkFormat, int idLang);

    LargeBinaryData createLargeBinaryData(long idContentVersion, String filename, InputStream inputStream, int length);

}
