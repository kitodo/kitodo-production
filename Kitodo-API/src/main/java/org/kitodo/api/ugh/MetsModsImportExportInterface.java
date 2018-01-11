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

package org.kitodo.api.ugh;

public interface MetsModsImportExportInterface extends FileformatInterface {

    static final String CREATE_LABEL_ATTRIBUTE_TYPE = "...";
    static final String CREATE_MPTR_ELEMENT_TYPE = "...";
    static final String CREATE_ORDERLABEL_ATTRIBUTE_TYPE = "...";

    void setContentIDs(String replace);

    void setDigiprovPresentation(String replace);

    void setDigiprovPresentationAnchor(String replace);

    void setDigiprovReference(String replace);

    void setDigiprovReferenceAnchor(String replace);

    void setMptrAnchorUrl(String pointer);

    void setMptrUrl(Object object);

    void setPurlUrl(String replace);

    void setRightsOwner(String replace);

    void setRightsOwnerContact(String replace);

    void setRightsOwnerLogo(String replace);

    void setRightsOwnerSiteURL(String replace);

    void setWriteLocal(boolean writeLocalFileGroup);
}
