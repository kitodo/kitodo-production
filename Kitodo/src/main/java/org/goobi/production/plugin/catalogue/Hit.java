/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.goobi.production.plugin.catalogue;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.model.bibliography.Citation;

/**
 * The class Hit represents a hit retrieved from the search plug-in.
 *
 * <p>
 * The class Hit unwraps the contents of a hit result of the basic java types
 * <code>Map&lt;String, Object&gt;</code>. The map should contain a key
 * <code>fileformat</code> holding an instance of {@link LegacyMetsModsDigitalDocumentHelper}
 * with the record data and a field <code>type</code> holding the DocType.
 *
 * <p>
 * The following additional basic bibliographic metadata entries in the map are
 * supported and will be used to display a summary of the hit in bibliographic
 * citation style. All of them must be String except for year where both Integer
 * and String are supported. The field <kbd>format</kbd> is used to pick the
 * appropriate citation formatting style.
 *
 * <p>
 * <kbd>accessed</kbd> − Date and time of last access (for internet resources
 * and online journals)<br>
 * <kbd>article</kbd> − Title of an article<br>
 * <kbd>contributor</kbd> − Editors, compilers, translators … of an
 * anthology<br/>
 * <kbd>creator</kbd> − Author name(s), scheme: Lastname, Firstname ; Lastname,
 * Firstname<br>
 * <kbd>date</kbd> − Date of publication, if year is insufficient<br>
 * <kbd>department</kbd> − Department (for academic writings)<br>
 * <kbd>edition</kbd> − Edition identifier<br>
 * <kbd>employer</kbd> − Employer of an academic writer, usually the name of the
 * university<br>
 * <kbd>format</kbd> − Record type. Supported values are “monograph” (books),
 * “thesis” (academic writings), “standard” (standards) and “internet” (online
 * resources) for physical media and “anthology” and “periodical” for articles
 * from these two kinds of publishing.<br>
 * <kbd>number</kbd> − For monographs and antologies that appeared as part of a
 * series the number in that series. For journals the number of the issue. For
 * standards their identification number, i.e. “ICD-10”.<br>
 * <kbd>pages</kbd> − Page range of an article<br>
 * <kbd>part</kbd> − Part or parts of an article<br>
 * <kbd>place</kbd> − Place of publication<br>
 * <kbd>publisher</kbd> − Name of the publishing house<br>
 * <kbd>series</kbd> − Name of the series, if any<br>
 * <kbd>subseries</kbd> − Name of the subseries, if any<br>
 * <kbd>theses</kbd> − Kind of academic writing (i.e. “Diss.”)<br>
 * <kbd>title</kbd> − Main title<br>
 * <kbd>url</kbd> − URL (for internet ressources and online journals)<br/>
 * <kbd>volume</kbd> − Number of the volume, if any<br/>
 * <kbd>volumetitle</kbd> − Title of the volume, if any<br/>
 * <kbd>year</kbd> − 4-digit year of publication
 */
public class Hit {

    /**
     * The field data holds a reference to the map holding the hit result.
     */
    private final Map<String, Object> data;

    /**
     * Hit constructor. The constructor saves a reference to the map holding the
     * hit result in the final field data.
     *
     * @param data
     *            map holding the hit result
     */
    Hit(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * Returns the creators of the work described in this hit.
     *
     * @return the creators of the work
     */
    public String getAuthors() {
        return getAs("creator", String.class);
    }

    /**
     * Returns a summary of this hit in bibliographic citation style as HTML.
     *
     * @return a summary of this hit in bibliographic citation style as HTML
     */
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
        return result.toString();
    }

    /**
     * Returns the DocType of this hit.
     *
     * @return the DocType of this hit
     */
    public String getDocType() {
        return getAs("type", String.class);
    }

    /**
     * Returns the full hit record as provided by the library catalog as
     * {@link LegacyMetsModsDigitalDocumentHelper} object.
     *
     * @return the full hit record
     */
    public LegacyMetsModsDigitalDocumentHelper getFileformat() {
        return getAs("fileformat", LegacyMetsModsDigitalDocumentHelper.class);
    }

    /**
     * Returns the title of the work described in this hit.
     *
     * @return the title of the work
     */
    public String getTitle() {
        return getAs("title", String.class);
    }

    /**
     * Returns the point in time when the work was last accessed.
     *
     * @return the point in time when the work was last accessed as
     *         {@link DateTime}
     */
    private DateTime getAccessTime() {
        String accessed = getAs("accessed", String.class);
        return Objects.nonNull(accessed) ? new DateTime(accessed) : null;
    }

    /**
     * Returns the title of the article described in this hit as String.
     *
     * @return the title of the article
     */
    private String getArticleTitle() {
        return getAs("article", String.class);
    }

    /**
     * Returns an entry form the map holding the hit result as an object of the
     * given class (which may be null). If the object cannot be casted to the
     * desired result type, a ClassCastException will be thrown.
     *
     * @param key
     *            the key whose associated value is to be returned
     * @param clazz
     *            desired result type
     * @return the value to which the specified key is mapped, or null if the
     *         map contains no mapping for the key
     * @throws ClassCastException
     *             if the content type of field cannot be cast to the desired
     *             result type
     */
    @SuppressWarnings("unchecked")
    private <T> T getAs(String key, Class<T> clazz) {
        Object value = data.get(key);
        if (Objects.isNull(value) || clazz.isAssignableFrom(value.getClass())) {
            return (T) value;
        } else {
            throw new ClassCastException("Bad content type of field " + key + " (" + value.getClass().getName()
                    + "), must be " + clazz.getName());
        }
    }

    /**
     * Returns the day when the work was published.
     *
     * @return the day when the work was published as {@link LocalDate}
     */
    private LocalDate getDatePublished() {
        String date = getAs("date", String.class);
        return Objects.nonNull(date) ? new LocalDate(date) : null;
    }

    /**
     * Returns the department of the author of the academic writing described in
     * this hit is in.
     *
     * @return the department of the author of the academic writing
     */
    private String getDepartment() {
        return getAs("department", String.class);
    }

    /**
     * Returns edition information of the work described by this hit.
     *
     * @return edition information
     */
    private String getEdition() {
        return getAs("edition", String.class);
    }

    /**
     * Returns the editors, compilers, translators, … of the anthology described
     * in this hit.
     *
     * @return the editors of the anthology
     */
    private String getEditors() {
        return getAs("contributor", String.class);
    }

    /**
     * Returns the employer—usually a university—of the author of the academic
     * writing described in this hit is in.
     *
     * @return the employer of the author of the academic writing
     */
    private String getEmployer() {
        return getAs("employer", String.class);
    }

    /**
     * Returns the citation format that is to be preferred to summarize the
     * contents of this hit. Supported values are “monograph” (books), “thesis”
     * (academic writings), “standard” (standards) and “internet” (online
     * resources) for physical media and “anthology” and “periodical” for
     * articles from these two kinds of publishing
     *
     * @return the format that is to be preferred to cite this hit.
     */
    private String getFormat() {
        return getAs("format", String.class);
    }

    /**
     * Returns the name of the publishing house that published the work
     * described in this hit.
     *
     * @return the name of the house of publish
     */
    private String getPublisher() {
        return getAs("publisher", String.class);
    }

    /**
     * Returns the number of the work described in this hit.
     *
     * @return the number
     */
    private String getNumber() {
        return getAs("number", String.class);
    }

    /**
     * Returns the title of the series that the work described in this hit
     * appeared in.
     *
     * @return the title of the series
     */
    private String getOverallTitle() {
        return getAs("series", String.class);
    }

    /**
     * Returns the page range covered by the article described in this hit.
     *
     * @return the page range covered by this article
     */
    private String getPages() {
        final Pattern pageRange = Pattern.compile("(\\d+)(\\s*-\\s*)(\\d+)");
        String pages = getAs("pages", String.class);
        if (Objects.nonNull(pages)) {
            Matcher pageRangeMatcher = pageRange.matcher(pages);
            if (pageRangeMatcher.matches() && pageRangeMatcher.group(3).length() < pageRangeMatcher.group(1).length()) {
                pages = pageRangeMatcher.group(1) + pageRangeMatcher.group(2)
                        + pageRangeMatcher.group(1).substring(0,
                                pageRangeMatcher.group(1).length() - pageRangeMatcher.group(3).length())
                        + pageRangeMatcher.group(3);
            }
        }
        return pages;
    }

    /**
     * Returns the part of the article described in this hit.
     *
     * @return the part of the article
     */
    private String getPart() {
        return getAs("part", String.class);
    }

    /**
     * Returns the place of publication of the work described in this hit.
     *
     * @return the place of publication
     */
    private String getPlaceOfPublication() {
        return getAs("place", String.class);
    }

    /**
     * Returns the subseries of the work described in this hit appared in.
     *
     * @return the subseries
     */
    private String getSubseries() {
        return getAs("subseries", String.class);
    }

    /**
     * Returns the kind of academic writing described in this hit, i.e. “Diss.”.
     *
     * @return the kind of academic writing
     */
    private String getTheses() {
        return getAs("theses", String.class);
    }

    /**
     * Returns the Internet address of the online resource described in this
     * hit.
     *
     * @return the Internet address
     */
    private String getURL() {
        return getAs("url", String.class);
    }

    /**
     * Returns the volume count of the work described in this hit.
     *
     * @return the volume number
     */
    private String getVolume() {
        return getAs("volume", String.class);
    }

    /**
     * Returns the volume title of the work described in this hit.
     *
     * @return the volume title
     */
    private String getVolumeTitle() {
        return getAs("volumeTitle", String.class);
    }

    /**
     * Returns the year the work described in this hit was created as Integer. A
     * String value is also supported, if it can be parsed to an Integer.
     *
     * @return the year the work was created
     */
    private Integer getYearPublished() {
        try {
            return getAs("year", Integer.class);
        } catch (ClassCastException integerExpected) {
            try {
                String year = getAs("year", String.class);
                return Integer.valueOf(year);
            } catch (ClassCastException | NumberFormatException e) {
                throw integerExpected;
            }
        }
    }
}
