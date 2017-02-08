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

package org.kitodo.services;

import de.sub.goobi.helper.FilesystemHelper;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import org.kitodo.data.database.beans.Process;

import static org.junit.Assert.*;
import static org.kitodo.data.database.beans.Batch.Type.LOGISTIC;

/**
 * Tests for ProcessService class.
 */
public class ProcessServiceTest {

    @Test
    public void shouldFindProcess() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        boolean condition = process.getTitle().equals("First process") && process.getOutputName().equals("Test");
        assertTrue("Process was not found in database!", condition);
    }

    @Test
    public void shouldFindAllProcesses() throws Exception {
        ProcessService processService = new ProcessService();

        List<Process> processes = processService.findAll();
        assertEquals("Not all processes were found in database!", 2, processes.size());
    }

    @Test
    public void shouldGetBatchesByType() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        boolean condition = processService.getBatchesByType(process, LOGISTIC).size() == 1;
        assertTrue("Table size is incorrect!", condition);
    }

    @Ignore("problem with lazy fetching")
    @Test
    public void shouldGetBlockedUsers() throws Exception {
        ProcessService processService = new ProcessService();
        UserService userService = new UserService();

        Process process = processService.find(1);
        System.out.println(process.getTitle() + " " + process.getDocket().getName());
        System.out.println(userService.find(3).getFullName() + " " + userService.find(3).getTasks().size());
        boolean condition = processService.getBlockedUsers(process) == userService.find(3);
        assertTrue("Blocked user doesn't match to given user!", condition);
    }

    @Test
    public void shouldGetImagesTifDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getImagesTifDirectory(true, process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\images\\First process_media\\");
        assertTrue("Images TIF directory doesn't match to given directory!", condition);

        directory = processService.getImagesTifDirectory(false, process);
        condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\images\\First process_media\\");
        assertTrue("Images TIF directory doesn't match to given directory!", condition);
        //I don't know what changes this useFallback so I'm testing for both cases
    }

    @Ignore("not sure how method works")
    @Test
    public void shouldCheckIfTifDirectoryExists() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        //it is weird but it says that it doesn't exist....
        FilesystemHelper.createDirectory("C:\\dev\\kitodo\\metadata\\1\\images\\First process_media\\");
        boolean condition = processService.checkIfTifDirectoryExists(process);
        assertTrue("Images TIF directory doesn't exist!", condition);

        process = processService.find(2);
        condition = processService.checkIfTifDirectoryExists(process);
        assertTrue("Images TIF directory exists, but it shouldn't!", !condition);
    }

    @Test
    public void shouldGetImagesOrigDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getImagesOrigDirectory(false, process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\images\\master_First process_media\\");
        assertTrue("Images orig directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetImagesDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getImagesDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\images\\");
        assertTrue("Images directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetSourceDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getSourceDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\images\\First process_source");
        assertTrue("Source directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetProcessDataDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getProcessDataDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\");
        assertTrue("Process data directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetOcrDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getOcrDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\ocr\\");
        assertTrue("OCR directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetTxtDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getTxtDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\ocr\\First process_txt\\");
        assertTrue("TXT directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetWordDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getWordDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\ocr\\First process_wc\\");
        assertTrue("Word directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetPdfDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getPdfDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\ocr\\First process_pdf\\");
        assertTrue("PDF directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetAltoDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getAltoDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\ocr\\First process_alto\\");
        assertTrue("Alto directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetImportDirectory() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getImportDirectory(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\import\\");
        assertTrue("Import directory doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetProcessDataDirectoryIgnoreSwapping() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String directory = processService.getProcessDataDirectoryIgnoreSwapping(process);
        boolean condition = directory.equals("C:\\dev\\kitodo\\metadata\\1\\");
        assertTrue("Process data directory ignore swapping doesn't match to given directory!", condition);
    }

    @Test
    public void shouldGetBatchId() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        String batchId = processService.getBatchID(process);
        boolean condition = batchId.equals("First batch, Third batch");
        assertTrue("BatchId doesn't match to given plain text!", condition);
    }

    @Ignore("progress contains only 000000")
    @Test
    public void shouldGetProgress() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        System.out.println("Progress: " + processService.getProgress(process));
        boolean condition = processService.getProgress(process).equals("");
        assertTrue("Progress doesn't match given plain text!", condition);
    }

    @Ignore("no idea how check if it is correct - Fileformat class")
    @Test
    public void shouldReadMetadataFile() throws Exception {
        ProcessService processService = new ProcessService();

        Process process = processService.find(1);
        System.out.println(processService.readMetadataFile(process));
        boolean condition = processService.readMetadataFile(process).equals("");
        assertTrue("Images tif directory doesn't match to given directory!", condition);
    }
}
