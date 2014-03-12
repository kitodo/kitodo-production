package de.sub.goobi.forms;

import org.goobi.production.model.bibliography.course.BreakMode;
import org.goobi.production.model.bibliography.course.Course;

public class GranularityForm {

	private BreakMode granularity;
	// @ManagedProperty(value="#{CalendarForm.course}")
	private Course course;
	private Long numberOfPages;

	public String getGranularity() {
		if (granularity == null)
			return "null";
		return granularity.toString().toLowerCase();
	}

	public int getIssueCount() {
		return course.getIndividualIssues().size();
	}

	public Long getNumberOfPages() {
		return numberOfPages;
	}

	public int getNumberOfProcesses() {
		return course.getBreaksCount() + 1;
	}

	public void issuesClick() {
		granularity = BreakMode.ISSUES;
		course.setBreaks(granularity);
	}

	public void monthsClick() {
		granularity = BreakMode.MONTHS;
		course.setBreaks(granularity);
	}

	public void setNumberOfPages(Long arg0) {
		numberOfPages = arg0;
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
		course.setBreaks(granularity);
	}

	public void yearsClick() {
		granularity = BreakMode.YEARS;
		course.setBreaks(granularity);
	}
}
