/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.URI;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.TreeDeleter;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.services.ServiceManager;

public class ExportDmsIT {

    // parameters
    static int processId = 1;
    static File testBaseDirectory = new File("src/test/resources").getAbsoluteFile();
    static File downloadDir = new File(testBaseDirectory, "downloadHere");

    /**
     * Initializes the test.
     */
    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();

        // create directory structure
        File maxDirectory = new File(testBaseDirectory, "metadata/1/jpgs/max");
        makeDirectoryWithSomeFiles(maxDirectory, 184, "%08d.jpg", 358000);

        File defaultDirectory = new File(testBaseDirectory, "metadata/1/jpgs/default");
        makeDirectoryWithSomeFiles(defaultDirectory, 184, "%08d.jpg", 181554);

        File thumbsDirectory = new File(testBaseDirectory, "metadata/1/jpgs/thumbs");
        makeDirectoryWithSomeFiles(thumbsDirectory, 184, "%08d.jpg", 1890);

        File altoDirectory = new File(testBaseDirectory, "metadata/1/ocr/alto");
        makeDirectoryWithSomeFiles(altoDirectory, 184, "%08d.xml", 25157);

        File pdfDirectory = new File(testBaseDirectory, "metadata/1/pdf");
        makeDirectoryWithSomeFiles(pdfDirectory, 184, "%08d.pdf", 3548885);
    }

    static void makeDirectoryWithSomeFiles(File parent, int count, String pattern, int length)
            throws FileNotFoundException, IOException {
        parent.mkdirs();
        for (int index = 1; index <= count; index++) {
            File file = new File(parent, String.format(pattern, index));
            try (RandomAccessFile randomAccess = new RandomAccessFile(file, "rw")) {
                randomAccess.setLength(length);
            }
        }
    }

    /**
     * Cleans up after the test.
     */
    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();

        TreeDeleter.deltree(new File(testBaseDirectory, "metadata/" + processId));
        TreeDeleter.deltree(downloadDir);
    }

    @Test
    public void testDirectoryDownload() throws Exception {
        Process process = ServiceManager.getProcessService().getById(processId);
        URI destination = downloadDir.toURI();
        String destinationString = destination.toString();
        if (!destinationString.endsWith("/")) {
            destination = URI.create(destinationString.concat("/"));
        }

        Method directoryDownload = ExportDms.class.getDeclaredMethod("directoryDownload", Process.class, URI.class);
        directoryDownload.setAccessible(true);
        long start = System.nanoTime();
        directoryDownload.invoke(new ExportDms(), process, destination);
        final long tookNanos = System.nanoTime() - start;

        assertTrue(downloadDir.isDirectory(), "Destination location should exist");

        File jpgsMax = new File(downloadDir, "jpgs/max");
        assertTrue(jpgsMax.isDirectory(), "Folder jpgs/max should exist in destination location");
        assertEquals(184, jpgsMax.list((directory, filename) -> filename.endsWith(".jpg")).length, "jpgs/max should contain 184 JPEGs");

        File jpgsDefault = new File(downloadDir, "jpgs/default");
        assertTrue(jpgsDefault.isDirectory(), "Folder jpgs/default should exist in destination location");
        assertEquals(184, jpgsDefault.list((directory, filename) -> filename.endsWith(".jpg")).length, "jpgs/default should contain 184 JPEGs");

        File jpgsThumbs = new File(downloadDir, "jpgs/thumbs");
        assertTrue(jpgsThumbs.isDirectory(), "Folder jpgs/thumbs should exist in destination location");
        assertEquals(184, jpgsThumbs.list((directory, filename) -> filename.endsWith(".jpg")).length, "jpgs/thumbs should contain 184 JPEGs");

        File ocrAlto = new File(downloadDir, "ocr/alto");
        assertTrue(ocrAlto.isDirectory(), "Folder ocr/alto should exist in destination location");
        assertEquals(184, ocrAlto.list((directory, filename) -> filename.endsWith(".xml")).length, "ocr/alto should contain 184 XMLs");

        File pdf = new File(downloadDir, "pdf");
        assertTrue(pdf.isDirectory(), "Folder pdf should exist in destination location");
        assertEquals(184, pdf.list((directory, filename) -> filename.endsWith(".pdf")).length, "pdf should contain 184 PDFs");

        float totalBytes = 184f * (358000 + 181554 + 1890 + 25157 + 3548885);
        float mbps = totalBytes / tookNanos * 1953125 / 2048;
        assertTrue(mbps > 50, "It should have been copied >50 MB/s (was: " + mbps + " MB/s)");
    }
}
