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

/**
 * Interface to the METS/MODS export writer.
 */
public interface MetsModsImportExportInterface extends FileformatInterface {

    /**
     * For each meta data element of this type that is associated with a
     * DocStruct element of the logical structure tree of a digital document, a
     * METS pointer element will be created during export.
     */
    public static final String CREATE_MPTR_ELEMENT_TYPE = "MetsPointerURL";

    /**
     * If there is a meta data element of this type associated with a DocStruct
     * element of the logical structure tree of a digital document, a LABEL
     * attribute will be attached to the logical div element during export which
     * will have assigned the value assigned to the last meta data element of
     * this type associated with the DocStruct element.
     */
    public static final String CREATE_LABEL_ATTRIBUTE_TYPE = "TitleDocMain";

    /**
     * If there is a meta data element of this type associated with a DocStruct
     * element of the logical structure tree of a digital document, an
     * ORDERLABEL attribute will be attached to the logical div element during
     * export which will have assigned the value assigned to the last meta data
     * element of this type associated with the DocStruct element.
     */
    public static final String CREATE_ORDERLABEL_ATTRIBUTE_TYPE = "TitleDocMainShort";

    /**
     * Sets the content IDs of the METS/MODS import export.
     *
     * @param contentIDs
     *            the content IDs to set
     */
    void setContentIDs(String contentIDs);

    /**
     * Sets the digiprov presentation of the METS/MODS import export.
     *
     * @param digiprovPresentation
     *            the digiprov presentation to set
     */
    void setDigiprovPresentation(String digiprovPresentation);

    /**
     * Sets the digiprov presentation anchor of the METS/MODS import export.
     *
     * @param digiprovPresentationAnchor
     *            the digiprov presentation anchor to set
     */
    void setDigiprovPresentationAnchor(String digiprovPresentationAnchor);

    /**
     * Sets the digiprov reference of the METS/MODS import export.
     *
     * @param digiprovReference
     *            the digiprov reference to set
     */
    void setDigiprovReference(String digiprovReference);

    /**
     * Sets the digiprov reference anchor of the METS/MODS import export.
     *
     * @param digiprovReferenceAnchor
     *            the digiprov reference anchor to set
     */
    void setDigiprovReferenceAnchor(String digiprovReferenceAnchor);

    /**
     * Sets the mets:mptr anchor URL of the METS/MODS import export.
     *
     * @param mptrAnchorUrl
     *            the mets:mptr anchor URL to set
     */
    void setMptrAnchorUrl(String mptrAnchorUrl);

    /**
     * Adds a mets:mptr URL to the METS/MODS import export.
     *
     * <p>
     * <b>Caution: This is a misnomer!</b>
     *
     * @param mptrUrl
     *            the mets:mptr URL to set
     */
    void setMptrUrl(String mptrUrl);

    /**
     * Sets the purl URL of the METS/MODS import export.
     *
     * @param purlUrl
     *            the purl URL to set
     */
    void setPurlUrl(String purlUrl);

    /**
     * Sets the rights owner of the METS/MODS import export.
     *
     * @param rightsOwner
     *            the rights owner to set
     */
    void setRightsOwner(String rightsOwner);

    /**
     * Sets the rights owner contact of the METS/MODS import export.
     *
     * @param rightsOwnerContact
     *            the rights owner contact to set
     */
    void setRightsOwnerContact(String rightsOwnerContact);

    /**
     * Sets the rights owner logo of the METS/MODS import export.
     *
     * @param rightsOwnerLogo
     *            the rights owner logo to set
     */
    void setRightsOwnerLogo(String rightsOwnerLogo);

    /**
     * Sets the rights owner site URL of the METS/MODS import export.
     *
     * @param rightsOwnerSiteURL
     *            the rights owner site URL to set
     */
    void setRightsOwnerSiteURL(String rightsOwnerSiteURL);

    /**
     * Sets whether the METS/MODS import export shall write a local file group.
     *
     * @param writeLocalFileGroup
     *            whether the METS/MODS import export shall write a local file
     *            group
     */
    void setWriteLocal(boolean writeLocalFileGroup);
}
