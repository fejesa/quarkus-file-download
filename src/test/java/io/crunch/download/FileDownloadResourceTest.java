package io.crunch.download;

import io.quarkus.test.junit.QuarkusTest;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@QuarkusTest
class FileDownloadResourceTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "/download/asyncFile/sample.pdf",
            "/download/asyncBuffer/sample.pdf",
            "/download/asyncMultiBuffer/sample.pdf",
            "/download/stream/sample.pdf",
            "/download/byteArray/sample.pdf",
            "/download/byteArrayVirtual/sample.pdf"})
    void whenDownloadFileDownloadSuccessfully(String url) throws Exception {
        var response =
            given()
                .when()
                .header("Accept", "application/octet-stream")
                .get(url)
                .then()
                .statusCode(RestResponse.Status.OK.getStatusCode())
                .extract()
                .response();

        byte[] content = response.getBody().asByteArray();
        assertThat(content).isEqualTo(Files.readAllBytes(getSampleFile()));
    }

    private Path getSampleFile() throws URISyntaxException {
        var url = FileDownloadResourceTest.class.getResource("/sample/sample.pdf");
        return Paths.get(Objects.requireNonNull(url).toURI());
    }
}
