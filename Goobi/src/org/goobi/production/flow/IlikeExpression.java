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

package org.goobi.production.flow;

import org.hibernate.criterion.LikeExpression;

public class IlikeExpression extends LikeExpression {

    /**
     *
     */
    private static final long serialVersionUID = -4799319673461424418L;




    protected IlikeExpression(
            String propertyName,
            String value,
            Character escapeChar) {
        super(propertyName, escapeString(value), escapeChar, false);
    }

    static String escapeString(String inputString) {
        inputString = inputString.replace("!", "!!");
        inputString = inputString.replace("%", "!%");
        inputString = inputString.replace("_", "!_");
        inputString = inputString.replace("?", "_");
        inputString = inputString.replace("*", "%");
        return inputString;
      }

    public static org.hibernate.criterion.Criterion ilike(String propertyName, String value, Character escapeChar) {
        return new IlikeExpression(propertyName, value, escapeChar);
    }

}
