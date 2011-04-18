package org.goobi.production.search.lucene;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - http://gdz.sub.uni-goettingen.de - http://www.intranda.com
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.goobi.production.search.api.IIndexer;

import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Prozesseigenschaft;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Beans.Schritteigenschaft;
import de.sub.goobi.Beans.Vorlage;
import de.sub.goobi.Beans.Vorlageeigenschaft;
import de.sub.goobi.Beans.Werkstueck;
import de.sub.goobi.Beans.Werkstueckeigenschaft;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;

/**
 * 
 * This class implements the IIndexer interface for Lucene.
 * 
 * @author Robert Sehr
 * 
 */
public class LuceneIndex implements IIndexer {
	private static final Logger logger = Logger.getLogger(LuceneIndex.class);
	// Helper help = new Helper();
	static RAMDirectory ramDir = null;
	private static String index_path = "";
//	private static String analyser_path = "";

	static Analyzer analyser;
	private static LuceneIndex li;
	private static MaxFieldLength mfl = new MaxFieldLength(1000);
	private static IndexWriter iwriter;
	private Version luceneVersion = Version.LUCENE_29;
	
//	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyymmdd");

	/**
	 * Constructor, reads configuration from main configuration file
	 */

	private LuceneIndex() {
//		analyser_path = ConfigMain.getParameter("analyser", "GermanAnalyser");
		index_path = ConfigMain.getParameter("index_path", new Helper().getGoobiDataDirectory());
//		if (analyser_path == "GermanAnalyser") {
//			analyser = new GermanAnalyzer(luceneVersion);
//		} else {
			analyser = new StandardAnalyzer(luceneVersion);
//		}
	}

	/**
	 * Constructor
	 * 
	 * @return LuceneIndex returns instance of LuceneIndex
	 */
	public static LuceneIndex initialize() {
		if (li == null) {
			li = new LuceneIndex();
		}
		return li;
	}

	/**
	 * open a connection to the index files
	 * 
	 * @param createNew
	 *            if true, the old index will be removed and a new one will be created, else the old one will be used
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	public static void openLuceneIndex(boolean createNew) throws CorruptIndexException, LockObtainFailedException, IOException {
		iwriter = new IndexWriter(FSDirectory.open(new File(index_path)), analyser, true, mfl);
	}

	/**
	 * closes the connection to the index files
	 * 
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public static void closeIndex() throws CorruptIndexException, IOException {
		iwriter.close();
	}

	/*
	 * creates a new document for lucene
	 */

	private static Document getNewDocument(Prozess process) {

		Document doc = new Document();
		// if (!process.getProjekt().getTitel().contains("Altdaten")) {
		// nessesary to remove filter
		doc.add(new Field("all", "true", Field.Store.YES, Field.Index.NOT_ANALYZED));
		// name information
		if (process.getId() != null) {
			doc.add(new Field(SearchEnums.processId.getLuceneTitle(), String.valueOf(process.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
		if (process.getTitel() != null) {
			doc
					.add(new Field(SearchEnums.processTitle.getLuceneTitle(), process.getTitel().toLowerCase(), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
		}
		if (process.isIstTemplate()) {
			doc.add(new Field(SearchEnums.template.getLuceneTitle(), "true", Field.Store.YES, Field.Index.NOT_ANALYZED));
		} else {
			doc.add(new Field(SearchEnums.template.getLuceneTitle(), "false", Field.Store.YES, Field.Index.NOT_ANALYZED));
		}

		// project information
		doc.add(new Field(SearchEnums.project.getLuceneTitle(), process.getProjekt().getTitel().toLowerCase(), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		doc.add(new Field(SearchEnums.project.getLuceneTitle(), String.valueOf(process.getProjekt().getId()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));

		// Schritt
		if (process.getSchritte() != null) {
			for (Schritt step : process.getSchritteList()) {
				if (step.getId() != null) {
					doc.add(new Field(SearchEnums.step.getLuceneTitle(), String.valueOf(step.getId().intValue()), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
				}
				if (step.getNormalizedTitle() != null) {
					doc.add(new Field(SearchEnums.stepTitle.getLuceneTitle(), step.getNormalizedTitle().toLowerCase(), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
				}
				if (step.getBearbeitungsstatusAsString() != null) {
					doc.add(new Field(step.getNormalizedTitle().toLowerCase(), step.getBearbeitungsstatusAsString().toLowerCase(), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
					doc.add(new Field(step.getBearbeitungsstatusAsString().toLowerCase(), String.valueOf(step.getReihenfolge().intValue()),
							Field.Store.YES, Field.Index.NOT_ANALYZED));
					doc.add(new Field(step.getBearbeitungsstatusAsString().toLowerCase(), step.getNormalizedTitle().toLowerCase(), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
				}
				if (step.getBearbeitungsstatusEnum().equals(StepStatus.DONE)) {
					doc
							.add(new Field(SearchEnums.stepdone.getLuceneTitle(), step.getNormalizedTitle().toLowerCase(), Field.Store.YES,
									Field.Index.NOT_ANALYZED));
					doc.add(new Field(SearchEnums.stepdone.getLuceneTitle(), String.valueOf(step.getReihenfolge().intValue()), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
				}
				if (step.getBearbeitungsstatusEnum().equals(StepStatus.OPEN)) {
					doc
							.add(new Field(SearchEnums.stepopen.getLuceneTitle(), step.getNormalizedTitle().toLowerCase(), Field.Store.YES,
									Field.Index.NOT_ANALYZED));
					doc.add(new Field(SearchEnums.stepopen.getLuceneTitle(), String.valueOf(step.getReihenfolge().intValue()), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
				}
				if (step.getBearbeitungsstatusEnum().equals(StepStatus.LOCKED)) {
					doc.add(new Field(SearchEnums.steplocked.getLuceneTitle(), step.getNormalizedTitle().toLowerCase(), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
					doc.add(new Field(SearchEnums.steplocked.getLuceneTitle(), String.valueOf(step.getReihenfolge().intValue()), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
				}
				if (step.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)) {
					doc.add(new Field(SearchEnums.stepinwork.getLuceneTitle(), step.getNormalizedTitle().toLowerCase(), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
					doc.add(new Field(SearchEnums.stepinwork.getLuceneTitle(), String.valueOf(step.getReihenfolge().intValue()), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
				}

				if (step.getBearbeitungsbeginnAsFormattedString() != null) {
					doc.add(new Field(step.getNormalizedTitle().toLowerCase(), step.getStartDate(), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
				}
				if (step.getBearbeitungsendeAsFormattedString() != null) {
					doc.add(new Field(step.getNormalizedTitle().toLowerCase(), step.getEndDate(), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
				}
				if (step.getEditTypeEnum() != null) {
					doc.add(new Field(step.getNormalizedTitle().toLowerCase(), String.valueOf(step.getEditTypeEnum().getTitle()), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
				}
				if (step.getEigenschaften() != null) {
					for (Schritteigenschaft prop : step.getEigenschaftenList()) {
						if (prop.getTitel() != null && prop.getWert() != null) {
							doc
									.add(new Field(prop.getNormalizedTitle().toLowerCase(), normalize(prop.getNormalizedValue().toLowerCase()), Field.Store.YES,
											Field.Index.NOT_ANALYZED));
							doc.add(new Field(SearchEnums.propertyValue.getLuceneTitle(), normalize(prop.getNormalizedValue().toLowerCase()), Field.Store.YES,
									Field.Index.NOT_ANALYZED));
							doc.add(new Field(SearchEnums.property.getLuceneTitle(), normalize(prop.getNormalizedTitle().toLowerCase()), Field.Store.YES,
									Field.Index.NOT_ANALYZED));
						}
					}
				}
			}
			// Metadata
/*
			try {
				Fileformat ff = process.readMetadataFile();
				if (ff != null) {
					DigitalDocument dd = ff.getDigitalDocument();
					if (dd != null) {
						DocStruct topstruct = dd.getLogicalDocStruct();
						if (topstruct != null) {
							List<Field> meta = getMetaData(topstruct);
							if (meta != null) {
								for (Field e : meta) {
									doc.add(e);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("no metadata found: " + e);
			}
	*/
			try {
				File f = new File(process.getImagesTifDirectory());
				File[] filelist = f.listFiles();
				if (filelist != null) {
					doc
							.add(new Field(SearchEnums.pages.getLuceneTitle(), String.valueOf(filelist.length), Field.Store.YES,
									Field.Index.NOT_ANALYZED));
				}
			} catch (Exception e) {
				logger.warn("no images found: " + e);
			}
		}

		// werkstück
		if (process.getWerkstuecke() != null) {
			for (Werkstueck work : process.getWerkstueckeList()) {
				doc.add(new Field(SearchEnums.werkId.getLuceneTitle(), String.valueOf(work.getId().intValue()), Field.Store.YES,
						Field.Index.NOT_ANALYZED));
				if (work.getEigenschaften() != null) {
					for (Werkstueckeigenschaft prop : work.getEigenschaftenList()) {
						if (prop.getTitel() != null && prop.getWert() != null) {
							doc
									.add(new Field(prop.getNormalizedTitle().toLowerCase(), normalize(prop.getNormalizedValue().toLowerCase()), Field.Store.YES,
											Field.Index.NOT_ANALYZED));
							doc.add(new Field(SearchEnums.propertyValue.getLuceneTitle(), normalize(prop.getNormalizedValue().toLowerCase()), Field.Store.YES,
									Field.Index.NOT_ANALYZED));
							doc.add(new Field(SearchEnums.property.getLuceneTitle(), normalize(prop.getNormalizedTitle().toLowerCase()), Field.Store.YES,
									Field.Index.NOT_ANALYZED));

						}
					}
				}
			}
		}
		if (process.getVorlagen() != null) {
			for (Vorlage template : process.getVorlagenList()) {
				doc.add(new Field(SearchEnums.vorlId.getLuceneTitle(), String.valueOf(template.getId().intValue()), Field.Store.YES,
						Field.Index.NOT_ANALYZED));
				if (template.getEigenschaften() != null) {
					for (Vorlageeigenschaft prop : template.getEigenschaftenList()) {
						if (prop.getTitel() != null && prop.getWert() != null) {
							doc
									.add(new Field(prop.getNormalizedTitle().toLowerCase(), normalize(prop.getNormalizedValue().toLowerCase()), Field.Store.YES,
											Field.Index.NOT_ANALYZED));
							doc.add(new Field(SearchEnums.propertyValue.getLuceneTitle(), normalize(prop.getNormalizedValue().toLowerCase()), Field.Store.YES,
									Field.Index.NOT_ANALYZED));
							doc.add(new Field(SearchEnums.property.getLuceneTitle(), normalize(prop.getNormalizedTitle().toLowerCase()), Field.Store.YES,
									Field.Index.NOT_ANALYZED));
						}
					}
				}
			}

		}

		if (process.getEigenschaften() != null) {
			for (Prozesseigenschaft prop : process.getEigenschaftenList()) {
				if (prop.getTitel() != null && prop.getWert() != null) {
					doc.add(new Field(prop.getNormalizedTitle().toLowerCase(), prop.getNormalizedValue().toLowerCase(), Field.Store.YES, Field.Index.NOT_ANALYZED));
					doc.add(new Field(SearchEnums.propertyValue.getLuceneTitle(), prop.getNormalizedValue().toLowerCase(), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
					doc
							.add(new Field(SearchEnums.property.getLuceneTitle(), normalize(prop.getNormalizedTitle().toLowerCase()), Field.Store.YES,
									Field.Index.NOT_ANALYZED));
				}
			}

		}

		if (process.getRegelsatz().getTitel() != null) {
			doc.add(new Field(SearchEnums.ruleset.getLuceneTitle(), process.getRegelsatz().getTitel().toLowerCase(), Field.Store.YES,
					Field.Index.NOT_ANALYZED));
		}
		// }
		return doc;
	}

	/*
	 * reading metadata of the given DocStruct and returns a list of indexable Fields
	 */

//	private static List<Field> getMetaData(DocStruct topstruct) {
//		List<Field> meta = new ArrayList<Field>();
//		List<Metadata> metalist = topstruct.getAllMetadata();
//		if (metalist != null) {
//			for (Metadata md : metalist) {
//				MetadataType mdt = md.getType();
//				HashMap<String, String> langmap = mdt.getAllLanguages();
//				Set<String> langset = langmap.keySet();
//				for (String lang : langset) {
//					meta.add(new Field(normalize(md.getType().getNameByLanguage(lang).toLowerCase()), normalize(md.getValue().toLowerCase()), Field.Store.YES,
//							Field.Index.NOT_ANALYZED));
//				}
//			}
//			List<DocStruct> children = topstruct.getAllChildren();
//			if (children != null) {
//				for (DocStruct ds : children) {
//					List<Field> childmeta = getMetaData(ds);
//					if (childmeta != null) {
//						for (Field f : childmeta) {
//							meta.add(f);
//						}
//					}
//				}
//			}
//		}
//		return meta;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.search.interfaces.IIndexer#addObject(Prozess p)
	 */
	public void addObject(Prozess process) {
		if (ConfigMain.getBooleanParameter("useLucene")) {
			try {
				// IndexWriter iwriter = new IndexWriter(index_path, analyser,
				// false, mfl);
				iwriter.addDocument(getNewDocument(process));
				// iwriter.close();
			} catch (Exception e) {
				Helper.setFehlerMeldung("could not add process to index", e);
				// logger.error(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.search.interfaces.IIndexer#removeObject(Prozess p)
	 */
	public void removeObject(Prozess process) {
		if (ConfigMain.getBooleanParameter("useLucene")) {
			try {
				// IndexWriter iwriter = new IndexWriter(index_path, analyser,
				// false, mfl);
				iwriter.deleteDocuments(new Term(SearchEnums.processId.getLuceneTitle(), String.valueOf(process.getId().intValue())));
				// iwriter.close();
			} catch (Exception e) {
				// do nothing, process is not in index
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.search.interfaces.IIndexer#updateObject(Prozess p)
	 */
	public void updateObject(Prozess process) {
		if (ConfigMain.getBooleanParameter("useLucene")) {
			removeObject(process);
			addObject(process);
		}
	}

	/**
	 * updating processes in lucene from DAO classes
	 * 
	 * @param p
	 *            {@link Prozess}
	 */

	public static void updateProcess(Prozess p) {
		if (ConfigMain.getBooleanParameter("useLucene")) {
			LuceneIndex li = LuceneIndex.initialize();
			try {
				LuceneIndex.openLuceneIndex(false);
				li.updateObject(p);
				LuceneIndex.closeIndex();
			} catch (CorruptIndexException e) {
				logger.error(e);
			} catch (LockObtainFailedException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	/**
	 * deleting processes in lucene from DAO classes
	 * 
	 * @param p
	 *            {@link Prozess}
	 */

	public static void deleteProcess(Prozess p) {
		if (ConfigMain.getBooleanParameter("useLucene")) {
			try {
				LuceneIndex li = LuceneIndex.initialize();
				LuceneIndex.openLuceneIndex(false);
				li.removeObject(p);
				LuceneIndex.closeIndex();
			} catch (CorruptIndexException e) {
				logger.error(e);
			} catch (LockObtainFailedException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	public static String normalize(String value) {
		value = value.replace("+","").replace("-","").replace("&&", "").replace("||", "").replace("(", "\\(")
         .replace(")", "\\)").replace("{", "\\{").replace("}","\\}").replace("^","")
         .replace("\"","").replace("~","").replace(",","").replace(";","").replaceAll("\\b\\s{2,}\\b", "").replaceAll("\\b[\\s|\\s]{2,}\\b", "");
		return value;
	}
}
