package de.sub.goobi.helper;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Collection of simple utility methods.
 * 
 * @author <a href="mailto:nick@systemmobile.com">Nick Heudecker</a>
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */

public final class Util {

    private Util() {
    }

    /**
       Returns an HQL query from the resource bundle.

       @param key the resource key
       @return String
    */
    public static String getQuery(String key) {
        ResourceBundle bundle = getResourceBundle();
        return bundle.getString(key);
    }

    /**
       Utility method to create a <code>Date</code> class
       from <code>dateString</code>.

       @param dateString
       @return Date
       @throws RuntimeException is dateString is invalid
    */
    public static Date parseDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
            return sdf.parse(dateString);
        }
        catch (ParseException pe) {
            throw new RuntimeException("Not a valid date: "+dateString+
                                       ". Must be of YYYY-MMM-DD format.");
        }
    }

    /**
       Returns the resource bundle specified by <code>RESOURCE_BUNDLE</code>.

       @return ResourceBundle
    */
    private static ResourceBundle getResourceBundle() {
        if(bundle == null) {
            bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE, Locale.ENGLISH,
                                              Thread.currentThread()
                                              .getContextClassLoader());
        }
        return bundle;
    }

    private static ResourceBundle bundle;
    public static final String RESOURCE_BUNDLE = "example_app";

	/**
	 * Calculates the optimal initial capacity for a HashMap or HashSet instance
	 * that is to be populated with the given collection and isn’t intended to
	 * grow any further.
	 * 
	 * @param collection
	 *            collection whose size shall be used to determine the initial
	 *            capacity for a HashMap
	 * @return the appropriate capacity
	 */
	public static int hashCapacityFor(Collection<?> collection) {
		return (int) Math.ceil(collection.size() / 0.75);
	}
}
