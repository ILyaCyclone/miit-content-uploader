package ru.miit.contentuploader.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import ru.miit.contentuploader.config.UploaderProperties;
import ru.miit.contentuploader.model.Content;
import ru.miit.contentuploader.model.ContentVersion;
import ru.miit.contentuploader.model.LargeBinaryData;
import ru.miit.contentuploader.model.LargeBinaryMetadata;

import java.io.InputStream;
import java.time.Instant;
import java.util.Random;

@Repository
@ConditionalOnProperty(name="uploader.dry-run", havingValue = "true", matchIfMissing = false)
public class UploadDryRunRepository implements UploadRepository {
    private final UploaderProperties uploaderProperties;

    public UploadDryRunRepository(UploaderProperties uploaderProperties) {
        this.uploaderProperties = uploaderProperties;
    }

    @Override
    public Content createContent(long idkContent) {
        Content content = new Content();
        content.setIdContent(generateLong());
        content.setIdWebMetaterm(generateLong());
        return content;
    }

    @Override
    public int getIdkFormat(String extension) {
        return extension.hashCode();
    }

    @Override
    public void createInformationContent(long idInfo, long idContent) {
        // do nothing
    }

    @Override
    public ContentVersion createContentVersion(long idContent, int idkFormat, int idLang) {
        ContentVersion contentVersion = new ContentVersion();
        contentVersion.setIdContentVersion(generateLong());
        contentVersion.setIdWebMetaterm(generateLong());
        contentVersion.setIdkFormat(idkFormat);
        contentVersion.setIdContent(idContent);
        contentVersion.setIdLang(idLang);
        return contentVersion;
    }

    @Override
    public LargeBinaryData createLargeBinaryData(long idContentVersion, String filename, InputStream inputStream, int length) {
        LargeBinaryData largeBinaryData = new LargeBinaryData();
        largeBinaryData.setIdContentVersion(idContentVersion);
        largeBinaryData.setFilename(filename);

        if (uploaderProperties.isQueryBinaryCalculatedAttributes()) {
            largeBinaryData.setMetadata(new LargeBinaryMetadata(Instant.now(), "somehash" + idContentVersion
                    , new Random().nextInt(2000), new Random().nextInt(2000)));
        }
        return largeBinaryData;
    }

    private long generateLong() {
        return System.nanoTime();
    }
}
