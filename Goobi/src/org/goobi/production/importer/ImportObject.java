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

import de.sub.goobi.beans.Batch;
import de.sub.goobi.beans.Prozesseigenschaft;
import de.sub.goobi.beans.Vorlageeigenschaft;
import de.sub.goobi.beans.Werkstueckeigenschaft;

public class ImportObject {

	// TODO must end with ".xml" in current implementation
	private String processTitle ="";
	private final Collection<Batch> batches = new LinkedList<Batch>();
	
	private String metsFilename ="";

    private String importFileName = "";

	// error handling
	private ImportReturnValue importReturnValue = ImportReturnValue.ExportFinished;
	private String errorMessage ="";
	
	// additional information
	private List<Prozesseigenschaft> processProperties = new ArrayList<Prozesseigenschaft>();
	private List<Werkstueckeigenschaft> workProperties = new ArrayList<Werkstueckeigenschaft>();
	private List<Vorlageeigenschaft> templateProperties = new ArrayList<Vorlageeigenschaft>();
	
	
	
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
	public List<Prozesseigenschaft> getProcessProperties() {
		return this.processProperties;
	}
	public void setProcessProperties(List<Prozesseigenschaft> processProperties) {
		this.processProperties = processProperties;
	}
	public List<Werkstueckeigenschaft> getWorkProperties() {
		return this.workProperties;
	}
	public void setWorkProperties(List<Werkstueckeigenschaft> workProperties) {
		this.workProperties = workProperties;
	}
	public List<Vorlageeigenschaft> getTemplateProperties() {
		return this.templateProperties;
	}
	public void setTemplateProperties(List<Vorlageeigenschaft> templateProperties) {
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
