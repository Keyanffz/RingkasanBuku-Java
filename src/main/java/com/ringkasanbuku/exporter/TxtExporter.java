package com.ringkasanbuku.exporter;

import com.ringkasanbuku.model.Summary;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TxtExporter implements FileExporter {
    @Override
    public void export(Summary summary, String path) throws IOException {
        Files.writeString(Path.of(path), summary.getResult());
    }
}
