package de.sub.goobi.Persistence.apache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class UserManager {
	private static final Logger logger = Logger.getLogger(MySQLHelper.class);

	public static void addFilter(int userId, String filterstring) {
		if (getFilters(userId).contains(filterstring)) {
			return;
		}
		try {
			MySQLHelper.addFilterToUser(userId, filterstring);
		} catch (SQLException e) {
			logger.error("Cannot not add filter to user with id " + userId, e);
		}

	}

	public static void removeFilter(int userId, String filterstring) {
		if (!getFilters(userId).contains(filterstring)) {
			return;
		}
		try {
			MySQLHelper.removeFilterFromUser(userId, filterstring);
		} catch (SQLException e) {
			logger.error("Cannot not remove filter from user with id " + userId, e);
		}

	}

	public static List<String> getFilters(int userId) {
		List<String> answer = new ArrayList<String>();
		try {
			answer = MySQLHelper.getFilterForUser(userId);
		} catch (SQLException e) {
			logger.error("Cannot not load filter for user with id " + userId, e);
		}

		return answer;
	}
}
