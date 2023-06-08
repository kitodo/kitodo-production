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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.MappingFile;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named
@ViewScoped
public class MappingFileEditView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(MappingFileEditView.class);
    private MappingFile mappingFile = new MappingFile();

    /**
     * Load mapping file by ID.
     *
     * @param id
     *            ID of mapping file to load
     */
    public void load(int id) {
        try {
            if (id > 0) {
                mappingFile = ServiceManager.getMappingFileService().getById(id);
            } else {
                mappingFile = new MappingFile();
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] { ObjectType.MAPPING_FILE.getTranslationSingular(), id }, logger, e);
        }
    }

    /**
     * Save mapping file.
     *
     * @return projects page or empty String
     */
    public String save() {
        try {
            ServiceManager.getMappingFileService().saveToDatabase(mappingFile);
            return projectsPage;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.MAPPING_FILE.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Get mappingFile.
     *
     * @return value of mappingFile
     */
    public MappingFile getMappingFile() {
        return mappingFile;
    }

    /**
     * Set mappingFile.
     *
     * @param mappingFile as org.kitodo.data.database.beans.MappingFile
     */
    public void setMappingFile(MappingFile mappingFile) {
        this.mappingFile = mappingFile;
    }

    /**
     * Get list of mapping file filenames.
     *
     * @return list of mapping file filenames
     */
    public List<Path> getFilenames() {
        try (Stream<Path> mappingFiles = Files.walk(Paths.get(ConfigCore.getParameter(ParameterCore.DIR_XSLT)))) {
            return mappingFiles.filter(Files::isRegularFile)
                    .filter(f -> f.toString().endsWith(".xsl") || f.toString().endsWith("xslt"))
                    .map(Path::getFileName).sorted().collect(Collectors.toList());
        } catch (IOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.MAPPING_FILE.getTranslationPlural() },
                logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get metadata formats.
     *
     * @return metadata formats
     */
    public List<MetadataFormat> getMetadataFormats() {
        return List.of(MetadataFormat.values());
    }
}
