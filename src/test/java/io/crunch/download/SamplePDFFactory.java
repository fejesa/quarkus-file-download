package io.crunch.download;

import org.apache.pdfbox.tools.TextToPDF;

import java.io.*;
import java.nio.file.Path;
import java.util.Random;
import java.util.stream.IntStream;

public class SamplePDFFactory {

    private static final int ONE_MB = 1_465_000;

    /**
     * Creates PDF file with a dummy content and saves it to the given path.
     * @param size Number of characters should be written to the file.
     * @param file The output path where the file should be written to.
     * @throws IOException If error occurs during the file generation.
     */
    public static void create(int size, Path file) throws IOException {
        var r = new Random();
        var stream = IntStream.iterate(0, i -> i + 1)
            .limit(size)
            .map(i -> i % 100 == 0 ? '\n' : r.nextInt(26) + 'a');

        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(toByteArray(stream)));
             var document = new TextToPDF().createPDFFromText(reader)) {
            document.save(file.toFile());
        }
    }

    private static byte[] toByteArray(IntStream stream) {
        return stream
                .collect(
                    ByteArrayOutputStream::new,
                    (bas, i) -> bas.write((byte) i),
                    (bas1, bas2) -> bas1.write(bas2.toByteArray(), 0, bas2.size()))
                .toByteArray();
    }

    public static void main(String[] args) throws IOException {
//        forPermTest("sample_001mb.pdf", 1);
//        forPermTest("sample_002mb.pdf", 2);
//        forPermTest("sample_005mb.pdf", 5);
//        forPermTest("sample_010mb.pdf", 10);
//        forPermTest("sample_020mb.pdf", 20);
        forPermTest("sample.pdf", 100);
    }

    private static void forPermTest(String fileNamePattern, int factor) throws IOException {
        create(ONE_MB * factor, Path.of(fileNamePattern));
    }
}
