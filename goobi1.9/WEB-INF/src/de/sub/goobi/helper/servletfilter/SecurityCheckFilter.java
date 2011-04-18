package de.sub.goobi.helper.servletfilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SecurityCheckFilter implements Filter {

   

   public SecurityCheckFilter() { //called once. no method arguments allowed here!
   }

   

   public void init(FilterConfig conf) throws ServletException {
   }

   

   public void destroy() {
   }

   

   /** Creates a new instance of SecurityCheckFilter */
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
         throws IOException, ServletException {

      HttpServletRequest hreq = (HttpServletRequest) request;
      HttpServletResponse hres = (HttpServletResponse) response;
      HttpSession session = hreq.getSession();

      if (session.isNew()) {
         hres.sendRedirect(hreq.getContextPath());
         return;
      }

      //deliver request to next filter 
      chain.doFilter(request, response);
   }
}
