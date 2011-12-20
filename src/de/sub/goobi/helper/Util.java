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

package de.sub.goobi.helper;
//TODO: Check license
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

/**
   Collection of simple utility methods.

   @author <a href="mailto:nick@systemmobile.com">Nick Heudecker</a>
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
}
