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

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.constants.StringConstants;

@FacesValidator("AbsolutePathValidator")
public class AbsolutePathValidator implements Validator<String> {



    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, String pathString)
            throws ValidatorException {
        // only validate when saving
        if (!facesContext.getExternalContext().getRequestParameterMap().containsKey(StringConstants.EDIT_FORM_SAVE)) {
            return;
        }
        if (StringUtils.isNotBlank(pathString) && !pathString.startsWith("/")) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Non empty URL path must be absolute, e.g. start with a '/'!", null));
        }
    }
}
