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

package org.kitodo.data.database.helper;

import java.io.Serializable;
import java.util.Objects;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;

/**
 * Class contains methods needed for beans and persistence.
 */
public class HibernateHelper implements Serializable {

    private static final long serialVersionUID = -7449236652821237059L;

    /**
     * Get managed bean value.
     *
     * @param beanName
     *            String
     * @return managed bean
     */
    public static Object getManagedBeanValue(String beanName) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return null;
        } else {
            Object value = null;
            ELContext elContext = context.getELContext();
            if (Objects.nonNull(elContext)) {
                ELResolver elResolver = elContext.getELResolver();
                if (Objects.nonNull(elResolver)) {
                    value = elResolver.getValue(context.getELContext(), null, beanName);
                }
            }
            return value;
        }
    }
}
