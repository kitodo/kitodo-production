/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package de.sub.goobi.helper.tasks;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.joda.time.LocalDate;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;

import com.sharkysoft.util.NotImplementedException;

import de.sub.goobi.beans.Batch;
import de.sub.goobi.beans.Projekt;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.ArrayListMap;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;

public class ExportBatchTask extends CloneableLongRunningTask {
	private static final Logger logger = Logger.getLogger(ExportBatchTask.class);

	private static final double GAUGE_INCREMENT_PER_ACTION = 100 / 3d;
	private static final String METADATA_ELEMENT_DAY = "PublicationDay";
	private static final String METADATA_ELEMENT_ISSUE = "Issue";
	private static final String METADATA_ELEMENT_MONTH = "PublicationMonth";
	private static final String METADATA_ELEMENT_YEAR = "PublicationYear";
	private static final String METADATA_FIELD_LABEL = "TitleDocMain";
	private static final String METADATA_FIELD_MPTR = "MetsPointerURL";
	private static final String METADATA_FIELD_ORDERLABEL = "TitleDocMainShort";

	/**
	 * The field batch holds the batch whose processes are to export.
	 */
	private final Batch batch;

	/**
	 * The field aggregation holds a 4-dimensional list (hierarchy: year, month,
	 * day, issue) into which the issues are aggregated.
	 */
	private ArrayListMap<org.joda.time.LocalDate, String> aggregation;

	/**
	 * The field action holds the number of the action the task currently is in.
	 * Valid values range from 1 to 2. This is to start up at the right position
	 * after an interruption again.
	 */
	private int action;

	/**
	 * The field processesIterator holds an iterator object to walk though the
	 * processes of the batch. The processes in a batch are a Set
	 * implementation, so we use an iterator to walk through and do not use an
	 * index.
	 */
	private Iterator<Prozess> processesIterator;

	/**
	 * The field dividend holds the number of processes that have been processed
	 * in this action. The fields dividend and divisor are used to display a
	 * progress bar.
	 */
	private int dividend;

	/**
	 * The field dividend holds the number of processes to process in each
	 * action. The fields dividend and divisor are used to display a progress
	 * bar.
	 */
	private double divisor;

	private HashMap<Integer, String> collectedYears;

	/**
	 * Constructor to create an ExportBatchTask.
	 * 
	 * @param batch
	 *            batch to export
	 * @throws HibernateException
	 *             if the batch isn’t attached to a Hibernate session and cannot
	 *             be reattached either
	 */
	public ExportBatchTask(Batch batch) throws HibernateException {
		this.batch = batch;
		action = 1;
		aggregation = new ArrayListMap<LocalDate, String>();
		collectedYears = new HashMap<Integer, String>();
		processesIterator = batch.getProcesses().iterator();
		dividend = 0;
		divisor = GAUGE_INCREMENT_PER_ACTION / batch.getProcesses().size();
	}

	/**
	 * The function run() is the main function of this task (which is a thread).
	 * It will aggregate the data from all processes and then export all
	 * processes with the recombined data. The statusProgress variable is being
	 * updated to show the operator how far the task has proceeded.
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		Prozess process = null;
		try {
			if (action == 1) {
				while (processesIterator.hasNext()) {
					if (isInterrupted()) {
						stopped();
						return;
					}
					process = processesIterator.next();
					Integer processesYear = Integer.valueOf(getYear(process.getDigitalDocument()));
					if (!collectedYears.containsKey(processesYear))
						collectedYears.put(processesYear, getMetsYearAnchorPointerURL(process));
					aggregation.addAll(getIssueDates(process.getDigitalDocument()), getMetsPointerURL(process));
					setStatusProgress(++dividend / divisor);
				}
				action = 2;
				processesIterator = batch.getProcesses().iterator();
				dividend = 0;
			}

			if (action == 2)
				while (processesIterator.hasNext()) {
					if (isInterrupted()) {
						stopped();
						return;
					}
					MetsModsImportExport extendedData = buildExportableMetsMods(process = processesIterator.next(),
							collectedYears, aggregation);
					setStatusProgress(GAUGE_INCREMENT_PER_ACTION + ++dividend / divisor);

					export(extendedData);
					setStatusProgress(GAUGE_INCREMENT_PER_ACTION + ++dividend / divisor);
				}

		} catch (Exception e) { // PreferencesException, ReadException, SwapException, DAOException, IOException, InterruptedException and some runtime exceptions
			String message = e.getClass().getSimpleName() + " while " + (action == 1 ? "examining " : "exporting ")
					+ (process != null ? process.getTitel() : "") + ": " + e.getMessage();
			logger.error(message, e);
			setStatusMessage(message);
			setStatusProgress(-1);
			return;
		}
	}

	/**
	 * The function getYear() returns the year that of issues are contained in
	 * this act. The function relies on the assumption that the first level of
	 * the logical structure tree of the act is of type METADATA_ELEMENT_YEAR,
	 * is present exactly once and has a METADATA_FIELD_LABEL whose value
	 * represents the year and can be parsed to integer.
	 * 
	 * @param act
	 *            act to examine
	 * @return the year
	 * @throws ReadException
	 *             if one of the preconditions fails
	 */
	private static int getYear(DigitalDocument act) throws ReadException {
		List<DocStruct> children = act.getLogicalDocStruct().getAllChildren();
		if (children == null)
			throw new ReadException(
					"Could not get date year: Logical structure tree doesn’t have elements. Exactly one element of type "
							+ METADATA_ELEMENT_YEAR + " is required.");
		if (children.size() > 1)
			throw new ReadException(
					"Could not get date year: Logical structure has several elements. Exactly one element (of type "
							+ METADATA_ELEMENT_YEAR + ") is required.");
		try {
			return getMetadataIntValueByName(children.get(0), METADATA_FIELD_LABEL);
		} catch (NoSuchElementException nose) {
			throw new ReadException("Could not get date year: " + METADATA_ELEMENT_YEAR + " has no meta data field "
					+ METADATA_FIELD_LABEL + '.');
		} catch (NumberFormatException uber) {
			throw new ReadException("Could not get date year: " + METADATA_FIELD_LABEL + " value from "
					+ METADATA_ELEMENT_YEAR + " cannot be interpeted as whole number.");
		}
	}

	/**
	 * The function getMetadataIntValueByName() returns the value of a named
	 * meta data entry associated with a structure entity as int.
	 * 
	 * @param structureTypeName
	 *            structureEntity to get the meta data value from
	 * @param metaDataTypeName
	 *            name of the meta data element whose value is to obtain
	 * @return value of a meta data element with the given name
	 * @throws NoSuchElementException
	 *             if there is no such element
	 * @throws NumberFormatException
	 *             if the value cannot be parsed to int
	 */
	private static int getMetadataIntValueByName(DocStruct structureTypeName, String metaDataTypeName)
			throws NoSuchElementException, NumberFormatException {
		List<MetadataType> metadataTypes = structureTypeName.getType().getAllMetadataTypes();
		for (MetadataType metadataType : metadataTypes)
			if (metaDataTypeName.equals(metadataType.getName()))
				return Integer.parseInt(new HashSet<Metadata>(structureTypeName.getAllMetadataByType(metadataType))
						.iterator().next().getValue());
		throw new NoSuchElementException();
	}

	/**
	 * The function getMetsYearAnchorPointerURL() returns the URL for a METS
	 * pointer to retrieve the course of appearance data for the year the given
	 * process is in.
	 * 
	 * @param process
	 *            process whese year anchor pointer shall be returend
	 * @return URL to retrieve the year data
	 * @throws PreferencesException
	 *             if the no node corresponding to the file format is available
	 *             in the rule set configured
	 * @throws ReadException
	 *             if the no node corresponding to the file format is available
	 *             in the rule set configured
	 * @throws SwapException
	 *             if an error occurs while the process is swapped back in
	 * @throws DAOException
	 *             if an error occurs while saving the fact that the process has
	 *             been swapped back in to the database
	 * @throws IOException
	 *             if creating the process directory or reading the meta data
	 *             file fails
	 * @throws InterruptedException
	 *             if the current thread is interrupted by another thread while
	 *             it is waiting for the shell script to create the directory to
	 *             finish
	 */
	private static String getMetsYearAnchorPointerURL(Prozess process) throws PreferencesException, ReadException,
			SwapException, DAOException, IOException, InterruptedException {

		VariableReplacer replacer = new VariableReplacer(process.getDigitalDocument(), process.getRegelsatz()
				.getPreferences(), process, null);
		String metsPointerPathAnchor = process.getProjekt().getMetsPointerPathAnchor();
		if (metsPointerPathAnchor.contains(Projekt.ANCHOR_SEPARATOR))
			metsPointerPathAnchor = metsPointerPathAnchor.split(Projekt.ANCHOR_SEPARATOR)[1];
		return replacer.replace(metsPointerPathAnchor);
	}

	/**
	 * The function getIssueDates() returns a list with all the dates of the
	 * issues contained in this process. The function relies on the assumption
	 * that the child elements descending from the topmost logical structure
	 * entity of the process represent year, month and day of appearance and
	 * that the immediate children of the day level represent issues. The levels
	 * year, month and day must have meta data elements named "PublicationYear",
	 * "PublicationMonth" and "PublicationDay" associated whose value can be
	 * interpreted as an integer.
	 * 
	 * @return a list with the dates of all issues in this process
	 * @throws PreferencesException
	 *             if the no node corresponding to the file format is available
	 *             in the rule set used
	 * @throws ReadException
	 *             if the meta data file cannot be read
	 * @throws SwapException
	 *             if an error occurs while the process is swapped back in
	 * @throws DAOException
	 *             if an error occurs while saving the fact that the process has
	 *             been swapped back in to the database
	 * @throws IOException
	 *             if creating the process directory or reading the meta data
	 *             file fails
	 * @throws InterruptedException
	 *             if the current thread is interrupted by another thread while
	 *             it is waiting for the shell script to create the directory to
	 *             finish
	 */
	private static List<LocalDate> getIssueDates(DigitalDocument act) throws PreferencesException, ReadException,
			SwapException, DAOException, IOException, InterruptedException {
		List<LocalDate> result = new LinkedList<LocalDate>();
		DocStruct logicalDocStruct = act.getLogicalDocStruct();
		for (DocStruct annualNode : skipIfNull(logicalDocStruct.getAllChildren())) {
			int year = getMetadataIntValueByName(annualNode, METADATA_ELEMENT_YEAR);
			for (DocStruct monthNode : skipIfNull(annualNode.getAllChildren())) {
				int monthOfYear = getMetadataIntValueByName(monthNode, METADATA_ELEMENT_MONTH);
				for (DocStruct dayNode : skipIfNull(monthNode.getAllChildren())) {
					LocalDate appeared = new LocalDate(year, monthOfYear, getMetadataIntValueByName(dayNode,
							METADATA_ELEMENT_DAY));
					for (@SuppressWarnings("unused")
					DocStruct entry : skipIfNull(dayNode.getAllChildren()))
						result.add(appeared);
				}
			}
		}
		return result;
	}

	/**
	 * The function skipIfNull() returns the list passed in, or
	 * Collections.emptyList() if the list is null.
	 * {@link DocStruct#getAllChildren()} does return null if no children are
	 * contained. This would throw a NullPointerException if passed into a loop.
	 * Replacing null by Collections.emptyList() results in the loop to be
	 * silently skipped, so that the outer code continues normally.
	 * 
	 * @param list
	 *            list to check for being null
	 * @return the list, Collections.emptyList() if the list is null
	 */
	private static <T> List<T> skipIfNull(List<T> list) {
		if (list == null)
			list = Collections.emptyList();
		return list;
	}

	/**
	 * The function getMetsPointerURL() investigates the METS pointer URL of the
	 * process.
	 * 
	 * @param process
	 *            process to take values for path variables from
	 * @return the METS pointer URL of the process
	 * @throws PreferencesException
	 *             if the no node corresponding to the file format is available
	 *             in the rule set used
	 * @throws ReadException
	 *             if the meta data file cannot be read
	 * @throws SwapException
	 *             if an error occurs while the process is swapped back in
	 * @throws DAOException
	 *             if an error occurs while saving the fact that the process has
	 *             been swapped back in to the database
	 * @throws IOException
	 *             if creating the process directory or reading the meta data
	 *             file fails
	 * @throws InterruptedException
	 *             if the current thread is interrupted by another thread while
	 *             it is waiting for the shell script to create the directory to
	 *             finish
	 */
	private static String getMetsPointerURL(Prozess process) throws PreferencesException, ReadException, SwapException,
			DAOException, IOException, InterruptedException {
		VariableReplacer replacer = new VariableReplacer(process.getDigitalDocument(), process.getRegelsatz()
				.getPreferences(), process, null);
		return replacer.replace(process.getProjekt().getMetsPointerPath());
	}

	/**
	 * The function buildExportableMetsMods() returns a MetsModsImportExport
	 * object whose logical document structure tree has been enriched with all
	 * nodes that have to be exported along with the data to make
	 * cross-newspaper referencing work. References to both year data files and
	 * other issues within the same year have been attached.
	 * 
	 * @param process
	 *            process to get the METS/MODS data from
	 * @param years
	 *            a map with all years and their pointer URLs
	 * @param issues
	 *            a map with all issues and their pointer URLs
	 * @return an enriched MetsModsImportExport object
	 * @throws PreferencesException
	 *             if the no node corresponding to the file format is available
	 *             in the rule set used
	 * @throws ReadException
	 *             if the meta data file cannot be read
	 * @throws SwapException
	 *             if an error occurs while the process is swapped back in
	 * @throws DAOException
	 *             if an error occurs while saving the fact that the process has
	 *             been swapped back in to the database
	 * @throws IOException
	 *             if creating the process directory or reading the meta data
	 *             file fails
	 * @throws InterruptedException
	 *             if the current thread is interrupted by another thread while
	 *             it is waiting for the shell script to create the directory to
	 *             finish
	 * @throws TypeNotAllowedForParentException
	 *             is thrown, if this DocStruct is not allowed for a parent
	 * @throws MetadataTypeNotAllowedException
	 *             if the DocStructType of this DocStruct instance does not
	 *             allow the MetadataType or if the maximum number of Metadata
	 *             (of this type) is already available
	 * @throws TypeNotAllowedAsChildException
	 *             if a child should be added, but it's DocStruct type isn't
	 *             member of this instance's DocStruct type
	 */
	private static MetsModsImportExport buildExportableMetsMods(Prozess process, HashMap<Integer, String> years,
			ArrayListMap<LocalDate, String> issues) throws PreferencesException, ReadException, SwapException,
			DAOException, IOException, InterruptedException, TypeNotAllowedForParentException,
			MetadataTypeNotAllowedException, TypeNotAllowedAsChildException {

		Prefs ruleSet = process.getRegelsatz().getPreferences();
		MetsModsImportExport result = new MetsModsImportExport(ruleSet);
		((MetsMods) result).read(process.getMetadataFilePath());

		DigitalDocument act = result.getDigitalDocument();
		int ownYear = getMetadataIntValueByName(act.getLogicalDocStruct().getAllChildren().iterator().next(),
				METADATA_FIELD_LABEL);
		String ownMetsPointerURL = getMetsPointerURL(process);

		insertReferencesToYears(years, act, ruleSet);
		insertReferencesToOtherIssuesInThisYear(issues, ownYear, ownMetsPointerURL, act, ruleSet);
		return result;
	}

	/**
	 * @param years
	 * @param currentYear
	 * @param act
	 * @param ruleSet
	 * @throws TypeNotAllowedForParentException
	 *             is thrown, if this DocStruct is not allowed for a parent
	 * @throws MetadataTypeNotAllowedException
	 *             if the DocStructType of this DocStruct instance does not
	 *             allow the MetadataType or if the maximum number of Metadata
	 *             (of this type) is already available
	 * @throws TypeNotAllowedAsChildException
	 *             if a child should be added, but it's DocStruct type isn't
	 *             member of this instance's DocStruct type
	 */
	private static void insertReferencesToYears(HashMap<Integer, String> years, DigitalDocument act, Prefs ruleSet)
			throws TypeNotAllowedForParentException, MetadataTypeNotAllowedException, TypeNotAllowedAsChildException {
		for (Integer year : years.keySet()) {
			DocStruct child = getOrCreateChild(act.getLogicalDocStruct(), METADATA_ELEMENT_YEAR, METADATA_FIELD_LABEL,
					year.toString(), null, act, ruleSet);
			docStruct_addMetadata(child, METADATA_FIELD_MPTR, years.get(year));
		}
	}

	/**
	 * The function getOrCreateChild() returns a child of a DocStruct of the
	 * given type and identified by an identifier in a meta data field of
	 * choice. If no such child exists, it will be created.
	 * 
	 * @param parent
	 *            DocStruct to get the child from or create it in.
	 * @param type
	 *            type of the DocStruct to return
	 * @param identifierField
	 *            field whose value identifies the DocStruct to return
	 * @param identifier
	 *            value that identifies the DocStruct to return
	 * @param optionalField
	 *            optionally, adds another meta data field with this name and
	 *            the value used as identifier (may be null)
	 * @param act
	 *            act to create the child in
	 * @param ruleset
	 *            rule set the act is based on
	 * @return the first child matching the given conditions, if any, or a newly
	 *         created child with these properties otherwise
	 * @throws TypeNotAllowedForParentException
	 *             is thrown, if this DocStruct is not allowed for a parent
	 * @throws MetadataTypeNotAllowedException
	 *             if the DocStructType of this DocStruct instance does not
	 *             allow the MetadataType or if the maximum number of Metadata
	 *             (of this type) is already available
	 * @throws TypeNotAllowedAsChildException
	 *             if a child should be added, but it's DocStruct type isn't
	 *             member of this instance's DocStruct type
	 */
	private static DocStruct getOrCreateChild(DocStruct parent, String type, String identifierField, String identifier,
			String optionalField, DigitalDocument act, Prefs ruleset) throws TypeNotAllowedForParentException,
			MetadataTypeNotAllowedException, TypeNotAllowedAsChildException {
		try {
			return docStruct_getChild(parent, type, identifierField, identifier);
		} catch (NoSuchElementException nose) {
			DocStruct child = docStruct_createChild(parent, type, act, ruleset);
			docStruct_addMetadata(child, identifierField, identifier);
			if (optionalField != null)
				docStruct_addMetadata(child, optionalField, identifier);
			return child;
		}
	}

	/**
	 * @param issues
	 * @param currentYear
	 * @param ownMetsPointerURL
	 * @param act
	 * @param ruleSet
	 * @throws TypeNotAllowedForParentException
	 *             is thrown, if this DocStruct is not allowed for a parent
	 * @throws MetadataTypeNotAllowedException
	 *             if the DocStructType of this DocStruct instance does not
	 *             allow the MetadataType or if the maximum number of Metadata
	 *             (of this type) is already available
	 * @throws TypeNotAllowedAsChildException
	 *             if a child should be added, but it's DocStruct type isn't
	 *             member of this instance's DocStruct type
	 * @throws DocStructHasNoTypeException
	 */
	private static void insertReferencesToOtherIssuesInThisYear(ArrayListMap<LocalDate, String> issues,
			int currentYear, String ownMetsPointerURL, DigitalDocument act, Prefs ruleSet)
			throws TypeNotAllowedForParentException, TypeNotAllowedAsChildException, MetadataTypeNotAllowedException {
		for (int i = 0; i < issues.size(); i++)
			if (issues.getKey(i).getYear() == currentYear && !issues.getValue(i).equals(ownMetsPointerURL))
				insertIssueReference(act, ruleSet, issues.getKey(i), issues.getValue(i));
		return;
	}

	/**
	 * Inserts a reference (METS pointer (mptr) URL) for an issue on a given
	 * date into an act.
	 * 
	 * @param act
	 *            act in whose logical structure the pointer is to create
	 * @param ruleset
	 *            rule set the act is based on
	 * @param date
	 *            date of the issue to create a pointer to
	 * @param metsPointerURL
	 *            URL of the issue
	 * @throws TypeNotAllowedForParentException
	 * @throws TypeNotAllowedAsChildException
	 * @throws MetadataTypeNotAllowedException
	 */
	private static void insertIssueReference(DigitalDocument act, Prefs ruleset, LocalDate date, String metsPointerURL)
			throws TypeNotAllowedForParentException, TypeNotAllowedAsChildException, MetadataTypeNotAllowedException {
		DocStruct year = getOrCreateChild(act.getLogicalDocStruct(), METADATA_ELEMENT_YEAR, METADATA_FIELD_LABEL,
				Integer.toString(date.getYear()), null, act, ruleset);
		DocStruct month = getOrCreateChild(year, METADATA_ELEMENT_MONTH, METADATA_FIELD_ORDERLABEL,
				Integer.toString(date.getYear()), METADATA_FIELD_LABEL, act, ruleset);
		DocStruct day = getOrCreateChild(month, METADATA_ELEMENT_DAY, METADATA_FIELD_ORDERLABEL,
				Integer.toString(date.getYear()), METADATA_FIELD_LABEL, act, ruleset);
		DocStruct issue = docStruct_createChild(day, METADATA_ELEMENT_ISSUE, act, ruleset);
		docStruct_addMetadata(issue, METADATA_FIELD_MPTR, metsPointerURL);
	}

	private void export(MetsModsImportExport extendedData) {
		throw new NotImplementedException("Auto-generated method stub"); // TODO
	}

	/*
	 * TODO: Refactor the functions below into UGHlib
	 */

	/**
	 * The function addMetadata() adds a meta data field with the given name to
	 * this DocStruct and sets it to the given value.
	 * 
	 * TODO move this function into ugh.dl.DocStruct class
	 * 
	 * @param obj
	 *            object this function works on
	 * @param fieldName
	 *            name of the meta data field to add
	 * @param value
	 *            value to set the field to
	 * @return the object to be able to write this in-line
	 * @throws MetadataTypeNotAllowedException
	 *             if no corresponding MetadataType object is returned by
	 *             getAddableMetadataTypes()
	 */
	private static DocStruct docStruct_addMetadata(DocStruct obj, String fieldName, String value)
			throws MetadataTypeNotAllowedException {
		boolean success = false;
		for (MetadataType fieldType : obj.getAddableMetadataTypes()) {
			if (fieldType.getName().equals(fieldName)) {
				Metadata field = new Metadata(fieldType);
				field.setValue(value);
				obj.addMetadata(field);
				success = true;
				break;
			}
		}
		if (!success)
			throw new MetadataTypeNotAllowedException("Couldn’t add " + fieldName + " to " + obj.getType().getName()
					+ ": No corresponding MetadataType object in result of DocStruc.getAddableMetadataTypes().");
		return obj;
	}

	/**
	 * The function createChild() creates a child DocStruct below a DocStruct.
	 * This is a convenience function to add a DocStruct by its type name
	 * string.
	 * 
	 * TODO move this function into ugh.dl.DocStruct class; remove “act”
	 * (available as “digdog”), and perhaps remove Prefs if available otherwise
	 * 
	 * @param obj
	 *            object this function works on
	 * @param type
	 *            structural type of the child to create
	 * @param fieldNames
	 *            list of meta data fields to create in the child (may be empty)
	 * @param value
	 *            value to set the meta data fields to
	 * @param act
	 *            act to create the child in
	 * @param ruleset
	 *            rule set the act is based on
	 * @return the child created
	 * @throws TypeNotAllowedForParentException
	 *             is thrown, if this DocStruct is not allowed for a parent
	 * @throws TypeNotAllowedAsChildException
	 *             if a child should be added, but it's DocStruct type isn't
	 *             member of this instance's DocStruct type
	 */
	private static DocStruct docStruct_createChild(DocStruct obj, String type, DigitalDocument act, Prefs ruleset)
			throws TypeNotAllowedForParentException, TypeNotAllowedAsChildException {
		DocStruct result = act.createDocStruct(ruleset.getDocStrctTypeByName(type));
		obj.addChild(result);
		return result;
	}

	/**
	 * The function getChild() returns a child of a DocStruct, identified by its
	 * type and an identifier in a meta data field of choice. More formally,
	 * returns the first child matching the given conditions and does not work
	 * recursively. If no matching child is found, throws
	 * NoSuchElementException.
	 * 
	 * TODO move this function into ugh.dl.DocStruct class
	 * 
	 * @param obj
	 *            object this function works on
	 * @param type
	 *            structural type of the child to locate
	 * @param identifierField
	 *            meta data field that holds the identifer to locate the child
	 * @param identifier
	 *            identifier of the child to locate
	 * @return the child, if found
	 * @throws NoSuchElementException
	 *             if no matching child is found
	 */
	private static DocStruct docStruct_getChild(DocStruct obj, String type, String identifierField, String identifier)
			throws NoSuchElementException {
		for (DocStruct child : obj.getAllChildrenByTypeAndMetadataType(type, identifierField))
			for (Metadata metadataElement : child.getAllMetadata())
				if (metadataElement.getType().getName().equals(identifierField)
						&& metadataElement.getValue().equals(identifier))
					return child;
		throw new NoSuchElementException("No child " + type + " with " + identifierField + " = " + identifier + " in "
				+ obj + '.');
	}

	/**
	 * The function clone() creates a copy of this CreateProcessesTask for
	 * providing the possibility to restart it because a Thread can only be
	 * started once.
	 * 
	 * @see de.sub.goobi.helper.tasks.CloneableLongRunningTask#clone()
	 */
	@Override
	public CloneableLongRunningTask clone() {
		ExportBatchTask copy = new ExportBatchTask(batch);
		copy.action = action;
		copy.aggregation = aggregation;
		copy.collectedYears = collectedYears;
		copy.processesIterator = processesIterator;
		copy.dividend = dividend;
		copy.divisor = divisor;
		return copy;
	}
}