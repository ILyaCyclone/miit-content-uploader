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

    public static void main(String[] args) {
        try {
            if (logger.isTraceEnabled()) logger.trace("call with arguments: {}", String.join(", ", args));

            if (args.length == 0) {
                printHelp();
                return;
            }

            String commandArgument = args[0];
            Command command = Command.fromString(commandArgument);

            switch (command) {

                case UPLOAD_SINGLE_FOLDER: {
                    Path sourceFolderPath = Paths.get(args[1]);
                    long destIdInfo = Long.parseLong(args[2]);
                    int idkContent = Integer.parseInt(args[3]);

                    ApplicationContext context = SpringApplication.run(ContentUploaderApplication.class, args);
                    FolderService folderService = context.getBean(FolderService.class);
                    UploaderProperties uploaderProperties = context.getBean(UploaderProperties.class);

                    logger.info("Uploading folder '{}' to id_information {}, idk_content {}..."
                            , sourceFolderPath, destIdInfo, idkContent);

                    if (uploaderProperties.isDryRun()) {
                        logger.info("DRY RUN - no actual content will be created");
                    }

                    Map<String, Content> created = uploadSingleFolder(sourceFolderPath, destIdInfo, idkContent, folderService, uploaderProperties);
                    CsvOutput csvOutput = new CsvOutput(created, uploaderProperties);
                    String csvOutputFilename = sourceFolderPath + ".csv";
                    csvOutput.print(new PrintStream(csvOutputFilename));

                    logger.info("Finished uploading {} files", created.size());
                    logger.info("Created CSV file: {}", csvOutputFilename);

                    break;
                }
                case UPLOAD_FOLDER_STRUCTURE: {
                    Path rootFolderPath = Paths.get(args[1]);
                    int idkContent = Integer.parseInt(args[2]); // 1162

                    ApplicationContext context = SpringApplication.run(ContentUploaderApplication.class, args);
                    FolderService folderService = context.getBean(FolderService.class);
                    UploaderProperties uploaderProperties = context.getBean(UploaderProperties.class);

                    if (uploaderProperties.isDryRun()) {
                        logger.info("DRY RUN - no actual content will be created");
                    }

                    uploadFolderStructure(rootFolderPath, idkContent, folderService, uploaderProperties);
                    break;
                }
                case HELP:
                    printHelp();
                    break;
                case VERSION:
                    printVersion();
                    break;
                default:
                    System.out.println("First argument must be command");
                    printHelp();
            }
        } catch (Exception e) {
            printHelp();
            System.err.println("Exception happened: " + e);
        }
    }

    private static void uploadFolderStructure(String rootFolderPath, int idkContent, FolderService folderService, UploaderProperties uploaderProperties) throws IOException {
        uploadFolderStructure(Paths.get(rootFolderPath), idkContent, folderService, uploaderProperties);
    }

    private static void uploadFolderStructure(Path rootFolderPath, int idkContent, FolderService folderService, UploaderProperties uploaderProperties) throws IOException {
        String outputFolderPath = rootFolderPath.toString() + "_csv";
        File outputFolder = new File(outputFolderPath);
        if (!outputFolder.exists()) outputFolder.mkdir();

        try (Stream<Path> paths = Files.walk(rootFolderPath)) {
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

                        Map<String, Content> created = Collections.emptyMap();
                        try {
                            long destIdInfo = Long.parseLong(folderName);
                            created = uploadSingleFolder(folder, destIdInfo, idkContent, folderService, uploaderProperties);
                        } catch (NumberFormatException e) {
                            logger.error("Failed processing folder '{}': folder name must be number (ID information)", folderName);
                        } catch (Exception e) {
                            logger.error("Failed processing folder '{}'", folderName, e);
                        }

                        if (created.isEmpty()) {
                            logger.info("Nothing was created");
                        } else {
                            try {
                                CsvOutput csvOutput = new CsvOutput(created, uploaderProperties);
                                String outputFilename = outputFolderPath + File.separator + folderName + ".csv";

                                PrintStream fileOutput = new PrintStream(outputFilename);
                                PrintStream[] outputs = new PrintStream[]{/*System.out, */fileOutput};

                                for (PrintStream output : outputs) {
                                    csvOutput.print(output);
                                }
                            } catch (Exception e) {
                                logger.error("Failed generating output for folder '{}'", folderName, e);
                            }
                        }
                    });
            logger.info("Finished processing {} folders", foldersCount);
        }
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
        System.out.println("usage:" +
                '\n' + Command.UPLOAD_SINGLE_FOLDER.id() + " <source folder path> <destination ID information> <destination IDK content>" +
                '\n' + Command.UPLOAD_FOLDER_STRUCTURE.id() + " <source folder path> <destination IDK content>" +
                '\n' + Command.HELP.id() + " print short help" +
                '\n' + Command.VERSION.id() + " print program version");
    }

    private static void printVersion() {
        ApplicationContext context = SpringApplication.run(ContentUploaderApplication.class);
        String version = context.getBean(UploaderProperties.class).getVersion();

        System.out.println("Mass content uploader for MIIT schema" +
                "\nVersion: " + version);
    }
}
