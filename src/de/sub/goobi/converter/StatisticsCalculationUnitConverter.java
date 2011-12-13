package de.sub.goobi.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.goobi.production.flow.statistics.enums.CalculationUnit;

/**
 * StatisticCalculationUnitConverter for statistics CalculationUnits as select-items in jsf-guis
 * 
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 **************************************************************************************/
public class StatisticsCalculationUnitConverter implements Converter {
	public static final String CONVERTER_ID = "StatisticsCalculationUnitConverter";

	/**
	 * convert String to CalculationUnit 
	 **************************************************************************************/
	public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
		if (value==null){
			return CalculationUnit.volumes;
		}else {
			return CalculationUnit.getById(value);
		}
	}
	
	/**
	 * convert ResultOutput to String
	 **************************************************************************************/
	public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
		if (value == null || !(value instanceof CalculationUnit)) {
			return CalculationUnit.volumes.getId();
		} else{
			return ((CalculationUnit) value).getId();
		}
	}

}
