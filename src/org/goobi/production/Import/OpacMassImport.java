package org.goobi.production.Import;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.goobi.production.cli.CommandLineInterface;
import org.goobi.production.enums.ImportFormat;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IImportPlugin;

import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.unigoettingen.sub.search.opac.Catalogue;

public class OpacMassImport {

	private List<String> recordList = new ArrayList<String>();
	private List<String> ids = new ArrayList<String>();
	private Catalogue opac = null;
	private Prozess template = null;
	private Prefs ruleset = null;
	private ImportFormat format = null;

	/**
	 * Constructor for records
	 */

	public OpacMassImport(String records, Prozess template, ImportFormat format) {
		this.recordList = parseRecords(records);
		this.template = template;
		this.ruleset = template.getRegelsatz().getPreferences();
		this.format = format;
		// this.form = form;
		// this.template = form.getProzessVorlage();
	}

	/**
	 * Constructor for list of ids
	 */

	public OpacMassImport(String ids, Catalogue opac, Prozess template, ImportFormat format) {
		this.setIds(paseIds(ids));
		this.setOpac(opac);
		this.template = template;
		setRuleset(template.getRegelsatz().getPreferences());
		this.format = format;
		// this.form = form;

	}

	/**
	 * converts input data to processes
	 * 
	 * @return List of processes
	 */

	public void convertData() {
		PluginLoader loader = new PluginLoader(PluginType.Import, format.getTitle());
		IImportPlugin ip = (IImportPlugin) loader.getPlugin();
		try {
			for (String record : recordList) {
				ip.setData(record);
				ip.setPrefs(ruleset);
				Fileformat ff = ip.convertData();
				if (ff != null) {
					MetsMods mm = new MetsMods(ruleset);
					mm.setDigitalDocument(ff.getDigitalDocument());
					mm.write(ip.getImportFolder() + ip.getProcessTitle());
				}
			}
			CommandLineInterface.generateNewProcess(template.getId(), ip.getImportFolder());
		} catch (ReadException e) {

		} catch (PreferencesException e) {

		} catch (SwapException e) {

		} catch (DAOException e) {

		} catch (WriteException e) {

		} catch (IOException e) {

		} catch (InterruptedException e) {

		}

	}

	private List<String> paseIds(String ids2) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<String> parseRecords(String records) {

		// TODO Auto-generated method stub
		List<String> recordList = new ArrayList<String>();
		recordList.add(records);

		return recordList;
	}

	/**
	 * @param recordList
	 *            the recordList to set
	 */
	public void setRecordList(List<String> recordList) {
		this.recordList = recordList;
	}

	/**
	 * @return the recordList
	 */
	public List<String> getRecordList() {
		return recordList;
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
	 * @param opac
	 *            the opac to set
	 */
	public void setOpac(Catalogue opac) {
		this.opac = opac;
	}

	/**
	 * @return the opac
	 */
	public Catalogue getOpac() {
		return opac;
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
	 * @param ruleset
	 *            the ruleset to set
	 */
	public void setRuleset(Prefs ruleset) {
		this.ruleset = ruleset;
	}

	/**
	 * @return the ruleset
	 */
	public Prefs getRuleset() {
		return ruleset;
	}

	/**
	 * @param format
	 *            the format to set
	 */
	public void setFormat(ImportFormat format) {
		this.format = format;
	}

	/**
	 * @return the format
	 */
	public ImportFormat getFormat() {
		return format;
	}
}
