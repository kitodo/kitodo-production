package org.goobi.production.model.bibliography;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.sharkysoft.util.UnreachableCodeException;

public class Citation {

	private enum Type {
		MONOGRAPH, ANTHOLOGY, PERIODICAL, THESIS, STANDARD, INTERNET
	}

	private static final DateTimeFormatter accessTimeFormatter = DateTimeFormat.forPattern("d. MMMM. yyyy HH:mm ZZ");
	private static final DateTimeFormatter publicationTimeFormatter = DateTimeFormat.forPattern("d. MMMM. yyyy");
	private final Type style;
	private DateTime accessed;
	private final List<String> contributors = new ArrayList<String>();
	private final List<String> creators = new ArrayList<String>();
	private String department;
	private String dependentTitle;
	private String edition;
	private String employer;
	private String number;
	private String overallTitle;
	private String pages;
	private String part;
	private String place;
	private LocalDate published;
	private String publisher;
	private String subseries;
	private String title;
	private String type;
	private String url;
	private String volume;
	private String volumetitle;
	private Integer year;

	public Citation(String format) {
		Type type;
		try {
			type = Type.valueOf(format.toUpperCase());
		} catch (IllegalArgumentException formatCodeUnknown) {
			type = Type.MONOGRAPH;
		} catch (NullPointerException formatIsNull) {
			type = Type.MONOGRAPH;
		}
		style = type;
	}

	public void addMultipleAuthors(String creators, String separatedBy) {
		addMultiple(creators, separatedBy, this.creators);
	}

	public void addMultipleContributors(String contributors, String separatedBy) {
		addMultiple(contributors, separatedBy, this.contributors);
	}

	private void addMultiple(String list, String separator, List<String> destination) {
		if (list == null || list.trim().length() == 0)
			return;
		while (list.indexOf(separator) > -1) {
			if (list.substring(0, list.indexOf(separator)).trim().length() > 0) {
				destination.add(list.substring(0, list.indexOf(separator)).trim());
			}
			list = list.substring(list.indexOf(separator) + 1);
		}
		if (list.trim().length() > 0) {
			destination.add(list.trim());
		}
	}

	public void setAccessTime(DateTime accessed) {
		this.accessed = accessed;
	}

	public void setArticleTitle(String title) {
		if ("".equals(title))
			title = null;
		this.dependentTitle = title;
	}

	public void setDepartment(String department) {
		if ("".equals(department))
			department = null;
		this.department = department;
	}

	public void setEdition(String edition) {
		if ("".equals(edition))
			edition = null;
		this.edition = edition;
	}

	public void setEmployer(String employer) {
		if ("".equals(employer))
			employer = null;
		this.employer = employer;
	}

	public void setNumber(String number) {
		if ("".equals(number))
			number = null;
		this.number = number;
	}

	public void setOverallTitle(String title) {
		if ("".equals(title))
			title = null;
		this.overallTitle = title;
	}

	public void setPages(String pages) {
		if ("".equals(pages))
			pages = null;
		this.pages = pages;
	}

	public void setPart(String part) {
		if ("".equals(part))
			part = null;
		this.part = part;
	}

	public void setPlace(String place) {
		if ("".equals(place))
			place = null;
		this.place = place;
	}

	public void setPublicationDate(LocalDate date) {
		this.published = date;
	}

	public void setPublisher(String publisher) {
		if ("".equals(publisher))
			publisher = null;
		this.publisher = publisher;
	}

	public void setSubseries(String subseries) {
		if ("".equals(subseries))
			subseries = null;
		this.subseries = subseries;
	}

	public void setTitle(String title) {
		if ("".equals(title))
			title = null;
		this.title = title;
	}

	public void setType(String type) {
		if ("".equals(type))
			type = null;
		this.type = type;
	}

	public void setURL(String url) {
		if ("".equals(url))
			url = null;
		this.url = url;
	}

	public void setVolume(String volume) {
		if ("".equals(volume))
			volume = null;
		this.volume = volume;
	}

	public void setVolumeTitle(String title) {
		if ("".equals(title))
			title = null;
		this.volumetitle = title;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public String toHTML() {
		StringBuilder result = new StringBuilder();
		switch (style) {
		case MONOGRAPH:
			appendNames(creators, result);
			appendYear(result);
			appendTitle(result);
			appendVolumeInformation(result);
			appendEdition(result, true);
			appendPlaceAndPublisher(result);
			appendOverallTitleAndNumber(result);
			break;
		case ANTHOLOGY:
			appendNames(creators, result);
			appendYear(result);
			appendArticle(result);
			result.append(" In: ");
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
			result.append(" In: ");
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
				result.append(published.toString(publicationTimeFormatter));
				result.append(')');
			}
			if (number != null) {
				result.append(' ');
				result.append(number);
			}
			appendPagerange(result);
			if (accessed != null) {
				result.append(" (Zugriff: ");
				result.append(accessed.toString(accessTimeFormatter));
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
			if (department != null && (employer != null || place != null))
				result.append(',');
			if (department != null) {
				result.append(' ');
				result.append(department);
			}
			if (type != null && (employer != null || place != null || department != null))
				result.append(',');
			if (type != null) {
				result.append(' ');
				result.append(type);
			}
			break;
		case STANDARD:
			if (number != null)
				result.append(number);
			appendEdition(result, false);
			appendTitle(result);
			break;
		case INTERNET:
			appendNames(creators, result);
			appendYear(result);
			appendTitle(result);
			if (published != null || accessed != null) {
				result.append(" (");
				if (published != null) {
					result.append("Stand: ");
					result.append(published.toString(publicationTimeFormatter));
				}
				if (published != null && accessed != null)
					result.append(", ");
				if (accessed != null) {
					result.append("Zugriff: ");
					result.append(accessed.toString(accessTimeFormatter));
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

	private void appendArticle(StringBuilder builder) {
		if (dependentTitle != null) {
			builder.append(": „");
			builder.append(dependentTitle);
			if (!(dependentTitle.endsWith(".") || dependentTitle.endsWith("?") || dependentTitle.endsWith("!")))
				builder.append(".");
			builder.append('“');
		}
	}

	private void appendEdition(StringBuilder builder, boolean sentenceMark) {
		if (edition != null) {
			builder.append(' ');
			builder.append(edition);
			if (sentenceMark && !(edition.endsWith(".") || edition.endsWith("?") || edition.endsWith("!")))
				builder.append(".");
		}
	}

	private void appendNames(List<String> names, StringBuilder builder) {
		if (names != null && names.size() > 0) {
			builder.append(formatName(names.get(0), true));
			for (int i = 1; i < names.size(); i++) {
				builder.append(" ; ");
				builder.append(formatName(names.get(i), false));
			}
		}
	}

	private String formatName(String s, boolean colon) {
		String lastname = s;
		String firstname = "";
		if (s.indexOf(",") > -1) {
			lastname = s.substring(0, s.indexOf(",")).trim();
			firstname = s.substring(s.indexOf(",") + 1).trim();
		}
		StringBuilder result = new StringBuilder();
		if (!colon && firstname.length() > 0) {
			result.append(firstname);
			result.append(' ');
		}
		result.append("<span style=\"font-variant: small-caps; \">");
		result.append(lastname);
		result.append("</span>");
		if (colon && firstname.length() > 0) {
			result.append(", ");
			result.append(firstname);
		}
		return result.toString();
	}

	private void appendOverallTitleAndNumber(StringBuilder builder) {
		if (overallTitle != null || number != null)
			builder.append(" (");
		if (overallTitle != null)
			builder.append(overallTitle);
		if (overallTitle != null && number != null)
			builder.append(' ');
		if (number != null)
			builder.append(number);
		if (overallTitle != null || number != null)
			builder.append(')');
	}

	private void appendPagerange(StringBuilder builder) {
		if (pages != null) {
			builder.append(", Seiten ");
			builder.append(pages);
		}
	}

	private void appendPlaceAndPublisher(StringBuilder builder) {
		if (place != null) {
			builder.append(' ');
			builder.append(place);
		}
		if (place != null && publisher != null)
			builder.append(" :");
		if (publisher != null) {
			builder.append(' ');
			builder.append(publisher);
		}
	}

	private void appendTitle(StringBuilder builder) {
		if (title != null) {
			builder.append(": <span style=\"font-style: italic; \">");
			builder.append(title);
			if (!(title.endsWith(".") || title.endsWith("?") || title.endsWith("!")))
				builder.append(".");
			builder.append("</span>");
		}
	}

	private void appendURL(StringBuilder builder) {
		if (url != null) {
			builder.append(" &lt;");
			builder.append(url);
			builder.append("&gt;");
		}
	}

	private void appendVolumeInformation(StringBuilder builder) {
		if (volume != null) {
			builder.append(' ');
			builder.append(volume);
		}
		if (volume != null && volumetitle != null)
			builder.append(':');

		if (volumetitle != null) {
			builder.append(' ');
			builder.append(volumetitle);
			if (!(volumetitle.endsWith(".") || volumetitle.endsWith("?") || volumetitle.endsWith("!")))
				builder.append(".");
		}
	}

	private void appendYear(StringBuilder builder) {
		if (year != null) {
			builder.append(" (");
			builder.append(year);
			builder.append(')');
		}
	}

	@Override
	public String toString() {
		return toHTML().replaceAll("</?span.*?>", "");
	}

}
