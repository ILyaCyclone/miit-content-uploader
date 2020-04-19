package ru.miit.contentuploader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ru.miit.contentuploader.config.UploaderProperties;
import ru.miit.contentuploader.model.Content;
import ru.miit.contentuploader.service.FolderService;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@SpringBootApplication
public class ContentUploaderApplication {
    private static final Logger logger = LoggerFactory.getLogger(ContentUploaderApplication.class);


    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        logger.trace("call with arguments:", String.join(", ", args));

        if (args.length != 3) {
            System.err.println("arguments: <source folder directory> <destination ID information> <destination IDK content>");
        }

        Path sourceFolderPath = Paths.get(args[0]); // D:\miit\en_landing\images\this
        long destIdInfo = Long.parseLong(args[1]); // 168019
        int idkContent = Integer.parseInt(args[2]); // 1162


        if (!Files.exists(sourceFolderPath)) {
            System.err.println("'" + sourceFolderPath.getFileName() + "' not found");
            System.exit(1);
        }
        if (!Files.isDirectory(sourceFolderPath)) {
            System.err.println("'" + sourceFolderPath.getFileName() + "' is not a directory");
            System.exit(1);
        }

        ApplicationContext context = SpringApplication.run(ContentUploaderApplication.class, args);
        FolderService folderService = context.getBean(FolderService.class);
        UploaderProperties uploaderProperties = context.getBean(UploaderProperties.class);

        Map<String, Content> created = folderService.massUpload(sourceFolderPath, destIdInfo, idkContent);

        if (created.isEmpty()) {
            System.out.println("Nothing was created");
        } else {
            CsvOutput csvOutput = new CsvOutput(created, uploaderProperties);

            PrintStream fileOutput = new PrintStream("d:\\content.csv", StandardCharsets.UTF_8.toString());
            PrintStream[] outputs = new PrintStream[]{System.out, fileOutput};

            for (PrintStream output : outputs) {
                csvOutput.print(output);
            }
        }

    }
}
