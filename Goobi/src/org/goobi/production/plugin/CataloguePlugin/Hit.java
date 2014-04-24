package org.goobi.production.plugin.CataloguePlugin;

import java.util.Map;

import org.goobi.production.model.bibliography.Citation;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import ugh.dl.Fileformat;

/**
 * The class Hit represents a hit retrieved from the search plugin.
 * 
 * The class Hit unwraps the contents of a hit result of the basic java types
 * <code>Map&lt;String, Object&gt;</code>. The map should contain a key
 * <code>fileformat</code> holding an instance of {@link ugh.dl.Fileformat} with
 * the record data and a field <code>type</code> holding the DocType.
 * 
 * <p>
 * The following additional basic bibliographic metadata entries in the map are
 * supported and will be used to display a summary of the hit in bibliographic
 * citation style. All of them must be String except for year where both Integer
 * and String are supported. The field <kbd>format</kbd> is used to pick the
 * appropriate citation formatting style.
 * </p>
 * 
 * <p>
 * <kbd>accessed</kbd> − Date and time of last access (for internet ressources
 * and online journals)<br/>
 * <kbd>article</kbd> − Title of an article<br/>
 * <kbd>contributor</kbd> − Editors, compilers, translators … of an anthology<br/>
 * <kbd>creator</kbd> − Author name(s), scheme: Lastname, Firstname ; Lastname,
 * Firstname<br/>
 * <kbd>date</kbd> − Date of publication, if year is insufficient<br/>
 * <kbd>department</kbd> − Department (for academic writings)<br/>
 * <kbd>edition</kbd> − Edition identifier<br/>
 * <kbd>employer</kbd> − Employer of an academic writer, usually the name of the
 * university<br/>
 * <kbd>format</kbd> − Record type. Supported values are “monograph” (books),
 * “thesis” (academic writings), “standard” (standards) and “internet” (online
 * ressources) for physical media and “anthology” and “periodical” for articles
 * from these two kinds of publishing. <kbd>number</kbd> − For monographs and
 * antologies that appeared as part of a series the number in that series. For
 * journals the number of the issue. For standards their identification number,
 * i.e. “ICD-10”.<br/>
 * <kbd>pages</kbd> − Page range of an article<br/>
 * <kbd>part</kbd> − Part or parts of an article<br/>
 * <kbd>place</kbd> − Place of publication<br/>
 * <kbd>publisher</kbd> − Name of the publishing house<br/>
 * <kbd>series</kbd> − Name of the series, if any<br/>
 * <kbd>subseries</kbd> − Name of the series, if any<br/>
 * <kbd>theses</kbd> − Kind of academic writing (i.e. “Diss.”)<br/>
 * <kbd>title</kbd> − Main title<br/>
 * <kbd>type</kbd> − The document type as used in PICA+ records <kbd>url</kbd> −
 * URL (for internet ressources and online journals)<br/>
 * <kbd>volume</kbd> − Number of the volume, if any<br/>
 * <kbd>volumetitle</kbd> − Title of the volume, if any<br/>
 * <kbd>year</kbd> − 4-digit year of publication
 * </p>
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class Hit {

	private final Map<String, Object> data;

	public Hit(Map<String, Object> data) {
		this.data = data;
	}

	public String getAuthors() {
		return getAs("creator", String.class);
	}

	public String getBibliographicCitation() {
		Citation result = new Citation(getFormat());
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

	private String getFormat() {
		return getAs("format", String.class);
	}

	private String getPublisher() {
		return getAs("publisher", String.class);
	}

	private String getNumber() {
		return getAs("number", String.class);
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
