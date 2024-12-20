package io.crunch.download;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.AsyncFile;
import io.vertx.mutiny.core.streams.ReadStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The {@code FileStore} interface defines a contract for handling file storage operations,
 * combining Vert.x's asynchronous file handling capabilities with Mutiny's reactive programming model.
 * <p>
 * This interface allows retrieving file content in various formats and performing operations asynchronously, and synchronously as well
 * to enhance application performance and scalability. It is built using:
 * <ul>
 *     <li>{@link io.vertx.mutiny.core.file.AsyncFile} for asynchronous file access, leveraging Vert.x's
 *     non-blocking I/O model.</li>
 *     <li>{@link io.smallrye.mutiny.Uni} for reactive programming, providing a declarative way to compose
 *     asynchronous operations.</li>
 *     <li>{@link io.smallrye.mutiny.Multi} for reactive programming, and handling streams of data, such as reading file content in chunks.</li>
 * </ul>
 */
public interface FileStore {

    /**
     * Retrieves the specified file as an {@link AsyncFile}.
     * <p>
     * The {@link AsyncFile} provides methods to read and write file data asynchronously using
     * Vert.x's non-blocking I/O model. Operations on the file can be composed using Mutiny's lazy asynchronous actions.
     *
     * @param fileName the name of the file to retrieve
     * @return a {@link Uni} emitting the {@link AsyncFile} instance representing the file,
     *         or a failure if the file does not exist or cannot be opened
     * @see <a href="https://vertx.io/docs/vertx-core/java/#_asynchronous_files">Vert.x AsyncFile Documentation</a>
     * @see <a href="https://smallrye.io/smallrye-mutiny/">Mutiny Documentation</a>
     */
    Uni<AsyncFile> getAsyncFile(String fileName);

    /**
     * Reads the content of the specified file into a {@link Buffer}.
     * <p>
     * This method uses the Vert.x file system API to asynchronously read the file's content into memory.
     *
     * @param fileName the name of the file to read
     * @return a {@link Uni} emitting the {@link Buffer} containing the file's content,
     *         or a failure if the file cannot be read
     * @apiNote Do not use this method to read very large files, or you risk running out of available RAM.
     * @see <a href="https://vertx.io/docs/vertx-core/java/#_buffers">Vert.x Buffer Documentation</a>
     * @see <a href="https://smallrye.io/smallrye-mutiny/">Mutiny Documentation</a>
     */
    Uni<Buffer> getBuffer(String fileName);

    /**
     * Reads the content of the specified file into a {@link Multi} of {@link Buffer} instances.
     * <p>
     * Internally it uses the Vert.x {@link AsyncFile} that is a {@link ReadStream}, and reads the file's content in chunks
     *
     * @param fileName the name of the file to read
     * @return a {@link Multi} emitting {@link Buffer} instances containing the file's content.
     * @see <a href="https://vertx.io/docs/vertx-core/java/#_buffers">Vert.x Buffer Documentation</a>
     * @see <a href="https://smallrye.io/smallrye-mutiny/">Mutiny Documentation</a>
     */
    Multi<Buffer> getMultiBuffer(String fileName);

    /**
     * Retrieves the content of the specified file as a byte array.
     * <p>
     * This method blocks the calling thread while reading the file, making it suitable for use cases
     * where synchronous operations are acceptable.
     *
     * @param fileName the name of the file to retrieve
     * @return a byte array containing the file's content, or {@code null} if the file does not exist
     */
    byte[] getByteArray(String fileName);

    /**
     * Retrieves the size of the specified file in bytes.
     * <p>
     * This method blocks the calling thread and returns the file's size. It is suitable for scenarios
     * where synchronous access is required.
     *
     * @param fileName the name of the file to retrieve the size for
     * @return the size of the file in bytes
     */
    long getFileSize(String fileName);

    /**
     * Writes the content of the specified file to the given {@link OutputStream}.
     * <p>
     * This method blocks the calling thread while writing data to the stream. It should be used with caution
     * in scenarios where performance is critical, as it performs I/O operations synchronously.
     *
     * @param fileName the name of the file whose content is to be written
     * @param output   the {@link OutputStream} to write the file's content to
     * @throws IOException if an I/O error occurs during the write operation
     * @see java.io.OutputStream
     */
    void writeContent(String fileName, OutputStream output) throws IOException;
}
