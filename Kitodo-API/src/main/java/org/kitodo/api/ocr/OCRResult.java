/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the GPL3-License.txt file that was
 * distributed with this source code.
 */

package org.kitodo.api.ocr;

import java.io.File;
import java.util.ArrayList;

public class OCRResult {

    /** The process id. */
    private Integer processId;
    /** The result, if OCR was successfull */
    private boolean success;
    /** A result message */
    private String message;
    /** The generated OCD Files */
    private ArrayList<File> outputFiles;

    /** Gets the processId. */
    public Integer getProcessId() {
        return processId;
    }

    /** Sets the processId. */
    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    /** Gets the success. */
    public boolean isSuccess() {
        return success;
    }

    /** Sets the success. */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /** Gets the messages. */
    public String getMessage() {
        return message;
    }

    /** Sets the messages. */
    public void setMessage(String message) {
        this.message = message;
    }

    /** Gets the outputFiles. */
    public ArrayList<File> getOutputFiles() {
        return outputFiles;
    }

    /** Sets the outputFiles. */
    public void setOutputFiles(ArrayList<File> outputFiles) {
        this.outputFiles = outputFiles;
    }
}
