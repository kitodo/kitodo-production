package de.sub.goobi.helper.enums;

import de.sub.goobi.helper.Helper;

/**
 * Enum of all history event types for all history events for processes
 * 
 * @author Steffen Hankiewicz
 * @version 24.05.2009
 */
public enum HistoryEventType {
	/**
	 * default type is unknown for all properties, which still dont have a specific
	 * type
	 */
	unknown(0, "unknown",false, false, null),

	/** storageDifference */
	storageDifference(1, "storageDifference", true, false, null),

	/** imagesWorkDiff */
	imagesWorkDiff(2, "imagesWorkDiff", true, false, null),

	/** imagesMasterDiff */
	imagesMasterDiff(3, "imagesMasterDiff", true, false, null),

	/** metadataDiff */
	metadataDiff(4, "metadataDiff", true, false, null),
	
	/** docstructDiff, */
	docstructDiff(5, "docstructDiff", true, false, null),
	
	/** stepDone, order number and title */
	stepDone(6, "stepDone", true, true, "max"),
	
	/** stepOpen, order number and title */
	stepOpen(7, "stepOpen", true, true, "min"),
	
	/** stepInWork, order number and title */
	stepInWork(8, "stepInWork", true, true, null ),
	
	/** stepError, step order number, step title */
	stepError(9, "stepError", true, true, null);

	private int value;
	private String title;
	private Boolean isNumeric;
	private Boolean isString;
	private String groupingExpression;

	/**
	 * private constructor, initializes integer value, title and sets boolean, 
	 * if EventType contains string and/or numeric content
	 */
	private HistoryEventType(int inValue, String inTitle, Boolean inIsNumeric, Boolean inIsString, String groupingExpression) {
		value = inValue;
		title = inTitle;
		isNumeric = inIsNumeric;
		isString = inIsString;
		this.groupingExpression = groupingExpression;
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
	 * return if type contains numeric content
	 * 
	 * @return isNumeric as {@link Boolean}
	 */
	
	public Boolean isNumeric() {
		return isNumeric;
	}

	/**
	 * return if type contains string content
	 * 
	 * @return isNumeric as {@link String}
	 */
	public Boolean isString() {
		return isString;
	}
	
	/**
	 * return grouping function if needed
	 * 
	 * @return groupingExpression as{@link String}
	 */
	public String getGroupingFunction(){
		return this.groupingExpression;
	}

	/**
	 * retrieve history event type by integer value, neccessary for database handlings,
	 * where only integer is saved but not type safe
	 * 
	 * @param inType
	 *            as integer value
	 * @return {@link HistoryEventType} for given integer
	 */
	public static HistoryEventType getTypeFromValue(Integer inType) {
		if (inType != null) {
			for (HistoryEventType ss : values()) {
				if (ss.getValue() == inType.intValue())
					return ss;
			}
		}
		return unknown;
	}

}