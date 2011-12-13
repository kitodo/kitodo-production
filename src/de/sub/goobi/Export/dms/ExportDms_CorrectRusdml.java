package de.sub.goobi.Export.dms;

//TODO: Remove this class
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.UghHelperException;

public class ExportDms_CorrectRusdml {
	private Prefs myPrefs;
	private List<DocStruct> docStructsOhneSeiten;
	private Prozess myProzess;
	private DigitalDocument mydocument;
	BeanHelper bhelp = new BeanHelper();
	private static final Logger logger = Logger.getLogger(ExportDms_CorrectRusdml.class);

	public ExportDms_CorrectRusdml(Prozess inProzess, Prefs inPrefs, Fileformat inGdzfile) throws PreferencesException {
		myPrefs = inPrefs;
		mydocument = inGdzfile.getDigitalDocument();
		myProzess = inProzess;
	}

	/* =============================================================== */

	public String correctionStart() throws DocStructHasNoTypeException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException {
		String atsPpnBand;
		DocStruct logicalTopstruct = mydocument.getLogicalDocStruct();
		docStructsOhneSeiten = new ArrayList<DocStruct>();

		/*
		 * -------------------------------- Prozesseigenschaften ermitteln --------------------------------
		 */
		atsPpnBand = bhelp.WerkstueckEigenschaftErmitteln(myProzess, "ATS") + bhelp.WerkstueckEigenschaftErmitteln(myProzess, "TSL") + "_";
		String ppn = bhelp.WerkstueckEigenschaftErmitteln(myProzess, "PPN digital");
		if (!ppn.startsWith("PPN"))
			ppn = "PPN" + ppn;
		atsPpnBand += ppn;
		String bandnummer = bhelp.WerkstueckEigenschaftErmitteln(myProzess, "Band");
		if (bandnummer != null && bandnummer.length() > 0)
			atsPpnBand += "_" + bhelp.WerkstueckEigenschaftErmitteln(myProzess, "Band");

		/*
		 * -------------------------------- DocStruct rukursiv durchlaufen und die Metadaten prüfen --------------------------------
		 */
		RusdmlDocStructPagesAuswerten(logicalTopstruct);
		RusdmlPathImageFilesKorrigieren(mydocument.getPhysicalDocStruct(), "./" + atsPpnBand + "_tif");
		RusdmlAddMissingMetadata(logicalTopstruct, myProzess);

		return atsPpnBand;
	}

	/* =============================================================== */

	/**
	 * alle Strukturelemente rekursiv durchlaufen und den Elternelementen die Seiten der Kinder zuweisen
	 * 
	 * @param inStruct
	 * @throws MetadataTypeNotAllowedException
	 * @throws DocStructHasNoTypeException
	 */
	@SuppressWarnings("unchecked")
	private void RusdmlDocStructPagesAuswerten(DocStruct inStruct) throws DocStructHasNoTypeException, MetadataTypeNotAllowedException {
		// myLogger.info("Typ: " + inStruct.getType().getName());
		// DropUnusedMetadata(inStruct);
		RusdmlDropMetadata(inStruct);
		RusdmlDropPersons(inStruct);
		RusdmlUmlauteDemaskieren(inStruct);
		RusdmlCheckMetadata(inStruct);

		/* hat das Docstruct keine Bilder, wird es in die Liste genommen */
		if (inStruct.getAllToReferences().size() == 0 && !inStruct.getType().isAnchor())
			docStructsOhneSeiten.add(inStruct);

		/* alle Kinder des aktuellen DocStructs durchlaufen */
		if (inStruct.getAllChildren() != null) {
			for (Iterator iter = inStruct.getAllChildren().iterator(); iter.hasNext();) {
				DocStruct child = (DocStruct) iter.next();
				RusdmlDocStructPagesAuswerten(child);
				/* dem DocStruct alle Seiten der Kinder zuweisen */
				// inStruct.getAllReferences("to").addAll(child.getAllReferences("to"));
			}
		}
	}

	/* =============================================================== */

	/**
	 * alle nicht benötigten Metadaten des RUSDML-Projektes rauswerfen
	 * 
	 * @param inStruct
	 * @throws MetadataTypeNotAllowedException
	 * @throws DocStructHasNoTypeException
	 * @throws MetadataTypeNotAllowedException
	 * @throws DocStructHasNoTypeException
	 */

	private void RusdmlDropMetadata(DocStruct inStruct) throws DocStructHasNoTypeException, MetadataTypeNotAllowedException {
		String titelRu = "";
		String titelOther = "";
		String language = "";

		if (inStruct.getAllVisibleMetadata() != null) {
			List<Metadata> kopie = new ArrayList<Metadata>(inStruct.getAllMetadata());
			for (Metadata meta : kopie) {
				// Metadata meta = (Metadata) iter.next();

				/*
				 * -------------------------------- jetzt alle nicht benötigten Metadaten löschen --------------------------------
				 */
				if (meta.getType().getName().equals("RUSMainTitle")) {
					titelRu = meta.getValue();
					inStruct.getAllMetadata().remove(meta);
				}
				if (meta.getType().getName().equals("TitleDocMain")) {
					titelOther = meta.getValue();
					inStruct.getAllMetadata().remove(meta);
				}

				if (meta.getType().getName().equals("DocLanguage")) {
					meta.setValue(meta.getValue().toLowerCase());
					language = meta.getValue();
				}

				if (meta.getType().getName().equals("RUSPublisher"))
					inStruct.getAllMetadata().remove(meta);
				if (meta.getType().getName().equals("RUSPlaceOfPublication"))
					inStruct.getAllMetadata().remove(meta);
				if (meta.getType().getName().equals("RUSPublicationHouse"))
					inStruct.getAllMetadata().remove(meta);
				// if (meta.getType().getName().equals("RUSKeyword"))
				// inStruct.getAllMetadata().remove(meta);

				if (meta.getType().getName().equals("ZBLSource"))
					inStruct.getAllMetadata().remove(meta);
				if (meta.getType().getName().equals("ZBLIntern"))
					inStruct.getAllMetadata().remove(meta);
				if (meta.getType().getName().equals("ZBLPageNumber"))
					inStruct.getAllMetadata().remove(meta);
				if (meta.getType().getName().equals("ZBLReviewLink"))
					inStruct.getAllMetadata().remove(meta);
				if (meta.getType().getName().equals("ZBLReviewAuthor"))
					inStruct.getAllMetadata().remove(meta);
				if (meta.getType().getName().equals("ZBLCita"))
					inStruct.getAllMetadata().remove(meta);
				if (meta.getType().getName().equals("ZBLTempID"))
					inStruct.getAllMetadata().remove(meta);

				/*
				 * den Abstrakt des ZBLs übernehmen, aber nur die 255 ersten Zeichen
				 */
				if (meta.getType().getName().equals("ZBLAbstract")) {
					MetadataType mdt = myPrefs.getMetadataTypeByName("Abstract");
					meta.setType(mdt);
					if (meta.getValue().length() > 255)
						meta.setValue(meta.getValue().substring(0, 254));
				}
			}
		}

		/*
		 * -------------------------------- nachdem alle Metadaten durchlaufen wurden, jetzt abhängig vom Sprachcode den richtigen MainTitle zuweisen
		 * --------------------------------
		 */
		MetadataType mdt_org = myPrefs.getMetadataTypeByName("TitleDocMain");
		Metadata meta_org = new Metadata(mdt_org);
		MetadataType mdt_trans = myPrefs.getMetadataTypeByName("MainTitleTranslated");
		Metadata meta_trans = new Metadata(mdt_trans);
		if (language.equals("ru")) {
			meta_org.setValue(titelRu);
			meta_trans.setValue(titelOther);
		} else {
			meta_trans.setValue(titelRu);
			meta_org.setValue(titelOther);
		}

		if (meta_org.getValue() != null && meta_org.getValue().length() > 0)
			inStruct.addMetadata(meta_org);
		if (meta_trans.getValue() != null && meta_trans.getValue().length() > 0)
			inStruct.addMetadata(meta_trans);
	}

	/* =============================================================== */

	/**
	 * alle nicht benötigten Personen rauswerfen
	 * 
	 * @param inStruct
	 */
	@SuppressWarnings("unchecked")
	private void RusdmlDropPersons(DocStruct inStruct) {
		if (inStruct.getAllPersons() != null) {
			List kopie = new ArrayList(inStruct.getAllPersons());
			for (Iterator iter = kopie.iterator(); iter.hasNext();) {
				Metadata meta = (Metadata) iter.next();
				if (meta.getType().getName().equals("ZBLAuthor")) {
					inStruct.getAllPersons().remove(meta);
				}
			}
		}
	}

	/* =============================================================== */

	/**
	 * alle zu ändernden Metadaten ändern
	 * 
	 * @param inStruct
	 */
	private void RusdmlCheckMetadata(DocStruct inStruct) {
		/*
		 * -------------------------------- generell ausführen --------------------------------
		 */
		if (inStruct.getType().getName().equals("Illustration")) {
			DocStructType dst = myPrefs.getDocStrctTypeByName("Figure");
			inStruct.setType(dst);
		}
	}

	/* =============================================================== */

	private void RusdmlPathImageFilesKorrigieren(DocStruct phys, String inNeuerWert) throws ExportFileException {
		MetadataType MDTypeForPath = myPrefs.getMetadataTypeByName("pathimagefiles");
		List<? extends Metadata> alleMetadaten = phys.getAllMetadataByType(MDTypeForPath);
		if (alleMetadaten.size() > 0) {
			for (Metadata meta : alleMetadaten) {
				// Metadata meta = (Metadata) iter.next();
				meta.setValue(inNeuerWert);
			}
		} else
			throw new ExportFileException("Exportfehler: Imagepfad noch nicht gesetzt");
	}

	/* =============================================================== */

	/**
	 * dabei die zentralen Projekteinstellungen in der xml-Konfiguration ber�cksichtigen
	 * 
	 * @param inTopStruct
	 * @param myProzess
	 * @throws ExportFileException
	 * @throws UghHelperException
	 * @throws DocStructHasNoTypeException
	 * @throws MetadataTypeNotAllowedException
	 */
	private void RusdmlAddMissingMetadata(DocStruct inTopStruct, Prozess myProzess) throws ExportFileException, UghHelperException {
		/*
		 * -------------------------------- bei fehlender digitaler PPN: Fehlermeldung und raus --------------------------------
		 */
		String PPN = bhelp.WerkstueckEigenschaftErmitteln(myProzess, "PPN digital");
		if (PPN.length() == 0)
			throw new ExportFileException("Exportfehler: Keine PPN digital vorhanden");
		RusdmlAddMissingMetadata(inTopStruct, myProzess, PPN);

		// /* --------------------------------
		// * jetzt die fehlenden projektspezifischen Metadaten erg�nzen
		// * --------------------------------*/
		// List alleMissingDaten = cp.getParamList("dmsImport.add");
		// /* standard */
		// if (alleMissingDaten.contains("standard")) {
		// addMissingMetadata_standard(inTopStruct, myProzess);
		// }
		// /* rusdml */
		// if (alleMissingDaten.contains("rusdml")) {
		// addMissingMetadata_Rusdml(inTopStruct, myProzess, PPN);
		// }
		// /* creatorsAll */
		// if (alleMissingDaten.contains("creatorsAll")) {
		// addMissingMetadata_creatorsAll(inTopStruct, myProzess);
		// }

	}

	/* =============================================================== */

	// /**
	// * Metadaten über alle Autoren für oberstes Strukturelement
	// * @param inTopStruct
	// * @param myProzess
	// * @throws ExportFileException
	// * @throws UghHelperException
	// */
	// private void addMissingMetadata_creatorsAll(DocStruct inTopStruct,
	// Prozess myProzess)
	// throws ExportFileException, UghHelperException {
	// MetadataType mdt = ughHelp.getMetadataType(myPrefs, "CreatorsAllOrigin");
	//
	// /* --------------------------------
	// * wenn das Feld CreatorsAllOrigin schon existiert, sofort raus
	// * --------------------------------*/
	// if (inTopStruct.getAllMetadataByType(mdt) != null)
	// return;
	//
	// /* --------------------------------
	// * Metadaten erzeugen
	// * --------------------------------*/
	// Metadata mdCreators = new Metadata();
	// mdCreators.setType(mdt);
	// mdCreators.setValue("");
	//
	// /* --------------------------------
	// * alle beteiligten Personen durchlaufen und in das Feld übernehmen
	// * --------------------------------*/
	// if (inTopStruct.getAllPersons() != null) {
	// for (Iterator iter = inTopStruct.getAllPersons().iterator();
	// iter.hasNext();) {
	// Person p = (Person) iter.next();
	// String tempname = (mdCreators.getValue().length() > 0 ? "; " : "");
	// tempname += p.getLastname() + ", " + p.getFirstname();
	// mdCreators.setValue(mdCreators.getValue() + tempname);
	// }
	// }
	//
	// /* --------------------------------
	// * das Metadatum dem TopStruct zuweisen
	// * --------------------------------*/
	// try {
	// inTopStruct.addMetadata(mdCreators);
	// } catch (Exception e) {
	// throw new ExportFileException(e.getMessage());
	// }
	// }
	/* =============================================================== */

	// /**
	// * Metadaten über alle Autoren für oberstes Strukturelement
	// * @param inTopStruct
	// * @param myProzess
	// * @throws ExportFileException
	// * @throws UghHelperException
	// */
	// private void addMissingMetadata_standard(DocStruct inTopStruct, Prozess
	// myProzess)
	// throws ExportFileException, UghHelperException {
	//
	// /* --------------------------------
	// * Metadatentypen ermitteln
	// * --------------------------------*/
	// MetadataType mdt_PublicationYear = ughHelp.getMetadataType(myPrefs,
	// "PublicationYear");
	// MetadataType mdt_PublisherName = ughHelp.getMetadataType(myPrefs,
	// "PublisherName");
	// MetadataType mdt_PlaceOfPublication = ughHelp.getMetadataType(myPrefs,
	// "PlaceOfPublication");
	// MetadataType mdt_CatalogIDSource = ughHelp.getMetadataType(myPrefs,
	// "CatalogIDSource");
	// MetadataType mdt_copyrightimageset = ughHelp.getMetadataType(myPrefs,
	// "copyrightimageset");
	// MetadataType mdt_imagedescr = ughHelp.getMetadataType(myPrefs,
	// "imagedescr");
	// MetadataType mdt_shelfmarkarchiveimageset = ughHelp
	// .getMetadataType(myPrefs, "shelfmarkarchiveimageset");
	// MetadataType mdt_mediumsource = ughHelp.getMetadataType(myPrefs,
	// "mediumsource");
	//
	// /* --------------------------------
	// * Metadaten erzeugen für logical Topstruct
	// * --------------------------------*/
	// Metadata md_PublicationYear = new Metadata();
	// md_PublicationYear.setType(mdt_PublicationYear);
	// md_PublicationYear.setValue(WerkstueckEigenschaftErmitteln(myProzess,
	// "Erscheinungsjahr"));
	// Metadata md_PublisherName = new Metadata();
	// md_PublisherName.setType(mdt_PublisherName);
	// md_PublisherName.setValue(WerkstueckEigenschaftErmitteln(myProzess,
	// "Verlag"));
	// Metadata md_PlaceOfPublication = new Metadata();
	// md_PlaceOfPublication.setType(mdt_PlaceOfPublication);
	// md_PlaceOfPublication.setValue(WerkstueckEigenschaftErmitteln(myProzess,
	// "Erscheinungsort"));
	// Metadata md_CatalogIDSource = new Metadata();
	// md_CatalogIDSource.setType(mdt_CatalogIDSource);
	// md_CatalogIDSource.setValue(ScanvorlagenEigenschaftErmitteln(myProzess,
	// "PPN analog"));
	// if (!md_CatalogIDSource.getValue().startsWith("PPN"))
	// md_CatalogIDSource.setValue("PPN" + md_CatalogIDSource.getValue());
	//
	// /* --------------------------------
	// * Metadaten erzeugen für physical Topstruct
	// * --------------------------------*/
	// Metadata md_copyrightimageset = new Metadata();
	// md_copyrightimageset.setType(mdt_copyrightimageset);
	// md_copyrightimageset.setValue(WerkstueckEigenschaftErmitteln(myProzess,
	// "Artist"));
	// Metadata md_imagedescr = new Metadata();
	// md_imagedescr.setType(mdt_imagedescr);
	// TiffHeader tiff = new TiffHeader(myProzess);
	// md_imagedescr.setValue(tiff.getImageDescription());
	// Metadata md_shelfmarkarchiveimageset = new Metadata();
	// md_shelfmarkarchiveimageset.setType(mdt_shelfmarkarchiveimageset);
	// md_shelfmarkarchiveimageset.setValue(ScanvorlagenEigenschaftErmitteln(myProzess,
	// "Signatur"));
	// Metadata md_mediumsource = new Metadata();
	// md_mediumsource.setType(mdt_mediumsource);
	// md_mediumsource.setValue("Book");
	//
	// /* --------------------------------
	// * die Metadaten dem TopStruct zuweisen
	// * --------------------------------*/
	// try {
	// if (inTopStruct.getAllMetadataByType(mdt_PublicationYear) == null)
	// inTopStruct.addMetadata(md_PublicationYear);
	// if (inTopStruct.getAllMetadataByType(mdt_PublisherName) == null)
	// inTopStruct.addMetadata(md_PublisherName);
	// if (inTopStruct.getAllMetadataByType(mdt_PlaceOfPublication) == null)
	// inTopStruct.addMetadata(md_PlaceOfPublication);
	// if (inTopStruct.getAllMetadataByType(mdt_CatalogIDSource) == null)
	// inTopStruct.addMetadata(md_CatalogIDSource);
	// } catch (Exception e) {
	// throw new ExportFileException(e.getMessage());
	// }
	//
	// /* --------------------------------
	// * die Metadaten dem BoundBook zuweisen
	// * --------------------------------*/
	// DocStruct boundBook = mydocument.getPhysicalDocStruct();
	// try {
	// if (boundBook.getAllMetadataByType(mdt_copyrightimageset) == null)
	// boundBook.addMetadata(md_copyrightimageset);
	// if (boundBook.getAllMetadataByType(mdt_imagedescr) == null)
	// boundBook.addMetadata(md_imagedescr);
	// if (boundBook.getAllMetadataByType(mdt_shelfmarkarchiveimageset) == null)
	// boundBook.addMetadata(md_shelfmarkarchiveimageset);
	// if (boundBook.getAllMetadataByType(mdt_mediumsource) == null)
	// boundBook.addMetadata(md_mediumsource);
	// } catch (Exception e) {
	// throw new ExportFileException(e.getMessage());
	// }
	// }
	/* =============================================================== */

	// /**
	// * alle nicht benötigten Metadaten rauswerfen
	// * @param inStruct
	// * @throws MetadataTypeNotAllowedException
	// * @throws DocStructHasNoTypeException
	// */
	// @SuppressWarnings("unchecked")
	// private void DropUnusedMetadata(DocStruct inStruct) throws
	// DocStructHasNoTypeException,
	// MetadataTypeNotAllowedException {
	//    
	//      
	// if (inStruct.getAllVisibleMetadata() != null) {
	// List alleDropDaten = cp.getParamList("dmsImport.drop");
	// if (alleDropDaten.contains("rusdml")) {
	// DropMetadata_Rusdml(inStruct);
	// DropPersons_Rusdml(inStruct);
	// }
	// }
	// }
	/* =============================================================== */

	/**
	 * Fehlende Metadaten für Rusdml erg�nzen
	 * 
	 * @param inTopStruct
	 * @param myProzess
	 * @param PPN
	 */
	private void RusdmlAddMissingMetadata(DocStruct inTopStruct, Prozess myProzess, String PPN) {
		/*
		 * -------------------------------- Eigenschaften aus dem Werkstück holen --------------------------------
		 */
		String Titel = bhelp.WerkstueckEigenschaftErmitteln(myProzess, "Haupttitel");
		String Verlag = bhelp.WerkstueckEigenschaftErmitteln(myProzess, "Verlag");
		String Ort = bhelp.WerkstueckEigenschaftErmitteln(myProzess, "Erscheinungsort");
		String ISSN = bhelp.WerkstueckEigenschaftErmitteln(myProzess, "ISSN");
		String BandNummer = bhelp.WerkstueckEigenschaftErmitteln(myProzess, "Band");

		/*
		 * -------------------------------- die Metadaten erzeugen --------------------------------
		 */
		Metadata mdVerlag = null;
		Metadata mdOrt = null;
		Metadata mdISSN = null;
		Metadata mdPPN = null;
		Metadata mdPPNBand = null;
		Metadata mdSorting = null;
		try {
			Metadata mdTitel = new Metadata(myPrefs.getMetadataTypeByName("TitleDocMain"));
			mdTitel.setValue(Titel);
			mdVerlag = new Metadata(myPrefs.getMetadataTypeByName("PublisherName"));
			mdVerlag.setValue(Verlag);
			mdOrt = new Metadata(myPrefs.getMetadataTypeByName("PlaceOfPublication"));
			mdOrt.setValue(Ort);
			mdISSN = new Metadata(myPrefs.getMetadataTypeByName("ISSN"));
			mdISSN.setValue(ISSN);
			mdPPN = new Metadata(myPrefs.getMetadataTypeByName("CatalogIDDigital"));
			mdPPN.setValue("PPN" + PPN);
			mdPPNBand = new Metadata(myPrefs.getMetadataTypeByName("CatalogIDDigital"));
			mdPPNBand.setValue("PPN" + PPN + "_" + BandNummer);
			mdSorting = new Metadata(myPrefs.getMetadataTypeByName("CurrentNoSorting"));
		} catch (MetadataTypeNotAllowedException e1) {
			logger.error(e1);
		}
		try {
			int BandInt = Integer.parseInt(BandNummer) * 10;
			mdSorting.setValue(String.valueOf(BandInt));
		} catch (NumberFormatException e) {
		}

		/*
		 * -------------------------------- die Metadaten der Zeitschrift zuweisen --------------------------------
		 */
		inTopStruct.getAllMetadataByType(myPrefs.getMetadataTypeByName("TitleDocMain")).get(0).setValue(Titel);

		try {
			inTopStruct.addMetadata(mdVerlag);
			inTopStruct.addMetadata(mdOrt);
			inTopStruct.addMetadata(mdPPN);
			inTopStruct.addMetadata(mdISSN);
		} catch (Exception e) {
		}

		/*
		 * -------------------------------- die Metadaten dem Band zuweisen --------------------------------
		 */
		DocStruct structBand = inTopStruct.getAllChildren().get(0);
		if (structBand != null) {
			try {
				structBand.addMetadata(mdVerlag);
				structBand.addMetadata(mdOrt);
				structBand.addMetadata(mdPPNBand);
				structBand.addMetadata(mdSorting);
			} catch (Exception e) {
			}

		}
	}

	/* =============================================================== */

	/**
	 * Alle Metadaten eines Strukturelements durchlaufen und deren Umlaute maskieren
	 * 
	 * @param inStruct
	 */

	private void RusdmlUmlauteDemaskieren(DocStruct inStruct) {
		List<Metadata> kopie = inStruct.getAllMetadata();
		if (kopie != null) {
			for (Metadata meta : kopie) {
				// Metadata meta = (Metadata) iter.next();
				/* in den Metadaten die Umlaute entfernen */
				RusdmlUmlauteDemaskieren1(meta);
			}
		}
	}

	/* =============================================================== */

	private void RusdmlUmlauteDemaskieren1(Metadata meta) {
		String neuerWert = meta.getValue();
		if (neuerWert == null)
			return;
		neuerWert = neuerWert.replaceAll("\\\\star", "\u002a");
		neuerWert = neuerWert.replaceAll("\\\\times", "\u00d7");
		neuerWert = neuerWert.replaceAll("\\\\div", "\u00f7");
		neuerWert = neuerWert.replaceAll("\\\\dot G", "\u0120");
		neuerWert = neuerWert.replaceAll("\\\\Gamma", "\u0393");
		neuerWert = neuerWert.replaceAll("\\\\Delta", "\u00394");
		neuerWert = neuerWert.replaceAll("\\\\Lambda", "\u039b");
		neuerWert = neuerWert.replaceAll("\\\\Sigma", "\u03a3");
		neuerWert = neuerWert.replaceAll("\\\\Omega", "\u03a9");
		neuerWert = neuerWert.replaceAll("\\\\alpha", "\u03b1");
		neuerWert = neuerWert.replaceAll("\\\\beta", "\u03b2");
		neuerWert = neuerWert.replaceAll("\\\\gamma", "\u03b3");
		neuerWert = neuerWert.replaceAll("\\\\delta", "\u002a");
		neuerWert = neuerWert.replaceAll("\\\\epsilon", "\u03b5");
		neuerWert = neuerWert.replaceAll("\\\\zeta", "\u03b6");
		neuerWert = neuerWert.replaceAll("\\\\eta", "\u03b7");
		neuerWert = neuerWert.replaceAll("\\\\theta", "\u03b8");
		neuerWert = neuerWert.replaceAll("\\\\lambda", "\u03bb");
		neuerWert = neuerWert.replaceAll("\\\\mu", "\u03bc");
		neuerWert = neuerWert.replaceAll("\\\\nu", "\u03bd");
		neuerWert = neuerWert.replaceAll("\\\\pi", "\u03c0");
		neuerWert = neuerWert.replaceAll("\\\\sigma", "\u03c3");
		neuerWert = neuerWert.replaceAll("\\\\phi", "\u03c6");
		neuerWert = neuerWert.replaceAll("\\\\omega", "\u03c9");
		neuerWert = neuerWert.replaceAll("\\\\ell", "\u2113");
		neuerWert = neuerWert.replaceAll("\\\\rightarrow", "\u2192");
		neuerWert = neuerWert.replaceAll("\\\\sim", "\u223c");
		neuerWert = neuerWert.replaceAll("\\\\le", "\u2264");
		neuerWert = neuerWert.replaceAll("\\\\ge", "\u2265");
		neuerWert = neuerWert.replaceAll("\\\\odot", "\u2299");
		neuerWert = neuerWert.replaceAll("\\\\infty", "\u221e");
		neuerWert = neuerWert.replaceAll("\\\\circ", "\u2218");
		neuerWert = neuerWert.replaceAll("\\\\dot\\{P\\}", "\u1e56");
		neuerWert = neuerWert.replaceAll("\\\\symbol\\{94\\}", "\u005e");
		neuerWert = neuerWert.replaceAll("\\\\symbol\\{126\\}", "\u007e");
		neuerWert = neuerWert.replaceAll("\\\\u g", "\u011f");
		neuerWert = neuerWert.replaceAll("\\\\AE ", "\u00c6");
		neuerWert = neuerWert.replaceAll("\\\\ae ", "\u00e6");
		neuerWert = neuerWert.replaceAll("\\\\oe ", "\u0153");
		neuerWert = neuerWert.replaceAll("\\\\OE ", "\u0152");
		neuerWert = neuerWert.replaceAll("\\\\uu ", "u");
		neuerWert = neuerWert.replaceAll("\\\\UU ", "U");
		neuerWert = neuerWert.replaceAll("\\\\Dj ", "Dj");
		neuerWert = neuerWert.replaceAll("\\\\dj ", "dj");
		neuerWert = neuerWert.replaceAll("\\\\c\\{c\\}", "\u00e7");
		neuerWert = neuerWert.replaceAll("\\\\c c", "\u00e7");
		neuerWert = neuerWert.replaceAll("\\\\c\\{C\\}", "\u00c7");
		neuerWert = neuerWert.replaceAll("\\\\c C", "\u00c7");
		// NOTE The following one only for schummling and correcting errors!
		neuerWert = neuerWert.replaceAll("\\{\\\\ss \\}", "\u00dF");
		neuerWert = neuerWert.replaceAll("\\{\\\\ss\\}", "\u00dF");
		neuerWert = neuerWert.replaceAll("\\{\\\\ss\\}", "\u00dF");
		neuerWert = neuerWert.replaceAll("\\\\ss ", "\u00df");
		neuerWert = neuerWert.replaceAll("\\\\aa ", "\u00e5");
		neuerWert = neuerWert.replaceAll("\\\\AA ", "\u00c5");
		neuerWert = neuerWert.replaceAll("\\\\dh ", "\u00f0");
		neuerWert = neuerWert.replaceAll("\\\\th ", "\u00fe");
		neuerWert = neuerWert.replaceAll("\\\\'a", "á");
		neuerWert = neuerWert.replaceAll("\\\\'A", "Á");
		neuerWert = neuerWert.replaceAll("\\\\`a", "à");
		neuerWert = neuerWert.replaceAll("\\\\`A", "À");
		neuerWert = neuerWert.replaceAll("\\\\\\^a", "â");
		neuerWert = neuerWert.replaceAll("\\\\\\^A", "Â");
		neuerWert = neuerWert.replaceAll("\\\\~a", "\u00e3");
		neuerWert = neuerWert.replaceAll("\\\\~A", "\u00c3");
		neuerWert = neuerWert.replaceAll("\\\\\\\"A", "Ä");
		neuerWert = neuerWert.replaceAll("\\\\\\\"a", "ä");
		neuerWert = neuerWert.replaceAll("\\\\'e", "é");
		neuerWert = neuerWert.replaceAll("\\\\'E", "É");
		neuerWert = neuerWert.replaceAll("\\\\`e", "è");
		neuerWert = neuerWert.replaceAll("\\\\`E", "È");
		neuerWert = neuerWert.replaceAll("\\\\\\^e", "e");
		neuerWert = neuerWert.replaceAll("\\\\\\^E", "Ê");
		neuerWert = neuerWert.replaceAll("\\\\\\\"E", "\u00cb");
		neuerWert = neuerWert.replaceAll("\\\\\\\"e", "\u00eb");
		neuerWert = neuerWert.replaceAll("\\\\'i", "í");
		neuerWert = neuerWert.replaceAll("\\\\'I", "Í");
		neuerWert = neuerWert.replaceAll("\\\\`i", "ì");
		neuerWert = neuerWert.replaceAll("\\\\`I", "Ì");
		neuerWert = neuerWert.replaceAll("\\\\\\^i", "î");
		neuerWert = neuerWert.replaceAll("\\\\\\^I", "Î");
		neuerWert = neuerWert.replaceAll("\\\\\\\"I", "\u00cf");
		neuerWert = neuerWert.replaceAll("\\\\\\\"i", "\u00ef");
		neuerWert = neuerWert.replaceAll("\\\\~n", "\u00f1");
		neuerWert = neuerWert.replaceAll("\\\\~N", "\u00d1");
		neuerWert = neuerWert.replaceAll("\\\\'o", "ó");
		neuerWert = neuerWert.replaceAll("\\\\'O", "Ó");
		neuerWert = neuerWert.replaceAll("\\\\`o", "ò");
		neuerWert = neuerWert.replaceAll("\\\\`O", "Ò");
		neuerWert = neuerWert.replaceAll("\\\\\\^o", "ô");
		neuerWert = neuerWert.replaceAll("\\\\\\^O", "Ô");
		neuerWert = neuerWert.replaceAll("\\\\~o", "\u00f5");
		neuerWert = neuerWert.replaceAll("\\\\~O", "\u00d5");
		neuerWert = neuerWert.replaceAll("\\\\\\\"O", "Ö");
		neuerWert = neuerWert.replaceAll("\\\\\\\"o", "ö");
		neuerWert = neuerWert.replaceAll("\\\\'u", "ú");
		neuerWert = neuerWert.replaceAll("\\\\'U", "Ú");
		neuerWert = neuerWert.replaceAll("\\\\`u", "ù");
		neuerWert = neuerWert.replaceAll("\\\\`U", "Ù");
		neuerWert = neuerWert.replaceAll("\\\\\\^u", "û");
		neuerWert = neuerWert.replaceAll("\\\\\\^U", "Û");
		neuerWert = neuerWert.replaceAll("\\\\\"U", "Ü");
		neuerWert = neuerWert.replaceAll("\\\\\"u", "ü");
		neuerWert = neuerWert.replaceAll("\\\\'y", "ý");
		neuerWert = neuerWert.replaceAll("\\\\'Y", "Ý");
		neuerWert = neuerWert.replaceAll("\\\\\\\"y", "\u00ff");
		neuerWert = neuerWert.replaceAll("\\\\H ", "\"");
		neuerWert = neuerWert.replaceAll("\\\\O", "\u00d8");
		neuerWert = neuerWert.replaceAll("\\\\o", "\u00f8");
		neuerWert = neuerWert.replaceAll("\\\\'C", "\u0106");
		neuerWert = neuerWert.replaceAll("\\\\'c", "\u0107");
		neuerWert = neuerWert.replaceAll("\\\\v C", "\u010c");
		neuerWert = neuerWert.replaceAll("\\\\v c", "\u010d");
		neuerWert = neuerWert.replaceAll("\\\\v S", "\u0160");
		neuerWert = neuerWert.replaceAll("\\\\v s", "\u0161");
		neuerWert = neuerWert.replaceAll("\\\\v Z", "\u017d");
		neuerWert = neuerWert.replaceAll("\\\\v z", "\u017e");
		neuerWert = neuerWert.replaceAll("\\\\v r", "\u0159");
		neuerWert = neuerWert.replaceAll("\\\\'s", "\u015b");
		neuerWert = neuerWert.replaceAll("\\\\'S", "\u015a");
		neuerWert = neuerWert.replaceAll("\\\\L", "\u0141");
		neuerWert = neuerWert.replaceAll("\\\\l", "\u0142");
		neuerWert = neuerWert.replaceAll("\\\\'N", "\u0143");
		neuerWert = neuerWert.replaceAll("\\\\'n", "\u0144");
		neuerWert = neuerWert.replaceAll("\\\\'t", "\u0165");
		neuerWert = neuerWert.replaceAll("\\\\=u", "\u016b");
		neuerWert = neuerWert.replaceAll("\\\\'z", "\u017a");
		neuerWert = neuerWert.replaceAll("\\\\.Z", "\u017b");
		neuerWert = neuerWert.replaceAll("\\\\.z", "\u017c");
		neuerWert = neuerWert.replaceAll("\\\\#", "\u0023");
		neuerWert = neuerWert.replaceAll("\\\\%", "\u0025");
		neuerWert = neuerWert.replaceAll("\\\\_", "\u005f");
		neuerWert = neuerWert.replaceAll("\\\\~ ", " ");
		neuerWert = neuerWert.replaceAll("\\\\=", "");

		meta.setValue(neuerWert);
	}
}
