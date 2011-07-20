package de.sub.goobi.Converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import de.sub.goobi.Beans.Regelsatz;
import de.sub.goobi.Persistence.RegelsatzDAO;
import de.sub.goobi.helper.exceptions.DAOException;


public class RegelsatzConverter implements Converter {
   public static final String CONVERTER_ID = "RegelsatzConverter";

   public Object getAsObject(FacesContext context, UIComponent component, String value)
         throws ConverterException {
      if (value == null) {
         return null;
      } else {
         try {
				return (Regelsatz) new RegelsatzDAO().get(new Integer(value));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return "0";
			} catch (DAOException e) {
				e.printStackTrace();
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
