package de.sub.goobi.helper;

import java.util.TreeSet;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

/**
 * The class DateFuncs contains an omnium-gatherum of functions that work on
 * calendar dates.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class DateFuncs {

	public static boolean sameMonth(LocalDate current, LocalDate next) {
		if (!sameYear(current, next))
			return false;
		return current.getMonthOfYear() == next.getMonthOfYear();
	}

	public static boolean sameYear(LocalDate current, LocalDate next) {
		if (next == null)
			return false;
		return current.getYear() == next.getYear();
	}

	public static int lastMonthForYear(TreeSet<LocalDate> data, int year) {
		return data.headSet(new LocalDate(year, DateTimeConstants.DECEMBER, 31), true).last().getMonthOfYear();
	}
}
