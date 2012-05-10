package de.sub.goobi.Persistence.apache;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import de.sub.goobi.Beans.ProjectFileGroup;

public class ProjectManager {
	private static final Logger logger = Logger.getLogger(MySQLHelper.class);
	
	
	public static ProjectObject getProjectById(int projectId) {
		try {
			return MySQLHelper.getProjectObjectById(projectId);
		} catch (SQLException e) {
			logger.error("Cannot not load project with id " + projectId, e);
		}
		return null;
	}


	public static List<ProjectFileGroup> getFilegroupsForProjectId(int projectId) {
		try {
			return MySQLHelper.getFilegroupsForProjectId(projectId);
		} catch (SQLException e) {
			logger.error("Cannot not load project filegroups with id " + projectId, e);
		}
		return null;
	}
	
}
