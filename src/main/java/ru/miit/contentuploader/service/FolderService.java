package ru.miit.contentuploader.service;

import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.miit.contentuploader.model.Content;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FolderService {
    private static final Logger logger = LoggerFactory.getLogger(FolderService.class);

    private final Uploader uploader;
    static final Set<String> supportedExtensions = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "svg"));

    public FolderService(Uploader uploader) {
        this.uploader = uploader;
    }

    /**
     *
     * @param sourceFolderPath folder path, storing content files
     * @param destIdInfo id_information where to load (information must be created beforehand)
     * @param idkContent idk_content for content's to be created
     * @return map of filename and created content, entries sorted by filename
     */
    public Map<String, Content> massUpload(Path sourceFolderPath, long destIdInfo, int idkContent) {
        logger.trace("massUpload(sourceFolderPath='{}', destIdInfo={}, idkContent={}", sourceFolderPath, destIdInfo, idkContent);

        List<File> files = readDirectory(sourceFolderPath);
        Map<String, Content> createdContents = files.stream()
//                .collect(Collectors.toMap(File::getName, file -> uploader.uploadFile(file, destIdInfo, idkContent)));
                // return sorted map
                .collect(LinkedHashMap::new, (map, file) -> map.put(file.getName()
                        , uploader.uploadFile(file, destIdInfo, idkContent)), LinkedHashMap::putAll);
        return createdContents;
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
