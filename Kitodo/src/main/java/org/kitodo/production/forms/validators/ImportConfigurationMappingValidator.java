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

package org.kitodo.production.forms.validators;

import java.util.List;
import java.util.Objects;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.data.database.beans.MappingFile;
import org.kitodo.production.helper.Helper;
import org.primefaces.model.DualListModel;

@FacesValidator("MappingValidator")
public class ImportConfigurationMappingValidator implements Validator<DualListModel<MappingFile>> {

    private static final String PROCESS_TEMPLATE = "PROCESS_TEMPLATE";
    private static final String CONFIG_TYPE = "editForm:importConfigurationTabView:configurationType";
    private static final String METADATA_TYPE = "editForm:importConfigurationTabView:metadataFormat";
    private static final String PRESTRUCTURED_IMPORT = "editForm:importConfigurationTabView:prestructuredImport";
    private static final String SAVE = "editForm:save";

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, DualListModel<MappingFile> mappingModel)
            throws ValidatorException {
        // only validate when saving
        if (!facesContext.getExternalContext().getRequestParameterMap().containsKey(SAVE)) {
            return;
        }
        UIInput configType = (UIInput)facesContext.getViewRoot().findComponent(CONFIG_TYPE);
        // mapping files are not required for process template configurations
        if (Objects.isNull(configType.getValue()) || PROCESS_TEMPLATE.equals(configType.getValue())) {
            return;
        }
        List<MappingFile> mappingFiles = mappingModel.getTarget();
        UIInput metadataFormatInput = (UIInput) facesContext.getViewRoot().findComponent(METADATA_TYPE);
        String metadataFormat = (String) metadataFormatInput.getValue();
        UIInput prestructuredImportInput = (UIInput) facesContext.getViewRoot().findComponent(PRESTRUCTURED_IMPORT);
        Boolean prestructuredImport = (Boolean) prestructuredImportInput.getValue();

        // if no mapping files are configured, the imported metadata must already be in KITODO internal format
        if (!Objects.equals(metadataFormat, MetadataFormat.KITODO.name())
                && mappingFiles.isEmpty()) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, Helper.getTranslation(
                    "importConfig.validation.error.missingMappingFiles", metadataFormat), null));
        } else {
            for (MappingFile mappingFile : mappingFiles) {
                int index = mappingFiles.indexOf(mappingFile);
                // first mapping file must map import configuration metadata format to some other format
                if (index == 0 && !Objects.equals(metadataFormat, mappingFile.getInputMetadataFormat())) {
                    throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            Helper.getTranslation("importConfig.validation.error.firstMappingFormat",
                                    mappingFile.getTitle(), metadataFormat, mappingFile.getInputMetadataFormat()), null));
                }
                if (index == mappingFiles.size() - 1) {
                    validateLastMappingFile(mappingFile, prestructuredImport);
                }
                // intermediate mapping files output metadata format must match next files input metadata format
                if (mappingFiles.size() > index + 1 && !Objects.equals(mappingFile.getOutputMetadataFormat(),
                        mappingFiles.get(index + 1).getInputMetadataFormat())) {
                    throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, Helper.getTranslation(
                            "importConfig.validation.error.intermediateMappingFormat",
                            mappingFile.getTitle(), mappingFiles.get(index + 1).getInputMetadataFormat(),
                            mappingFile.getOutputMetadataFormat()), null));
                }
            }
        }
    }

    private void validateLastMappingFile(MappingFile mappingFile, Boolean prestructuredImport) {
        // last mapping file must map to KITODO internal metadata format
        if (!Objects.equals(mappingFile.getOutputMetadataFormat(), MetadataFormat.KITODO.name())) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, Helper.getTranslation(
                    "importConfig.validation.error.lastMappingFormat", mappingFile.getTitle(),
                    mappingFile.getOutputMetadataFormat()), null));
        }
        // 'prestructured import' flag of mapping file must be equal to corresponding flag in import configuration
        if (!Objects.equals(mappingFile.getPrestructuredImport(), prestructuredImport)) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, Helper.getTranslation(
                    "importConfig.validation.error.prestructuredImport", mappingFile.getTitle()), null));
        }
    }
}
