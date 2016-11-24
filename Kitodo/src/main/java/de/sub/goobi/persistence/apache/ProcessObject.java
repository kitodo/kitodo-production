package de.sub.goobi.persistence.apache;

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
import de.sub.goobi.helper.enums.MetadataFormat;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.metadaten.MetadatenHelper;

import java.io.IOException;
import java.util.Date;

import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import ugh.fileformats.excel.RDFFile;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;
import ugh.fileformats.mets.XStream;

public class ProcessObject {

	private int id;
	private String title;
	private String ausgabename;
	private boolean isTemplate;
	private boolean swappedOut;
	private boolean inAuswahllisteAnzeigen;
	private String sortHelperStatus;
	private int sortHelperImages;
	private int sortHelperArticles;
	private Date erstellungsdatum;
	private int projekteID;
	private int rulesetId;
	private int sortHelperDocstructs;
	private int sortHelperMetadata;
	private String wikifield;

	/**
	 * @param processId add description
	 * @param title add description
	 * @param ausgabename add description
	 * @param isTemplate add description
	 * @param swappedOut add description
	 * @param inAuswahllisteAnzeigen add description
	 * @param sortHelperStatus add description
	 * @param sortHelperImages add description
	 * @param sortHelperArticles add description
	 * @param erstellungsdatum add description
	 * @param projekteID add description
	 * @param metadatenKonfigurationID add description
	 * @param sortHelperDocstructs add description
	 * @param sortHelperMetadata add description
	 * @param wikifield add description
	 */
	public ProcessObject(int processId, String title, String ausgabename, boolean isTemplate, boolean swappedOut,
			boolean inAuswahllisteAnzeigen, String sortHelperStatus, int sortHelperImages, int sortHelperArticles,
			Date erstellungsdatum, int projekteID, int metadatenKonfigurationID, int sortHelperDocstructs,
			int sortHelperMetadata, String wikifield) {
		super();
		this.id = processId;
		this.title = title;
		this.ausgabename = ausgabename;
		this.isTemplate = isTemplate;
		this.swappedOut = swappedOut;
		this.inAuswahllisteAnzeigen = inAuswahllisteAnzeigen;
		this.sortHelperStatus = sortHelperStatus;
		this.sortHelperImages = sortHelperImages;
		this.sortHelperArticles = sortHelperArticles;
		this.erstellungsdatum = erstellungsdatum;
		this.projekteID = projekteID;
		this.rulesetId = metadatenKonfigurationID;
		this.sortHelperDocstructs = sortHelperDocstructs;
		this.sortHelperMetadata = sortHelperMetadata;
		this.wikifield = wikifield;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int processId) {
		this.id = processId;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAusgabename() {
		return this.ausgabename;
	}

	public void setAusgabename(String ausgabename) {
		this.ausgabename = ausgabename;
	}

	public boolean isTemplate() {
		return this.isTemplate;
	}

	public void setTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
	}

	public boolean isSwappedOut() {
		return this.swappedOut;
	}

	public void setSwappedOut(boolean swappedOut) {
		this.swappedOut = swappedOut;
	}

	public boolean isInAuswahllisteAnzeigen() {
		return this.inAuswahllisteAnzeigen;
	}

	public void setInAuswahllisteAnzeigen(boolean inAuswahllisteAnzeigen) {
		this.inAuswahllisteAnzeigen = inAuswahllisteAnzeigen;
	}

	public String getSortHelperStatus() {
		return this.sortHelperStatus;
	}

	public void setSortHelperStatus(String sortHelperStatus) {
		this.sortHelperStatus = sortHelperStatus;
	}

	public int getSortHelperImages() {
		return this.sortHelperImages;
	}

	public void setSortHelperImages(int sortHelperImages) {
		this.sortHelperImages = sortHelperImages;
	}

	public int getSortHelperArticles() {
		return this.sortHelperArticles;
	}

	public void setSortHelperArticles(int sortHelperArticles) {
		this.sortHelperArticles = sortHelperArticles;
	}

	public Date getErstellungsdatum() {
		return this.erstellungsdatum;
	}

	public void setErstellungsdatum(Date erstellungsdatum) {
		this.erstellungsdatum = erstellungsdatum;
	}

	public int getProjekteID() {
		return this.projekteID;
	}

	public void setProjekteID(int projekteID) {
		this.projekteID = projekteID;
	}

	public int getRulesetId() {
		return this.rulesetId;
	}

	public void setRulesetId(int metadatenKonfigurationID) {
		this.rulesetId = metadatenKonfigurationID;
	}

	public int getSortHelperDocstructs() {
		return this.sortHelperDocstructs;
	}

	public void setSortHelperDocstructs(int sortHelperDocstructs) {
		this.sortHelperDocstructs = sortHelperDocstructs;
	}

	public int getSortHelperMetadata() {
		return this.sortHelperMetadata;
	}

	public void setSortHelperMetadata(int sortHelperMetadata) {
		this.sortHelperMetadata = sortHelperMetadata;
	}

	public String getWikifield() {
		return this.wikifield;
	}

	public void setWikifield(String wikifield) {
		this.wikifield = wikifield;
	}

	/**
	 * @param metadataFile add description
	 * @param prefs add description
	 * @return add description
	 * @throws IOException add description
	 * @throws PreferencesException add description
	 * @throws ReadException add description
	 */
	public Fileformat readMetadataFile(String metadataFile, Prefs prefs) throws IOException, PreferencesException,
			ReadException {
		/* prüfen, welches Format die Metadaten haben (Mets, xstream oder rdf */
		String type = MetadatenHelper.getMetaFileType(metadataFile);
		Fileformat ff = null;
		if (type.equals("metsmods")) {
			ff = new MetsModsImportExport(prefs);
		} else if (type.equals("mets")) {
			ff = new MetsMods(prefs);
		} else if (type.equals("xstream")) {
			ff = new XStream(prefs);
		} else {
			ff = new RDFFile(prefs);
		}
		ff.read(metadataFile);

		return ff;
	}

	/**
	 * @param gdzfile add description
	 * @param metadataFile add description
	 * @param prefs add description
	 * @param fileformat add description
	 * @throws IOException add description
	 * @throws InterruptedException add description
	 * @throws SwapException add description
	 * @throws DAOException add description
	 * @throws WriteException add description
	 * @throws PreferencesException add description
	 */
	public void writeMetadataFile(Fileformat gdzfile, String metadataFile, Prefs prefs, String fileformat)
			throws IOException, InterruptedException, SwapException, DAOException, WriteException,
			PreferencesException {
		Fileformat ff;

		switch (MetadataFormat.findFileFormatsHelperByName(fileformat)) {
			case METS:
				ff = new MetsMods(prefs);
				break;

			case RDF:
				ff = new RDFFile(prefs);
				break;

			default:
				ff = new XStream(prefs);
				break;
		}

		ff.setDigitalDocument(gdzfile.getDigitalDocument());
		ff.write(metadataFile);
	}

}
