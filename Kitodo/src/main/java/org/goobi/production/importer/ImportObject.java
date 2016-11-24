package org.goobi.production.importer;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
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
	private String processTitle = "";
	private final Collection<Batch> batches = new LinkedList<Batch>();

	private String metsFilename = "";

	private String importFileName = "";

	// error handling
	private ImportReturnValue importReturnValue = ImportReturnValue.ExportFinished;
	private String errorMessage = "";

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
