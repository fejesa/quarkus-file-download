package io.crunch.download;

import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.file.AsyncFile;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
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
    @Path("/asynchFile/{name}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Uni<RestResponse<AsyncFile>> downloadAsAsyncFile(@RestPath("name") String fileName) {
        logger.info("asynchFile [{}]", fileName);
        return fileStore.getAsAsyncFile(fileName)
            .onItem()
            .transform(asyncFile ->
                RestResponse.ResponseBuilder.ok(asyncFile, "application/pdf").build());
    }

    /**
     * Endpoint to download a file as a byte array asynchronously.
     * <p>
     * This method runs on a worker thread pool to avoid blocking the event loop.
     *
     * @param fileName the name of the file to download
     * @return a {@link Uni} emitting a {@link RestResponse} containing the file's byte array
     * @apiNote The call is executed on the event loop thread.
     */
    @Path("/asyncByteArray/{name}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Uni<RestResponse<byte[]>> downloadAsAsyncByteArray(@RestPath("name") String fileName) {
        logger.info("asyncByteArray [{}]", fileName);
        return fileStore.getAsBuffer(fileName)
            .map(Buffer::getBytes)
            .map(b -> RestResponse.ResponseBuilder.ok(b)
                    //.header("Content-Length", fileStore.getFileSize(fileName))
                    .build())
            .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
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
    public RestResponse<StreamingOutput> downloadAsStream(@RestPath("name") String fileName) {
        logger.info("stream [{}]", fileName);
        try {
            StreamingOutput streamingOutput = output -> fileStore.writeContent(fileName, output);
            return RestResponse.ResponseBuilder.ok(streamingOutput, "application/pdf")
                    //.header("Content-Length", fileStore.getFileSize(fileName))
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
    public RestResponse<byte[]> downloadAsByteArray(@RestPath("name") String fileName) {
        logger.info("byteArray [{}]", fileName);
        return RestResponse.ResponseBuilder.ok(fileStore.getAsByteArray(fileName))
                .header("Content-Length", fileStore.getFileSize(fileName)).build();
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
    public RestResponse<byte[]> downloadAsByteArrayVirtual(@RestPath("name") String fileName) {
        logger.info("byteArrayVirtual [{}]", fileName);
        return RestResponse.ResponseBuilder.ok(fileStore.getAsByteArray(fileName))
                .header("Content-Length", fileStore.getFileSize(fileName)).build();
    }
}
