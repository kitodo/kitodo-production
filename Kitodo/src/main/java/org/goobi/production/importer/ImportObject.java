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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.goobi.production.enums.ImportReturnValue;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Property;

public class ImportObject {

    // TODO must end with ".xml" in current implementation
    private String processTitle = "";
    private final Collection<Batch> batches = new LinkedList<>();

    private URI metsFilename;
    private URI importFileName;

    // error handling
    private ImportReturnValue importReturnValue = ImportReturnValue.ExportFinished;
    private String errorMessage = "";

    // additional information
    private List<Property> processProperties = new ArrayList<>();
    private List<Property> workProperties = new ArrayList<>();
    private List<Property> templateProperties = new ArrayList<>();

    public String getProcessTitle() {
        return this.processTitle;
    }

    public void setProcessTitle(String processTitle) {
        this.processTitle = processTitle;
    }

    public URI getMetsFilename() {
        return this.metsFilename;
    }

    public void setMetsFilename(URI metsFilename) {
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

    public List<Property> getProcessProperties() {
        return this.processProperties;
    }

    public void setProcessProperties(List<Property> processProperties) {
        this.processProperties = processProperties;
    }

    public List<Property> getWorkProperties() {
        return this.workProperties;
    }

    public void setWorkProperties(List<Property> workProperties) {
        this.workProperties = workProperties;
    }

    public List<Property> getTemplateProperties() {
        return this.templateProperties;
    }

    public void setTemplateProperties(List<Property> templateProperties) {
        this.templateProperties = templateProperties;
    }

    public Collection<Batch> getBatches() {
        return batches;
    }

    public URI getImportFileName() {
        return importFileName;
    }

    public void setImportFileName(URI importFileName) {
        this.importFileName = importFileName;
    }
}
