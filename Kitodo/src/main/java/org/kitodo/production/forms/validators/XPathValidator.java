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

import javax.xml.xpath.XPathExpressionException;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.production.helper.XMLUtils;

@FacesValidator("XPathValidator")
public class XPathValidator implements Validator<String> {
    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, String xpathString)
            throws ValidatorException {
        if (StringUtils.isNotBlank(xpathString)) {
            try {
                XMLUtils.validateXPathSyntax(xpathString);
            } catch (XPathExpressionException e) {
                throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "XPath syntax error in provided String '"
                                + xpathString + "': " + e.getMessage(), null));
            }
        }
    }
}
