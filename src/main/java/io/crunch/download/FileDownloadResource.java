package io.crunch.download;

import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.AsyncFile;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * {@code FileDownloadResource} provides RESTful endpoints for downloading files in various formats.
 * <p>
 * This class leverages:
 * <ul>
 *   <li>SmallRye Mutiny for reactive programming.</li>
 *   <li>RESTEasy Reactive for building non-blocking RESTful APIs.</li>
 *   <li>Vert.x Mutiny APIs for asynchronous file handling.</li>
 * </ul>
 */
@Path("/download")
public class FileDownloadResource {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final FileStore fileStore;

    public FileDownloadResource(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    /**
     * Endpoint to download a file as an {@link AsyncFile}.
     *
     * @param fileName the name of the file to download
     * @return a {@link Uni} emitting a {@link RestResponse} containing the {@link AsyncFile}
     * @apiNote The call is executed on the event loop thread.
     */
    @Path("/asyncFile/{name}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Uni<RestResponse<AsyncFile>> downloadAsyncFile(@RestPath("name") String fileName) {
        logger.info("asyncFile [{}]", fileName);
        return fileStore.getAsyncFile(fileName)
            .onItem()
            .transform(asyncFile -> RestResponse.ResponseBuilder.ok(asyncFile, "application/pdf").build());
    }

    /**
     * Endpoint to download a file as a Vert.x {@link Buffer} asynchronously.
     *
     * @param fileName the name of the file to download
     * @return a {@link Uni} emitting a {@link RestResponse} containing the file's content as a {@link Buffer}
     * @apiNote The call is executed on the event loop thread.
     */
    @Path("/asyncBuffer/{name}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Uni<RestResponse<Buffer>> downloadAsyncBuffer(@RestPath("name") String fileName) {
        logger.info("asyncBuffer [{}]", fileName);
        return fileStore.getBuffer(fileName)
            .onItem()
            .transform(b -> RestResponse.ResponseBuilder.ok(b).build());
    }

    /**
     * Endpoint to download a file as a {@link Multi} of {@link Buffer} instances asynchronously.
     * <p>
     * @param fileName the name of the file to download
     * @return a {@link Multi} emitting the file's content as a stream of {@link Buffer} instances
     * @apiNote The call is executed on the event loop thread.
     */
    @Path("/asyncMultiBuffer/{name}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Multi<Buffer> downloadAsyncMultiBuffer(@RestPath("name") String fileName) {
        logger.info("asyncMultiBuffer [{}]", fileName);
        return fileStore.getMultiBuffer(fileName);
    }

    /**
     * Endpoint to download a file as a streaming output.
     * <p>
     * This method uses blocking I/O to stream the file content directly to the HTTP response.
     *
     * @param fileName the name of the file to download
     * @return a {@link RestResponse} containing a {@link StreamingOutput} for the file's content
     * @apiNote The call is executed on worker thread pool to avoid blocking the event loop (limit concurrency).
     */
    @Path("/stream/{name}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public RestResponse<StreamingOutput> downloadStream(@RestPath("name") String fileName) {
        logger.info("stream [{}]", fileName);
        try {
            StreamingOutput streamingOutput = output -> fileStore.writeContent(fileName, output);
            return RestResponse.ResponseBuilder.ok(streamingOutput, "application/pdf")
                    .header(HttpHeaders.CONTENT_LENGTH, fileStore.getFileSize(fileName))
                    .build();
        } catch (Exception e) {
            throw new NotFoundException("File not found");
        }
    }

    /**
     * Endpoint to download a file as a byte array using blocking I/O.
     *
     * @param fileName the name of the file to download
     * @return a {@link RestResponse} containing the file's byte array
     * @apiNote The call is executed on worker thread pool to avoid blocking the event loop (limit concurrency).
     */
    @Path("/byteArray/{name}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public RestResponse<byte[]> downloadByteArray(@RestPath("name") String fileName) {
        logger.info("byteArray [{}]", fileName);
        return RestResponse.ResponseBuilder.ok(fileStore.getByteArray(fileName))
                .header(HttpHeaders.CONTENT_LENGTH, fileStore.getFileSize(fileName)).build();
    }

    /**
     * Endpoint to download a file as a byte array using virtual threads.
     *
     * @param fileName the name of the file to download
     * @return a {@link RestResponse} containing the file's byte array
     * @apiNote This method runs on a virtual thread to handle blocking I/O efficiently, but risk of pinning, monopolization and under-efficient object pooling
     * @see RunOnVirtualThread
     */
    @Path("/byteArrayVirtual/{name}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RunOnVirtualThread
    public RestResponse<byte[]> downloadByteArrayVirtual(@RestPath("name") String fileName) {
        logger.info("byteArrayVirtual [{}]", fileName);
        return RestResponse.ResponseBuilder.ok(fileStore.getByteArray(fileName))
                .header(HttpHeaders.CONTENT_LENGTH, fileStore.getFileSize(fileName)).build();
    }
}
