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

package org.goobi.production.flow.statistics.hibernate;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.goobi.production.flow.statistics.StatisticsManager;

/**
 * Class helps to convert results returned from Projections or Queries, where
 * data types don't match the target data type .
 *
 * @author Wulf Riebensahm
 * @version 23.05.2009
 */
class Converter {
    private static final Logger logger = Logger.getLogger(Converter.class);

    Object myObject = null;

    SimpleDateFormat sdf;

    /**
     * constructor retrieves current locale and uses it for formatting data.
     */
    private Converter() {
        try {
            this.sdf = new SimpleDateFormat("yyyy.MM.dd", new DateFormatSymbols(StatisticsManager.getLocale()));
        } catch (NullPointerException e) {
            logger.error("Class statistics.hibernate.Converter Error, can't get FacesContext");
        }
    }

    /**
     * constructor (parameterless constructor is set to private).
     *
     * @param Object
     *            which will get converted
     */
    protected Converter(Object obj) {
        this();
        if (obj == null) {
            throw new NullPointerException();
        }
        this.myObject = obj;
    }

    /**
     * Get Integer.
     *
     * @return Integer if possible
     */
    protected Integer getInteger() {
        if (this.myObject instanceof Integer) {
            return (Integer) this.myObject;
        } else if (this.myObject instanceof Double) {
            return ((Double) this.myObject).intValue();
        } else if (this.myObject instanceof String) {
            return Integer.parseInt((String) this.myObject);
        } else if (this.myObject instanceof Long) {
            return ((Long) this.myObject).intValue();
        } else {
            throw new NumberFormatException();
        }
    }

    /**
     * Get Double.
     *
     * @return Double if possible
     */
    protected Double getDouble() {
        if (this.myObject instanceof Integer) {

            return Double.valueOf(((Integer) this.myObject).intValue());
        } else if (this.myObject instanceof Double) {

            return (Double) this.myObject;
        } else if (this.myObject instanceof String) {

            return Double.parseDouble((String) this.myObject);
        } else if (this.myObject instanceof Long) {
            return ((Long) this.myObject).doubleValue();
        } else {
            throw new NumberFormatException();
        }
    }

    /**
     * Get String.
     *
     * @return String, fall back is toString() method
     */
    protected String getString() {
        if (this.myObject instanceof Date) {
            return this.sdf.format(this.myObject);
        } else {
            return this.myObject.toString();

        }
    }

    /**
     * Get GB.
     *
     * @return Double value of GB, calculated on the basis of Bytes
     */
    protected Double getGB() {
        return getDouble() / (1024 * 1024 * 1024);

    }

}
