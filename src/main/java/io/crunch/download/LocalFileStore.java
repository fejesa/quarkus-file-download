package io.crunch.download;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.file.OpenOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.AsyncFile;
import io.vertx.mutiny.core.streams.ReadStream;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@code LocalFileStore} is an implementation of the {@link FileStore} interface
 * that provides file storage operations on a local file system (NFS).
 * <p>
 * This class integrates with Vert.x and Mutiny to perform asynchronous file operations
 * and supports synchronous file access methods for scenarios where blocking operations are acceptable.
 */
@ApplicationScoped
public class LocalFileStore implements FileStore {

    /**
     * The root directory of the file store.
     */
    private final String fileStoreRootDirectory;

    /**
     * The Vert.x instance used to perform file system operations.
     */
    private final Vertx vertx;

    public LocalFileStore(@ConfigProperty(name = "app.filestore.root") String fileStoreRootDirectory, Vertx vertx) {
        this.fileStoreRootDirectory = fileStoreRootDirectory;
        this.vertx = vertx;
    }

    /**
     * Opens the specified file as an {@link AsyncFile} for asynchronous read operations.
     * <p>
     * This method uses Vert.x's file system API with {@link OpenOptions} to open the file in read-only mode.
     * The default read buffer size is set to 8KB.
     *
     * @param fileName the name of the file to open
     * @return a {@link Uni} emitting the {@link AsyncFile} instance representing the file,
     *         or a failure if the file cannot be opened
     */
    @Override
    public Uni<AsyncFile> getAsyncFile(String fileName) {
        var openOptions = new OpenOptions()
                .setRead(true)
                .setCreate(false)
                .setWrite(false);
        return vertx.fileSystem().open(getPathAsString(fileName), openOptions);
    }

    /**
     * Reads the content of the specified file into a {@link Buffer}.
     * <p>
     * This method uses the Vert.x file system API to asynchronously read the file's content into memory.
     *
     * @param fileName the name of the file to read
     * @return a {@link Uni} emitting the {@link Buffer} containing the file's content,
     *         or a failure if the file cannot be read
     * @apiNote Do not use this method to read very large files, or you risk running out of available RAM.
     */
    @Override
    public Uni<Buffer> getBuffer(String fileName) {
        return vertx.fileSystem().readFile(getPathAsString(fileName));
    }

    /**
     * Reads the content of the specified file into a {@link Multi} of {@link Buffer} instances.
     * <p>
     * Internally it uses the Vert.x {@link AsyncFile} that is a {@link ReadStream}, and reads the file's content in chunks.
     *
     * @param fileName the name of the file to read
     * @return a {@link Multi} emitting {@link Buffer} instances containing the file's content.
     */
    @Override
    public Multi<Buffer> getMultiBuffer(String fileName) {
        return getAsyncFile(fileName).onItem().transformToMulti(AsyncFile::toMulti);
    }

    /**
     * Retrieves the size of the specified file in bytes synchronously.
     * <p>
     * This method uses the Vert.x file system API's blocking method {@code propsAndAwait}.
     *
     * @param fileName the name of the file to retrieve the size for
     * @return the size of the file in bytes
     */
    @Override
    public long getFileSize(String fileName) {
        return vertx.fileSystem().propsAndAwait(getPathAsString(fileName)).size();
    }

    /**
     * Reads the content of the specified file into a byte array synchronously.
     * <p>
     * This method uses Java's {@link FileInputStream} to read the file into a {@link ByteArrayOutputStream}.
     *
     * @param fileName the name of the file to read
     * @return a byte array containing the file's content
     * @throws RuntimeException if an I/O error occurs while reading the file
     */
    @Override
    public byte[] getByteArray(String fileName) {
        try (var fis = new FileInputStream(getPath(fileName).toFile());
             var baos = new ByteArrayOutputStream()) {
            fis.transferTo(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes the content of the specified file to the given {@link OutputStream}.
     * <p>
     * This method uses Java's {@link FileInputStream} to read the file in chunks and write it
     * to the provided {@link OutputStream}.
     *
     * @param fileName the name of the file whose content is to be written
     * @param output   the {@link OutputStream} to write the file's content to
     * @throws IOException if an I/O error occurs during the operation
     */
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

    private String getPathAsString(String fileName) {
        return getPath(fileName).toString();
    }
}
