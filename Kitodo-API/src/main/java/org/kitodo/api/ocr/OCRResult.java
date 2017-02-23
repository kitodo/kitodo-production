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

	public Integer getProcessId() {
		return processId;
	}

	public void setProcessId(Integer processId) {
		this.processId = processId;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ArrayList<File> getOutputFiles() {
		return outputFiles;
	}

	public void setOutputFiles(ArrayList<File> outputFiles) {
		this.outputFiles = outputFiles;
	}
}
