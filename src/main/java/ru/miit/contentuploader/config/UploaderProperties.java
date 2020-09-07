package ru.miit.contentuploader.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "uploader")
public class UploaderProperties {
    private int idLang = 1;
    private boolean queryBinaryMetadata = false;
    private boolean dryRun = false;

    private String version;
}
