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

import java.util.ArrayList;

public class OCRInput {

	/** The process id. */
	private Integer processId;
	/** The font type. */
	private String fontType;
	/** The used languages. */
	private ArrayList<String> languages;
	/** Properties for given and returned files. */
	private ArrayList<FileProperty> fileProperties;
	/** The type of the result xml file. */
	private String resultType;
	/** Whole result should be stored in one file, or if every input gets an output file. */
	private Boolean singleResultFile;

	public Integer getProcessId() {
		return processId;
	}

	public void setProcessId(Integer processId) {
		this.processId = processId;
	}

	public String getFontType() {
		return fontType;
	}

	public void setFontType(String fontType) {
		this.fontType = fontType;
	}

	public ArrayList<String> getLanguages() {
		return languages;
	}

	public void setLanguages(ArrayList<String> languages) {
		this.languages = languages;
	}

	public ArrayList<FileProperty> getFileProperties() {
		return fileProperties;
	}

	public void setFileProperties(ArrayList<FileProperty> fileProperties) {
		this.fileProperties = fileProperties;
	}

	public String getResultType() {
		return resultType;
	}

	public void setResultType(String resultType) {
		this.resultType = resultType;
	}

	public Boolean getSingleResultFile() {
		return singleResultFile;
	}

	public void setSingleResultFile(Boolean singleResultFile) {
		this.singleResultFile = singleResultFile;
	}
}
