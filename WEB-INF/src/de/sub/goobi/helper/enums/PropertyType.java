package de.sub.goobi.helper.enums;

import de.sub.goobi.helper.Helper;

/**
 * Enum of all property types for properties of steps, processes etc.
 * 
 * @author Steffen Hankiewicz
 * @version 23.05.2009
 */
public enum PropertyType {
	/**
	 * default type is unknown for all properties, which still dont have a specific
	 * type
	 */
	unknown(0, "unknown"),

	/** general type */
	general(1, "general"),

	/** normal message */
	messageNormal(2, "messageNormal"),

	/** important message */
	messageImportant(3, "messageImportant"),

	/** error message */
	messageError(4, "messageError");

	private int value;
	private String title;

	/**
	 * private constructor, initializes integer value and title
	 */
	private PropertyType(int inValue, String inTitle) {
		value = inValue;
		title = inTitle;
	}

	/**
	 * return integer value for database savings
	 * 
	 * @return value as integer
	 */
	public Integer getValue() {
		return value;
	}

	/**
	 * get title from type
	 * 
	 * @return title as translated string for current locale from standard-jsf-messages
	 */
	public String getTitle() {
		return Helper.getTranslation(title);
	}

	/**
	 * retrieve propertytype by integer value, neccessary for database handlings,
	 * where only integer is saved but not type safe
	 * 
	 * @param inType
	 *            as integer value
	 * @return {@link PropertyType} for given integer
	 */
	public static PropertyType getTypeFromValue(Integer inType) {
		if (inType != null) {
			for (PropertyType ss : values()) {
				if (ss.getValue() == inType.intValue())
					return ss;
			}
		}
		return unknown;
	}

}