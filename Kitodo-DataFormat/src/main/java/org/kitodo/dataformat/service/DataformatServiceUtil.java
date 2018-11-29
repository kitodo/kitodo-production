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

package org.kitodo.dataformat.service;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

class DataformatServiceUtil {
    private static DatatypeFactory getDatatypeFactory() {
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            String message = e.getMessage();
            throw new NoClassDefFoundError(message != null ? message
                    : "Implementation of DatatypeFactory not available or cannot be instantiated.");
        }
        return datatypeFactory;
    }

    public static XMLGregorianCalendar convertDate(GregorianCalendar gregorianCalendar) {
        return getDatatypeFactory().newXMLGregorianCalendar(gregorianCalendar);
    }
}
