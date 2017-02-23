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

package de.sub.goobi.helper;

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
 * @author Matthias Ronge
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
