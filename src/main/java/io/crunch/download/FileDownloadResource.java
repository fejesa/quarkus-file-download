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

@Path("/download")
public class FileDownloadResource {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final FileStore fileStore;

    public FileDownloadResource(FileStore fileStore) {
        this.fileStore = fileStore;
    }

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

    @Path("/byteArray/{name}")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public RestResponse<byte[]> downloadAsByteArray(@RestPath("name") String fileName) {
        logger.info("byteArray [{}]", fileName);
        return RestResponse.ResponseBuilder.ok(fileStore.getAsByteArray(fileName))
                .header("Content-Length", fileStore.getFileSize(fileName)).build();
    }

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
