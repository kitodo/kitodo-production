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

import java.util.ArrayList;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import org.kitodo.constants.StringConstants;

@FacesValidator("ImportConfigurationClientValidator")
public class ImportConfigurationClientValidator implements Validator<ArrayList<?>> {

    @Override
    public void validate(FacesContext context, UIComponent component, ArrayList<?> clientList)
            throws ValidatorException {
        // only validate when saving
        if (!context.getExternalContext().getRequestParameterMap().containsKey(StringConstants.EDIT_FORM_SAVE)) {
            return;
        }
        if (clientList.isEmpty()) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "The import configuration must be assigned to at least one client", null));
        }
    }
}
