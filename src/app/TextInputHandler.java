package app;
import java.io.*;
import java.nio.file.*;

public class TextInputHandler {
    public String loadFromFile(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    public String loadFromClipboard() {
        return "";
    }
}