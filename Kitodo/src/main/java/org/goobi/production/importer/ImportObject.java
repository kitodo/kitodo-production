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

package org.goobi.production.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.goobi.production.enums.ImportReturnValue;

import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.ProcessProperty;
import org.kitodo.data.database.beans.TemplateProperty;
import org.kitodo.data.database.beans.WorkpieceProperty;

public class ImportObject {

	// TODO must end with ".xml" in current implementation
	private String processTitle = "";
	private final Collection<Batch> batches = new LinkedList<Batch>();
	
	private String metsFilename = "";
	private String importFileName = "";

	// error handling
	private ImportReturnValue importReturnValue = ImportReturnValue.ExportFinished;
	private String errorMessage ="";
	
	// additional information
	private List<ProcessProperty> processProperties = new ArrayList<>();
	private List<WorkpieceProperty> workProperties = new ArrayList<>();
	private List<TemplateProperty> templateProperties = new ArrayList<>();

	public ImportObject() {
	}

	public String getProcessTitle() {
		return this.processTitle;
	}

	public void setProcessTitle(String processTitle) {
		this.processTitle = processTitle;
	}

	public String getMetsFilename() {
		return this.metsFilename;
	}

	public void setMetsFilename(String metsFilename) {
		this.metsFilename = metsFilename;
	}

	public ImportReturnValue getImportReturnValue() {
		return this.importReturnValue;
	}

	public void setImportReturnValue(ImportReturnValue importReturnValue) {
		this.importReturnValue = importReturnValue;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public List<ProcessProperty> getProcessProperties() {
		return this.processProperties;
	}

	public void setProcessProperties(List<ProcessProperty> processProperties) {
		this.processProperties = processProperties;
	}

	public List<WorkpieceProperty> getWorkProperties() {
		return this.workProperties;
	}

	public void setWorkProperties(List<WorkpieceProperty> workProperties) {
		this.workProperties = workProperties;
	}

	public List<TemplateProperty> getTemplateProperties() {
		return this.templateProperties;
	}

	public void setTemplateProperties(List<TemplateProperty> templateProperties) {
		this.templateProperties = templateProperties;
	}

	public Collection<Batch> getBatches() {
		return batches;
	}

	public String getImportFileName() {
        return importFileName;
    }

	public void setImportFileName(String importFileName) {
        this.importFileName = importFileName;
    }
}
