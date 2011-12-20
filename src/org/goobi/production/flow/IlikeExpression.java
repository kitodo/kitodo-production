/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
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
