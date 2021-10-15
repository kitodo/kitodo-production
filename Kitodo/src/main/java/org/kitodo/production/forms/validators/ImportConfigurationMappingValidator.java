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
import org.primefaces.model.DualListModel;

@FacesValidator("MappingValidator")
public class ImportConfigurationMappingValidator implements Validator<DualListModel<MappingFile>> {

    private static final String PROCESS_TEMPLATE = "PROCESS_TEMPLATE";
    private static final String CONFIG_TYPE = "editForm:importConfigurationTabView:configurationType";
    private static final String METADATA_TYPE = "editForm:importConfigurationTabView:metadataFormat";
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

        // if no mapping files are configured, the imported metadata must already be in KITODO internal format
        if (!Objects.equals(metadataFormat, MetadataFormat.KITODO.name())
                && mappingFiles.isEmpty()) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Import configuration must map imported metadata format '" + metadataFormat
                    + "' to KITODO internal metadata format with appropriate mapping files!", null));
        } else {
            for (MappingFile mappingFile : mappingFiles) {
                int index = mappingFiles.indexOf(mappingFile);
                // first mapping file must map import configuration metadata format to some other format
                if (index == 0 && !Objects.equals(metadataFormat, mappingFile.getInputMetadataFormat()))
                    throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "First configured mapping file '" + mappingFile.getTitle() + "' must have '"
                                    + metadataFormat
                                    + "' as input metadata format (found '" + mappingFile.getInputMetadataFormat()
                                    + " instead)!", null));
                // last mapping file must map to KITODO internal metadata format
                if (index == mappingFiles.size() - 1
                        && !Objects.equals(mappingFile.getOutputMetadataFormat(), MetadataFormat.KITODO.name())) {
                    throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Last configured mapping file '" + mappingFile.getTitle()
                                    + "' must map to KITODO internal format (found '"
                                    + mappingFile.getOutputMetadataFormat() + "' instead)", null));
                }
                // intermediate mapping files output metadata format must match next files input metadata format
                if (mappingFiles.size() > index + 1 && !Objects.equals(mappingFile.getOutputMetadataFormat(),
                        mappingFiles.get(index + 1).getInputMetadataFormat())) {
                    throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Mapping file '"
                            + mappingFile.getTitle() + "' output metadata format must match next mapping file's input metadata format '"
                            + mappingFiles.get(index + 1).getInputMetadataFormat() + "' (found '"
                            + mappingFile.getOutputMetadataFormat() + "' instead!)", null));
                }
            }
        }
    }
}
