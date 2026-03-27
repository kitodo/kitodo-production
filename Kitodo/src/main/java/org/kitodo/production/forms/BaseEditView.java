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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for an edit view.
 * 
 * <p>Manages the view state, e.g. forwarded list view options via URL query parameters.</p>
 * 
 * <p>(BaseForm methods specific to edit views should be moved here in the future)</p>
 */
public class BaseEditView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(BaseEditView.class);

    protected String referrerListOptions;

    /**
     * Return the list options (URL query parameters) that were used while browsing a list before navigating to an edit view.
     * 
     * @return the referrer list view options (URL query parameters)
     */
    public String getReferrerListOptions() {
        return referrerListOptions;
    }

    /**
     * Set the list options (URL query parameters) that were used while browsing a list before navigating to an edit view.
     * 
     * @param referrerListOptions the referrer list options (URL query parameters)
     */
    public void setReferrerListOptionsFromTemplate(String referrerListOptions) {
        if (Objects.nonNull(referrerListOptions) && referrerListOptions.startsWith("_")) {
            // referrerListOptions were URL encoded twice due to JSF's auto encoding of view paths
            // manually decode them again
            this.referrerListOptions = URLDecoder.decode(referrerListOptions.substring(1), StandardCharsets.UTF_8);
        } else {
            this.referrerListOptions = referrerListOptions;
        }
    }
    
}
