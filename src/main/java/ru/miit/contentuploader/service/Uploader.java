package ru.miit.contentuploader.service;

import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.miit.contentuploader.config.UploaderProperties;
import ru.miit.contentuploader.model.Content;
import ru.miit.contentuploader.model.ContentVersion;
import ru.miit.contentuploader.model.LargeBinaryData;
import ru.miit.contentuploader.repository.UploadRepository;

import java.io.File;
import java.io.FileInputStream;

@Service
public class Uploader {
    private static final Logger logger = LoggerFactory.getLogger(Uploader.class);

    private final UploadRepository uploadRepository;
    private final UploaderProperties uploaderProperties;

    public Uploader(UploadRepository uploadRepository, UploaderProperties uploaderProperties) {
        this.uploadRepository = uploadRepository;
        this.uploaderProperties = uploaderProperties;
    }

    /**
     * @return created id_content_version
     */
    @SneakyThrows
    @Transactional
    public Content uploadFile(File file, long idInfo, int idkContent) {
        String filename = file.getName();
        logger.info("loading file {}", filename);
        String extension = FilenameUtils.getExtension(filename);

        Content content = uploadRepository.createContent(idkContent);
        uploadRepository.createInformationContent(idInfo, content.getIdContent());
        int idkFormat = uploadRepository.getIdkFormat(extension);
        ContentVersion contentVersion = uploadRepository.createContentVersion(content.getIdContent(), idkFormat, uploaderProperties.getIdLang());
        LargeBinaryData largeBinaryData = uploadRepository.createLargeBinaryData(contentVersion.getIdContentVersion(), filename, new FileInputStream(file), (int) file.length());
        contentVersion.setLargeBinaryData(largeBinaryData);

        content.setContentVersion(contentVersion);
        return content;
    }
}
