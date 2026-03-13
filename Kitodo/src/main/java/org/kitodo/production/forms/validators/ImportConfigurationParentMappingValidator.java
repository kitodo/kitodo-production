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

import java.util.Objects;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.data.database.beans.MappingFile;

@FacesValidator("ParentMappingValidator")
public class ImportConfigurationParentMappingValidator implements Validator<MappingFile> {

    private static final String METADATA_TYPE = "editForm:importConfigurationTabView:uploadMetadataFormat";
    private static final String SAVE = "editForm:save";

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, MappingFile mappingFile)
            throws ValidatorException {
        // only validate when saving
        if (!facesContext.getExternalContext().getRequestParameterMap().containsKey(SAVE)) {
            return;
        }
        UIInput metadataFormatInput = (UIInput) facesContext.getViewRoot().findComponent(METADATA_TYPE);
        String metadataFormat = (String) metadataFormatInput.getValue();
        // if parent mapping file is provided, it needs to map from specified metadata format to KITODO!
        if (Objects.nonNull(mappingFile)
                && !MetadataFormat.KITODO.name().equals(metadataFormat)) {
            if (!MetadataFormat.KITODO.name().equals(mappingFile.getOutputMetadataFormat())) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "If provided, parent mapping file must map to KITODO metadata format (found "
                                + mappingFile.getOutputMetadataFormat() + " instead)",
                        null));
            }
            if (!metadataFormat.equals(mappingFile.getInputMetadataFormat())) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "If provided, parent mapping file must map from " + metadataFormat + " format (found "
                                + mappingFile.getInputMetadataFormat() + " instead)",
                        null));
            }
        }
    }
}
