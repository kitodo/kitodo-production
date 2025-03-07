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

package org.kitodo.production.forms.dataeditor;

import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * Bean that is used in externalView.xhtml.
 */
@Named("ExternalView")
@RequestScoped
public class ExternalView {  

    /**
     * Return the shortened ID of the media by removing leading zeros.
     * 
     * @param id the long id of the media file as string
     * @return the shortened id of the media file
     */
    public static String convertToShortId(String id) {
        if (Objects.nonNull(id)) {
            return id.replaceFirst("^0+(?!$)", "");
        } else {
            return "-";
        }
    }

}
