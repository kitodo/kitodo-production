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

package org.kitodo.production.converter;

import java.util.Objects;

import javax.faces.convert.ConverterException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.data.base.SearchDatabaseService;

public abstract class BeanConverter {

    private static final Logger logger = LogManager.getLogger(BeanConverter.class);

    /**
     * Get as object for bean convert.
     *
     * @param searchDatabaseService
     *            service used for query the object
     * @param value
     *            id of object as String
     * @return null if value is null, bean object if id is correct and exists in
     *         database, "0" when id is incorrect or object with this id doesn't
     *         exist in database
     */
    protected Object getAsObject(SearchDatabaseService searchDatabaseService, String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        } else {
            try {
                return searchDatabaseService.getById(Integer.parseInt(value));
            } catch (DAOException | NumberFormatException e) {
                logger.error(e.getMessage(), e);
                return "0";
            }
        }
    }

    /**
     * Get as string for bean convert.
     *
     * @param value
     *            bean to be converted
     * @param translationKey
     *            for possible error message
     * @return null when bean is null, otherwise string id or string representation
     *         of value
     */
    protected String getAsString(Object value, String translationKey) {
        if (Objects.isNull(value)) {
            return null;
        } else if (value instanceof BaseBean) {
            Integer beanId = ((BaseBean) value).getId();
            if (Objects.nonNull(beanId)) {
                return String.valueOf(beanId);
            }
            return "0";
        } else if (value instanceof String) {
            return (String) value;
        } else {
            throw new ConverterException(Helper.getTranslation("errorConvert",
                value.getClass().getCanonicalName(), translationKey));
        }
    }
}
