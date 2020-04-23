package ru.miit.contentuploader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ru.miit.contentuploader.config.UploaderProperties;
import ru.miit.contentuploader.model.Content;
import ru.miit.contentuploader.service.FolderService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SpringBootApplication
public class ContentUploaderApplication {
    private static final Logger logger = LoggerFactory.getLogger(ContentUploaderApplication.class);

    private static final String COMMAND_UPLOAD_SINGLE_FOLDER = "upload-folder";
    private static final String COMMAND_UPLOAD_FOLDER_TREE = "upload-folder-tree";

    public static void main(String[] args) {
        try {
            logger.trace("call with arguments: {}", String.join(", ", args));

            if (args.length == 0) {
                printHelp();
            }

            String command = args[0];
            if (!command.equals(COMMAND_UPLOAD_SINGLE_FOLDER) && !command.equals(COMMAND_UPLOAD_FOLDER_TREE)) {
                printHelp();
            }

            Path sourceFolderPath = Paths.get(args[1]); // D:\miit\en_landing\images\

            if (command.equals(COMMAND_UPLOAD_SINGLE_FOLDER)) {
                long destIdInfo = Long.parseLong(args[2]); // 168019
                int idkContent = Integer.parseInt(args[3]); // 1162

                ApplicationContext context = SpringApplication.run(ContentUploaderApplication.class, args);
                FolderService folderService = context.getBean(FolderService.class);
                UploaderProperties uploaderProperties = context.getBean(UploaderProperties.class);

                if (uploaderProperties.isDryRun()) {
                    logger.info("DRY RUN - no actual content will be created");
                }

                Map<String, Content> created = uploadSingleFolder(sourceFolderPath, destIdInfo, idkContent, folderService, uploaderProperties);
                CsvOutput csvOutput = new CsvOutput(created, uploaderProperties);
                csvOutput.print(new PrintStream(sourceFolderPath+".csv"));
                return;
            }

            if (command.equals(COMMAND_UPLOAD_FOLDER_TREE)) {
                int idkContent = Integer.parseInt(args[2]); // 1162

                ApplicationContext context = SpringApplication.run(ContentUploaderApplication.class, args);
                FolderService folderService = context.getBean(FolderService.class);
                UploaderProperties uploaderProperties = context.getBean(UploaderProperties.class);

                if (uploaderProperties.isDryRun()) {
                    logger.info("DRY RUN - no actual content will be created");
                }

                uploadFolderTree(sourceFolderPath, idkContent, folderService, uploaderProperties);
            }
        } catch (Exception e) {
            printHelp();
            System.err.println("Exception happened: " + e);
        }
    }

    private static void uploadFolderTree(String rootFolderPath, int idkContent, FolderService folderService, UploaderProperties uploaderProperties) throws IOException {
        uploadFolderTree(Paths.get(rootFolderPath), idkContent, folderService, uploaderProperties);
    }

    private static void uploadFolderTree(Path rootFolderPath, int idkContent, FolderService folderService, UploaderProperties uploaderProperties) throws IOException {
        Stream<Path> paths = Files.walk(rootFolderPath);
        List<Path> folders = paths
                .filter(Files::isDirectory)
                .filter(folder -> !folder.getFileName().toString().equals(rootFolderPath.getFileName().toString())) // not root directory
                .collect(Collectors.toList());
        int foldersCount = folders.size();
        IntStream.range(0, foldersCount)
                .forEach(i -> {
                    Path folder = folders.get(i);
                    String folderName = folder.getFileName().toString();
                    logger.info("{}/{} Processing folder '{}'", (i + 1), foldersCount, folderName);

                    long destIdInfo = Long.parseLong(folderName);

                    Map<String, Content> created = Collections.emptyMap();
                    try {
                        created = uploadSingleFolder(folder, destIdInfo, idkContent, folderService, uploaderProperties);
                    } catch (Exception e) {
                        logger.error("Failed processing folder '" + folderName + "'", e);
                    }

                    if (created.isEmpty()) {
                        System.out.println("Nothing was created");
                    } else {
                        try {
                            CsvOutput csvOutput = new CsvOutput(created, uploaderProperties);

                            String outputFolderPath = rootFolderPath.toString() + "_csv";
                            File outputFolder = new File(outputFolderPath);
                            if (!outputFolder.exists()) outputFolder.mkdir();
                            PrintStream fileOutput = new PrintStream(outputFolderPath + "//" + destIdInfo + ".csv");
                            PrintStream[] outputs = new PrintStream[]{/*System.out, */fileOutput};

                            for (PrintStream output : outputs) {
                                csvOutput.print(output);
                            }
                        } catch (Exception e) {
                            logger.error("Failed generating output for folder '" + folderName + "'", e);
                        }
                    }
                });
        logger.info("Finished processing {} folders", foldersCount);
    }



    private static Map<String, Content> uploadSingleFolder(String sourceFolderPath, long destIdInfo, int idkContent
            , FolderService folderService, UploaderProperties uploaderProperties) throws FileNotFoundException {
        return uploadSingleFolder(Paths.get(sourceFolderPath), destIdInfo, idkContent, folderService, uploaderProperties);
    }

    private static Map<String, Content> uploadSingleFolder(Path sourceFolderPath, long destIdInfo, int idkContent
            , FolderService folderService, UploaderProperties uploaderProperties) throws FileNotFoundException {
        if (!Files.exists(sourceFolderPath)) {
            System.err.println("'" + sourceFolderPath.getFileName() + "' not found");
            throw new FileNotFoundException(sourceFolderPath.toString());
        }
        if (!Files.isDirectory(sourceFolderPath)) {
            System.err.println("'" + sourceFolderPath.getFileName() + "' is not a directory");
            throw new FileNotFoundException("'" + sourceFolderPath.getFileName() + "' is not a directory");
        }

        Map<String, Content> created = folderService.massUpload(sourceFolderPath, destIdInfo, idkContent);
        return created;
    }

    private static void printHelp() {
        System.err.println("usage:" +
                "\n" + COMMAND_UPLOAD_SINGLE_FOLDER + " <source folder path> <destination ID information> <destination IDK content>" +
                "\n" + COMMAND_UPLOAD_FOLDER_TREE + " <source folder path> <destination IDK content>");
    }
}
