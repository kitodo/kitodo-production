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

package org.goobi.production.flow.statistics.enums;

import de.sub.goobi.helper.Helper;

/**
 * Enum of all calculation units for the statistics
 *
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 ****************************************************************************/
public enum CalculationUnit {

    volumes("1", "volumes"), pages("2", "pages"), volumesAndPages("3",
            "volumesAndPages");

    private String id;
    private String title;

    /**
     * private constructor for setting id and title
     *
     * @param inTitle
     *            title as String
     * @param inId
     *            id as string
     ****************************************************************************/
    private CalculationUnit(String inId, String inTitle) {
        id = inId;
        title = inTitle;
    }

    /**
     * return unique ID for CalculationUnit
     *
     * @return unique ID as String
     ****************************************************************************/
    public String getId() {
        return id;
    }

    /**
     * return localized title for CalculationUnit
     *
     * @return localized title
     ****************************************************************************/
    public String getTitle() {
        return Helper.getTranslation(title);
    }

    /**
     * get CalculationUnit by unique ID
     *
     * @param inId
     *            the unique ID
     * @return {@link CalculationUnit} with given ID
     ****************************************************************************/
    public static CalculationUnit getById(String inId) {
        for (CalculationUnit unit : CalculationUnit.values()) {
            if (unit.getId().equals(inId)) {
                return unit;
            }
        }
        return volumes;
    }

}
