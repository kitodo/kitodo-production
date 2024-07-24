/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.production.forms.validators;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.xml.xpath.XPathExpressionException;

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
