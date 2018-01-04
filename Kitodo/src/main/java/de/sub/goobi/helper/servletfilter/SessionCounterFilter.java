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

// TODO: Parts of this class seem to be copied from the Internet, what's the
// licence?

import de.sub.goobi.forms.SessionForm;

import java.io.IOException;

import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

@WebFilter(filterName = "SessionCounterFilter", urlPatterns = "*.jsf")
public class SessionCounterFilter implements Filter {
    ServletContext servletContext;

    @Override
    public void init(FilterConfig filterConfig) {
        servletContext = filterConfig.getServletContext();
    }

    /**
     * Do filter.
    */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        FacesContext context = getFacesContext(request, response);
        SessionForm sf = (SessionForm) context.getApplication().createValueBinding("#{SessionForm}").getValue(context);
        sf.sessionAktualisieren(httpReq.getSession());

        chain.doFilter(request, response);
    }

    // You need an inner class to be able to call
    // FacesContext.setCurrentInstance
    // since it's a protected method
    private abstract static class InnerFacesContext extends FacesContext {
        protected static void setFacesContextAsCurrentInstance(FacesContext facesContext) {
            FacesContext.setCurrentInstance(facesContext);
        }
    }

    private FacesContext getFacesContext(ServletRequest request, ServletResponse response) {
        // Try to get it first
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            return facesContext;
        }

        FacesContextFactory contextFactory = (FacesContextFactory) FactoryFinder
                .getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder
                .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

        // Either set a private member servletContext =
        // filterConfig.getServletContext();
        // in you filter init() method or set it here like this:
        // ServletContext servletContext = ((HttpServletRequest)
        // request).getSession().getServletContext();
        // Note that the above line would fail if you are using any other
        // protocol than http

        // Doesn't set this instance as the current instance of
        // FacesContext.getCurrentInstance
        facesContext = contextFactory.getFacesContext(servletContext, request, response, lifecycle);

        // Set using our inner class
        InnerFacesContext.setFacesContextAsCurrentInstance(facesContext);

        // set a new viewRoot, otherwise context.getViewRoot returns null
        UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, "yourOwnID");
        facesContext.setViewRoot(view);

        return facesContext;
    }

    /**
     * Destroy.
    */
    @Override
    public void destroy() {
        // Nothing necessary
    }

}
