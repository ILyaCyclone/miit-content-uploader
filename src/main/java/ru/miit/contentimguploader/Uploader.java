package ru.miit.contentimguploader;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;

@Service
public class Uploader {

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
    public long uploadFile(File file, long idInfo, long idkContent) {
        String filename = file.getName();
        System.out.println("loading file " + filename);
        String extension = filename.substring(filename.lastIndexOf('.') + 1);

        long idContent = uploadRepository.createContent(idkContent);
        uploadRepository.createInformationContent(idInfo, idContent);
        long idkFormat = uploadRepository.getIdkFormat(extension);
        long idContentVersion = uploadRepository.createContentVersion(idContent, idkFormat, uploaderProperties.getIdLang());
        uploadRepository.createLargeBinaryData(idContentVersion, filename, new FileInputStream(file), (int) file.length());
        return idContentVersion;
    }
}
