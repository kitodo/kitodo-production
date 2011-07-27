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
import de.sub.goobi.Beans.Batch;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Persistence.BatchDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;

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
		setUsablePluginsForRecords(this.ipl.getPluginsForType(ImportType.Record));
		setUsablePluginsForIDs(this.ipl.getPluginsForType(ImportType.ID));
		setUsablePluginsForFiles(this.ipl.getPluginsForType(ImportType.FILE));
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
		this.possibleDigitalCollections = new ArrayList<String>();
		String filename = this.help.getGoobiConfigDirectory() + "digitalCollections.xml";
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
					if (projektname.getText().equalsIgnoreCase(this.template.getProjekt().getTitel())) {
						List<Element> myCols = projekt.getChildren("DigitalCollection");
						for (Iterator<Element> it2 = myCols.iterator(); it2.hasNext();) {
							Element col = it2.next();
							this.possibleDigitalCollections.add(col.getText());
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
			// found list with ids
			Prefs prefs = this.template.getRegelsatz().getPreferences();
			String tempfolder = ConfigMain.getParameter("tempfolder");
			if (StringUtils.isNotEmpty(this.idList)) {
				IImportPlugin plugin = (IImportPlugin) PluginLoader.getPlugin(PluginType.Import, this.currentPlugin);
				plugin.setImportFolder(tempfolder);
				plugin.setPrefs(prefs);
				List<String> ids = plugin.splitIds(this.idList);
				List<Record> recordList = new ArrayList<Record>();
				for (String id : ids) {
					Record r = new Record();
					r.setData(id);
					r.setId(id);
					r.setCollections(this.digitalCollections);
					recordList.add(r);
				}

				answer = plugin.generateFiles(recordList);
			} else if (this.importFile != null) {
				// uploaded file
				IImportPlugin plugin = (IImportPlugin) PluginLoader.getPlugin(PluginType.Import, this.currentPlugin);
				plugin.setImportFolder(tempfolder);

				plugin.setPrefs(prefs);
				plugin.setFile(this.importFile);
				List<Record> recordList = plugin.generateRecordsFromFile();
				for (Record r : recordList) {
					r.setCollections(this.digitalCollections);
				}
				answer = plugin.generateFiles(recordList);
			}
			// found list with records
			else if (StringUtils.isNotEmpty(this.records)) {
				IImportPlugin plugin = (IImportPlugin) PluginLoader.getPlugin(PluginType.Import, this.currentPlugin);
				plugin.setImportFolder(tempfolder);

				plugin.setPrefs(prefs);
				List<Record> recordList = plugin.splitRecords(this.records);
				for (Record r : recordList) {
					r.setCollections(this.digitalCollections);
				}
				answer = plugin.generateFiles(recordList);
			}

			Batch b = new Batch();
			
			for (Entry<String, ImportReturnValue> data : answer.entrySet()) {
				if (data.getValue().equals(ImportReturnValue.ExportFinished)) {
					int returnValue = CommandLineInterface.generateProcess(data.getKey(), this.template, new File(tempfolder), null, "error", b);
					if (returnValue > 0) {
						Helper.setFehlerMeldung("import failed for " + data.getKey() + ", process generation failed with error code " + returnValue);
					}
					Helper.setMeldung(ImportReturnValue.ExportFinished.getValue() + " for " + data.getKey());
				} else {
					Helper.setFehlerMeldung("import failed for " + data.getKey() + " error code is: " + data.getValue());
				}

			}
			if (b.getBatchList().size()> 0) {
				try {
					new BatchDAO().save(b);
				} catch (DAOException e) {
					// TODO
				}
			}

		}

		// missing data
		else {
			Helper.setFehlerMeldung("missingData");
		}
		this.idList = null;
		this.importFile = null;
		this.records = "";
	}

	/**
	 * File upload with binary copying.
	 */
	public void uploadFile() {
		ByteArrayInputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			String filename = ConfigMain.getParameter("tempfolder", "/opt/digiverso/goobi/temp/") + this.uploadedFile.getName();

			inputStream = new ByteArrayInputStream(this.uploadedFile.getBytes());
			outputStream = new FileOutputStream(filename);

			byte[] buf = new byte[1024];
			int len;
			while ((len = inputStream.read(buf)) > 0) {
				outputStream.write(buf, 0, len);
			}

			this.importFile = new File(filename);
			Helper.setMeldung("File '" + this.uploadedFile.getName() + "' successfully uploaded, press 'Save' now...");
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
		return this.uploadedFile;
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
		if (StringUtils.isEmpty(this.idList) && StringUtils.isEmpty(this.records) && (this.importFile == null)) {
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
		if (this.format != null) {
			return this.format.getTitle();
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
		this.format = ImportFormat.getTypeFromTitle(formatTitle);
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
		return this.idList;
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
		return this.records;
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
		return this.processes;
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
		return this.template;
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
		return this.digitalCollections;
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
		return this.possibleDigitalCollections;
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
		return this.ids;
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
		if (this.format == null) {
			return "";
		}
		return this.format.getTitle();
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
		return this.currentPlugin;
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
		return this.usablePluginsForRecords;
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
		return this.usablePluginsForIDs;
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
		return this.usablePluginsForFiles;
	}
}
