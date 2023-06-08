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

package org.kitodo.production.forms;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.MappingFile;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyDTOModel;
import org.kitodo.production.services.ServiceManager;

@Named("MappingFileListView")
@ViewScoped
public class MappingFileListView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(MappingFileListView.class);
    private final String mappingFileEditPath = MessageFormat.format(REDIRECT_PATH, "mappingFileEdit");

    /**
     * Empty default constructor that also sets the LazyDTOModel instance of this bean.
     */
    public MappingFileListView() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(ServiceManager.getMappingFileService()));
    }

    /**
     * Get mapping files.
     *
     * @return mapping files
     */
    public List<MappingFile> getMappingFiles() {
        try {
            return ServiceManager.getMappingFileService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY,
                    new Object[] {ObjectType.MAPPING_FILE.getTranslationPlural() }, logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Return path to 'mappingFileEdit' view.
     *
     * @return path to 'mappingFileEdit' view
     */
    public String newMappingFile() {
        return mappingFileEditPath;
    }

    /**
     * Delete mapping file identified by ID.
     *
     * @param id ID of mapping file to delete
     */
    public void deleteById(int id) {
        try {
            ServiceManager.getMappingFileService().removeFromDatabase(id);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.MAPPING_FILE.getTranslationSingular() }, logger, e);
        }
    }

}
