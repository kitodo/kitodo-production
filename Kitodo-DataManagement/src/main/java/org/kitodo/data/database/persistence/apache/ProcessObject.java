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

package org.kitodo.data.database.persistence.apache;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.exceptions.SwapException;
import org.kitodo.data.database.helper.MetadataHelper;
import org.kitodo.data.database.helper.enums.MetadataFormat;

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
    private String outputName;
    private boolean template;
    private boolean swappedOut;
    private boolean inChoiceListShown;
    private String sortHelperStatus;
    private int sortHelperImages;
    private int sortHelperArticles;
    private Date creationDate;
    private int projectId;
    private int rulesetId;
    private int sortHelperDocstructs;
    private int sortHelperMetadata;
    private String wikiField;

    public ProcessObject(int processId, String title, String outputName, boolean template, boolean swappedOut,
            boolean inChoiceListShown, String sortHelperStatus, int sortHelperImages, int sortHelperArticles,
            Date creationDate, int projectId, int rulesetId, int sortHelperDocstructs, int sortHelperMetadata,
            String wikiField) {
        super();
        this.id = processId;
        this.title = title;
        this.outputName = outputName;
        this.template = template;
        this.swappedOut = swappedOut;
        this.inChoiceListShown = inChoiceListShown;
        this.sortHelperStatus = sortHelperStatus;
        this.sortHelperImages = sortHelperImages;
        this.sortHelperArticles = sortHelperArticles;
        this.creationDate = creationDate;
        this.projectId = projectId;
        this.rulesetId = rulesetId;
        this.sortHelperDocstructs = sortHelperDocstructs;
        this.sortHelperMetadata = sortHelperMetadata;
        this.wikiField = wikiField;
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

    public String getOutputName() {
        return this.outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public boolean isTemplate() {
        return this.template;
    }

    public void setTemplate(boolean isTemplate) {
        this.template = isTemplate;
    }

    public boolean isSwappedOut() {
        return this.swappedOut;
    }

    public void setSwappedOut(boolean swappedOut) {
        this.swappedOut = swappedOut;
    }

    public boolean isiNChoiceListShown() {
        return this.inChoiceListShown;
    }

    public void setInChoiceListShown(boolean inChoiceListShown) {
        this.inChoiceListShown = inChoiceListShown;
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

    public Date getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public int getProjectId() {
        return this.projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getRulesetId() {
        return this.rulesetId;
    }

    public void setRulesetId(int rulesetId) {
        this.rulesetId = rulesetId;
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

    public String getWikiField() {
        return this.wikiField;
    }

    public void setWikiField(String wikiField) {
        this.wikiField = wikiField;
    }

    public Fileformat readMetadataFile(URI metadataFile, Prefs prefs)
            throws IOException, PreferencesException, ReadException {
        /* prüfen, welches Format die Metadaten haben (Mets, xstream oder rdf */
        String type = MetadataHelper.getMetaFileType(metadataFile);
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
        ff.read(metadataFile.toString());

        return ff;
    }

    public void writeMetadataFile(Fileformat gdzfile, URI metadataFile, Prefs prefs, String fileFormat)
            throws IOException, InterruptedException, SwapException, DAOException, WriteException,
            PreferencesException {
        Fileformat ff;

        switch (MetadataFormat.findFileFormatsHelperByName(fileFormat)) {
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
        ff.write(metadataFile.toString());
    }

}
