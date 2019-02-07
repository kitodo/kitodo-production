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

package org.kitodo.production.metadata;

import java.util.ArrayList;

import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.production.helper.TreeNode;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;

public class TreeNodeStruct3 extends TreeNode {

    private LegacyDocStructHelperInterface struct;
    private String firstImage;
    private String lastImage;
    private String zblNummer;
    private String mainTitle;
    private String ppnDigital;
    private String identifier;
    private String zblSeiten;
    private boolean einfuegenErlaubt;

    /**
     * Constructor.
     *
     * @param expanded
     *            true or false
     * @param label
     *            as String
     * @param id
     *            as String
     */
    public TreeNodeStruct3(boolean expanded, String label, String id) {
        this.expanded = expanded;
        this.label = label;
        this.id = id;
        // TODO: Use generics
        this.children = new ArrayList<>();
    }

    /**
     * Constructor.
     *
     * @param label
     *            as String
     * @param struct
     *            as DocStruct
     */
    public TreeNodeStruct3(String label, LegacyDocStructHelperInterface struct) {
        this.label = label;
        this.struct = struct;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Get main title.
     *
     * @return main title
     */
    public String getMainTitle() {
        int maxSize = ConfigCore.getIntParameter(ParameterCore.METS_EDITOR_MAX_TITLE_LENGTH);
        if (maxSize > ConfigCore.INT_PARAMETER_NOT_DEFINED_OR_ERRONEOUS && this.mainTitle != null
                && this.mainTitle.length() > maxSize) {
            return this.mainTitle.substring(0, maxSize - 1);
        }

        return this.mainTitle;
    }

    public void setMainTitle(String mainTitle) {
        this.mainTitle = mainTitle;
    }

    public String getPpnDigital() {
        return this.ppnDigital;
    }

    public void setPpnDigital(String ppnDigital) {
        this.ppnDigital = ppnDigital;
    }

    public String getFirstImage() {
        return this.firstImage;
    }

    public void setFirstImage(String firstImage) {
        this.firstImage = firstImage;
    }

    public String getLastImage() {
        return this.lastImage;
    }

    public void setLastImage(String lastImage) {
        this.lastImage = lastImage;
    }

    public LegacyDocStructHelperInterface getStruct() {
        return this.struct;
    }

    public void setStruct(LegacyDocStructHelperInterface struct) {
        this.struct = struct;
    }

    public String getZblNummer() {
        return this.zblNummer;
    }

    public void setZblNummer(String zblNummer) {
        this.zblNummer = zblNummer;
    }

    public String getDescription() {
        return this.label;
    }

    public void setDescription(String description) {
        this.label = description;
    }

    public boolean isEinfuegenErlaubt() {
        return this.einfuegenErlaubt;
    }

    public void setEinfuegenErlaubt(boolean einfuegenErlaubt) {
        this.einfuegenErlaubt = einfuegenErlaubt;
    }

    public String getZblSeiten() {
        return this.zblSeiten;
    }

    public void setZblSeiten(String zblSeiten) {
        this.zblSeiten = zblSeiten;
    }

}
