package ru.miit.contentimguploader;

import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FolderService {

    private final Uploader uploader;
    static final Set<String> supportedExtensions = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "svg"));

    public FolderService(Uploader uploader) {
        this.uploader = uploader;
    }


    public Map<String, Long> massUpload(Path sourceFolderPath, long destIdInfo, long idkContent) {
        List<File> files = readDirectory(sourceFolderPath);
        Map<String, Long> createdIdContentVersions = files.stream()
                .collect(Collectors.toMap(File::getName, file -> uploader.uploadFile(file, destIdInfo, idkContent)));
        return createdIdContentVersions;
    }


    @SneakyThrows
    private List<File> readDirectory(Path path) {
        try (Stream<Path> paths = Files.walk(path)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .filter(this::isSupported)
                    .collect(Collectors.toList());
        }
    }

    private boolean isSupported(File file) {
        String extension = FilenameUtils.getExtension(file.getName());
        return isSupportedExtension(extension);
    }

    private boolean isSupportedExtension(String extension) {
        if (extension == null) return false;
        return supportedExtensions.contains(extension.toLowerCase());

    }
}
