package de.sub.goobi.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.goobi.production.flow.statistics.enums.TimeUnit;

/**
 * TimeUnitConverter for statistics TimeUnits as select-items in jsf-guis
 * 
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 **************************************************************************************/
public class StatisticsTimeUnitConverter implements Converter {
	public static final String CONVERTER_ID = "StatisticsTimeUnitConverter";

	/**
	 * convert String to TimeUnit 
	 **************************************************************************************/
	public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
		if (value==null){
			return TimeUnit.days;
		}else {
			return TimeUnit.getById(value);
		}
	}
	
	/**
	 * convert TimeUnit to String
	 **************************************************************************************/
	public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
		if (value == null || !(value instanceof TimeUnit)) {
			return TimeUnit.days.getId();
		} else{
			return ((TimeUnit) value).getId();
		}
	}

}
