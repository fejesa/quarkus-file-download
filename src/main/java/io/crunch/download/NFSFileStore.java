package io.crunch.download;

import io.smallrye.mutiny.Uni;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.AsyncFile;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class NFSFileStore implements FileStore {

    private final String fileStoreRootDirectory;

    private final Vertx vertx;

    public NFSFileStore(@ConfigProperty(name = "app.filestore.root") String fileStoreRootDirectory, Vertx vertx) {
        this.fileStoreRootDirectory = fileStoreRootDirectory;
        this.vertx = vertx;
    }

    @Override
    public Uni<AsyncFile> getAsAsyncFile(String fileName) {
        var openOptions = new OpenOptions()
                .setCreate(false)
                .setWrite(false);
        return vertx.fileSystem().open(getPath(fileName).toString(), openOptions);
    }

    @Override
    public Uni<Buffer> getAsBuffer(String fileName) {
        return vertx.fileSystem().readFile(getPath(fileName).toString());
    }

    @Override
    public long getFileSize(String fileName) {
        return vertx.fileSystem().propsAndAwait(getPath(fileName).toString()).size();
    }

    @Override
    public byte[] getAsByteArray(String fileName) {
        try (var fis = new FileInputStream(getPath(fileName).toFile());
             var baos = new ByteArrayOutputStream()) {
            fis.transferTo(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeContent(String fileName, OutputStream output) throws IOException {
        try (var is = new FileInputStream(getPath(fileName).toFile())) {
            int c;
            var buf = new byte[4096];
            while ((c = is.read(buf, 0, buf.length)) > 0) {
                output.write(buf, 0, c);
                output.flush();
            }
        }
    }

    private Path getPath(String fileName) {
        return Paths.get(fileStoreRootDirectory, fileName);
    }
}
