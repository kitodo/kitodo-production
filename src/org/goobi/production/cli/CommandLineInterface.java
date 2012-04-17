/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.production.cli;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.jar.Manifest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.goobi.production.GoobiVersion;
import org.goobi.production.cli.enums.Command;
import org.goobi.production.cli.helper.CopyProcess;

import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.beans.HistoryEvent;
import de.sub.goobi.beans.Projekt;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.persistence.BenutzergruppenDAO;
import de.sub.goobi.persistence.ProjektDAO;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.persistence.SchrittDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;

public class CommandLineInterface {
	private static CommandLine commandLine = null;
	private static Command command;

	public static void main(String[] args) {
		System.exit(testmain(args));
	}

	public static int testmain(String[] args) {
		CommandLineParser cliParser = new PosixParser();
		Options cliOptions = cliOptions();

		try {
			commandLine = cliParser.parse(cliOptions, args);
		} catch (ParseException e) {
			System.out.println("?ARGS MISSING  ERROR\nREADY.");
			System.out.println("This error should never have happened!");
			return 1;
		}

		// Help.
		if (commandLine.hasOption('h')) {
			HelpFormatter hf = new HelpFormatter();
			hf.defaultWidth = 79;
			hf.printHelp("java -jar goobi.jar [options]", cliOptions);
			return 0;
		}

		// Version.
		if (commandLine.hasOption('V')) {
			try {
				// Use Manifest to setup version information
				Manifest m = getManifestForClass(CommandLineInterface.class);
				GoobiVersion.setupFromManifest(m);

				System.out.println("Goobi version: " + GoobiVersion.getVersion());
				System.out.println("Goobi build date: " + GoobiVersion.getBuilddate());
				System.out.println("Goobi build version: " + GoobiVersion.getBuildversion());
			} catch (Exception e) {
				System.err.println("Cannot obtain version information from MANIFEST file: " + e.getMessage());
				return 1;
			}
			return 0;
		}
		// testing command
		if (!commandLine.hasOption("c")) {
			System.out.println("?ARGS MISSING  ERROR\nREADY.");
			HelpFormatter hf = new HelpFormatter();
			hf.defaultWidth = 79;
			hf.printHelp("java -jar goobi.jar [options]", cliOptions);
			return 1;
		}
		
		command = Command.getByName(commandLine.getOptionValue("c"));
		if (command == null) {
			System.out.println("?UNKNOWN ARGS ERROR\nREADY.");
			String s = "";
			for (Command c : Command.values()) {
				s += " " + c.getName() + " ";
			}
			System.out.println("usable commands: " + s);
			return 2;
		}

		if (command.equals(Command.closeStep)) {
			if (!commandLine.hasOption("s")) {
				System.out.println("?ARGS MISSING  ERROR\nREADY.");
				System.out.println("Missing step id.");
				return 3;
			}
			try {
				Integer stepId = new Integer(commandLine.getOptionValue("s"));
				closeStep(stepId);
			} catch (NumberFormatException e) {
				System.out.println("?UNKNOWN ARGS ERROR\nREADY.");
				System.out.println("given id is not a number.");
				return 4;
			} catch (DAOException e) {
				System.out.println("?UNKNOWN ARGS ERROR\nREADY.");
				System.out.println("no step for given id exists.");
				return 5;
			}
		}

		if (command.equals(Command.exportDms)) {
			if (!commandLine.hasOption("p")) {
				System.out.println("?ARGS MISSING  ERROR\nREADY.");
				System.out.println("Missing process id -p .");
				return 6;
			}
			try {
				Integer processId = new Integer(commandLine.getOptionValue("p"));
				exportToDms(processId);
			} catch (NumberFormatException e) {
				System.out.println("?UNKNOWN ARGS ERROR\nREADY.");
				System.out.println("given id is not a number.");
				return 7;
			} catch (DAOException e) {
				System.out.println("?UNKNOWN ARGS ERROR\nREADY.");
				System.out.println("no process for given id exists.");
				return 8;
			} catch (Exception e) {
				System.out.println("?UNKNOWN EXPORT ERROR\nREADY.");
				return 9;
			}

		}
		
		if (command.equals(Command.addStep)) {
			if (!commandLine.hasOption("P")) {
				System.out.println("?ARGS MISSING  ERROR\nREADY.");
				System.out.println("Missing project id -P");
				return 10;
			}
			if (!commandLine.hasOption("U")) {
				System.out.println("?ARGS MISSING  ERROR\nREADY.");
				System.out.println("Missing UserGroup id -U");
				return 11;
			}
			if (!commandLine.hasOption("S")) {
				System.out.println("?ARGS MISSING  ERROR\nREADY.");
				System.out.println("Missing stepdata -S");
				return 12;
			}
			if (!commandLine.hasOption("O")) {
				System.out.println("?ARGS MISSING  ERROR\nREADY.");
				System.out.println("Missing OrderNumber -O");
				return 13;
			}
			
			try {
				addStep(new Integer(commandLine.getOptionValue("P")), new Integer(commandLine.getOptionValue("U")), generateMap(commandLine.getOptionValue("S")), new Integer(commandLine.getOptionValue("O")));
			} catch (NumberFormatException e) {
				System.out.println("?UNKNOWN ARGS ERROR\nREADY.");
				System.out.println("given id is not a number.");
				return 7;
			} catch (DAOException e) {
				System.out.println("?UNKNOWN ARGS ERROR\nREADY.");
				System.out.println("no process for given id exists.");
				return 8;
			} catch (Exception e) {
				System.out.println("?UNKNOWN EXPORT ERROR\nREADY.");
				return 9;
			}
		}
		if (command.equals(Command.addProcess)) {
			if (!commandLine.hasOption("t")) {
				System.out.println("?ARGS MISSING  ERROR\nREADY.");
				System.out.println("Missing template id -t");
				return 14;
			}
			if (!commandLine.hasOption("i")) {
				System.out.println("?ARGS MISSING  ERROR\nREADY.");
				System.out.println("Missing import folder -i");
				return 15;
			}
			try {
				generateNewProcess(new Integer(commandLine.getOptionValue("t")), commandLine.getOptionValue("i"));
			} catch (NumberFormatException e) {
				System.out.println("?UNKNOWN ARGS ERROR\nREADY.");
				System.out.println("given id is not a number.");
				return 7;
			} catch (DAOException e) {
				System.out.println("?UNKNOWN ARGS ERROR\nREADY.");
				System.out.println("no process for given id exists.");
				return 8;
			} catch (Exception e) {
				System.out.println("?UNKNOWN EXPORT ERROR\nREADY.");
				return 9;
			}
			
			
		}
		return 0;
	}

	private static Manifest getManifestForClass(Class c) throws IOException {
		String className = c.getSimpleName() + ".class";
		String classPath = c.getResource(className).toString();

		if (!classPath.startsWith("jar")) {
			 throw new IOException("Cannot read Manifest file.");
		}

		String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
		return new Manifest(new URL(manifestPath).openStream());
	}

	public static int generateNewProcess(Integer vorlageId, String importFolder) throws ReadException, PreferencesException, SwapException, DAOException, WriteException, IOException, InterruptedException {	
			Prozess vorlage = new ProzessDAO().get(vorlageId);
			File dir = new File(importFolder);

			// Einlesen der mets Daten
			List<String> fileList = new ArrayList<String>();
			if (dir.isDirectory()) {
				String[] files = dir.list();
				for (int i = 0; i < files.length; i++) {
					fileList.add(files[i]);
				}
			}

			// jede Datei durchgehen
			for (String processTitle : fileList) {

				// wenn keine anchor Datei, dann Vorgang anlegen
				if (!processTitle.contains("anchor") && processTitle.endsWith("xml")) {
					CopyProcess form = new CopyProcess();
					form.setProzessVorlage(vorlage);
					form.metadataFile = dir.getAbsolutePath() + File.separator + processTitle;
					form.Prepare();
					form.getProzessKopie().setTitel(processTitle.substring(0, processTitle.length() - 4));
					List<String> digitalCollections = new ArrayList<String>();
					digitalCollections.add("varia");
					form.setDigitalCollections(digitalCollections);
					form.OpacAuswerten();
					Prozess p = form.NeuenProzessAnlegen2();
					if (p.getId() != null) {
						// copy files to new directory
						File images = new File(dir.getAbsoluteFile() + File.separator + processTitle.substring(0, processTitle.length() - 4)
								+ File.separator);
						List<String> imageDir = new ArrayList<String>();
						if (images.isDirectory()) {
							String[] files = images.list();
							for (int i = 0; i < files.length; i++) {
								imageDir.add(files[i]);
							}
							for (String file : imageDir) {
								File image = new File(images,file);
								File dest = new File(p.getImagesOrigDirectory() + image.getName());
								Helper.copyFile(image, dest);
							}
						}
					}

				}
			}

		
		return 0;
	}
	
	private static int exportToDms(Integer processId) throws DAOException, Exception {
		ProzessDAO dao = new ProzessDAO();
		Prozess p = dao.get(processId);
		ExportDms export = new ExportDms();
		export.startExport(p);
		return 0;
	}

	private static int closeStep(Integer stepId) throws DAOException {
		SchrittDAO dao = new SchrittDAO();
		Schritt step = dao.get(stepId);
		HelperSchritte hs = new HelperSchritte();
		step.setEditTypeEnum(StepEditType.AUTOMATIC);
		step.setBearbeitungsstatusEnum(StepStatus.DONE);
		dao.save(step);
		hs.SchrittAbschliessen(step, true);
		return 0;
	}

	private static int addStep(Integer projectId, Integer userGroupId, HashMap<String, String> stepdata, int orderNumber) throws DAOException {
		ProjektDAO pdao = new ProjektDAO();
		ProzessDAO pd = new ProzessDAO();
		BenutzergruppenDAO bendao = new BenutzergruppenDAO();
		Projekt projekt = pdao.get(projectId);

		List<Prozess> plist = new ArrayList<Prozess>();
		plist.addAll(projekt.getProzesse());
		Benutzergruppe b = bendao.get(userGroupId);
		for (Prozess p : plist) {
			boolean added = false;
			List<Schritt> slist = p.getSchritteList();
			Collections.reverse(slist);

			for (Schritt oldStep : slist) {
				if (!added) {
					int oldOrderNumber = oldStep.getReihenfolge();

					if (oldOrderNumber == orderNumber) {
						// create new step
						Schritt newStep = new Schritt();
						newStep.setReihenfolge(oldOrderNumber + 1);
						if (stepdata.get("titel") != null) {
							newStep.setTitel(stepdata.get("titel"));
						}
						if (stepdata.get("prioritaet") != null) {
							newStep.setPrioritaet(new Integer(stepdata.get("prioritaet")));
						}
						if (stepdata.get("typMetadaten") != null) {
							if (stepdata.get("typMetadaten").equals("true")) {
								newStep.setTypMetadaten(true);
							} else {
								newStep.setTypMetadaten(false);
							}
						}
						if (stepdata.get("typAutomatisch") != null) {
							if (stepdata.get("typAutomatisch").equals("true")) {
								newStep.setTypAutomatisch(true);
							} else {
								newStep.setTypAutomatisch(false);
							}
						}
						if (stepdata.get("typImportFileUpload") != null) {
							if (stepdata.get("typImportFileUpload").equals("true")) {
								newStep.setTypImportFileUpload(true);
							} else {
								newStep.setTypImportFileUpload(false);
							}
						}
						if (stepdata.get("typImagesLesen") != null) {
							if (stepdata.get("typImagesLesen").equals("true")) {
								newStep.setTypImagesLesen(true);
							} else {
								newStep.setTypImagesLesen(false);
							}
						}
						if (stepdata.get("typImagesSchreiben") != null) {
							if (stepdata.get("typImagesSchreiben").equals("true")) {
								newStep.setTypImagesSchreiben(true);
							} else {
								newStep.setTypImagesSchreiben(false);
							}
						}
						if (stepdata.get("typExportDMS") != null) {
							if (stepdata.get("typExportDMS").equals("true")) {
								newStep.setTypExportDMS(true);
							} else {
								newStep.setTypExportDMS(false);
							}
						}
						if (stepdata.get("typBeimAnnehmenModul") != null) {
							if (stepdata.get("typBeimAnnehmenModul").equals("true")) {
								newStep.setTypBeimAnnehmenModul(true);
							} else {
								newStep.setTypBeimAnnehmenModul(false);
							}
						}						

						if (stepdata.get("typBeimAnnehmenAbschliessen") != null) {
							if (stepdata.get("typBeimAnnehmenAbschliessen").equals("true")) {
								newStep.setTypBeimAnnehmenAbschliessen(true);
							} else {
								newStep.setTypBeimAnnehmenAbschliessen(false);
							}
						}	
						if (stepdata.get("typScriptStep") != null) {
							if (stepdata.get("typScriptStep").equals("true")) {
								newStep.setTypScriptStep(true);
							} else {
								newStep.setTypScriptStep(false);
							}
						}	
						if (stepdata.get("scriptname1") != null) {
							newStep.setScriptname1(stepdata.get("scriptname1"));
						}
						if (stepdata.get("typAutomatischScriptpfad") != null) {
							newStep.setTypAutomatischScriptpfad(stepdata.get("typAutomatischScriptpfad"));
						}
						if (stepdata.get("scriptname2") != null) {
							newStep.setScriptname2(stepdata.get("scriptname2"));
						}
						if (stepdata.get("typAutomatischScriptpfad2") != null) {
							newStep.setTypAutomatischScriptpfad2(stepdata.get("typAutomatischScriptpfad2"));
						}
						if (stepdata.get("scriptname3") != null) {
							newStep.setScriptname3(stepdata.get("scriptname3"));
						}
						if (stepdata.get("typAutomatischScriptpfad3") != null) {
							newStep.setTypAutomatischScriptpfad3(stepdata.get("typAutomatischScriptpfad3"));
						}
						if (stepdata.get("scriptname4") != null) {
							newStep.setScriptname4(stepdata.get("scriptname4"));
						}
						if (stepdata.get("typAutomatischScriptpfad4") != null) {
							newStep.setTypAutomatischScriptpfad4(stepdata.get("typAutomatischScriptpfad4"));
						}
						if (stepdata.get("scriptname5") != null) {
							newStep.setScriptname5(stepdata.get("scriptname5"));
						}
						if (stepdata.get("typAutomatischScriptpfad5") != null) {
							newStep.setTypAutomatischScriptpfad5(stepdata.get("typAutomatischScriptpfad5"));
						}

						if (stepdata.get("typModulName") != null) {
							newStep.setTypModulName(stepdata.get("typModulName"));
						}
						if (stepdata.get("typBeimAbschliessenVerifizieren") != null) {
							if (stepdata.get("typBeimAbschliessenVerifizieren").equals("true")) {
								newStep.setTypBeimAbschliessenVerifizieren(true);
							} else {
								newStep.setTypBeimAbschliessenVerifizieren(false);
							}
						}	
						
						newStep.setProzess(p);
						newStep.getBenutzergruppen().add(b);
						newStep.setBearbeitungsbeginn(oldStep.getBearbeitungsende());
						newStep.setEditTypeEnum(StepEditType.AUTOMATIC);
						newStep.setBearbeitungsstatusEnum(oldStep.getBearbeitungsstatusEnum());

						if (oldStep.getBearbeitungsstatusEnum().equals(StepStatus.DONE)) {
							newStep.setBearbeitungsstatusEnum(oldStep.getBearbeitungsstatusEnum());
							newStep.setBearbeitungsbenutzer(oldStep.getBearbeitungsbenutzer());
							newStep.setBearbeitungsende(oldStep.getBearbeitungsende());
							p.getHistory().add(
									new HistoryEvent(oldStep.getBearbeitungsende(), new Double(oldOrderNumber + 1).doubleValue(), newStep.getTitel(),
											HistoryEventType.stepDone, p));
						} else {
							newStep.setBearbeitungsstatusEnum(StepStatus.LOCKED);
						}
						p.getSchritte().add(newStep);
						added = true;
					} else {
						oldStep.setReihenfolge(oldOrderNumber + 1);
						List<HistoryEvent> history = p.getHistoryList();
						for (HistoryEvent e : history) {
							if (e.getStringValue() != null && e.getStringValue().equals(oldStep.getTitel())) {
								if (e.getNumericValue() != null && e.getNumericValue() == new Double(oldOrderNumber)) {
									e.setNumericValue(new Double(oldOrderNumber + 1));
								}
							}
						}
					}
				}
			}
			pd.save(p);
		}

		return 0;
	}

	private static HashMap<String, String> generateMap(String param) {
		HashMap<String, String> map = new HashMap<String, String>();
		String[] params = param.split(",");
		for (String p : params) {
			String[] values = p.split(":");
			map.put(values[0], values[1]);
		}
		return map;
	}

	private static Options cliOptions() {
		Options cliOptions = new Options();

		// help
		Option help = new Option("h", "help", false, "Shows this help message");
		cliOptions.addOption(help);

		// command to use
		Option command = new Option("c", "command", true, "commands: closeStep, exportDms , addStep, addProcess");
		command.setArgs(1);
		command.setArgName("command");
		cliOptions.addOption(command);

		// step id
		Option stepId = new Option("s", "stepId", true, "step id");
		stepId.setArgs(1);
		stepId.setArgName("stepId");
		cliOptions.addOption(stepId);

		// process id
		Option processId = new Option("p", "processId", true, "process id");
		processId.setArgs(1);
		processId.setArgName("processId");
		cliOptions.addOption(processId);
		
		// project id
		Option projectId = new Option("P", "projectId", true, "project id");
		projectId.setArgs(1);
		projectId.setArgName("projectId");
		cliOptions.addOption(projectId);
		
		// usergroup id
		Option userGroupId = new Option("U", "userGroupId", true, "usergroup id");
		userGroupId.setArgs(1);
		userGroupId.setArgName("userGroupId");
		cliOptions.addOption(userGroupId);
		
		// stepdata
		Option stepdata = new Option("S", "stepdata", true, "step data, usage: titel:X,typScriptstep:true,...");
		stepdata.setArgs(1);
		stepdata.setArgName("stepdata");
		cliOptions.addOption(stepdata);
		
		// orderNumber
		Option orderNumber = new Option("O", "orderNumber", true, "OrderNumber for step");
		orderNumber.setArgs(1);
		orderNumber.setArgName("orderNumber");
		cliOptions.addOption(orderNumber);

		
		// templateId
		Option templateId = new Option("t", "templateId", true, "template id");
		templateId.setArgs(1);
		templateId.setArgName("templateId");
		cliOptions.addOption(templateId);
		
		// importFolder
		Option importFolder = new Option("i", "importFolder", true, "importFolder");
		importFolder.setArgs(1);
		importFolder.setArgName("importFolder");
		cliOptions.addOption(importFolder);

		
		// Goobi version
		Option version = new Option("V", "version", false, "goobi version");
		cliOptions.addOption(version);

		return cliOptions;
	}
}
