package de.sub.goobi.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import de.sub.goobi.helper.exceptions.UghHelperException;

public class VariableReplacer {

	private enum MetadataLevel {
		ALL, FIRSTCHILD, TOPSTRUCT;
	}

	private static final Logger logger = Logger.getLogger(VariableReplacer.class);
	
	DigitalDocument dd;
	Prefs prefs;
	UghHelper uhelp;
	//	Helper help;
	// $(meta.abc)
	private final String namespaceMeta = "\\$\\(meta\\.([\\w.]*)\\)";
	// $(abc)
	//private final String namespaceOther = "\\$\\([\\w.]*\\)";

	@SuppressWarnings("unused")
	private VariableReplacer() {
	}

	public VariableReplacer(DigitalDocument inDigitalDocument, Prefs inPrefs) {
		dd = inDigitalDocument;
		prefs = inPrefs;
		uhelp = new UghHelper();
//		help = new Helper();
	}

	/**
	 * Variablen innerhalb eines Strings ersetzen. Dabei vergleichbar zu Ant 
	 * die Variablen durchlaufen und aus dem Digital Document holen
	 * ================================================================
		 */
	public String replace(String inString) {
		if (inString==null){
			return "";
		}

		/* Metadaten aus dem DigitalDocument: entweder beim FirstChild, beim TopStruct oder 
		 * irgendwo (wobei dann vorhandene Werte des FirstChilds bevorzugt werden) */
		for (MatchResult r : findRegexMatches(namespaceMeta, inString)) {
			if (r.group(1).toLowerCase().startsWith("firstchild.")) {
				inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.FIRSTCHILD, r
					.group(1).substring(11)));
			} else if (r.group(1).toLowerCase().startsWith("topstruct.")) {
				inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.TOPSTRUCT, r
					.group(1).substring(10)));
			} else {
				inString = inString.replace(r.group(), getMetadataFromDigitalDocument(MetadataLevel.ALL, r.group(1)));
			}
		}

		/* alle anderen Ersetzungsvorgänge */
//		for (MatchResult r : Helper.findRegexMatches(namespaceOther, outString)) {
//
//		}
		return inString;
	}

	/**
	 * Metadatum von FirstChild oder TopStruct ermitteln 
	 * (vorzugsweise vom FirstChild) und zurückgeben
	 * ================================================================
	 */
	private String getMetadataFromDigitalDocument(MetadataLevel inLevel, String metadata) {
		/* TopStruct und FirstChild ermitteln */
		DocStruct topstruct = dd.getLogicalDocStruct();
		DocStruct firstchildstruct = null;
		if (topstruct.getAllChildren() != null && topstruct.getAllChildren().size() > 0)
			firstchildstruct = topstruct.getAllChildren().get(0);

		/* MetadataType ermitteln und ggf. Fehler melden */
		MetadataType mdt;
		try {
			mdt = uhelp.getMetadataType(prefs, metadata);
		} catch (UghHelperException e) {
			new Helper().setFehlerMeldung(e);
			return "";
		}

		String result = "";
		String resultTop = getMetadataValue(topstruct, mdt);
		String resultFirst = null;
		if (firstchildstruct != null) {
			resultFirst = getMetadataValue(firstchildstruct, mdt);
		}

		switch (inLevel) {
		case FIRSTCHILD:
			/* ohne vorhandenes FirstChild, kann dieses nicht zurückgegeben werden */
			if (resultFirst == null) {
				logger.warn("Can not replace firstChild-variable for METS: " + metadata);
				result="";
			//	help.setFehlerMeldung("Can not replace firstChild-variable for METS: " + metadata);
			} else
				result = resultFirst;
			break;

		case TOPSTRUCT:
			if (resultTop == null) {
				result="";
				logger.warn("Can not replace topStruct-variable for METS: " + metadata);
		//		help.setFehlerMeldung("Can not replace topStruct-variable for METS: " + metadata);
			} else
				result = resultTop;
			break;

		case ALL:
			if (resultFirst != null) {
				result = resultFirst;
			} else if (resultTop != null) {
				result = resultTop;
			} else {
				result="";
				logger.warn("Can not replace variable for METS: " + metadata);
		//		help.setFehlerMeldung("Can not replace variable for METS: " + metadata);
			}
			break;

		}
		return result;
	}

	/**
	 * Metadatum von übergebenen Docstruct ermitteln, 
	 * im Fehlerfall wird null zurückgegeben
	 * ================================================================
	 */
	private String getMetadataValue(DocStruct inDocstruct, MetadataType mdt) {
		List mds = inDocstruct.getAllMetadataByType(mdt);
		if (mds.size() > 0)
			return ((Metadata) mds.get(0)).getValue();
		else
			return null;
	}
	

	/**
	 * Suche nach regulären Ausdrücken in einem String, liefert alle gefundenen
	 * Treffer als Liste zurück
	 * ================================================================
	 */
	public static Iterable<MatchResult> findRegexMatches(String pattern, CharSequence s) {
		List<MatchResult> results = new ArrayList<MatchResult>();
		for (Matcher m = Pattern.compile(pattern).matcher(s); m.find();) {
			results.add(m.toMatchResult());
		}
		return results;
	}
}