package ru.miit.contentuploader;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public enum Command {
    UPLOAD_SINGLE_FOLDER("upload-folder")
    , UPLOAD_FOLDER_STRUCTURE("upload-folder-structure")
    , HELP("-help")
    , VERSION("-version")
    ;

    private static final Map<String, Command> map =
            Stream.of(values()).collect(
                    toMap(Command::id, Function.identity()));

    private final String id;

    Command(String id) {
        this.id = id;
    }

    static Command fromString(String id) {
        return map.get(id);
    }

    String id() {
        return id;
    }
}
