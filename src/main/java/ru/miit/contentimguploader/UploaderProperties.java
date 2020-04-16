package ru.miit.contentimguploader;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "uploader")
public class UploaderProperties {
    private long idLang;
}
