package org.goobi.production.Import;

import java.util.ArrayList;
import java.util.List;

import org.goobi.production.enums.ImportReturnValue;

import de.sub.goobi.Beans.Prozesseigenschaft;
import de.sub.goobi.Beans.Vorlageeigenschaft;
import de.sub.goobi.Beans.Werkstueckeigenschaft;

public class ImportObject {

	// TODO must end with ".xml" in current implementation
	private String processTitle ="";
	
	// TODO needed?
	private String metsFilename ="";

	// error handling
	private ImportReturnValue importReturnValue = ImportReturnValue.ExportFinished;
	private String errorMessage ="";
	
	// additional information
	private List<Prozesseigenschaft> processProperties = new ArrayList<Prozesseigenschaft>();
	private List<Werkstueckeigenschaft> workProperties = new ArrayList<Werkstueckeigenschaft>();
	private List<Vorlageeigenschaft> templateProperties = new ArrayList<Vorlageeigenschaft>();
	
	
	
	public ImportObject(Record r) {
		this.processProperties = r.getProcessProperties();
		this.workProperties = r.getWorkProperties();
		this.templateProperties = r.getTemplateProperties();
	
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

	
	
}
