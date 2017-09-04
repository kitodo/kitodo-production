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
 * Enum of all result output possibilities for the statistics
 * 
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 */
public enum ResultOutput {

    chart("1", "chart"),
    table("2", "table"),
    chartAndTable("3", "chartAndTable");

    private String id;
    private String title;

    /**
     * private constructor for setting id and title
     * 
     * @param inTitle
     *            title as String
     * @param inId
     *            id as string
     */
    ResultOutput(String inId, String inTitle) {
        id = inId;
        title = inTitle;
    }

    /**
     * return unique ID for result output
     * 
     * @return unique ID as String
     */
    public String getId() {
        return id;
    }

    /**
     * return localized title for result output
     * 
     * @return localized title
     */
    public String getTitle() {
        return Helper.getTranslation(title);
    }

    /**
     * get presentation output by unique ID
     * 
     * @param inId
     *            the unique ID
     * @return {@link ResultOutput} with given ID
     */
    public static ResultOutput getById(String inId) {
        for (ResultOutput unit : ResultOutput.values()) {
            if (unit.getId().equals(inId)) {
                return unit;
            }
        }
        return table;
    }

}
