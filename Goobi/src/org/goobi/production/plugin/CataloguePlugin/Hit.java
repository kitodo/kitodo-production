package org.goobi.production.plugin.CataloguePlugin;

import java.util.Map;

import org.goobi.production.model.bibliography.Citation;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import ugh.dl.Fileformat;

public class Hit {

	private final Map<String, Object> data;

	public Hit(Map<String, Object> data) {
		this.data = data;
	}

	public String getAuthors() {
		return getAs("creator", String.class);
	}

	public String getBibliographicCitation() {
		Citation result = new Citation(getDocType());
		result.setAccessTime(getAccessTime());
		result.setArticleTitle(getArticleTitle());
		result.addMultipleAuthors(getAuthors(), ";");
		result.addMultipleContributors(getEditors(), ";");
		result.setDepartment(getDepartment());
		result.setEdition(getEdition());
		result.setEmployer(getEmployer());
		result.setNumber(getNumber());
		result.setOverallTitle(getOverallTitle());
		result.setPages(getPages());
		result.setPart(getPart());
		result.setPlace(getPlaceOfPublication());
		result.setPublicationDate(getDatePublished());
		result.setPublisher(getPublisher());
		result.setSubseries(getSubseries());
		result.setTitle(getTitle());
		result.setType(getTheses());
		result.setURL(getURL());
		result.setVolume(getVolume());
		result.setVolumeTitle(getVolumeTitle());
		result.setYear(getYearPublished());
		return result.toHTML();
	}

	public String getDocType() {
		return getAs("type", String.class);
	}

	public Fileformat getFileformat() {
		return getAs("fileformat", Fileformat.class);
	}

	public String getTitle() {
		return getAs("title", String.class);
	}

	private DateTime getAccessTime() {
		String accessed = getAs("accessed", String.class);
		return accessed != null ? new DateTime(accessed) : null;
	}

	private String getArticleTitle() {
		return getAs("article", String.class);
	}

	@SuppressWarnings("unchecked")
	private <T> T getAs(String key, Class<T> type) {
		Object value = data.get(key);
		if (value == null || type.isAssignableFrom(value.getClass()))
			return (T) value;
		else
			throw new ClassCastException("Bad content type of field " + key + " (" + value.getClass().getName()
					+ "), must be " + type.getName());
	}

	private LocalDate getDatePublished() {
		String date = getAs("date", String.class);
		return date != null ? new LocalDate(date) : null;
	}

	private String getDepartment() {
		return getAs("department", String.class);
	}

	private String getEdition() {
		return getAs("edition", String.class);
	}

	private String getEditors() {
		return getAs("contributor", String.class);
	}

	private String getEmployer() {
		return getAs("employer", String.class);
	}

	private String getPublisher() {
		return getAs("publisher", String.class);
	}

	private String getNumber() {
		return getAs("numbre", String.class);
	}

	private String getOverallTitle() {
		return getAs("series", String.class);
	}

	private String getPages() {
		return getAs("pages", String.class);
	}

	private String getPart() {
		return getAs("part", String.class);
	}

	private String getPlaceOfPublication() {
		return getAs("place", String.class);
	}

	private String getSubseries() {
		return getAs("subseries", String.class);
	}

	private String getTheses() {
		return getAs("theses", String.class);
	}

	private String getURL() {
		return getAs("url", String.class);
	}

	private String getVolume() {
		return getAs("volume", String.class);
	}

	private String getVolumeTitle() {
		return getAs("volumeTitle", String.class);
	}

	private Integer getYearPublished() {
		try {
			return getAs("year", Integer.class);
		} catch (ClassCastException integerExpected) {
			try {
				String year = getAs("year", String.class);
				return Integer.valueOf(year);
			} catch (ClassCastException e) {
				throw integerExpected;
			} catch (NumberFormatException e) {
				throw integerExpected;
			}
		}
	}
}
