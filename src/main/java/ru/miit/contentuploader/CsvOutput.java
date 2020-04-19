package ru.miit.contentuploader;

import ru.miit.contentuploader.config.UploaderProperties;
import ru.miit.contentuploader.model.Content;
import ru.miit.contentuploader.model.ContentVersion;
import ru.miit.contentuploader.model.LargeBinaryData;
import ru.miit.contentuploader.model.LargeBinaryMetadata;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvOutput {
    private static final String[] headerColumns = "FILENAME, ID_CONTENT, ID_WM_CONTENT, ID_CONTENT_VERSION, ID_WM_CONTENT_VERSION, ANY_IMAGE".split(", ");
    private static final String[] binaryMetadataHeaderColumns = "D_LAST, WIDTH, HEIGHT, HASH".split(", ");
    private static final String DEFAULT_DELIMITER = ",";

    private static final String anyImageMacroFormat = "[ANY_IMAGE(%d)]";

    private final Map<String, Content> created;
    private final UploaderProperties uploaderProperties;
    private final String delimiter;

    public CsvOutput(Map<String, Content> contentMap, UploaderProperties uploaderProperties) {
        this(contentMap, uploaderProperties, DEFAULT_DELIMITER);
    }

    public CsvOutput(Map<String, Content> contentMap, UploaderProperties uploaderProperties, String delimeter) {
        this.created = contentMap;
        this.uploaderProperties = uploaderProperties;
        this.delimiter = delimeter;
    }

    public void print(PrintStream out) {
        String header = String.join(delimiter, headerColumns);
        if (uploaderProperties.isQueryBinaryCalculatedAttributes()) {
            header += delimiter + String.join(delimiter, binaryMetadataHeaderColumns);
        }

        out.println(header);

        created.forEach((filename, content) -> {
            ContentVersion contentVersion = content.getContentVersion();
            LargeBinaryData largeBinaryData = contentVersion.getLargeBinaryData();
            LargeBinaryMetadata metadata = largeBinaryData.getMetadata();
            List<Object> attributes = new ArrayList<>(Arrays.asList(filename, content.getIdContent(), content.getIdWebMetaterm()
                    , contentVersion.getIdContentVersion(), contentVersion.getIdWebMetaterm(), String.format(anyImageMacroFormat, content.getIdWebMetaterm())));
            if (uploaderProperties.isQueryBinaryCalculatedAttributes()) {
                attributes.addAll(Arrays.asList(metadata.getLastModified(), metadata.getWidth(), metadata.getHeight(), metadata.getHash()));
            }
            String line = attributes.stream()
                    .map(String::valueOf)
                    .map(string -> string.replaceAll(",", "\\,")) // escape
                    .collect(Collectors.joining(delimiter));
            out.println(line);
        });
    }
}
