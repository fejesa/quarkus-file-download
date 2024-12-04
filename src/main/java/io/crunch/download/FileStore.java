package io.crunch.download;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.AsyncFile;

import java.io.IOException;
import java.io.OutputStream;

public interface FileStore {

    Uni<AsyncFile> getAsAsyncFile(String fileName);

    Uni<Buffer> getAsBuffer(String fileName);

    byte[] getAsByteArray(String fileName);

    long getFileSize(String fileName);

    void writeContent(String fileName, OutputStream output) throws IOException;
}
