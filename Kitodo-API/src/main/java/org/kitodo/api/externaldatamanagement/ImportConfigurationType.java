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

package org.kitodo.api.externaldatamanagement;

public enum ImportConfigurationType {

    OPAC_SEARCH("newProcess.catalogueSearch.heading"),
    FILE_UPLOAD("newProcess.fileUpload.heading"),
    PROCESS_TEMPLATE("processTemplate");

    private final String messageKey;

    ImportConfigurationType(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * Get messageKey.
     *
     * @return value of messageKey
     */
    public String getMessageKey() {
        return messageKey;
    }
}
