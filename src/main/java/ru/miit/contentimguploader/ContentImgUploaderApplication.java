package ru.miit.contentimguploader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@SpringBootApplication
public class ContentImgUploaderApplication {
    private static final Logger logger = LoggerFactory.getLogger(ContentImgUploaderApplication.class);

    public static void main(String[] args) {
        if(args.length != 3) {
            System.err.println("arguments: <source folder directory> <destination ID information> <destination IDK content>");
        }

        logger.debug("call with arguments '{}', {}, {}", args[0], args[2], args[2]);


        Path sourceFolderPath = Paths.get(args[0]); // D:\miit\en_landing\images\this
        long destIdInfo = Long.parseLong(args[1]); // 168019
        long idkContent = Long.parseLong(args[2]); // 1162


        if(!Files.exists(sourceFolderPath)) {
            System.err.println("'"+sourceFolderPath.getFileName() + "' not found");
            System.exit(1);
        }
        if(!Files.isDirectory(sourceFolderPath)) {
            System.err.println("'"+sourceFolderPath.getFileName() + "' is not a directory");
            System.exit(1);
        }

        ApplicationContext context = SpringApplication.run(ContentImgUploaderApplication.class, args);
        FolderService folderService = context.getBean(FolderService.class);

        Map<String, Long> createdIdContentVersions = folderService.massUpload(sourceFolderPath, destIdInfo, idkContent);
        createdIdContentVersions.forEach((filename, idContentVersion) -> {
            System.out.println(filename+", "+idContentVersion);
        });
    }
}
