/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.webapi.beans;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifierPPN {

    private String ppn;

    public IdentifierPPN(String ppn) {
        if (!isValid(ppn)) {
            throw new IllegalArgumentException("Given string is not a valid PPN identifier.");
        }
        this.ppn = ppn;
    }

    public static boolean isValid(String identifier) {
        Boolean result;
        int flags = Pattern.CASE_INSENSITIVE;
        Pattern pattern;
        Matcher matcher;

        if ((identifier == null) || (identifier.length() == 0)) {
            result = false;
        } else {
            pattern = Pattern.compile("^[0-9]{8}[0-9LXYZ]{1}$", flags);
            matcher = pattern.matcher(identifier);
            result = matcher.matches();
        }

        return result;
    }

    @Override
    public String toString() {
        return ppn;
    }


}
