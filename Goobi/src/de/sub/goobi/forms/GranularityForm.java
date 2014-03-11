package de.sub.goobi.forms;

import org.goobi.production.model.bibliography.course.BreakMode;
import org.goobi.production.model.bibliography.course.Course;

public class GranularityForm {

	private BreakMode granularity;
	// @ManagedProperty(value="#{CalendarForm.course}")
	private Course course;
	private Long numberOfPages;

	//	/**
	//	 * The function getCourse() provides access to the course data model which
	//	 * is passed as f:param name="course" by an h:commandButton in calendar.jsp.
	//	 * The incoming object is cached to be available in case of JSF postbacks.
	//	 * 
	//	 * @return the course of appearance data model
	//	 */
	//	private Course getCourse() {
	//		try {
	//			Course updatedCourse = (Course) FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
	//					.get("course");
	//			if (updatedCourse != null)
	//				cachedCourse = updatedCourse;
	//		} catch (NullPointerException e) {
	//		}
	//		return cachedCourse;
	//	}

	public String getGranularity() {
		if (granularity == null)
			return "null";
		return granularity.toString().toLowerCase();
	}

	public String getIssueCount() {
		return Integer.toString(course.getIndividualIssues().size());
	}

	public String getNumberOfPages() {
		if (numberOfPages == null)
			return "";
		return numberOfPages.toString();
	}

	public void issuesClick() {
		granularity = BreakMode.ISSUES;
	}

	public void monthsClick() {
		granularity = BreakMode.MONTHS;
	}

	public void setNumberOfPages(String arg0) {
		numberOfPages = new Long(arg0);
	}

	/**
	 * The method setCourse() is called by JSF to inject the course data model
	 * into the form. This behaviour is configured in faces-config.xml
	 * 
	 * @param course
	 *            Course of appearance data model to be used
	 */
	public void setCourse(Course course) {
		this.course = course;
	}

	public void weeksClick() {
		granularity = BreakMode.WEEKS;
	}

	public void yearsClick() {
		granularity = BreakMode.YEARS;
	}
}
