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

package org.kitodo.production.converter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.production.forms.dataeditor.IllustratedSelectItem;
import org.kitodo.production.helper.Helper;


@Named("IllustratedSelectItemConverter")
public class IllustratedSelectItemConverter implements Converter, Serializable {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (StringUtils.isNotEmpty(value)) {
            List<IllustratedSelectItem> illustratedSelectItems = (List<IllustratedSelectItem>) component.getAttributes()
                    .get("illustratedSelectItems");
            for (IllustratedSelectItem illustratedSelectItem : illustratedSelectItems) {
                if (Helper.getTranslation(value).equals(Helper.getTranslation(illustratedSelectItem.getLabel()))) {
                    return illustratedSelectItem;
                }
            }
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object object) {
        if (Objects.nonNull(object)) {
            IllustratedSelectItem selectItem = (IllustratedSelectItem) object;
            return selectItem.getLabel();
        }
        return null;
    }
}
