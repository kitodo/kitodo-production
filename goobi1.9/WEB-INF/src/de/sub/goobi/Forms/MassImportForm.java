package de.sub.goobi.Forms;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.goobi.production.Import.GoobiHotfolder;
import org.goobi.production.Import.Record;
import org.goobi.production.cli.CommandLineInterface;
import org.goobi.production.enums.ImportFormat;
import org.goobi.production.enums.ImportReturnValue;
import org.goobi.production.enums.ImportType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.ImportPluginLoader;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import ugh.dl.Prefs;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;

// TODO FIXME alle Meldungen durch  messages-Meldungen ersetzen
public class MassImportForm {
	private static final Logger logger = Logger.getLogger(MassImportForm.class);
	private Prozess template;
	private List<Prozess> processes;
	private List<String> digitalCollections;
	private List<String> possibleDigitalCollections;
	// private List<String> recordList = new ArrayList<String>();
	private List<String> ids = new ArrayList<String>();
	private ImportFormat format = null;
	private String idList = "";
	private String records = "";
	// private List<String> usablePlugins = new ArrayList<String>();
	private List<String> usablePluginsForRecords = new ArrayList<String>();
	private List<String> usablePluginsForIDs = new ArrayList<String>();
	private List<String> usablePluginsForFiles = new ArrayList<String>();
	private final ImportPluginLoader ipl = new ImportPluginLoader();
	private String currentPlugin = "";
	private File importFile = null;
	private final Helper help = new Helper();

	private UploadedFile uploadedFile = null;

	public MassImportForm() {

		// usablePlugins = ipl.getTitles();
		setUsablePluginsForRecords(ipl.getPluginsForType(ImportType.Record));
		setUsablePluginsForIDs(ipl.getPluginsForType(ImportType.ID));
		setUsablePluginsForFiles(ipl.getPluginsForType(ImportType.FILE));
	}

	public String Prepare() {
		initializePossibleDigitalCollections();
		return "MassImport";
	}

	/**
	 * generate a list with all possible collections for given project
	 * 
	 * 
	 */

	@SuppressWarnings("unchecked")
	private void initializePossibleDigitalCollections() {
		possibleDigitalCollections = new ArrayList<String>();
		String filename = help.getGoobiConfigDirectory() + "digitalCollections.xml";
		if (!(new File(filename).exists())) {
			Helper.setFehlerMeldung("File not found: ", filename);
			return;
		}

		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new File(filename));
			Element root = doc.getRootElement();
			List<Element> projekte = root.getChildren();
			for (Iterator<Element> iter = projekte.iterator(); iter.hasNext();) {
				Element projekt = iter.next();
				List<Element> projektnamen = projekt.getChildren("name");
				for (Iterator<Element> iterator = projektnamen.iterator(); iterator.hasNext();) {
					Element projektname = iterator.next();
					if (projektname.getText().equalsIgnoreCase(template.getProjekt().getTitel())) {
						List<Element> myCols = projekt.getChildren("DigitalCollection");
						for (Iterator<Element> it2 = myCols.iterator(); it2.hasNext();) {
							Element col = it2.next();
							possibleDigitalCollections.add(col.getText());
						}
					}
				}
			}
		} catch (JDOMException e1) {
			logger.error("error while parsing digital collections", e1);
			Helper.setFehlerMeldung("Error while parsing digital collections", e1);
		} catch (IOException e1) {
			logger.error("error while parsing digital collections", e1);
			Helper.setFehlerMeldung("Error while parsing digital collections", e1);
		}
	}

	public void convertData() {
		if (testForData()) {
			HashMap<String, ImportReturnValue> answer = new HashMap<String, ImportReturnValue>();
			// HashMap<String, Fileformat> meta = new HashMap<String,
			// Fileformat>();
			// found list with ids
			Prefs prefs = template.getRegelsatz().getPreferences();
			String tempfolder = ConfigMain.getParameter("tempfolder");
			if (StringUtils.isNotEmpty(idList)) {
				IImportPlugin plugin = (IImportPlugin) PluginLoader.getPlugin(PluginType.Import, currentPlugin);
				// if (getHotfolderPathForPlugin(template.getId()) != null) {
				// plugin.setImportFolder(getHotfolderPathForPlugin(template.getId()));
				plugin.setImportFolder(tempfolder);
				plugin.setPrefs(prefs);
				List<String> ids = plugin.splitIds(idList);
				List<Record> recordList = new ArrayList<Record>();
				for (String id : ids) {
					Record r = new Record();
					r.setData(id);
					r.setId(id);
					r.setCollections(digitalCollections);
					recordList.add(r);
				}

				answer = plugin.generateFiles(recordList);
				// meta = plugin.generateMetadata(recordList);
				// } else {
				// Helper.setFehlerMeldung("hotfolder for template " +
				// template.getTitel() + " does not exist");
				// }
			} else if (importFile != null) {
				// uploaded file
				IImportPlugin plugin = (IImportPlugin) PluginLoader.getPlugin(PluginType.Import, currentPlugin);
				// if (getHotfolderPathForPlugin(template.getId()) != null) {
				// plugin.setImportFolder(getHotfolderPathForPlugin(template.getId()));
				plugin.setImportFolder(tempfolder);

				plugin.setPrefs(prefs);
				plugin.setFile(importFile);
				List<Record> recordList = plugin.generateRecordsFromFile();
				for (Record r : recordList) {
					r.setCollections(digitalCollections);
				}
				answer = plugin.generateFiles(recordList);
				// meta = plugin.generateMetadata(recordList);
				// } else {
				// Helper.setFehlerMeldung("hotfolder for template " +
				// template.getTitel() + " does not exist");
				// }
			}
			// found list with records
			else if (StringUtils.isNotEmpty(records)) {
				IImportPlugin plugin = (IImportPlugin) PluginLoader.getPlugin(PluginType.Import, currentPlugin);
				// if (getHotfolderPathForPlugin(template.getId()) != null) {
				// plugin.setImportFolder(getHotfolderPathForPlugin(template.getId()));
				plugin.setImportFolder(tempfolder);

				plugin.setPrefs(prefs);
				List<Record> recordList = plugin.splitRecords(records);
				for (Record r : recordList) {
					r.setCollections(digitalCollections);
				}
				answer = plugin.generateFiles(recordList);
				// meta = plugin.generateMetadata(recordList);
				// } else {
				// Helper.setFehlerMeldung("hotfolder for template " +
				// template.getTitel() + " does not exist");
				// }
			}

			// for (Entry<String, ImportReturnValue> bla : answer.entrySet()) {
			// if (bla.getValue().equals(ImportReturnValue.ExportFinished)) {
			// Helper.setMeldung(ImportReturnValue.ExportFinished.getValue() +
			// " for " + bla.getKey());
			// } else {
			// Helper.setFehlerMeldung(bla.getValue() + " for " + bla.getKey());
			// }
			// }

			for (Entry<String, ImportReturnValue> data : answer.entrySet()) {
				if (data.getValue().equals(ImportReturnValue.ExportFinished)) {
					int returnValue = CommandLineInterface.generateProcess(data.getKey(), template, new File(tempfolder), null, "error");
					if (returnValue > 0) {
						Helper.setFehlerMeldung("import failed for " + data.getKey() + ", process generation failed with error code " + returnValue);
					}
					Helper.setMeldung(ImportReturnValue.ExportFinished.getValue() + " for " + data.getKey());
				} else {
					Helper.setFehlerMeldung("import failed for " + data.getKey() + " error code is: " + data.getValue());
				}

			}

		}

		// missing data
		else {
			Helper.setFehlerMeldung("missingData");
		}
		idList = null;
		importFile = null;
		records = "";
	}

	/**
	 * File upload with binary copying.
	 */
	public void uploadFile() {
		ByteArrayInputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			String filename = ConfigMain.getParameter("tempfolder", "/opt/digiverso/goobi/temp/") + uploadedFile.getName();

			inputStream = new ByteArrayInputStream(uploadedFile.getBytes());
			outputStream = new FileOutputStream(filename);

			byte[] buf = new byte[1024];
			int len;
			while ((len = inputStream.read(buf)) > 0) {
				outputStream.write(buf, 0, len);
			}

			importFile = new File(filename);
			Helper.setMeldung("File '" + uploadedFile.getName() + "' successfully uploaded, press 'Save' now...");
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			Helper.setFehlerMeldung("Upload failed");
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}

		}

	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	/**
	 * tests input fields for correct data
	 * 
	 * @return true if data is valid or false otherwise
	 */

	private boolean testForData() {
		// if (format == null) {
		// return false;
		// }
		if (StringUtils.isEmpty(idList) && StringUtils.isEmpty(records) && (importFile == null)) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @return list with all import formats
	 */
	public List<String> getFormats() {
		List<String> l = new ArrayList<String>();
		for (ImportFormat input : ImportFormat.values()) {
			l.add(input.getTitle());
		}
		return l;
	}

	public String getHotfolderPathForPlugin(int pluginId) {
		for (GoobiHotfolder hotfolder : GoobiHotfolder.getInstances()) {
			if (hotfolder.getTemplate() == pluginId) {
				return hotfolder.getFolderAsString();
			}
		}

		return null;
	}

	/**
	 * 
	 * @return current format
	 */

	public String getCurrentFormat() {
		if (format != null) {
			return format.getTitle();
		} else {
			return "";
		}
	}

	/**
	 * 
	 * @param formatTitle
	 *            current format
	 */
	public void setCurrentFormat(String formatTitle) {
		format = ImportFormat.getTypeFromTitle(formatTitle);
	}

	/**
	 * @param idList
	 *            the idList to set
	 */
	public void setIdList(String idList) {
		this.idList = idList;
	}

	/**
	 * @return the idList
	 */
	public String getIdList() {
		return idList;
	}

	/**
	 * @param records
	 *            the records to set
	 */
	public void setRecords(String records) {
		this.records = records;
	}

	/**
	 * @return the records
	 */
	public String getRecords() {
		return records;
	}

	/**
	 * @param process
	 *            the process to set
	 */
	public void setProcess(List<Prozess> processes) {
		this.processes = processes;
	}

	/**
	 * @return the process
	 */
	public List<Prozess> getProcess() {
		return processes;
	}

	/**
	 * @param template
	 *            the template to set
	 */
	public void setTemplate(Prozess template) {
		this.template = template;

	}

	/**
	 * @return the template
	 */
	public Prozess getTemplate() {
		return template;
	}

	/**
	 * @param digitalCollections
	 *            the digitalCollections to set
	 */
	public void setDigitalCollections(List<String> digitalCollections) {
		this.digitalCollections = digitalCollections;
	}

	/**
	 * @return the digitalCollections
	 */
	public List<String> getDigitalCollections() {
		return digitalCollections;
	}

	/**
	 * @param possibleDigitalCollection
	 *            the possibleDigitalCollection to set
	 */
	public void setPossibleDigitalCollection(List<String> possibleDigitalCollection) {
		this.possibleDigitalCollections = possibleDigitalCollection;
	}

	/**
	 * @return the possibleDigitalCollection
	 */
	public List<String> getPossibleDigitalCollection() {
		return possibleDigitalCollections;
	}

	public void setPossibleDigitalCollections(List<String> possibleDigitalCollections) {
		this.possibleDigitalCollections = possibleDigitalCollections;
	}

	public void setProcesses(List<Prozess> processes) {
		this.processes = processes;
	}

	/**
	 * @param ids
	 *            the ids to set
	 */
	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	/**
	 * @return the ids
	 */
	public List<String> getIds() {
		return ids;
	}

	/**
	 * @param format
	 *            the format to set
	 */
	public void setFormat(String format) {
		this.format = ImportFormat.getTypeFromTitle(format);
	}

	/**
	 * @return the format
	 */
	public String getFormat() {
		if (format == null) {
			return "";
		}
		return format.getTitle();
	}

	// /**
	// * @param usablePlugins
	// * the usablePlugins to set
	// */
	// public void setUsablePlugins(List<String> usablePlugins) {
	// this.usablePlugins = usablePlugins;
	// }
	//
	// /**
	// * @return the usablePlugins
	// */
	// public List<String> getUsablePlugins() {
	// return usablePlugins;
	// }

	/**
	 * @param currentPlugin
	 *            the currentPlugin to set
	 */
	public void setCurrentPlugin(String currentPlugin) {
		this.currentPlugin = currentPlugin;
	}

	/**
	 * @return the currentPlugin
	 */
	public String getCurrentPlugin() {
		return currentPlugin;
	}

	/**
	 * @param usablePluginsForRecords
	 *            the usablePluginsForRecords to set
	 */
	public void setUsablePluginsForRecords(List<String> usablePluginsForRecords) {
		this.usablePluginsForRecords = usablePluginsForRecords;
	}

	/**
	 * @return the usablePluginsForRecords
	 */
	public List<String> getUsablePluginsForRecords() {
		return usablePluginsForRecords;
	}

	/**
	 * @param usablePluginsForIDs
	 *            the usablePluginsForIDs to set
	 */
	public void setUsablePluginsForIDs(List<String> usablePluginsForIDs) {
		this.usablePluginsForIDs = usablePluginsForIDs;
	}

	/**
	 * @return the usablePluginsForIDs
	 */
	public List<String> getUsablePluginsForIDs() {
		return usablePluginsForIDs;
	}

	/**
	 * @param usablePluginsForFiles
	 *            the usablePluginsForFiles to set
	 */
	public void setUsablePluginsForFiles(List<String> usablePluginsForFiles) {
		this.usablePluginsForFiles = usablePluginsForFiles;
	}

	/**
	 * @return the usablePluginsForFiles
	 */
	public List<String> getUsablePluginsForFiles() {
		return usablePluginsForFiles;
	}
}
