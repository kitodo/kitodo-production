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

package org.kitodo.forms;

import java.io.FilenameFilter;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.goobi.production.GoobiVersion;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.kitodo.api.filemanagement.filters.FileNameEndsWithFilter;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.Parameters;
import org.kitodo.data.database.helper.enums.MetadataFormat;
import org.kitodo.services.ServiceManager;

/**
 * Helper form.
 *
 * @author Wulf Riebensahm
 */
@Named("HelperForm")
@SessionScoped
public class HelperForm implements Serializable {
    private static final long serialVersionUID = -5872893771807845586L;
    private transient ServiceManager serviceManager = new ServiceManager();

    public String getVersion() {
        return GoobiVersion.getBuildversion();
    }

    public boolean getAnonymized() {
        return ConfigCore.getBooleanParameter(Parameters.ANONYMIZE);
    }

    /**
     * Get file formats.
     *
     * @return list of file formats as Strings
     */
    public List<String> getFileFormats() {
        ArrayList<String> ffs = new ArrayList<>();
        for (MetadataFormat ffh : MetadataFormat.values()) {
            if (!ffh.equals(MetadataFormat.RDF)) {
                ffs.add(ffh.getName());
            }
        }
        return ffs;
    }

    /**
     * Get only internal file formats.
     *
     * @return list of internal file formats as Strings
     */
    public List<String> getFileFormatsInternalOnly() {
        ArrayList<String> ffs = new ArrayList<>();
        for (MetadataFormat ffh : MetadataFormat.values()) {
            if (ffh.isUsableForInternal() && !ffh.equals(MetadataFormat.RDF)) {
                ffs.add(ffh.getName());
            }
        }
        return ffs;
    }

    /**
     * Check if mass import is allowed.
     *
     * @return true or false
     */
    public boolean getMassImportAllowed() {
        return ConfigCore.getBooleanParameter(Parameters.MASS_IMPORT_ALLOWED)
                && !PluginLoader.getPluginList(PluginType.IMPORT).isEmpty();
    }

    /**
     * Returning value of configuration parameter withUserStepDoneSearch. Used
     * for enabling/disabling search for done steps by user.
     *
     * @return boolean
     */
    public boolean getUserStepDoneSearchEnabled() {
        return ConfigCore.getBooleanParameter(Parameters.WITH_USER_STEP_DONE_SEARCH);
    }
}
