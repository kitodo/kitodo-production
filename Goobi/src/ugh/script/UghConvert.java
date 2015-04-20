package ugh.script;

/*******************************************************************************
 * ugh.script / UghConvert.java
 * 
 * Copyright 2010 Center for Retrospective Digitization, Göttingen (GDZ)
 * 
 * http://gdz.sub.uni-goettingen.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 ******************************************************************************/

import java.io.File;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import ugh.UghCliVersion;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.Prefs;
import ugh.dl.VirtualFileGroup;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import ugh.fileformats.excel.RDFFile;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;
import ugh.fileformats.mets.XStream;
import ugh.fileformats.opac.PicaPlus;

/*******************************************************************************
 * <p>
 * This class converts metadata.
 * </p>
 * <p>
 * Reads METS, RDF, PicaPlus and XStream.
 * </p>
 * <p>
 * Writes METS, DVMETS, RDF and XStream.
 * </p>
 * 
 * @author Stefan Funk
 * @version 2010-05-05
 * @since 2008-09-21
 * 
 *        TODOLOG
 * 
 *        TODO Add switches for mets file and anchor file filenames!!
 * 
 *        TODO Add more Junit tests for UghConvert!!
 * 
 *        TODO Check missing/wrong switches for virtual filegroups!!
 * 
 *        TODO Add contentIDs to this script!!
 * 
 *        TODO Handle "-" for -i and -o arguments as alias for Stdin and Stdout. This requires UGH to handle streams!!
 * 
 *        CHANGELOG
 * 
 *        07.10.2010 --- Sehr --- Added experimental metadata exchange switch.
 * 
 *        05.05.2010 --- Funk --- Added LGPL header.
 * 
 *        10.03.2010 --- Funk --- Added the rightsOwnerContact switch.
 * 
 *        12.02.2010 --- Funk --- Repaired the AMD settings. Fixes bug DPD-399.
 * 
 *        15.01.2010 --- Funk --- Added some null checks for logging.
 * 
 *        08.12.2009 --- Funk --- Corrected some typos. ---Added content filed to log verbose output. --- Catching missing output filename and format
 *        now.
 * 
 *        30.10.2009 --- Funk --- Added UghConvert version. --- Improved log output. --- Added finals for Fileformat strings.
 * 
 *        14.10.2009 --- Funk --- Added virtual file group support for PRESENTATION and LOCAL.
 * 
 *        13.10.2009 --- Funk --- Fixed a bug comncerning the Fileformat's versions.
 * 
 *        06.10.2009 --- Funk --- Added the -q flag, some modifications for better usage. --- Removed internal metadata exchange code. --- Changed
 *        back return code numbers.
 * 
 *        02.10.2009 --- Funk --- Added the anchor flags.
 * 
 *        22.05.2009 --- Funk --- Added some correction implementation lines.
 * 
 *        27.03.2009 --- Funk --- Added MetsModsImportExport.
 * 
 *        16.02.2009 --- Funk --- copied from agora2mets.java.
 * 
 ******************************************************************************/

public class UghConvert {

	public static CommandLine commandLine = null;
	public static DigitalDocument document = null;
	private static boolean foundMetadata = false;

	private static final String METS_CLASSIFIER = "mets";
	private static final String DVMETS_CLASSIFIER = "dvmets";
	private static final String PICAPLUS_CLASSIFIER = "picaplus";
	private static final String RDF_CLASSIFIER = "rdf";
	private static final String XSTREAM_CLASSIFIER = "xstream";

	/***************************************************************************
	 * <p>
	 * Normal main class.
	 * </p>
	 * 
	 * @param args
	 **************************************************************************/
	public static void main(String args[]) {
		System.exit(testmain(args));
	}

	/***************************************************************************
	 * <p>
	 * Test main class, just to return int values for the JUnit tests... does all the work, of course...
	 * </p>
	 * 
	 * @param args
	 **************************************************************************/
	public static int testmain(String args[]) {

		boolean verbose = false;

		//
		// At first parse command line options.
		//
		CommandLineParser cliParser = new PosixParser();
		Options cliOptions = cliOptions();

		// If something goes wrong concerning the CLAs, complain and exit!
		try {
			commandLine = cliParser.parse(cliOptions, args);
		} catch (ParseException e1) {
			// Not thrown never.
			System.out.println("?ARGS MISSING  ERROR\nREADY.");
			System.out.println("This error should never have happened!");

			return 1;
		}

		// Help.
		if (commandLine.hasOption('h')) {
			HelpFormatter hf = new HelpFormatter();
			hf.defaultWidth = 79;
			hf.printHelp("java -jar ughCLI.jar [options]", cliOptions);
			return 0;
		}

		// Version.
		if (commandLine.hasOption('V')) {
			System.out.println("UghCLI version: " + UghCliVersion.VERSION);
			System.out.println("UghCLI build date: " + UghCliVersion.BUILDDATE);
			System.out.println("Fileformat class versions:");
			System.out.println("\t" + MetsMods.class.getSimpleName() + " (mets): " + MetsMods.getVersion());
			System.out.println("\t" + MetsModsImportExport.class.getSimpleName() + " (dvmets): " + MetsModsImportExport.getVersion());
			System.out.println("\t" + PicaPlus.class.getSimpleName() + " (picaplus): " + PicaPlus.getVersion());
			System.out.println("\t" + RDFFile.class.getSimpleName() + " (rdf): " + RDFFile.getVersion());
			System.out.println("\t" + XStream.class.getSimpleName() + " (xstream): " + XStream.getVersion());
			return 0;
		}

		// Check for config file, input file and input format.
		if (!commandLine.hasOption('c') || !commandLine.hasOption('i') || !commandLine.hasOption('r')) {
			System.out.println("?ARGS MISSING  ERROR\nREADY.");
			System.out.println("Please provide at least config filename, input filename and input format (-c -i -r)");
			HelpFormatter hf = new HelpFormatter();
			hf.defaultWidth = 79;
			hf.printHelp("java -jar ughCLI.jar [options]", cliOptions);
			return 2;
		}

		// Verbose.
		if (commandLine.hasOption('v')) {
			verbose = true;
		}

		// Set format to read from.
		String convertFrom = commandLine.getOptionValue('r');
		if (convertFrom == null) {
			System.out.println("?FORMAT MISSING  ERROR\nREADY.");
			System.out.println("Please provide an input format string");
			return 3;
		}
		// Formats: "mets", "rdf", "xstream", "picaplus".
		if (!(convertFrom.equals(METS_CLASSIFIER) || convertFrom.equals(RDF_CLASSIFIER) || convertFrom.equals(XSTREAM_CLASSIFIER) || convertFrom
				.equals(PICAPLUS_CLASSIFIER))) {
			System.out.println("?FORMAT NOT READABLE  ERROR\nREADY.");
			System.out.println("Input format '" + convertFrom + "' can not be read using the ugh metadata subsystem");
			return 4;
		}

		// Set format to write to.
		String convertTo = commandLine.getOptionValue('w');
		// Formats: "mets", "dvmets", "rdf", "xstream".
		if (convertTo != null) {
			if (!(convertTo.equals(METS_CLASSIFIER) || convertTo.equals(DVMETS_CLASSIFIER) || convertTo.equals(RDF_CLASSIFIER) || convertTo
					.equals(XSTREAM_CLASSIFIER))) {
				System.out.println("?FORMAT NOT WRITABLE  ERROR\nREADY.");
				System.out.println("Input format '" + convertTo
						+ "' can not be written using the ugh metadata subsystem. Please use one of mets, dvmets, rdf, or xstream");
				return 5;
			}
		}

		// Set input file.
		File inputFile = new File(commandLine.getOptionValue('i'));
		if (!inputFile.exists()) {
			System.out.println("?FILE NOT FOUND  ERROR\nREADY.");
			System.out.println("Input file '" + commandLine.getOptionValue('i') + "' can not be found");
			return 6;
		}

		// Set and load config file.
		File configFile = new File(commandLine.getOptionValue('c'));
		if (!configFile.exists()) {
			System.out.println("?FILE NOT FOUND  ERROR\nREADY.");
			System.out.println("Config file '" + commandLine.getOptionValue('c') + "' can not be found");
			return 7;
		}
		Prefs preferences = new Prefs();
		System.out.println("Loading config file '" + configFile.getAbsolutePath() + "'");
		try {
			preferences.loadPrefs(configFile.getAbsolutePath());
		} catch (PreferencesException e) {
			System.out.println("?READ  ERROR\nREADY.");
			System.out.println("Unable to parse config file");
			return 8;
		}

		//
		// Now check which format to read.
		//
		Fileformat fileFrom = null;
		String inputVersion = "";
		try {
			if (convertFrom.equals(DVMETS_CLASSIFIER)) {
				fileFrom = new MetsModsImportExport(preferences);
				inputVersion = MetsModsImportExport.class.getName() + " " + MetsModsImportExport.getVersion();
			} else if (convertFrom.equals(METS_CLASSIFIER)) {
				fileFrom = new MetsMods(preferences);
				inputVersion = MetsMods.class.getName() + " " + MetsMods.getVersion();
			} else if (convertFrom.equals(RDF_CLASSIFIER)) {
				fileFrom = new RDFFile(preferences);
				inputVersion = RDFFile.class.getName() + " " + RDFFile.getVersion();
			} else if (convertFrom.equals(XSTREAM_CLASSIFIER)) {
				fileFrom = new XStream(preferences);
				inputVersion = XStream.class.getName() + " " + XStream.getVersion();
			} else if (convertFrom.equals(PICAPLUS_CLASSIFIER)) {
				fileFrom = new PicaPlus(preferences);
				inputVersion = PicaPlus.getVersion();
			}
		} catch (PreferencesException e) {
			System.out.println("?READ  ERROR\nREADY.");
			System.out.println("Unable to parse config file");
			return 9;
		}

		//
		// Change value of metadata.
		//
		if (commandLine.hasOption('m')) {
			// Missing parameter.
			if (commandLine.getOptionValue("m") == null || commandLine.getOptionValue("nmv") == null) {
				System.out.println("Missing required input.");
				System.out.println("m - internal metadata name or nmv - new value is null");
				return 20;
			} else {
				// Internal metadata name.
				String metadataName = commandLine.getOptionValue("m");
				// New value.
				String newValue = commandLine.getOptionValue("nmv");
				System.out.println("change value for metadata " + metadataName + " to " + newValue);
				try {
					fileFrom.read(commandLine.getOptionValue('i'));
					DigitalDocument myDocument = fileFrom.getDigitalDocument();
					DocStruct log = myDocument.getLogicalDocStruct();
					// Search for metadata.
					changeMetadataValue(log, metadataName, newValue);
					fileFrom.write(commandLine.getOptionValue('i'));
					return 0;
				} catch (WriteException e) {
					System.out.println("error while saving new file");
				} catch (PreferencesException e) {
					System.out.println("error while saving new file");
				} catch (ReadException e) {
					System.out.println("error on reading file");
				}
			}
		}
		//
		// Print version and class name.
		//
		if (!commandLine.hasOption('q')) {
			System.out.println("Input class name and version is '" + inputVersion + "'");
		}

		//
		// Read the file and get the DigitalDocument.
		//
		boolean readOutcome = false;
		try {
			if (!commandLine.hasOption('q')) {
				System.out.println("Reading source file '" + inputFile.getAbsolutePath() + "'");
			}
			readOutcome = fileFrom.read(inputFile.getAbsolutePath());
			document = fileFrom.getDigitalDocument();
		} catch (ReadException e) {
			System.out.println("?READ  ERROR\nREADY.");
			System.out.println("Unable to parse input file '" + inputFile.getAbsolutePath() + "'");
			return 10;
		} catch (PreferencesException e) {
			System.out.println("?READ  ERROR\nREADY.");
			System.out.println("Unable to parse config file");
			return 11;
		}

		if (readOutcome) {
			if (!commandLine.hasOption('q')) {
				System.out.println(convertFrom + " file '" + inputFile.getAbsolutePath() + "' read");
			}
		} else {
			System.out.println("?READ  ERROR\nREADY.");
			System.out.println(convertFrom + " file '" + inputFile.getAbsolutePath() + "' could not be read");
			return 12;
		}

		// Give some verbose output.
		if (verbose) {
			System.out.println(document);
		}

		// TODO REMOVE DEBUG OUTPUT
		//
		// if (document.getLogicalDocStruct() != null) {
		// ugh.dl.DocStruct e = document.getLogicalDocStruct();
		//
		// for (DocStruct ds : e.getAllChildren()) {
		// for (DocStruct d : ds.getAllChildren()) {
		// if (d.getType().getName().equals("PeriodicalIssue")) {
		// System.out
		// .println("############################################################");
		// System.out.println("##  AddableMetadataTypes for "
		// + d.getType().getName());
		// System.out
		// .println("############################################################");
		// if (d.getAddableMetadataTypes() != null) {
		// for (ugh.dl.MetadataType m : d
		// .getAddableMetadataTypes()) {
		// System.out.println(m.getName());
		// }
		// }
		// System.out
		// .println("############################################################");
		// System.out
		// .println("##  DefaultDisplayMetadataTypes for "
		// + d.getType().getName());
		// System.out
		// .println("############################################################");
		// if (d.getDefaultDisplayMetadataTypes() != null) {
		// for (ugh.dl.MetadataType m : d
		// .getDefaultDisplayMetadataTypes()) {
		// System.out.println(m.getName());
		// }
		// }
		// System.out
		// .println("############################################################");
		// System.out.println("##  AllMetadata for "
		// + d.getType().getName());
		// System.out
		// .println("############################################################");
		// if (d.getAllMetadata() != null) {
		// for (ugh.dl.Metadata m : d.getAllMetadata()) {
		// System.out.println(m.getType().getName());
		// }
		// }
		// System.out
		// .println("############################################################");
		// System.out.println("##  AllVisibleMetadata for "
		// + d.getType().getName());
		// System.out
		// .println("############################################################");
		// if (d.getAllVisibleMetadata() != null) {
		// for (ugh.dl.Metadata m : d.getAllVisibleMetadata()) {
		// System.out.println(m.getType().getName());
		// }
		// }
		// }
		// }
		// }
		// }
		//
		// TODO REMOVE DEBUG OUTPUT

		// Return if no output file specified.
		if (convertTo == null) {
			return 0;
		}

		// Check for output file and output format.
		if (!commandLine.hasOption('o') || !commandLine.hasOption('w')) {
			System.out.println("?ARGS MISSING  ERROR\nREADY.");
			System.out.println("Please provide output filename and output format (-o -w)");
			HelpFormatter hf = new HelpFormatter();
			hf.defaultWidth = 79;
			hf.printHelp("java -jar ughCLI.jar [options]", cliOptions);
			return 17;
		}

		// Set output file.
		File outputFile = new File(commandLine.getOptionValue('o'));

		//
		// Check which format to write.
		//
		Fileformat fileTo = null;
		String outputVersion = "";
		try {
			if (convertTo.equals(DVMETS_CLASSIFIER)) {
				fileTo = new MetsModsImportExport(preferences);
				setMetsSpecificContent((MetsModsImportExport) fileTo);
				outputVersion = MetsModsImportExport.class.getName() + " " + MetsModsImportExport.getVersion();
			} else if (convertTo.equals(METS_CLASSIFIER)) {
				fileTo = new MetsMods(preferences);
				outputVersion = MetsMods.class.getName() + " " + MetsMods.getVersion();
			} else if (convertTo.equals(RDF_CLASSIFIER)) {
				fileTo = new RDFFile(preferences);
				outputVersion = RDFFile.class.getName() + " " + RDFFile.getVersion();
			} else if (convertTo.equals(XSTREAM_CLASSIFIER)) {
				fileTo = new XStream(preferences);
				outputVersion = XStream.class.getName() + " " + XStream.getVersion();
			}
		} catch (PreferencesException e) {
			e.printStackTrace();
			System.out.println("?WRITE  ERROR\nREADY.");
			System.out.println("Unable to parse config file");
			return 13;
		}

		//
		// Write the output version.
		//
		if (!commandLine.hasOption('q')) {
			System.out.println("Output class name and version is '" + outputVersion + "'");
		}

		//
		// Set the DigitalDocument and write the file.
		//
		boolean writeOutcome = false;
		try {
			// Set the digital document.
			fileTo.setDigitalDocument(document);

			if (verbose && document.getFileSet() != null) {
				for (VirtualFileGroup v : document.getFileSet().getVirtualFileGroups()) {
					System.out.println("Creating filegroup '" + v.getName() + "'");
				}
			}

			writeOutcome = fileTo.write(outputFile.getAbsolutePath());
		} catch (PreferencesException e) {
			e.printStackTrace();
			System.out.println("?READ  ERROR\nREADY.");
			System.out.println("Unable to parse config file");
			return 14;
		} catch (WriteException e) {
			e.printStackTrace();
			System.out.println("?WRITE  ERROR\nREADY.");
			System.out.println("Unable to write " + convertTo + " file");
			return 15;
		}

		if (writeOutcome) {
			if (!commandLine.hasOption('q')) {
				System.out.println(convertTo + " file '" + outputFile.getAbsolutePath() + "' written");
			}
		} else {
			System.out.println("?WRITE  ERROR\nREADY.");
			System.out.println(convertTo + " file '" + outputFile.getAbsolutePath() + "' could not be written");
			return 16;
		}

		return 0;
	}

	/***************************************************************************
	 * <p>
	 * </p>
	 * 
	 * @param theDocstruct
	 * @param theMetadataName
	 * @param theValue
	 * @return
	 **************************************************************************/
	private static boolean changeMetadataValue(DocStruct theDocstruct, String theMetadataName, String theValue) {

		List<Metadata> mdlist = theDocstruct.getAllMetadata();
		for (Metadata md : mdlist) {
			if (md.getType().getName().equals(theMetadataName)) {
				md.setValue(theValue);
				foundMetadata = true;
				break;
			}
		}
		if (!foundMetadata) {
			List<DocStruct> children = theDocstruct.getAllChildren();
			if (children != null && children.size() != 0) {
				for (DocStruct ds : children) {
					foundMetadata = changeMetadataValue(ds, theMetadataName, theValue);
				}
			}
		}

		return foundMetadata;
	}

	/***************************************************************************
	 * <p>
	 * The command line interface options are created here.
	 * </p>
	 * 
	 * @return The command line interface options.
	 **************************************************************************/
	private static Options cliOptions() {
		Options cliOptions = new Options();

		// Defines the CLI options using the following arguments:
		// Short option string, long option string, has arguments boolean,
		// description string.

		Option convertFrom = new Option("r", "read", false, "The format to convert/read from (" + DVMETS_CLASSIFIER + ", " + PICAPLUS_CLASSIFIER
				+ ", " + RDF_CLASSIFIER + ", " + XSTREAM_CLASSIFIER + ")");
		convertFrom.setArgs(1);
		convertFrom.setArgName("format");
		cliOptions.addOption(convertFrom);

		Option convertTo = new Option("w", "write", false, "The format to convert/write to (" + DVMETS_CLASSIFIER + ", " + METS_CLASSIFIER + ", "
				+ RDF_CLASSIFIER + ", " + XSTREAM_CLASSIFIER + ")");
		convertTo.setArgs(1);
		convertTo.setArgName("format");
		cliOptions.addOption(convertTo);

		Option inputFile = new Option("i", "input", true, "Input filename");
		inputFile.setArgs(1);
		inputFile.setArgName("file");
		cliOptions.addOption(inputFile);

		Option outputFile = new Option("o", "output", true, "Output filename");
		outputFile.setArgs(1);
		outputFile.setArgName("file");
		cliOptions.addOption(outputFile);

		Option config = new Option("c", "config", false, "Ruleset/Prefs config filename");
		config.setArgs(1);
		config.setArgName("file");
		cliOptions.addOption(config);

		Option verbose = new Option("v", "verbose", false, "Gives more output");
		cliOptions.addOption(verbose);

		Option version = new Option("V", "version", false, "Versions of the existing Fileformat classes");
		cliOptions.addOption(version);

		Option help = new Option("h", "help", false, "Shows this help message");
		cliOptions.addOption(help);

		Option quite = new Option("q", "quiet", false, "Does not print any information");
		cliOptions.addOption(quite);

		// METSRIGHTS
		Option mro = new Option("mro", "metsrightsowner", false, "METS rights owner");
		mro.setArgs(1);
		mro.setArgName("owner");
		cliOptions.addOption(mro);

		Option mrc = new Option("mrc", "metsrightscontact", false, "METS rights owner contact");
		mrc.setArgs(1);
		mrc.setArgName("contact");
		cliOptions.addOption(mrc);

		Option mrl = new Option("mrl", "metsrightslogo", false, "METS rights owner logo");
		mrl.setArgs(1);
		mrl.setArgName("url");
		cliOptions.addOption(mrl);

		Option mru = new Option("mru", "metsrightsurl", false, "METS rights owner URL");
		mru.setArgs(1);
		mru.setArgName("url");
		cliOptions.addOption(mru);

		// METSDIGIPROV
		Option mdr = new Option("mdr", "metsdigiprovreference", false, "METS digiprov reference");
		mdr.setArgs(1);
		mdr.setArgName("url");
		cliOptions.addOption(mdr);

		Option mdp = new Option("mdp", "metsdigiprovpresentation", false, "METS digiprov presentation reference");
		mdp.setArgs(1);
		mdp.setArgName("url");
		cliOptions.addOption(mdp);

		Option mdra = new Option("mdra", "metsdigiprovreferenceanchor", false, "METS digiprov anchor reference");
		mdra.setArgs(1);
		mdra.setArgName("url");
		cliOptions.addOption(mdra);

		Option mdpa = new Option("mdpa", "metsdigiprovpresentationanchor", false, "METS digiprov anchor presentation reference");
		mdpa.setArgs(1);
		mdpa.setArgName("url");
		cliOptions.addOption(mdpa);

		// FILEGROUP PRESENTATION
		Option ppre = new Option("ppre", "presentationpath", false, "METS file group PRESENTATION path name");
		ppre.setArgs(1);
		ppre.setArgName("path");
		cliOptions.addOption(ppre);

		Option spre = new Option("spre", "presentationidsuffix", false, "METS file group PRESENTATION id suffix");
		spre.setArgs(1);
		spre.setArgName("idSuffix");
		cliOptions.addOption(spre);

		Option mpre = new Option("mpre", "presentationmimetype", false, "METS file group PRESENTATION mimetype");
		mpre.setArgs(1);
		mpre.setArgName("mimetype");
		cliOptions.addOption(mpre);

		Option fpre = new Option("fpre", "presentationfilesuffix", false, "METS file group PRESENTATION filesuffix");
		fpre.setArgs(1);
		fpre.setArgName("filesuffix");
		cliOptions.addOption(fpre);

		// FILEGROUP MIN
		Option pmin = new Option("pmin", "minpath", false, "METS file group MIN path name");
		pmin.setArgs(1);
		pmin.setArgName("path");
		cliOptions.addOption(pmin);

		Option smin = new Option("smin", "minidsuffix", false, "METS file group MIN id suffix");
		smin.setArgs(1);
		smin.setArgName("idSuffix");
		cliOptions.addOption(smin);

		Option mmin = new Option("mmin", "minmimetype", false, "METS file group MIN mimetype");
		mmin.setArgs(1);
		mmin.setArgName("mimetype");
		cliOptions.addOption(mmin);

		Option fmin = new Option("fmin", "minfilesuffix", false, "METS file group MIN filesuffix");
		fmin.setArgs(1);
		fmin.setArgName("filesuffix");
		cliOptions.addOption(fmin);

		// FILEGROUP MAX
		Option pmax = new Option("pmax", "maxpath", false, "METS file group MAX path name");
		pmax.setArgs(1);
		pmax.setArgName("path");
		cliOptions.addOption(pmax);

		Option smax = new Option("smax", "maxidsuffix", false, "METS file group MAX id suffix");
		smax.setArgs(1);
		smax.setArgName("idSuffix");
		cliOptions.addOption(smax);

		Option mmax = new Option("mmax", "maxmimetype", false, "METS file group MAX mimetype");
		mmax.setArgs(1);
		mmax.setArgName("mimetype");
		cliOptions.addOption(mmax);

		Option fmax = new Option("fmax", "maxfilesuffix", false, "METS file group MAX filesuffix");
		fmax.setArgs(1);
		fmax.setArgName("filesuffix");
		cliOptions.addOption(fmax);

		// FILEGROUP THUMBS
		Option pthb = new Option("pthb", "thumbspath", false, "METS file group THUMBS path name");
		pthb.setArgs(1);
		pthb.setArgName("path");
		cliOptions.addOption(pthb);

		Option sthb = new Option("sthb", "thumbsidsuffix", false, "METS file group THUMBS id suffix");
		sthb.setArgs(1);
		sthb.setArgName("idSuffix");
		cliOptions.addOption(sthb);

		Option mthb = new Option("mthb", "thumbsmimetype", false, "METS file group THUMBS mimetype");
		mthb.setArgs(1);
		mthb.setArgName("mimetype");
		cliOptions.addOption(mthb);

		Option fthb = new Option("fthb", "thumbsfilesuffix", false, "METS file group THUMBS filesuffix");
		fthb.setArgs(1);
		fthb.setArgName("filesuffix");
		cliOptions.addOption(fthb);

		// FILEGROUP DOWNLOAD
		Option pdwl = new Option("pdwl", "downloadpath", false, "METS file group DOWNLOAD path name");
		pdwl.setArgs(1);
		pdwl.setArgName("path");
		cliOptions.addOption(pdwl);

		Option sdwl = new Option("sdwl", "downloadidsuffix", false, "METS file group DOWNLOAD id suffix");
		sdwl.setArgs(1);
		sdwl.setArgName("idSuffix");
		cliOptions.addOption(sdwl);

		Option mdwl = new Option("mdwl", "downloadmimetype", false, "METS file group DOWNLOAD mimetype");
		mdwl.setArgs(1);
		mdwl.setArgName("mimetype");
		cliOptions.addOption(mdwl);

		Option fdwl = new Option("fdwl", "downloadfilesuffix", false, "METS file group DOWNLOAD filesuffix");
		fdwl.setArgs(1);
		fdwl.setArgName("filesuffix");
		cliOptions.addOption(fdwl);

		// FILEGROUP DEFAULT
		Option pdef = new Option("pdef", "defaultpath", false, "METS file group DEFAULT path name");
		pdef.setArgs(1);
		pdef.setArgName("path");
		cliOptions.addOption(pdef);

		Option mdef = new Option("mdef", "defaultmimetype", false, "METS file group DEFAULT mimetype");
		mdef.setArgs(1);
		mdef.setArgName("mimetype");
		cliOptions.addOption(mdef);

		Option sdef = new Option("sdef", "defaultidsuffix", false, "METS file group DEFAULT id suffix");
		sdef.setArgs(1);
		sdef.setArgName("idSuffix");
		cliOptions.addOption(sdef);

		Option fdef = new Option("fdef", "defaultfilesuffix", false, "METS file group DEFAULT filesuffix");
		fdef.setArgs(1);
		fdef.setArgName("filesuffix");
		cliOptions.addOption(fdef);

		// FILEGROUP DEFAULT
		Option ploc = new Option("ploc", "localpath", false, "METS file group LOCAL path name");
		ploc.setArgs(1);
		ploc.setArgName("path");
		cliOptions.addOption(ploc);

		Option mloc = new Option("mloc", "localmimetype", false, "METS file group LOCAL mimetype");
		mloc.setArgs(1);
		mloc.setArgName("mimetype");
		cliOptions.addOption(mloc);

		Option sloc = new Option("sloc", "localidsuffix", false, "METS file group LOCAL id suffix");
		sloc.setArgs(1);
		sloc.setArgName("idSuffix");
		cliOptions.addOption(sloc);

		Option floc = new Option("floc", "localfilesuffix", false, "METS file group LOCAL filesuffix");
		floc.setArgs(1);
		floc.setArgName("filesuffix");
		cliOptions.addOption(floc);

		// Change value of metadata.
		Option metadata = new Option("m", "metadatatypename", true, "Name of metadata type to change (experimental)");
		inputFile.setArgs(1);
		inputFile.setArgName("metadata");
		cliOptions.addOption(metadata);

		Option value = new Option("nmv", "newmetadatavalue", true, "New value for metadata type (experimental)");
		inputFile.setArgs(1);
		inputFile.setArgName("value");
		cliOptions.addOption(value);

		return cliOptions;
	}

	/***************************************************************************
	 * @param theMets
	 * @return
	 **************************************************************************/
	private static MetsMods setMetsSpecificContent(MetsMods theMets) {

		MetsMods result = theMets;

		// Handle all the filegroups.
		setVirtualFilegroup("DEFAULT", "pdef", "mdef", "fdef", "sdef");
		setVirtualFilegroup("MIN", "pmin", "mmin", "fmin", "smin");
		setVirtualFilegroup("MAX", "pmax", "mmax", "fmax", "smax");
		setVirtualFilegroup("THUMBS", "pthb", "mthb", "fthb", "sthb");
		setVirtualFilegroup("DOWNLOAD", "pdwl", "mdwl", "fdwl", "sdwl");
		setVirtualFilegroup("PRESENTATION", "ppre", "mpre", "fpre", "spre");
		setVirtualFilegroup("LOCAL", "ploc", "mloc", "floc", "sloc");

		// Handle the mets rights and digiprov things.
		if (theMets instanceof MetsModsImportExport) {
			MetsModsImportExport newMets = (MetsModsImportExport) theMets;
			setMetsRights(newMets);
			setMetsDigiprov(newMets);
			theMets = newMets;
		}

		return result;
	}

	/***************************************************************************
	 * @param theName
	 * @param thePath
	 * @param theMimetype
	 * @param theFileSuffix
	 * @param theIdSuffix
	 **************************************************************************/
	private static void setVirtualFilegroup(String theName, String thePath, String theMimetype, String theFileSuffix, String theIdSuffix) {

		String path = commandLine.getOptionValue(thePath);
		String mimetype = commandLine.getOptionValue(theMimetype);
		String fileSuffix = commandLine.getOptionValue(theFileSuffix);
		String idSuffix = commandLine.getOptionValue(theIdSuffix);

		VirtualFileGroup v = new VirtualFileGroup();
		v.setName(theName);

		if (path != null) {
			v.setPathToFiles(path);
		}
		if (mimetype != null) {
			v.setMimetype(mimetype);
		}
		if (fileSuffix != null) {
			v.setFileSuffix(fileSuffix);
		}
		if (idSuffix != null) {
			v.setIdSuffix(idSuffix);
		}

		if (path != null || mimetype != null || fileSuffix != null || idSuffix != null) {
			document.getFileSet().addVirtualFileGroup(v);
		}
	}

	/***************************************************************************
	 * @param theMets
	 **************************************************************************/
	private static void setMetsRights(MetsModsImportExport theMets) {

		if (commandLine.getOptionValue("mro") != null) {
			theMets.setRightsOwner(commandLine.getOptionValue("mro"));
		}
		if (commandLine.getOptionValue("mrc") != null) {
			theMets.setRightsOwnerContact(commandLine.getOptionValue("mrc"));
		}
		if (commandLine.getOptionValue("mrl") != null) {
			theMets.setRightsOwnerLogo(commandLine.getOptionValue("mrl"));
		}
		if (commandLine.getOptionValue("mru") != null) {
			theMets.setRightsOwnerSiteURL(commandLine.getOptionValue("mru"));
		}
	}

	/***************************************************************************
	 * @param theMets
	 **************************************************************************/
	private static void setMetsDigiprov(MetsModsImportExport theMets) {

		if (commandLine.getOptionValue("mdr") != null) {
			theMets.setDigiprovReference(commandLine.getOptionValue("mdr"));
		}
		if (commandLine.getOptionValue("mdp") != null) {
			theMets.setDigiprovPresentation(commandLine.getOptionValue("mdp"));
		}
		if (commandLine.getOptionValue("mdra") != null) {
			theMets.setDigiprovReferenceAnchor(commandLine.getOptionValue("mdra"));
		}
		if (commandLine.getOptionValue("mdpa") != null) {
			theMets.setDigiprovPresentationAnchor(commandLine.getOptionValue("mdpa"));
		}
	}

}
