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

package org.goobi.production.model.bibliography;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kitodo.exceptions.UnreachableCodeException;
import org.kitodo.helper.Helper;

/**
 * The class Citation represents a citation as they are used in academic
 * writings.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class Citation {
    /**
     * The enum Type lists the citation formatting variants supported by the
     * citation class. These are:
     *
     * <p>
     * <b>MONOGRAPH</b> − to cite from a continuous book (unlike an
     * anthology)<br>
     * <b>ANTHOLOGY</b> − to cite from an article published in an antology<br>
     * <b>PERIODICAL</b> − to cite from an article published in a periodical<br>
     * <b>THESIS</b> − to cite from an academic writing <b>STANDARD</b> − to
     * cite from a standard <b>INTERNET</b> − to cite from a web site (use
     * PERIODICAL for online journals as well)
     * </p>
     *
     * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
     */
    private enum Type {
        MONOGRAPH, ANTHOLOGY, PERIODICAL, THESIS, STANDARD, INTERNET
    }

    /**
     * The constant ACCESS_TIME_FORMAT holds the DateTimeFormat used to format
     * the point in time when the work was last accessed.
     */
    private static final DateTimeFormatter ACCESS_TIME_FORMAT = DateTimeFormat.forPattern("d. MMMM yyyy HH:mm ZZ");

    /**
     * The constant PUBLICATION_DATE_FORMAT holds the DateTimeFormat used to
     * format the day when the work was published.
     */
    private static final DateTimeFormatter PUBLICATION_DATE_FORMAT = DateTimeFormat.forPattern("d. MMMM yyyy");

    /**
     * The field style holds the {@link Citation.Style} used for formatting this
     * citation.
     */
    private final Type style;

    /**
     * The field accessed holds the point in time when the work was last
     * accessed.
     */
    private DateTime accessed;

    /**
     * The field contributors holds the list of editors, compilers, translators,
     * … of the anthology.
     */
    private final List<String> contributors = new ArrayList<>();

    /**
     * The field creators holds the list of creators of the work.
     */
    private final List<String> creators = new ArrayList<>();

    /**
     * The field department holds the department of the author.
     */
    private String department;

    /**
     * The field dependentTitle holds the title of the article.
     */
    private String dependentTitle;

    /**
     * The field edition holds edition information of the work.
     */
    private String edition;

    /**
     * The field employer holds the employer of the author.
     */
    private String employer;

    /**
     * The field number holds the number of the work.
     */
    private String number;

    /**
     * The field overallTitle holds the title of the series that the work
     * appeared in.
     */
    private String overallTitle;

    /**
     * The field pages holds the page range covered by the article.
     */
    private String pages;

    /**
     * The field part holds the part of the article.
     */
    private String part;

    /**
     * The field place holds the place of publication of the work.
     */
    private String place;

    /**
     * The field published holds the day when the work was published.
     */
    private LocalDate published;

    /**
     * The field publisher holds the name of the publishing house that published
     * the work.
     */
    private String publisher;

    /**
     * The field subseries holds the subseries the work appeared in.
     */
    private String subseries;

    /**
     * The field title holds the title of the autonomous work.
     */
    private String title;

    /**
     * The field type holds the kind of academic writing.
     */
    private String type;

    /**
     * The field url holds the Internet address of the online resource.
     */
    private String url;

    /**
     * The field volume holds the volume count of the work.
     */
    private String volume;

    /**
     * The field volumetitle holds the volume title of the work.
     */
    private String volumetitle;

    /**
     * The field year holds the year the work was published.
     */
    private Integer year;

    /**
     * Constructor. Creates a new citation.
     *
     * @param format
     *            format that is to be used to summarise the bibliographic
     *            metadata, may be one of the {@link Citation.Type} types. An
     *            unknown entry will be formatted as monograph.
     */
    public Citation(String format) {
        Type type;
        try {
            type = Type.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            type = Type.MONOGRAPH;
        }
        style = type;
    }

    /**
     * The method addMultipleAuthors() can be used to set the creators of the
     * work.
     *
     * @param creators
     *            the creators of the work
     * @param separatedBy
     *            character sequence separating multiple author entries
     */
    public void addMultipleAuthors(String creators, String separatedBy) {
        addMultiple(creators, separatedBy, this.creators);
    }

    /**
     * The method addMultipleContributors() can be used to set the editors,
     * compilers, translators, … of the anthology.
     *
     * @param contributors
     *            the editors of the anthology
     * @param separatedBy
     *            character sequence separating multiple contributor entries
     */
    public void addMultipleContributors(String contributors, String separatedBy) {
        addMultiple(contributors, separatedBy, this.contributors);
    }

    /**
     * The function addMultiple() adds multiple entities from a String to a
     * collection.
     *
     * @param list
     *            a String listing one or more entities
     * @param separator
     *            character sequence separating multiple list entries
     * @param destination
     *            a collection to add the entities to
     */
    private void addMultiple(String list, String separator, Collection<String> destination) {
        if (list == null || list.trim().length() == 0) {
            return;
        }
        while (list.contains(separator)) {
            if (list.substring(0, list.indexOf(separator)).trim().length() > 0) {
                destination.add(list.substring(0, list.indexOf(separator)).trim());
            }
            list = list.substring(list.indexOf(separator) + 1);
        }
        if (list.trim().length() > 0) {
            destination.add(list.trim());
        }
    }

    /**
     * The method setAccessTime() can be used to set the point in time when the
     * work was last accessed.
     *
     * @param accessed
     *            the point in time when the work was last accessed
     */
    public void setAccessTime(DateTime accessed) {
        this.accessed = accessed;
    }

    /**
     * The method setArticleTitle() can be used to set the title of the article.
     *
     * @param title
     *            the title of the article
     */
    public void setArticleTitle(String title) {
        if ("".equals(title)) {
            title = null;
        }
        this.dependentTitle = title;
    }

    /**
     * The method setDepartment() can be used to set the department of the
     * author of the academic writing.
     *
     * @param department
     *            the department of the author
     */
    public void setDepartment(String department) {
        if ("".equals(department)) {
            department = null;
        }
        this.department = department;
    }

    /**
     * The method setEdition() can be used to set edition information of the
     * work.
     *
     * @param edition
     *            edition information
     */
    public void setEdition(String edition) {
        if ("".equals(edition)) {
            edition = null;
        }
        this.edition = edition;
    }

    /**
     * The method setEmployer() can be used to set the employer—usually a
     * university—of the author of the academic writing.
     *
     * @param employer
     *            the employer of the author
     */
    public void setEmployer(String employer) {
        if ("".equals(employer)) {
            employer = null;
        }
        this.employer = employer;
    }

    /**
     * The method setNumber() can be used to set the number of the work.
     *
     * @param number
     *            the number
     */
    public void setNumber(String number) {
        if ("".equals(number)) {
            number = null;
        }
        this.number = number;
    }

    /**
     * The method setOverallTitle() can be used to set the title of the series
     * that the work appeared in.
     *
     * @param title
     *            the title of the series
     */
    public void setOverallTitle(String title) {
        if ("".equals(title)) {
            title = null;
        }
        this.overallTitle = title;
    }

    /**
     * The method setPages() can be used to set the page range covered by the
     * article.
     *
     * @param pages
     *            the page range
     */
    public void setPages(String pages) {
        if ("".equals(pages)) {
            pages = null;
        }
        this.pages = pages;
    }

    /**
     * The method setPart() can be used to set the part of the article.
     *
     * @param part
     *            the part of the article
     */
    public void setPart(String part) {
        if ("".equals(part)) {
            part = null;
        }
        this.part = part;
    }

    /**
     * The method setPlace() can be used to set the place of publication of the
     * work.
     *
     * @param place
     *            the place of publication of the work
     */
    public void setPlace(String place) {
        if ("".equals(place)) {
            place = null;
        }
        this.place = place;
    }

    /**
     * The method setPublicationDate() can be used to set the day when the work
     * was published.
     *
     * @param date
     *            the day when the work was published
     */
    public void setPublicationDate(LocalDate date) {
        this.published = date;
    }

    /**
     * The method setPublisher() can be used to set the name of the publishing
     * house that published the work.
     *
     * @param publisher
     *            the name of the publishing house
     */
    public void setPublisher(String publisher) {
        if ("".equals(publisher)) {
            publisher = null;
        }
        this.publisher = publisher;
    }

    /**
     * The method setSubseries() can be used to set the subseries the work
     * appeared in.
     *
     * @param subseries
     *            the subseries the work appared in
     */
    public void setSubseries(String subseries) {
        if ("".equals(subseries)) {
            subseries = null;
        }
        this.subseries = subseries;
    }

    /**
     * The method setTitle() can be used to set the title of the autonomous
     * work.
     *
     * @param title
     *            the title of the autonomous work
     */
    public void setTitle(String title) {
        if ("".equals(title)) {
            title = null;
        }
        this.title = title;
    }

    /**
     * The method setType() can be used to set the kind of academic writing.
     *
     * @param type
     *            the kind of academic writing
     */
    public void setType(String type) {
        if ("".equals(type)) {
            type = null;
        }
        this.type = type;
    }

    /**
     * The method setURL() can be used to set the Internet address of the online
     * resource.
     *
     * @param url
     *            the Internet address
     */
    public void setURL(String url) {
        if ("".equals(url)) {
            url = null;
        }
        this.url = url;
    }

    /**
     * The method setVolume() can be used to set the volume count of the work.
     *
     * @param volume
     *            the volume count
     */
    public void setVolume(String volume) {
        if ("".equals(volume)) {
            volume = null;
        }
        this.volume = volume;
    }

    /**
     * The method setVolumeTitle() can be used to set the volume title of the
     * work.
     *
     * @param title
     *            the volume title
     */
    public void setVolumeTitle(String title) {
        if ("".equals(title)) {
            title = null;
        }
        this.volumetitle = title;
    }

    /**
     * The method setYear() can be used to set the year the work was published.
     *
     * @param year
     *            the year the work was published
     */
    public void setYear(Integer year) {
        this.year = year;
    }

    /**
     * The function toHTML() returns the bibliographic citation in HTML format.
     *
     * @return the bibliographic citation as HTML
     */
    public String toHTML() {
        StringBuilder result = new StringBuilder();
        switch (style) {
            case MONOGRAPH:
                if (!creators.isEmpty()) {
                    appendNames(creators, result);
                    appendYear(result);
                    appendTitle(result);
                    appendVolumeInformation(result);
                    appendEdition(result, true);
                    appendPlaceAndPublisher(result);
                    appendOverallTitleAndNumber(result);
                } else {
                    appendTitle(result, null);
                    appendVolumeInformation(result);
                    appendEdition(result, true);
                    appendPlaceAndPublisher(result);
                    appendYearSimple(result);
                    appendOverallTitleAndNumber(result);
                }
                break;
            case ANTHOLOGY:
                appendNames(creators, result);
                appendYear(result);
                appendArticle(result);
                appendContainedIn(result);
                appendNames(contributors, result);
                appendTitle(result);
                appendVolumeInformation(result);
                appendEdition(result, true);
                appendPlaceAndPublisher(result);
                appendOverallTitleAndNumber(result);
                appendPagerange(result);
                break;
            case PERIODICAL:
                appendNames(creators, result);
                appendYear(result);
                appendArticle(result);
                if (part != null) {
                    result.append(' ');
                    result.append(part);
                }
                appendContainedIn(result);
                appendTitle(result);
                if (subseries != null) {
                    result.append(' ');
                    result.append(subseries);
                }
                if (volume != null) {
                    result.append(' ');
                    result.append(volume);
                }
                if (published != null) {
                    result.append(" (");
                    result.append(published.toString(PUBLICATION_DATE_FORMAT));
                    result.append(')');
                }
                if (number != null) {
                    result.append(' ');
                    result.append(number);
                }
                appendPagerange(result);
                if (accessed != null) {
                    result.append(" (");
                    appendAccessed(result);
                    result.append(')');
                }
                appendURL(result);
                break;
            case THESIS:
                appendNames(creators, result);
                appendYear(result);
                appendTitle(result);
                if (employer != null) {
                    result.append(' ');
                    result.append(employer);
                }
                if (place != null) {
                    result.append(' ');
                    result.append(place);
                }
                if (department != null && (employer != null || place != null)) {
                    result.append(',');
                }
                if (department != null) {
                    result.append(' ');
                    result.append(department);
                }
                if (type != null && (employer != null || place != null || department != null)) {
                    result.append(',');
                }
                if (type != null) {
                    result.append(' ');
                    result.append(type);
                }
                break;
            case STANDARD:
                if (number != null) {
                    result.append(number);
                }
                appendEdition(result, false);
                appendTitle(result);
                break;
            case INTERNET:
                if (!creators.isEmpty()) {
                    appendNames(creators, result);
                    appendYear(result);
                    appendTitle(result);
                } else {
                    appendTitle(result, null);
                    appendYearSimple(result);
                }
                if (published != null || accessed != null) {
                    result.append(" (");
                    if (published != null) {
                        appendPublished(result);
                    }
                    if (published != null && accessed != null) {
                        result.append(", ");
                    }
                    if (accessed != null) {
                        appendAccessed(result);
                    }
                    result.append(')');
                }
                appendURL(result);
                break;
            default:
                throw new UnreachableCodeException();
        }
        return result.toString();
    }

    /**
     * The function toString() returns a string that textually represents this
     * object.
     *
     * @return a human raedable String representation
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return toHTML().replaceAll("</?span.*?>", "");
    }

    /**
     * The method appendAccessed() appends the time of last access to the given
     * StringBuilder.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendAccessed(StringBuilder builder) {
        final Pattern lookFor = Pattern.compile("(?<= )([+\\-]\\d{2}:\\d{2}$)");
        builder.append(Helper.getTranslation("citation.accessTimestamp"));
        builder.append(' ');
        StringBuffer result = new StringBuffer();
        String formatted = accessed.toString(ACCESS_TIME_FORMAT);
        Matcher scanner = lookFor.matcher(formatted);
        while (scanner.find()) {
            scanner.appendReplacement(result,
                    Helper.getTranslation("timeZone.".concat(scanner.group(1)), scanner.group(1)));
        }
        scanner.appendTail(result);
        builder.append(result);
    }

    /**
     * The method appendArticle() appends the article title to the given
     * StringBuilder. An unterminated phrase will be ended by a full stop.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendArticle(StringBuilder builder) {
        if (dependentTitle != null) {
            builder.append(": ");
            builder.append(Helper.getTranslation("citation.quotes.open"));
            builder.append(dependentTitle);
            if (!(dependentTitle.endsWith(".") || dependentTitle.endsWith("?") || dependentTitle.endsWith("!"))) {
                builder.append(".");
            }
            builder.append(Helper.getTranslation("citation.quotes.close"));
        }
    }

    /**
     * The method appendContainedIn() appends the “ In: ” remark to the given
     * StringBuilder.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendContainedIn(StringBuilder builder) {
        builder.append(' ');
        builder.append(Helper.getTranslation("citation.containedIn"));
        builder.append(' ');
    }

    /**
     * The method appendEdition() appends edition information to the given
     * StringBuilder. An unterminated phrase will be ended by a full stop.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendEdition(StringBuilder builder, boolean sentenceMark) {
        if (edition != null) {
            builder.append(' ');
            builder.append(edition);
            if (sentenceMark && !(edition.endsWith(".") || edition.endsWith("?") || edition.endsWith("!"))) {
                builder.append(".");
            }
        }
    }

    /**
     * The method appendNames() appends a list of names to the given
     * StringBuilder.
     *
     * @param names
     *            a list of names to append
     * @param builder
     *            StringBuilder to write to
     */
    private void appendNames(List<String> names, StringBuilder builder) {
        if (Objects.nonNull(names) && !names.isEmpty()) {
            builder.append(formatName(names.get(0), true));
            for (int i = 1; i < names.size(); i++) {
                builder.append(" ; ");
                builder.append(formatName(names.get(i), false));
            }
        }
    }

    /**
     * The method formatName() returns a name formatted in HTML. If colon is
     * true, it returns “Lastname, Firstname”, otherwise it returns “Firstname
     * Lastname”. In either case, the last name will be formatted in small caps.
     *
     * @param name
     *            Name to format, scheme "Lastname, Firstname"
     * @param colon
     *            wether to put the last name first, followed by a colon
     * @return the name formatted in HTML
     */
    private String formatName(String name, boolean colon) {
        String lastName = name;
        String firstName = "";
        if (name.contains(",")) {
            lastName = name.substring(0, name.indexOf(',')).trim();
            firstName = name.substring(name.indexOf(',') + 1).trim();
        }
        StringBuilder result = new StringBuilder();
        if (!colon && firstName.length() > 0) {
            result.append(firstName);
            result.append(' ');
        }
        result.append("<span style=\"font-variant: small-caps; \">");
        result.append(lastName);
        result.append("</span>");
        if (colon && firstName.length() > 0) {
            result.append(", ");
            result.append(firstName);
        }
        return result.toString();
    }

    /**
     * The method appendOverallTitleAndNumber() appends the series title and
     * number to the given StringBuilder.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendOverallTitleAndNumber(StringBuilder builder) {
        if (overallTitle != null || number != null) {
            builder.append(" (");
        }
        if (overallTitle != null) {
            builder.append(overallTitle);
        }
        if (overallTitle != null && number != null) {
            builder.append(' ');
        }
        if (number != null) {
            builder.append(number);
        }
        if (overallTitle != null || number != null) {
            builder.append(')');
        }
    }

    /**
     * The method appendPagerange() appends the page range to the given
     * StringBuilder.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendPagerange(StringBuilder builder) {
        if (pages != null) {
            builder.append(", ");
            builder.append(Helper.getTranslation("citation.pages"));
            builder.append(' ');
            builder.append(pages);
        }
    }

    /**
     * The method appendPlaceAndPublisher() appends the place of publication and
     * the publishing house to the given StringBuilder.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendPlaceAndPublisher(StringBuilder builder) {
        if (place != null) {
            builder.append(' ');
            builder.append(place);
        }
        if (place != null && publisher != null) {
            builder.append(" :");
        }
        if (publisher != null) {
            builder.append(' ');
            builder.append(publisher);
        }
    }

    /**
     * The method appendPublished() appends the date of publish to the given
     * StringBuilder.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendPublished(StringBuilder builder) {
        builder.append(Helper.getTranslation("citation.versionDate"));
        builder.append(' ');
        builder.append(published.toString(PUBLICATION_DATE_FORMAT));
    }

    /**
     * The method appendTitle() appends the main title to the given
     * StringBuilder. An unterminated phrase will be ended by a full stop.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendTitle(StringBuilder builder) {
        appendTitle(builder, ": ");
    }

    /**
     * The method appendTitle() appends the main title to the given
     * StringBuilder. An unterminated phrase will be ended by a full stop.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendTitle(StringBuilder builder, String prespan) {
        if (title != null) {
            if (prespan != null) {
                builder.append(prespan);
            }
            builder.append("<span style=\"font-style: italic; \">");
            builder.append(title);
            if (!(title.endsWith(".") || title.endsWith("?") || title.endsWith("!"))) {
                builder.append(".");
            }
            builder.append("</span>");
        }
    }

    /**
     * The method appendURL() appends an URL to the given StringBuilder.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendURL(StringBuilder builder) {
        if (url != null) {
            builder.append(" &lt;");
            builder.append(url);
            builder.append("&gt;");
        }
    }

    /**
     * The method appendVolumeInformation() appends volume information to the
     * given StringBuilder. An unterminated phrase will be ended by a full stop.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendVolumeInformation(StringBuilder builder) {
        if (volume != null) {
            builder.append(' ');
            builder.append(volume);
        }
        if (volume != null && volumetitle != null) {
            builder.append(':');
        }

        if (volumetitle != null) {
            builder.append(' ');
            builder.append(volumetitle);
            if (!(volumetitle.endsWith(".") || volumetitle.endsWith("?") || volumetitle.endsWith("!"))) {
                builder.append(".");
            }
        }
    }

    /**
     * The method appendYear() appends information about the year of publishing
     * to the given StringBuilder.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendYear(StringBuilder builder) {
        if (year != null) {
            builder.append(" (");
            builder.append(year);
            builder.append(')');
        }
    }

    /**
     * The method appendYear() appends information about the year of publishing
     * to the given StringBuilder.
     *
     * @param builder
     *            StringBuilder to write to
     */
    private void appendYearSimple(StringBuilder builder) {
        if (year != null) {
            builder.append(' ');
            builder.append(year);
        }
    }

}
