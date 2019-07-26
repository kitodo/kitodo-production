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

/**
 * Backing bean for the title record link tab.
 */
package org.kitodo.production.forms.copyprocess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TitleRecordLinkTab {
    private static final Logger logger = LogManager.getLogger(TitleRecordLinkTab.class);

    /**
     * Process creation dialog to which this tab belongs.
     */
    private final ProzesskopieForm copyProcessForm;

    /**
     * Creates a new data object underlying the title record link tab.
     *
     * @param copyProcessForm
     *            process copy form containing the object
     */
    public TitleRecordLinkTab(ProzesskopieForm copyProcessForm) {
        this.copyProcessForm = copyProcessForm;
    }
}
