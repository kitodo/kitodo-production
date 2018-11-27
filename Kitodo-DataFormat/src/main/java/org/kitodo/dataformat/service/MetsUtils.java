package org.kitodo.dataformat.service;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

class MetsUtils {
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
