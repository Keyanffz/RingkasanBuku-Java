package com.ringkasanbuku.exporter;

import com.ringkasanbuku.model.Summary;
import java.io.IOException;

public interface FileExporter {
    void export(Summary summary, String path) throws IOException;
}
