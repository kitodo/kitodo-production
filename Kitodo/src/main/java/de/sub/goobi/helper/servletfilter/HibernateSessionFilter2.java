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

package de.sub.goobi.helper.servletfilter;

import de.sub.goobi.helper.exceptions.GUIExceptionWrapper;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;
import org.kitodo.data.database.persistence.HibernateSessionLong;

@WebFilter(filterName = "HibernateFilter", urlPatterns = "*.jsf")
public class HibernateSessionFilter2 implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException {
        HttpServletRequest myRequest = (HttpServletRequest) request;
        // das Managed Bean aus der Session holen
        HibernateSessionLong hsl = (HibernateSessionLong) myRequest.getSession().getAttribute("HibernateSessionLong");
        Session session = null;

        // wenn das Managed Bean bereits in der Http-Session war,
        // dann daraus die Hibernate-Session ermitteln
        if (hsl != null) {
            session = hsl.getSession();
        }
        if (session != null && !session.isConnected()) {
            // TODO: check why is it empty
        }

        try {
            chain.doFilter(request, response);
        } catch (IOException | RuntimeException e) {
            throw new ServletException(new GUIExceptionWrapper("Unexpected Error.", e));
        } finally {

            /*
             * die Hibernate-Session von der Datenbank trennen
             */
            // das Managed Bean aus der Session holen
            hsl = (HibernateSessionLong) myRequest.getSession().getAttribute("HibernateSessionLong");
            // wenn das Managed Bean bereits in der Http-Session war,
            // dann daraus die Hibernate-Session ermitteln
            if (hsl != null) {
                session = hsl.getSession();
                // gibt es eine Hibernate-Session und ist diese mit der DB
                // verbunden, dann wird diese jetzt getrennt
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            }
        }
    }

    /**
     * Destroy.
     */
    @Override
    public void destroy() {
        // Nothing necessary
    }

}
