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

package de.sub.goobi.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.apache.log4j.Logger;

import de.sub.goobi.beans.Regelsatz;
import de.sub.goobi.persistence.RegelsatzDAO;
import de.sub.goobi.helper.exceptions.DAOException;


public class RegelsatzConverter implements Converter {
   public static final String CONVERTER_ID = "RegelsatzConverter";
   private static final Logger logger = Logger.getLogger(RegelsatzConverter.class);
   
   public Object getAsObject(FacesContext context, UIComponent component, String value)
         throws ConverterException {
      if (value == null) {
         return null;
      } else {
         try {
				return (Regelsatz) new RegelsatzDAO().get(new Integer(value));
			} catch (NumberFormatException e) {
				logger.error(e);
				return "0";
			} catch (DAOException e) {
				logger.error(e);
				return "0";
			}
      }
   }

   public String getAsString(FacesContext context, UIComponent component, Object value)
         throws ConverterException {
      if (value == null) {
         return null;
      } else if (value instanceof Regelsatz) {
         return String.valueOf(((Regelsatz) value).getId().intValue());
      } else if (value instanceof String) {
         return (String) value;
      } else {
         throw new ConverterException("Falscher Typ: " + value.getClass() + " muss 'Regelsatz' sein!");
      }
   }

}
