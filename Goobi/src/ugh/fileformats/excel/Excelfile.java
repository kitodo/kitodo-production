package ugh.fileformats.excel;

/*******************************************************************************
 * ugh.fileformats.exel / Excelfile.java
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.FileSet;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Reference;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;

/*******************************************************************************
 * <p>
 * Title: UGH - DMS tools and system utilities
 * </p>
 * 
 * <p>
 * Description: read and writes rdf/xml files from GDZ
 * </p>
 * 
 * @author Markus Enders
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 * @version 2014-06-18
 * @since 2002
 * @deprecated
 * 
 *             TODOLOG
 * 
 *             TODO Calculate shortcutdoc (ATS, TSL)
 * 
 *             TODO Calculate TIFF Header
 * 
 *             TODO Calculate image path (from PPN, TIFF Header, volume number)
 * 
 *             CHANGELOG
 *             
 *             18.06.2014 --- Ronge --- Change anchor to be string value & create more files when necessary
 * 
 *             22.01.2010 --- Funk --- Minor improvements due to findbugs.
 * 
 *             18.01.2010 --- Funk --- Adapted class to changed
 *             DocStruct.getAllMetadataByType().
 * 
 *             21.12.2009 --- Funk --- Added some "? extends " to metadata
 *             things.
 * 
 *             09.12.2009 --- Funk --- Refactored some variable names.
 * 
 *             20.10.2009 --- Funk --- Deleted unneeded variable declarattion.
 * 
 *             09.10.2009 --- Funk --- Changed the deprecated anotations.
 * 
 *             05.10.2009 --- Funk --- Adapted metadata and person constructors.
 * 
 ******************************************************************************/
@Deprecated
public class Excelfile implements ugh.dl.Fileformat {

	private static final String							VERSION			= "1.0-20100118";

	// UGH Document.
	private ugh.dl.DigitalDocument						mydoc;
	// Imageset.
	private ugh.dl.FileSet								myImageset;
	// General preferences.
	private ugh.dl.Prefs								myPreferences;
	private org.apache.poi.hssf.usermodel.HSSFWorkbook	excelworkbook;
	// Version of excel sheet.
	private String										excel_version;
	// All PaginationSequences instances.
	private List<PaginationSequence>					allPaginations;
	// All Documentstures.
	private final List<DocStruct>						allDocStruct	= new LinkedList<DocStruct>();

	// Cell coordinates of allDocStruct.
	private final List<String>							allStructRow	= new LinkedList<String>();
	private final List<String>							allStructSheets	= new LinkedList<String>();

	// Hashtables are used for matching the internal Name of metadata and
	// docstructs to the name used in the Excel sheet. The contents is read from
	// the preferences in readPrefs method.
	private Hashtable<String, String>					excelNamesMD;
	private Hashtable<String, String>					excelGliederungMD;
	private Hashtable<String, String>					excelAbbMD;
	private Hashtable<String, String>					excelNamesDS;
	private Hashtable<String, String>					excelGliederungDS;
	private Hashtable<String, String>					excelAbbDS;

	/***************************************************************************
	 * Comment for <code>ELEMENT_NODE</code>
	 **************************************************************************/
	public static final short							ELEMENT_NODE	= 1;

	/***************************************************************************
	 * @param inPrefs
	 * @throws PreferencesException
	 **************************************************************************/
	public Excelfile(ugh.dl.Prefs inPrefs) throws PreferencesException {
		Node excelNode = null;

		this.myPreferences = inPrefs;
		this.mydoc = new DigitalDocument();
		this.excelNamesMD = new Hashtable<String, String>();
		this.excelGliederungMD = new Hashtable<String, String>();
		this.excelAbbMD = new Hashtable<String, String>();

		this.excelNamesDS = new Hashtable<String, String>();
		this.excelGliederungDS = new Hashtable<String, String>();
		this.excelAbbDS = new Hashtable<String, String>();

		// Read preferences.
		excelNode = inPrefs.getPreferenceNode("Excel");
		if (excelNode == null) {
			System.err
					.println("ERROR: Can't read preferences for Excel fileformat.");
			System.err.println("       node in XML-file not found!");
		} else {
			readPrefs(excelNode);
		}

		// Create a FileSet, Physical tree. These information is not retrieved
		// from the Excel sheet because they are not represented by an Excel
		// sheet.
		DocStructType dst = this.myPreferences
				.getDocStrctTypeByName("BoundBook");
		DocStruct boundBook = null;
		try {
			boundBook = this.mydoc.createDocStruct(dst);
		} catch (TypeNotAllowedForParentException tnafpe) {
			System.err
					.println("ERROR: Excelfile: BoundBook as physical type is not available or not allowed as root.");
			System.err.println("Check config-file...");
			return;
		}

		this.mydoc.setPhysicalDocStruct(boundBook);
		FileSet fs = new FileSet();
		this.mydoc.setFileSet(fs);
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public static String getVersion() {
		return VERSION;
	}

	/***************************************************************************
	 * Update the current excel instance; write it to new filename
	 * 
	 * @param filename
	 **************************************************************************/
	@Override
	public boolean update(String filename) {
		// Get metadata on topmost level - this one is in the
		// Bibliographic-table.
		DocStruct topmost = this.mydoc.getLogicalDocStruct();
		if (topmost == null) {
			return false;
		}
		if (!UpdateAllMetadata(topmost, true)) {
			return false;
		}

		// Write excel workbook again open file.
		FileOutputStream excelFile;
		// Get output stream.
		try {
			excelFile = new FileOutputStream(filename);
			this.excelworkbook.write(excelFile);
		} catch (Exception e) {
			System.err.println("ERROR: Can't write file " + filename);
			System.err.println(e);
			return false;
		}

		return true;
	}

	/***************************************************************************
	 * Updates all metadata for a single logcical structure entity, but only, if
	 * metadata needs to be updated The wasUpdated method is used for determine
	 * the necessity.
	 * 
	 * @param inStruct
	 * @param reverse
	 *            must be set to true, if metadata of all children should be
	 *            updated, too.
	 * @return true, if update was successful, in other cases false
	 **************************************************************************/
	private boolean UpdateAllMetadata(DocStruct inStruct, boolean reverse) {
		// We begin in the third row.
		boolean ret = UpdateAllMetadata(inStruct, reverse, 0);

		return ret;
	}

	/***************************************************************************
	 * @param inStruct
	 * @param reverse
	 * @param rowCounter
	 * @return
	 **************************************************************************/
	private boolean UpdateAllMetadata(DocStruct inStruct, boolean reverse,
			int rowCounter) {
		// First, check in which row we have to store information use
		// information from allDocStruct and allStructRow which had been filled
		// while reading the excel spredsheet.
		String currentsheet = null;
		int currentrow = 0;
		// We have to put it into comment, because allDocStruct.size is always
		// different from allStructRow.size, because maindocstruct and anchor
		// are also in the allDocStruct.
		for (int j = 0; j < this.allDocStruct.size(); j++) {
			DocStruct singledoc = this.allDocStruct.get(j);
			if (singledoc.equals(inStruct)) {
				// It is the correct docstruct, so get row and sheet.
				currentrow = Integer.parseInt(this.allStructRow.get(j));
				currentsheet = this.allStructSheets.get(j);
				break;
			}
		}
		if (currentrow == 0) {
			// We haven't found the inStruct in the structure maybe inStruct was
			// added after reading the excel sheet.
			return false;
		}
		if (currentsheet == null) {
			System.err.println("DEBUG: unknown sheet in Excel file");
			return false;
		}
		if (currentsheet.equals("Bibliographie")) {
			System.out.println("updating in Sheet \"Bibliographie\"!");
		}
		if (currentsheet.equals("Gliederung")) {
			List<Metadata> allMD = inStruct.getAllMetadata();
			// Count the row in the excel spreadsheet.
			rowCounter++;
			for (int i = 0; i < allMD.size(); i++) {
				Metadata md = allMD.get(i);
				MetadataType mdt = md.getType();
				if (mdt == null) {
					return false;
				}
				if (md.getType().getName().startsWith("_")) {
					// It's internal metadata, so we have to get out of loop, we
					// do not have to store internal metadata in excel sheet.
					continue;
				}
				if (md.wasUpdated()) {
					// Metadata field was updated; we have to update the
					// spreadsheet cell.
					Object mdnatobj = md.getNativeObject();
					if (mdnatobj == null) {
						// No object is available.
						if (md.getValue() != null) {
							// We have no cell, but a metadata value; so we have
							// to find the cell (column) and add it to the cell.
							HSSFSheet inSheet = this.excelworkbook
									.getSheet(currentsheet);
							org.apache.poi.hssf.usermodel.HSSFRow secondRow = inSheet
									.getRow(1);
							int currentcolumn = 0;
							int from = secondRow.getFirstCellNum();
							int to = secondRow.getLastCellNum();
							for (int k = from; k < to + 1; k++) {
								HSSFCell currentCell = secondRow
										.getCell((short) (k));
								String currentValue = null;
								if ((currentCell != null)
										&& (currentCell.getCellType() == HSSFCell.CELL_TYPE_STRING)) {
									currentValue = currentCell
											.getStringCellValue();
								}
								if (currentValue != null) {
									currentValue.trim();
									MetadataType columnmdt = getMDTypeByName(
											currentValue, "excelGliederung");
									if ((columnmdt != null)
											&& (columnmdt.getName().equals(mdt
													.getName()))) {
										// We found a column which has a
										// metadatatype.
										currentcolumn = k;
										// Get out of loop, we found the column
										// for metadata.
										break;
									}
								}
							}
							if (currentcolumn == 0) {
								// Metadata column wasn't found.
								System.err
										.println("DEBUG: column couldn' be found");
								return false;
							}
							// currentrow and currentcolumn contains the cell
							// coordinates.
							org.apache.poi.hssf.usermodel.HSSFRow cellRow = inSheet
									.getRow(currentrow);
							HSSFCell currentcell = cellRow
									.getCell((short) (currentcolumn));
							if (currentcell == null) {
								// Cell doesn't exists, so we create a new cell.
								currentcell = cellRow
										.createCell((short) (currentcolumn));
								System.err.println("excel cell at "
										+ currentrow + "/" + currentcolumn
										+ " (r/c) is null");
							}
							// Update the value.
							currentcell.setCellValue(md.getValue());
							continue;
						}
						// No metadata value and no object.
						continue;
					}
					if (mdnatobj.getClass().getName().equals("HSSFCell")) {
						HSSFCell mdcell = (HSSFCell) mdnatobj;
						if (md.getValue() == null) {
							mdcell.setCellValue("");
						} else {
							mdcell.setCellValue(md.getValue());
						}
					} else {
						// Wrong native object; not an excel spreadsheet we
						// should throw an exception here.
						return false;
					}
				}
			}
		}
		if (reverse) {
			// All children.
			List<DocStruct> allChildren = inStruct.getAllChildren();
			if (allChildren == null) {
				// No children, so we can get out.
				return true;
			}
			for (int i = 0; i < allChildren.size(); i++) {
				DocStruct child = allChildren.get(i);
				if (!UpdateAllMetadata(child, true, rowCounter)) {
					return false;
				}
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#write(java.lang.String)
	 */
	@Override
	public boolean write(String inFile) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#getDigitalDocument()
	 */
	@Override
	public DigitalDocument getDigitalDocument() {
		return this.mydoc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#setDigitalDocument(ugh.dl.DigitalDocument)
	 */
	@Override
	public boolean setDigitalDocument(DigitalDocument inDoc) {
		this.mydoc = inDoc;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#read(java.lang.String)
	 */
	@Override
	public boolean read(String filename) {
		// Excelsheet for bibliographic information.
		org.apache.poi.hssf.usermodel.HSSFSheet bibSheet;
		// Excelsheet for bibliographic information.
		org.apache.poi.hssf.usermodel.HSSFSheet pagSheet;
		// Excelsheet for bibliographic information.
		org.apache.poi.hssf.usermodel.HSSFSheet logSheet;

		if (filename == null) {
			return false;
		}

		// Open file.
		FileInputStream excelFile;
		// Get output stream.
		try {
			excelFile = new FileInputStream(filename);
			this.excelworkbook = new org.apache.poi.hssf.usermodel.HSSFWorkbook(
					excelFile);
		} catch (Exception e) {
			System.err.println("ERROR: Can't write file " + filename);
			System.err.println(e);
			return false;
		}
		int numberofsheets = this.excelworkbook.getNumberOfSheets();
		bibSheet = this.excelworkbook.getSheet("Bibliographie");
		for (int i = 0; i < numberofsheets; i++) {
			if (this.excelworkbook.getSheetName(i).equals("Bibliographie")) {
				bibSheet = this.excelworkbook.getSheetAt(i);
				// Found; get out of loop.
				break;
			}
		}

		if (bibSheet == null) {
			System.err.println("ERROR: Can't find table \"Bibliographie\"");
			return false;
		}

		for (int x = 0; x < bibSheet.getPhysicalNumberOfRows(); x++) {
			org.apache.poi.hssf.usermodel.HSSFRow currentRow = bibSheet
					.getRow(x);
			org.apache.poi.hssf.usermodel.HSSFCell cell = currentRow
					.getCell((short) 0); // get first cell / column
			if ((cell != null)
					&& (cell.getCellType() == HSSFCell.CELL_TYPE_STRING)
					&& (cell.getStringCellValue()
							.equals("VERSION_DOKUMENT_BESCHR"))) {
				org.apache.poi.hssf.usermodel.HSSFCell versioncell = currentRow
						.getCell((short) 1);
				if (versioncell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
					this.excel_version = versioncell.getStringCellValue();
				} else {
					System.err
							.println("ERROR: Can't read version information; wrong cell type");
					return false;
				}
			}
			if (this.excel_version != null) {
				break;
			}
		}
		if (this.excel_version == null) {
			System.err.println("ERROR: Can't read version of excel-sheet");
			return false;
		}
		System.out.println("DEBUG: found Excel-version " + this.excel_version);

		// Read bibliographic description.
		try {
			ReadBibliography(bibSheet);
		} catch (TypeNotAllowedForParentException e) {
			System.err.println("ERROR: while reading bibliographiy table");
			System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
		} catch (MetadataTypeNotAllowedException e) {
			System.err.println("ERROR: while reading bibliographiy table");
			System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		// Read pagination sequences.
		pagSheet = this.excelworkbook.getSheet("Sequenzen_Paginierung");
		if (pagSheet == null) {
			System.err
					.println("ERROR: Can't find table \"Sequenzen_Paginierung\"");
			return false;
		}
		try {
			ReadPaginationSequences(pagSheet, "test");
		} catch (MetadataTypeNotAllowedException e1) {
			System.err.println("ERROR: Error reading pagination sequence");
			return false;
		}

		// Read logical struture.
		logSheet = this.excelworkbook.getSheet("Gliederung");
		if (logSheet == null) {
			System.err.println("ERROR: Can't find table \"Gliederung\"");
			return false;
		}

		try {
			ReadGliederung(logSheet);
		} catch (TypeNotAllowedForParentException e) {
			System.err.println("ERROR: Can't read Gliederung table");
			System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
		} catch (MetadataTypeNotAllowedException e) {
			System.err.println("ERROR: Can't read Gliederung table");
			System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/***************************************************************************
	 * @param bibSheet
	 * @return
	 * @throws TypeNotAllowedForParentException
	 * @throws MetadataTypeNotAllowedException
	 **************************************************************************/
	private boolean ReadBibliography(HSSFSheet bibSheet)
			throws TypeNotAllowedForParentException,
			MetadataTypeNotAllowedException {
		// Uppermost document onlyif we have a volume or perodical volume.
		DocStruct anchor = null;
		// Document which is described in this excel sheet.
		DocStruct mainstruct = null;
		DocStructType structtype;
		// Doctype has to be treated differntly.
		String doctype = ReadValueInBib(bibSheet, "DOC_TYPE",
				this.excel_version);
		String catalogidband = ReadValueInBib(bibSheet, "ID_DOC_BAND_DIGIT",
				this.excel_version);
		// Doctype has to be treated differntly.
		String band = ReadValueInBib(bibSheet, "BAND", this.excel_version);
		// Doctype has to be treated differntly.
		String bandid = ReadValueInBib(bibSheet, "BAND_IDENTIFIKATION",
				this.excel_version);

		// Build structure for main docstruct and anchor. This depends on the
		// version of the excel sheet.
		if (this.excel_version.equals("DD4.0.2_X7 / 27.05.2002")) {
			if (doctype.equals("Band_Zeitschrift")) {
				structtype = getDSTypeByName("Zeitschrift", "ExcelName");
				if (structtype == null) {
					System.err
							.println("ERROR: requested structure type (Zeitschrift) is not available");
					return false;
				}
				anchor = this.mydoc.createDocStruct(structtype);

				structtype = getDSTypeByName("Band_Zeitschrift", "ExcelName");
				if (structtype == null) {
					System.err
							.println("ERROR: requested structure type (Band_Zeitschrift) is not available");
					return false;
				}
				mainstruct = this.mydoc.createDocStruct(structtype);
			}

			// Multivolume work.
			//
			if (doctype.equals("Band_MultiVolumeWork")) {
				structtype = getDSTypeByName("MultiVolumeWork", "ExcelName");
				if (structtype == null) {
					System.err
							.println("ERROR: requested structure type (MultiVolumeWork) is not available");
					return false;
				}
				anchor = this.mydoc.createDocStruct(structtype);

				structtype = getDSTypeByName("Band_MultiVolumeWork",
						"ExcelName");
				if (structtype == null) {
					System.err
							.println("ERROR: requested structure type (Band_MultiVolumeWork) is not available");
					return false;
				}
				mainstruct = this.mydoc.createDocStruct(structtype);
			}

			// Monograph.
			//
			if (doctype.equals("Monographie")) {
				structtype = getDSTypeByName("Monograph", "ExcelName");
				if (structtype == null) {
					System.err
							.println("ERROR: requested structure type (Monograph) is not available");
					return false;
				}
				mainstruct = this.mydoc.createDocStruct(structtype);
			}
		}

		// Create metadata objects and add them to mainstruct (and anchor).
		for (int x = 0; x < bibSheet.getPhysicalNumberOfRows(); x++) {
			// Value of single metadata cell.
			String value = null;
			org.apache.poi.hssf.usermodel.HSSFRow currentRow = bibSheet
					.getRow(x);
			org.apache.poi.hssf.usermodel.HSSFCell currentCell = currentRow
					.getCell((short) 0);

			if ((currentCell != null)
					&& (currentCell.getCellType() == HSSFCell.CELL_TYPE_STRING)) {
				// Cell found; get value.
				org.apache.poi.hssf.usermodel.HSSFCell valuecell = currentRow
						.getCell((short) 1);
				if (valuecell == null) {
					continue;
				}
				if ((valuecell != null)
						&& (valuecell.getCellType() == HSSFCell.CELL_TYPE_STRING)) {
					value = valuecell.getStringCellValue();
				}
				if ((valuecell != null)
						&& (valuecell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)) {
					value = Double.toString(valuecell.getNumericCellValue());
				}
				// Can only happen, if valuecell is not a String or
				// Numeric-cell.
				if (value == null) {
					continue;
				}
				// Get metadata type.
				MetadataType mdType = getMDTypeByName(currentCell
						.getStringCellValue(), "ExcelName");
				if (mdType == null) {
					if (!currentCell.getStringCellValue().equals("Variable")
							&& !currentCell.getStringCellValue().equals(
									"DOC_TYPE")
							&& !currentCell.getStringCellValue().equals(
									"VERSION_DOKUMENT_BESCHR")
							&& !currentCell.getStringCellValue().equals(
									"FILM_DOC_LFD_NR")
							&& !currentCell.getStringCellValue().equals(
									"ID_DOC_BAND_DIGIT")) {
						System.out
								.println("WARNING: metadata type unknown or excel bibliography("
										+ currentCell.getStringCellValue()
										+ ")");
					}
					// All others are rows, used for internal EXCEL information;
					// no metadata contained.
					continue;
				}

				Metadata md = new Metadata(mdType);
				md.setValue(value);
				// Add excel spreadsheet cell as native object.
				md.setNativeObject(currentCell);
				md.wasUpdated(false);

				// Add metadata to docstruct-entitites.
				//
				// HAUPTTITEL (for mainstruct and anchor).
				try {
					if ((mdType.getName().equals("MainTitle"))
							&& (mainstruct != null)) {
						mainstruct.addMetadata(md);
					}
					if ((mdType.getName().equals("MainTitle"))
							&& (anchor != null)) {
						anchor.addMetadata(md);
					}

					// ERSCHEINUNGSORT (for mainstruct and anchor).
					if ((mdType.getName().equals("PlaceOfPublication"))) {
						// Parse author list.
						List<Metadata> places = ReadPlacePub(value);
						for (int z = 0; z < places.size(); z++) {
							Metadata place = places.get(z);
							if (mainstruct != null) {
								mainstruct.addMetadata(place);
							}
							if (anchor != null) {
								anchor.addMetadata(place);
							}
						}
					}

					// VERLAG (for mainstruct and anchor).
					if ((mdType.getName().equals("Publisher"))
							&& (mainstruct != null)) {
						mainstruct.addMetadata(md);
					}
					if ((mdType.getName().equals("Publisher"))
							&& (anchor != null)) {
						anchor.addMetadata(md);
					}

					// JAHR (for mainstruct and only for anchor it it's a
					// multivolume).
					if ((mdType.getName().equals("PublicationYear"))
							&& (mainstruct != null)) {
						mainstruct.addMetadata(md);
					}
					if ((mdType.getName().equals("PublicationYear"))
							&& (anchor != null)
							&& (doctype.equals("Band_MultiVolumeWork"))) {
						anchor.addMetadata(md);
					}

					// ID_DOC_BAND_VORLAGE (for mainstructc and only for anchor
					// it it's a multivolume).
					if ((mdType.getName().equals("IdentifierSource"))
							&& (anchor != null)) {
						anchor.addMetadata(md);
					} else {
						if (mdType.getName().equals("IdentifierSource")) {
							mainstruct.addMetadata(md);
						}
					}

					// ID_DOC_HAUPTAUFNAHME_DIGIT (for mainstructc and only for
					// anchor it it's a multivolume).
					if ((mdType.getName().equals("IdentifierDigital"))
							&& (anchor != null)
							&& (doctype.equals("Band_MultiVolumeWork"))) {
						anchor.addMetadata(md);
						// Construct identifier for mainstruct (construct
						// value).
						String bandforppn = null;
						if (bandid == null) {
							// Add leading zeros, so that we have 0001 instead
							// of 1.
							if ((band != null) && (band.length() < 2)) {
								band = "000" + band;
							}
							if ((band != null) && (band.length() < 3)) {
								band = "00" + band;
							}
							if ((band != null) && (band.length() < 4)) {
								band = "0" + band;
							}
							bandforppn = band;

							// Get information of HEFT and add it, if available;
							// Doctype has to be
							// treated differntly.
							String heft = ReadValueInBib(bibSheet, "HEFT",
									this.excel_version);
							if ((heft != null) && (heft.length() < 2)) {
								heft = "0" + heft;
							}
							if (heft != null) {
								bandforppn = bandforppn + "_" + heft;
							}

						} else {
							// Use value from BAND_IDENTIFIKATION.
							bandforppn = bandid;
						}
						// Now we should have the volume_issue numbers.
						String idmainstruct = catalogidband + "_" + bandforppn;

						// Create metadataobject and add it to the mainstruct.
						Metadata md2 = new Metadata(mdType);
						md2.setValue(idmainstruct);
						mainstruct.addMetadata(md2);
					}
				} catch (MetadataTypeNotAllowedException mtnae) {
					System.err
							.println("ERROR: ReadBibliography: Can't read metadata; metadata type not allowed!");
					return false;
				}

				if ((mdType.getName().equals("IdentifierDigital"))
						&& (anchor != null)
						&& (doctype.equals("Band_Zeitschrift"))) {
					try {
						anchor.addMetadata(md);
					} catch (MetadataTypeNotAllowedException mtnae) {
						System.err
								.println("ERROR: ReadBibliography: Can't read metadata; metadata type not allowed!");
						System.err.println("       " + md.getType().getName()
								+ " can't be added for DocStruct "
								+ anchor.getType().getName());
						return false;
					}
					// Construct identifier for mainstruct (construct value).
					String bandforppn = null;
					// No band number available; calculate one add leading
					// zeros, so that we have 0001 instead of 1.
					if (bandid == null) {
						if ((band != null) && (band.length() < 2)) {
							band = "000" + band;
						}
						if ((band != null) && (band.length() < 3)) {
							band = "00" + band;
						}
						if ((band != null) && (band.length() < 4)) {
							band = "0" + band;
						}
						bandforppn = band;

						// Get information of HEFT and add it, if available;
						// Doctype has to be treated differntly.
						String heft = ReadValueInBib(bibSheet, "HEFT",
								this.excel_version);
						if ((heft != null) && (heft.length() < 2)) {
							heft = "0" + heft;
						}
						if (heft != null) {
							bandforppn = bandforppn + "_" + heft;
						}

					} else {
						// Use value from BAND_IDENTIFIKATION.
						bandforppn = bandid;
					}
					// Now we should have the volume_issue numbers.
					String idmainstruct = catalogidband + "_" + bandforppn;

					// Create metadataobject and add it to the mainstruct.
					Metadata md2 = new Metadata(mdType);
					md2.setValue(idmainstruct);
					try {
						mainstruct.addMetadata(md2);
					} catch (MetadataTypeNotAllowedException mtnae) {
						System.err
								.println("ERROR: ReadBibliography: Can't read metadata; metadata type not allowed!");
						System.err.println("       " + md2.getType().getName()
								+ " can't be added for DocStruct "
								+ mainstruct.getType().getName());
						return false;
					}
				}

				if ((mdType.getName().equals("IdentifierDigital"))
						&& (anchor != null) && (doctype.equals("Monographie"))) {
					try {
						anchor.addMetadata(md);
					} catch (MetadataTypeNotAllowedException mtnae) {
						System.err
								.println("ERROR: ReadBibliography: Can't read metadata; metadata type not allowed!");
						System.err.println("       " + md.getType().getName()
								+ " can't be added for DocStruct "
								+ anchor.getType().getName());
						return false;
					}
				}

				// AUTOREN/HERAUSGEBER.
				if ((mdType.getName().equals("Author"))
						|| ((mdType.getName().equals("Author")))) {
					// Parse author list.
					List<Metadata> authors = ReadAuthors(value);
					for (int z = 0; z < authors.size(); z++) {
						Metadata author = authors.get(z);
						if (mainstruct != null) {
							try {
								mainstruct.addMetadata(author);
							} catch (MetadataTypeNotAllowedException mtnae) {
								System.err
										.println("ERROR: ReadBibliography: Can't read metadata; metadata type not allowed!");
								System.err.println("       "
										+ author.getType().getName()
										+ " can't be added for DocStruct "
										+ mainstruct.getType().getName());
								return false;
							}
						}
						if (anchor != null) {
							try {
								anchor.addMetadata(author);
							} catch (MetadataTypeNotAllowedException mtnae) {
								System.err
										.println("ERROR: ReadBibliography: Can't read metadata; metadata type not allowed!");
								System.err.println("       "
										+ author.getType().getName()
										+ " can't be added for DocStruct "
										+ anchor.getType().getName());
								return false;
							}
						}
					}
				}

				// Handle metadata for pyhsical structure entity (BoundBook).
				if (mdType.getName().equals("mediumsource")) {
					DocStruct boundBook = this.mydoc.getPhysicalDocStruct();
					try {
						boundBook.addMetadata(md);
					} catch (MetadataTypeNotAllowedException mtnae) {
						System.err
								.println("ERROR: ReadBibliography: Can't read metadata; metadata type not allowed!");
						System.err.println("       " + md.getType().getName()
								+ " can't be added for DocStruct "
								+ boundBook.getType().getName());
						return false;
					}
				}
				if (mdType.getName().equals("shelfmarksource")) {
					DocStruct boundBook = this.mydoc.getPhysicalDocStruct();
					try {
						boundBook.addMetadata(md);
					} catch (MetadataTypeNotAllowedException mtnae) {
						System.err
								.println("ERROR: ReadBibliography: Can't read metadata; metadata type not allowed!");
						System.err.println("       " + md.getType().getName()
								+ " can't be added for DocStruct "
								+ boundBook.getType().getName());
						return false;
					}
				}
			}
		}

		// Add structure to main document.
		if (anchor != null) {
			try {
				anchor.addChild(mainstruct);
			} catch (TypeNotAllowedAsChildException tnaace) {
				// Can't add child to anchor.
				System.err
						.println("ERROR: ReadBibliography: can't add DocStrct as child");
				System.err.println("       " + mainstruct.getType().getName()
						+ " can't be added to:" + anchor.getType().getName());
				return false;
			}
			this.mydoc.setLogicalDocStruct(anchor);
			this.allDocStruct.add(mainstruct);
			this.allDocStruct.add(anchor);
			this.allStructSheets.add("Bibliographie");
			// Set it to -1 because the whole sheet represents the DocStruct.
			this.allStructRow.add("-1");
		} else {
			System.out
					.println("DEBUG: Excelfile.ReadBibliography: no anchor found");
			this.mydoc.setLogicalDocStruct(mainstruct);
			this.allDocStruct.add(mainstruct);
			this.allStructSheets.add("Bibliographie");
			// Set it to -1 because the whole sheet represents the DocStruct.
			this.allStructRow.add("-1");
		}

		return true;
	}

	//
	// ReadPaginationSequences.
	//

	/***************************************************************************
	 * Read paginiation sequences from Excel sheet and creates physical
	 * docstruct entities (one for each page) and creates Image-instances
	 * 
	 * @param inSheet
	 *            single sheet of a whole excel file containing the pagination
	 *            sequences
	 * @param pathasstring
	 * @return true, if everything is okay; otherwise false
	 * @throws MetadataTypeNotAllowedException
	 **************************************************************************/
	private boolean ReadPaginationSequences(HSSFSheet inSheet,
			String pathasstring) throws MetadataTypeNotAllowedException {

		DocStruct boundbook = this.mydoc.getPhysicalDocStruct();

		double oldPhysicalend = 0;
		// Positions of appropriate columns in the spreadsheet.
		int countedstartpageCol = 0;
		int countedendpageCol = 0;
		int uncountedstartpageCol = 0;
		int uncountedendpageCol = 0;
		int formatCol = 0;
		// Contains all pagination sequences; will be used later when reading
		// the hierarchy.
		this.allPaginations = new LinkedList<PaginationSequence>();

		// Get column's names and positions from the second row in the
		// spreadsheet.
		org.apache.poi.hssf.usermodel.HSSFRow secondRow = inSheet.getRow(1);
		int from = secondRow.getFirstCellNum();
		int to = secondRow.getLastCellNum();
		for (int i = from; i < to + 1; i++) {
			HSSFCell currentCell = secondRow.getCell((short) (i));

			if ((currentCell != null)
					&& (currentCell.getCellType() == HSSFCell.CELL_TYPE_STRING)) {
				String currentValue = currentCell.getStringCellValue();
				if ((currentValue != null) && (currentValue.length() >= 7)
						&& (currentValue.substring(0, 7).equals("GSEIT_S"))) {
					countedstartpageCol = i;
					continue;
				}
				if ((currentValue != null) && (currentValue.length() >= 7)
						&& (currentValue.substring(0, 7).equals("GSEIT_E"))) {
					countedendpageCol = i;
					continue;
				}
				if ((currentValue != null) && (currentValue.length() >= 7)
						&& (currentValue.substring(0, 7).equals("USEIT_S"))) {
					uncountedstartpageCol = i;
					continue;
				}
				if ((currentValue != null) && (currentValue.length() >= 7)
						&& (currentValue.substring(0, 7).equals("USEIT_E"))) {
					uncountedendpageCol = i;
					continue;
				}
				if ((currentValue != null) && (currentValue.length() >= 7)
						&& (currentValue.substring(0, 7).equals("FRMT_S"))) {
					formatCol = i;
					continue;
				}
			}
		}

		// Now we can begin to read the pagination sequences; we'll start from
		// third row.
		for (int x = 2; x < inSheet.getPhysicalNumberOfRows(); x++) {
			// Each row is one pagination sequence.
			org.apache.poi.hssf.usermodel.HSSFRow currentRow = inSheet
					.getRow(x);
			// Get cell values.
			HSSFCell countedstartpagecell = currentRow
					.getCell((short) countedstartpageCol);
			HSSFCell countedendpagecell = currentRow
					.getCell((short) countedendpageCol);
			HSSFCell uncountedstartpagecell = currentRow
					.getCell((short) uncountedstartpageCol);
			HSSFCell uncountedendpagecell = currentRow
					.getCell((short) uncountedendpageCol);
			HSSFCell formatcell = currentRow.getCell((short) formatCol);
			// These variables are for one pagination sequence.
			double numpages = 0;
			double uncountedstartpage = 0;
			double uncountedendpage = 0;
			double countedstartpage = 0;
			double countedendpage = 0;
			String pageformat = null;

			// Check if we have to go out of loop, cause entries are empty.
			if ((countedstartpagecell == null) || (countedendpagecell == null)) {
				// Get out of loop; no value in start or endpage available; must
				// be the last one.
				break;
			}
			if ((countedstartpagecell.getCellType() == HSSFCell.CELL_TYPE_BLANK)
					|| (countedendpagecell.getCellType() == HSSFCell.CELL_TYPE_BLANK)) {
				break;
			}

			// Get cell values.
			if ((countedstartpagecell != null)
					&& (countedstartpagecell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)) {
				countedstartpage = countedstartpagecell.getNumericCellValue();
			} else {
				if (countedstartpagecell != null) {
					System.err
							.println("WARNING: value for counted page start in Pagination sequences is NOT numeric ("
									+ x + ")");
				} else {
					System.err
							.println("WARNING: value for counted page start has no value in Pagination Sequence ("
									+ x + ")");
				}
			}
			if ((countedendpagecell != null)
					&& (countedendpagecell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)) {
				countedendpage = countedendpagecell.getNumericCellValue();
			} else {
				if (countedendpagecell != null) {
					System.err
							.println("WARNING: value for counted page end in Pagination sequences is NOT numeric ("
									+ x + ")");
				} else {
					System.err
							.println("WARNING: counted endpage has no value... ("
									+ x + ")");
				}
			}
			if ((uncountedstartpagecell != null)
					&& (uncountedstartpagecell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)) {
				uncountedstartpage = uncountedstartpagecell
						.getNumericCellValue();
			} else {
				if ((uncountedstartpagecell != null)
						&& (uncountedstartpagecell.getCellType() != HSSFCell.CELL_TYPE_BLANK)) {
					System.err
							.println("WARNING: value for uncounted startpage in Pagination sequences is NOT numeric ("
									+ x + ")");
				}
			}
			if ((uncountedendpagecell != null)
					&& (uncountedendpagecell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)) {
				uncountedendpage = uncountedendpagecell.getNumericCellValue();
			} else {
				if ((uncountedendpagecell != null)
						&& (uncountedendpagecell.getCellType() != HSSFCell.CELL_TYPE_BLANK)) {
					System.err
							.println("WARNING: value for uncounted endpage in Pagination sequences is NOT numeric ("
									+ x + ")");
				}
			}
			// Seitenformat.
			if ((formatcell != null)
					&& ((formatcell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) || (formatcell
							.getCellType() == HSSFCell.CELL_TYPE_FORMULA))) {
				pageformat = "1";
			}
			if ((formatcell != null)
					&& (formatcell.getCellType() == HSSFCell.CELL_TYPE_STRING)) {
				pageformat = formatcell.getStringCellValue();
				if (!pageformat.equalsIgnoreCase("R")) {
					System.err
							.println("WARNING: unknown page format - neither arabic nor roman... ("
									+ x + ")");
				}
				pageformat = "R";
			}
			if ((formatcell != null)
					&& (formatcell.getCellType() == HSSFCell.CELL_TYPE_BLANK)) {
				// Blank cell.
				pageformat = "1";
			}
			if (formatcell == null) {
				// Assume that it's an arabic number, when no value was set.
				pageformat = "1";
			}
			// Calculate physical start and endpages.
			if (((uncountedstartpage == 0) && (uncountedendpage != 0))
					|| ((uncountedstartpage != 0) && (uncountedendpage == 0))) {
				System.err
						.println("WARNING: uncounted start or endpage is NOT set... ("
								+ x + ")");
				continue;
			}
			if ((countedstartpage > countedendpage)
					|| (uncountedstartpage > uncountedendpage)) {
				System.err
						.println("WARNING: startpage is larger then endpage ("
								+ x + ")");
			}
			if (uncountedstartpage == 0) {
				numpages = countedendpage - countedstartpage;
			} else {
				numpages = uncountedendpage - uncountedstartpage;
			}

			double physicalstart = oldPhysicalend + 1;
			double physicalend = physicalstart + numpages;
			oldPhysicalend = physicalend;

			// Create pagination sequence.
			PaginationSequence ps = new PaginationSequence(this.myPreferences);
			if ((countedstartpage == countedendpage)
					&& (uncountedstartpage != 0) && (uncountedendpage != 0)) {
				// A sequence of uncounted pages.
				ps.logcountedstart = 0;
				ps.logcountedend = 0;
				ps.lognotcountedstart = (int) uncountedstartpage;
				ps.lognotcountedend = (int) uncountedendpage;
				ps.pageformatnumber = pageformat;
			}
			if (countedstartpage != countedendpage) {
				// A sequence of counted pages.
				if (uncountedstartpage != uncountedendpage) {
					System.err
							.println("WARNING: counted page sequence can be may be an uncounted sequence... ("
									+ x + ")");
				}
				ps.logcountedstart = (int) countedstartpage;
				ps.logcountedend = (int) countedendpage;
				ps.pageformatnumber = pageformat;
			}
			ps.physicalstart = (int) physicalstart;
			ps.physicalend = (int) physicalend;
			// Convert pagination sequence to physical strucutre; get a list of
			// physical structures
			LinkedList<?> pages = ps.ConvertToPhysicalStructure(this.mydoc);
			this.allPaginations.add(ps);

			// Add number of pagination sequence necessary to calculate physical
			// page number in Gliederung.
			MetadataType seqno = new MetadataType();
			seqno.setName("_PaginationNo");
			for (int u = 0; u < pages.size(); u++) {
				DocStruct page = (DocStruct) pages.get(u);
				Metadata md = new Metadata(seqno);
				md.setValue(Integer.toString(x - 1));
				try {
					page.addMetadata(md);
					// Add the single page to the uppermost physical structure
					// (bound book).
					boundbook.addChild(page);
				} catch (TypeNotAllowedAsChildException tnaace) {
					System.err
							.println("ERROR: ReadPaginationSequences: Can't add pages to BoundBook");
					return false;
				} catch (MetadataTypeNotAllowedException mtnae) {
					System.err
							.println("ERROR: ReadPaginationSequences: Can't read metadata; metadata type not allowed!");
					System.err.println("       " + md.getType().getName()
							+ " can't be added for DocStruct "
							+ page.getType().getName());
					return false;
				}
			}
		}

		// Create top physical document structure (BoundBook)
		if ((boundbook.getAllChildren() == null)
				|| (boundbook.getAllChildren().size() == 0)) {
			System.out
					.println("DEBUG: rdffile.ReadPagSequence: No pages available...");
			return false;
		}

		// Add page information to parent logical structure.
		DocStruct parent = this.mydoc.getLogicalDocStruct();
		if (parent == null) {
			System.out
					.println("ERROR: Excelfile.ReadPaginationSequences: Can't find any parent element on topmost level");
			return false;
		}

		DocStructType parentType = parent.getType();
		if (parentType.getAnchorClass() != null) {
			// It's an anchor (e.g. a periodical...) so we cannot add any
			// children to this but we must get the next structure entity
			// (child) - e.g. the volume.
			List<DocStruct> children = parent.getAllChildren();
			if (children == null) {
				System.out
						.println("ERROR: ReadPaginationSequences: Parent is anchor but has no child");
				return false;
			}
			// Get first child as new parent.
			parent = children.get(0);
		}
		List<DocStruct> allpages = boundbook.getAllChildren();
		for (int i = 0; i < allpages.size(); i++) {
			// Get single node.
			DocStruct currentPage = allpages.get(i);
			parent.addReferenceTo(currentPage, "logical_physical");
			currentPage.addReferenceFrom(parent, "physical_physical");
		}
		// Add internal metadata: start and endpage.
		MetadataType endpagemd = this.myPreferences
				.getMetadataTypeByName("_pagephysend");
		Metadata mdnew = new Metadata(endpagemd);
		mdnew.setValue(Integer.toString(allpages.size()));

		try {
			// Add physical page number.
			parent.addMetadata(mdnew);
		} catch (MetadataTypeNotAllowedException mtnae) {
			System.err
					.println("ERROR: ReadPaginationSequences: Can't add metadata");
			System.err.println("       " + mdnew.getType().getName()
					+ " can't be added to " + parent.getType().getName());
			return false;
		}
		MetadataType startpagemd = this.myPreferences
				.getMetadataTypeByName("_pagephysstart");
		mdnew = new Metadata(startpagemd);
		// Physical page number begins always with 1.
		mdnew.setValue("1");

		try {
			// Add physical page number.
			parent.addMetadata(mdnew);
		} catch (MetadataTypeNotAllowedException mtnae) {
			System.err
					.println("ERROR: ReadPaginationSequences: Can't add metadata");
			System.err.println("       " + mdnew.getType().getName()
					+ " can't be added to " + parent.getType().getName());
			return false;
		}

		// Create imageset; every page one image.
		if (this.myImageset == null) {
			this.myImageset = new ugh.dl.FileSet();
		}

		// NOT FINISHED
		//
		// Create File objects for images.
		for (int i = 0; i < allpages.size(); i++) {
			DocStruct currentPage = allpages.get(i);

			// Create new Image object and add it to myImageSet.
			ugh.dl.ContentFile newimage = new ugh.dl.ContentFile();
			String filename = "";

			ugh.dl.MetadataType MDT2 = this.myPreferences
					.getMetadataTypeByName("physPageNumber");
			List<? extends Metadata> physpagelist = currentPage
					.getAllMetadataByType(MDT2);
			int physpage = 0;
			for (Metadata md : physpagelist) {
				try {
					physpage = Integer.parseInt(md.getValue());
				} catch (Exception e) {
					System.err
							.println("ERROR: physical page number seems to be a non integer value!!");
					return false;
				}
			}

			if (physpage < 100000) {
				filename = "000" + physpage + ".tif";
			}
			if (physpage < 10000) {
				filename = "0000" + physpage + ".tif";
			}
			if (physpage < 1000) {
				filename = "00000" + physpage + ".tif";
			}
			if (physpage < 100) {
				filename = "000000" + physpage + ".tif";
			}
			if (physpage < 10) {
				filename = "0000000" + physpage + ".tif";
			}
			newimage.setLocation(pathasstring + "/" + filename);
			newimage.setMimeType("image/tiff");
			// Add the file to the imageset.
			this.myImageset.addFile(newimage);
			// Add contentFile to page.
			currentPage.addContentFile(newimage);
		}
		this.mydoc.setPhysicalDocStruct(boundbook);
		this.mydoc.setFileSet(this.myImageset);

		return true;
	}

	/***************************************************************************
	 * Reads the logical structure of a work from spreadsheet table "Gliederung"
	 * The content is attached to the "maindocstruct", which must already be
	 * available e.g. be reading the Bibliography first
	 * 
	 * @param inSheet
	 *            of the table "Gliederung"
	 * @return true, everything is okay; otherwise false
	 * @throws TypeNotAllowedForParentException
	 * @throws MetadataTypeNotAllowedException
	 * 
	 **************************************************************************/
	private boolean ReadGliederung(HSSFSheet inSheet)
			throws TypeNotAllowedForParentException,
			MetadataTypeNotAllowedException {
		int structtypeCol = -1; // position of structure type column
		int sequenceCol = -1;
		int levelCol = -1;
		int countedstartpageCol = -1;
		int uncountedstartpageCol = -1;
		int ueberlappungCol = -1;

		// Contains the column number for a metadata type.
		LinkedList<String> metadataColumn = new LinkedList<String>();
		// Contains the name of a metadata type (as String).
		LinkedList<String> metadataType = new LinkedList<String>();

		// Try to get the column-positions according to their names.
		org.apache.poi.hssf.usermodel.HSSFRow secondRow = inSheet.getRow(1);
		int from = secondRow.getFirstCellNum();
		int to = secondRow.getLastCellNum();
		for (int i = from; i < to + 1; i++) {
			HSSFCell currentCell = secondRow.getCell((short) (i));

			if ((currentCell != null)
					&& (currentCell.getCellType() == HSSFCell.CELL_TYPE_STRING)) {
				String currentValue = currentCell.getStringCellValue();

				// Checking for length of string and comapre only the first x
				// chars is necessary, because OpenOffice's excel files may
				// contain whitespaces after the cell-content. I'm not sure,
				// wether only whitespaces or also other characters may occur...
				if ((currentValue != null) && (currentValue.length() >= 8)
						&& (currentValue.substring(0, 8).equals("STRCT_EL"))) {
					structtypeCol = i;
					continue;
				}
				if ((currentValue != null) && (currentValue.length() >= 5)
						&& (currentValue.substring(0, 5).equals("LEVEL"))) {
					levelCol = i;
					continue;
				}
				if ((currentValue != null) && (currentValue.length() >= 3)
						&& (currentValue.substring(0, 3).equals("SEQ"))) {
					sequenceCol = i;
					continue;
				}
				if ((currentValue != null) && (currentValue.length() >= 7)
						&& (currentValue.substring(0, 7).equals("GSEIT_S"))) {
					countedstartpageCol = i;
					continue;
				}
				if ((currentValue != null) && (currentValue.length() >= 7)
						&& (currentValue.substring(0, 7).equals("USEIT_S"))) {
					uncountedstartpageCol = i;
					continue;
				}
				if ((currentValue != null)
						&& (currentValue.length() >= 12)
						&& (currentValue.substring(0, 12)
								.equals("Ueberlappung"))) {
					ueberlappungCol = i;
					continue;
				}

				// Check metadata columns; these are configurable using the
				// language excel:Gliederung.
				if (currentValue != null) {
					currentValue.trim();
					MetadataType columnmdt = getMDTypeByName(currentValue,
							"ExcelGliederung");
					if (columnmdt != null) {
						// We found a column which has a metadatatype.
						metadataType.add(currentValue);
						metadataColumn.add(Integer.toString(i));
					}
				}
			}
		}

		// Now we can begin to read the contents.
		//
		// Read DocStructs.
		//
		DocStruct alllevels[] = { null, null, null, null, null, null, null,
				null, null, null, null, null };
		int oldhierarchy = 0;
		// Each row is one pagination sequence.
		for (int x = 2; x < inSheet.getPhysicalNumberOfRows(); x++) {
			HSSFCell levelcell = null;
			HSSFCell structtypecell = null;
			HSSFCell sequencecell = null;
			HSSFCell countedstartpagecell = null;
			HSSFCell uncountedstartpagecell = null;
			HSSFCell ueberlappungcell = null;

			org.apache.poi.hssf.usermodel.HSSFRow currentRow = inSheet
					.getRow(x);
			if (structtypeCol > -1) {
				structtypecell = currentRow.getCell((short) structtypeCol);
			} else {
				System.err.println("ERROR: Can't find column 'STRCT_EL'");
				return false;
			}
			if (sequenceCol > -1) {
				sequencecell = currentRow.getCell((short) sequenceCol);
			} else {
				System.err.println("ERROR: Can't find column 'SEQ'");
				return false;
			}
			if (countedstartpageCol > -1) {
				countedstartpagecell = currentRow
						.getCell((short) countedstartpageCol);
			} else {
				System.err.println("ERROR: Can't find column 'GSEIT_S'");
				return false;
			}
			if (uncountedstartpageCol > -1) {
				uncountedstartpagecell = currentRow
						.getCell((short) uncountedstartpageCol);
			} else {
				System.err.println("ERROR: Can't find column 'USEIT_S'");
				return false;
			}

			if (levelCol > -1) {
				levelcell = currentRow.getCell((short) levelCol);
			}
			if (ueberlappungCol > -1) {
				ueberlappungcell = currentRow.getCell((short) ueberlappungCol);
			}

			// Get cells for metadata and store the cells in a LinkedList.
			LinkedList<HSSFCell> metadataCells = new LinkedList<HSSFCell>();
			for (int u = 0; u < metadataColumn.size(); u++) {
				int column = Integer.parseInt(metadataColumn.get(u));
				HSSFCell metadatacell = currentRow.getCell((short) column);
				metadataCells.add(metadatacell);
			}

			// Read values and create DocStruct object.
			int hierarchy = 0;
			String type = null;
			String overlapping = null;
			int sequence = 0;
			int countedpage = 0;
			int uncountedpage = 0;

			// Get hierarchy.
			//
			// Level-column is available.
			if ((levelCol > -1) && (levelcell != null)) {
				if (levelcell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
					hierarchy = (int) levelcell.getNumericCellValue();
				} else {
					if (levelcell.getCellType() != HSSFCell.CELL_TYPE_BLANK) {
						System.err
								.println("ERROR: Value of hierarchy is NOT numeric (1) - line "
										+ x);
					}
					continue;
				}
				// Level information is stored in type value.
			} else {
				if ((structtypecell != null)
						&& (structtypecell.getCellType() == HSSFCell.CELL_TYPE_STRING)) {
					type = structtypecell.getStringCellValue();
					// Needed to read OpenOffice excel files...
					type = TrimString(type);
				} else {
					if ((structtypecell != null)
							&& (structtypecell.getCellType() != HSSFCell.CELL_TYPE_BLANK)) {
						System.err
								.println("ERROR: Unknown cell type for structure entity type (not a string) - line "
										+ x);
					}
					continue;
				}
				// Separate level information from structtype.
				for (int z = 0; z < type.length(); z++) {
					// Position of space.
					int spacepos = type.indexOf(" ");
					String hierarchystring = type.substring(0, spacepos);
					hierarchy = Integer.parseInt(hierarchystring);
					type = type.substring(spacepos + 1);
				}
			}
			// Get type, but only if we don't have a type already.
			if ((type == null) && (structtypecell != null)) {
				if (structtypecell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
					type = structtypecell.getStringCellValue();
					// Needed to read OpenOffice excel files...
					type = TrimString(type);
				} else {
					System.err
							.println("ERROR: Wrong value for structure cell - line "
									+ x);
					continue;
				}
			}
			// Get start sequence.
			if ((sequencecell != null)
					&& (sequencecell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)) {
				sequence = (int) sequencecell.getNumericCellValue();
			} else {
				System.err
						.println("ERROR: Can't find pagination sequence for start page - line "
								+ x);
				continue;
			}
			// Get counted start page.
			if ((countedstartpagecell != null)
					&& (countedstartpagecell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)) {
				countedpage = (int) countedstartpagecell.getNumericCellValue();
			} else {
				System.err
						.println("ERROR: Can't find cell for counted startpage of cell value is not numeric - line "
								+ x);
				continue;
			}
			// Get uncounted start page.
			if ((uncountedstartpagecell != null)
					&& (uncountedstartpagecell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC)) {
				uncountedpage = (int) uncountedstartpagecell
						.getNumericCellValue();
			} else {
				if ((uncountedstartpagecell != null)
						&& (uncountedstartpagecell.getCellType() != HSSFCell.CELL_TYPE_BLANK)) {
					System.err
							.println("WARNING (Gliederung): invalid value in uncountedstartpage cell - line "
									+ x);
				}
			}

			// Get overlapping.
			if ((ueberlappungcell != null)
					&& (ueberlappungcell.getCellType() != HSSFCell.CELL_TYPE_BLANK)) {
				if (ueberlappungcell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
					overlapping = ueberlappungcell.getStringCellValue();
					// Needed to read OpenOffice excel files...
					overlapping = TrimString(overlapping);
				}
				if (ueberlappungcell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
					overlapping = Double.toString(ueberlappungcell
							.getNumericCellValue());
					// Needed to read OpenOffice excel files...
					overlapping = TrimString(overlapping);
				}
			}

			// Create DocStruct instance.
			//
			DocStruct newStruct = null;
			if (type != null) {
				DocStructType structType = getDSTypeByName(type,
						"ExcelGliederung");
				if (structType == null) {
					System.err
							.println("ERROR: Excelfile.ReadGliederung: Can't find DocStruct for type="
									+ type + "<");
					return false;
				}
				newStruct = this.mydoc.createDocStruct(structType);
			}

			// Add metadata as title, author, identifier etc... metadata is
			// configurable using the language excel:Gliederung.
			for (int u = 0; u < metadataColumn.size(); u++) {
				// Gt cell.
				HSSFCell metadatacell = metadataCells.get(u);
				String mdvalue = null;
				if ((metadatacell != null)
						&& (metadatacell.getCellType() != HSSFCell.CELL_TYPE_BLANK)) {
					if (metadatacell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
						mdvalue = metadatacell.getStringCellValue();
					}
					if (metadatacell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
						mdvalue = Double.toString(metadatacell
								.getNumericCellValue());
						// Needed to read OpenOffice excel files...
						mdvalue = TrimString(mdvalue);
					}
				} else {
					// It's a blank cell - so continue with next metadata
					// column.
					continue;
				}
				if (mdvalue != null) {
					String metadatatypeString = metadataType.get(u);
					MetadataType mdtype = getMDTypeByName(metadatatypeString,
							"ExcelGliederung");
					if (metadatatypeString.equals("Autoren")) {
						List<Metadata> allvalues = ReadAuthors(mdvalue);
						for (int j = 0; j < allvalues.size(); j++) {
							// Create new Metadata instance.
							Metadata partOfMdvalue = allvalues.get(j);
							// Excel spreadsheet cell as native object.
							partOfMdvalue.setNativeObject(metadatacell);
							partOfMdvalue.wasUpdated(false);
							try {
								// Addit to new DocStruct instance.
								if (!newStruct.addMetadata(partOfMdvalue)) {
									System.err
											.println("ERROR: Can't add metadata to new document structure - line "
													+ x + ".");
								}
							} catch (MetadataTypeNotAllowedException mtnaae) {
								System.err
										.println("ERROR: ReadGliederung: can't add metadata - line"
												+ x);
								System.err.println("       "
										+ partOfMdvalue.getType().getName()
										+ " can't be added to "
										+ newStruct.getType().getName());
							}
							// Add it to new DocStruct.
							partOfMdvalue.setDocStruct(newStruct);
						}
					} else {
						// Ccreate new Metadatainstance.
						Metadata md = new Metadata(mdtype);
						md.setValue(mdvalue);
						// Set the Excel-spreadsheet cell as native object.
						md.setNativeObject(metadatacell);
						md.wasUpdated(false);

						try {
							// Add it to new DocStruct instance.
							if (!newStruct.addMetadata(md)) {
								System.err
										.println("ERROR: Can't add metadata to new document structure - line "
												+ x + ".");
							}
						} catch (MetadataTypeNotAllowedException mtnae) {
							System.err
									.println("ERROR: ReadPaginationSequences: Can't add metadata - line "
											+ x);
							System.err.println("       "
									+ md.getType().getName()
									+ " can't be added to "
									+ newStruct.getType().getName());
						}
						md.setDocStruct(newStruct);
					}
				}
			}

			// Add physical page numbers.
			PaginationSequence currentSeq = null;
			int physnumber = 0;
			for (int u = 0; u < this.allPaginations.size(); u++) {
				if (u == (sequence - 1)) {
					currentSeq = this.allPaginations.get(u);
					if (uncountedpage > 0) {
						physnumber = CalculatePhysicalNumber(currentSeq,
								uncountedpage);
					}
					if ((countedpage > 0) && (uncountedpage <= 0)) {
						physnumber = CalculatePhysicalNumber(currentSeq,
								countedpage);
					}
					MetadataType pagenumberPhys = this.myPreferences
							.getMetadataTypeByName("_pagephysstart");
					Metadata mdnew = new Metadata(pagenumberPhys);
					mdnew.setValue(Integer.toString(physnumber));
					try {
						// Add physical page number.
						newStruct.addMetadata(mdnew);
					} catch (MetadataTypeNotAllowedException mtnaae) {
						System.err
								.println("ERROR: ReadGliederung: can't add metadata ");
						System.err.println("       "
								+ mdnew.getType().getName()
								+ " can't be added to "
								+ newStruct.getType().getName());
						return false;
					}
					// Add overlapping information.
					MetadataType overlappingType = this.myPreferences
							.getMetadataTypeByName("_overlapping");
					mdnew = new Metadata(overlappingType);
					mdnew.setValue(overlapping);
					try {
						// Add physical page number.
						newStruct.addMetadata(mdnew);
					} catch (MetadataTypeNotAllowedException mtnaae) {
						System.err
								.println("ERROR: ReadGliederung: can't add metadata ");
						System.err.println("       "
								+ mdnew.getType().getName()
								+ " can't be added to "
								+ newStruct.getType().getName());
						return false;
					}
					continue;
				}
			}

			// Get pagaination sequence, calculate number, add DocStruct to
			// tree.
			if (hierarchy < 0) {
				System.out.println("ERROR: Invalid hierarchy level (1)");
				return false;
			}
			if ((oldhierarchy < hierarchy) && ((hierarchy - oldhierarchy) > 1)) {
				// There is a jump in hiearchy level from 1 to 3 or so; this is
				// an error.
				System.out.println("ERROR: Invalid hierarchy level (2)");
				return false;
			}

			if (hierarchy == 1) {
				// Add it to the topmost element.
				DocStruct parent = this.mydoc.getLogicalDocStruct();
				if (parent == null) {
					System.out
							.println("ERROR: Can't find any parent element on topmost level");
					return false;
				}

				DocStructType parentType = parent.getType();
				if (parentType.getAnchorClass() != null) {
					// It's an anchor (e.g. a periodical...) so we cannot add
					// any children to this but we must get the next structure
					// entity (child) - e.g. the volume.
					List<DocStruct> children = parent.getAllChildren();
					if (children == null) {
						System.out
								.println("ERROR: Parent is anchor but has no child");
						return false;
					}
					// Get first child as new parent.
					parent = children.get(0);
				}

				// Aadd this new DocStruct object to the parent.
				try {
					if (!parent.addChild(newStruct)) {
						System.err
								.println("ERROR: Can't read Gliederung; can't add child");
						System.err.println("       "
								+ newStruct.getType().getName()
								+ " can't be added to "
								+ parent.getType().getName());
						return false;
					}
				} catch (TypeNotAllowedAsChildException tnaace) {
					// Type is not allowed to be added; wrong configuration.
					System.err
							.println("ERROR: Can't read Gliederung; can't add child");
					System.err.println("       "
							+ newStruct.getType().getName()
							+ " can't be added to "
							+ parent.getType().getName());
					return false;
				}

				// Store this newStruct as the current DocStruct instance for
				// hierarchy level 1.
				alllevels[1] = newStruct;
			} else {
				DocStruct oldStruct = alllevels[hierarchy - 1];
				if (oldStruct == null) {
					System.err.println("ERROR: ReadGliederung");
					System.err
							.println("       Can't find any structure entity on level"
									+ (hierarchy - 1));
					return false;
				}
				try {
					oldStruct.addChild(newStruct);
				} catch (TypeNotAllowedAsChildException tnaace) {
					System.err
							.println("ERROR: ReadGliederung: Can't add child");
					System.err.println("       "
							+ newStruct.getType().getName()
							+ " can't be added to "
							+ oldStruct.getType().getName());
					return false;
				}
				alllevels[hierarchy] = newStruct;
			}
			// Store the old value, so we can compare it with the new one in the
			// next row.
			oldhierarchy = hierarchy;

			// Add new DocStruct instance to LinkedList and add all other
			// information.
			this.allDocStruct.add(newStruct);
			this.allStructRow.add(String.valueOf(x));
			this.allStructSheets.add("Gliederung");

		}

		// Calculate physical endpages and add references for pages (DocStruct
		// instances).
		DocStruct parent = this.mydoc.getLogicalDocStruct();
		if (parent == null) {
			System.out
					.println("ERROR: Can't find any parent element on topmost level");
			return false;
		}

		DocStructType parentType = parent.getType();
		if (parentType.getAnchorClass() != null) {
			// It's an anchor (e.g. a periodical...) so we cannot add any
			// children to this but we must get the next structure entity
			// (child) - e.g. the volume.
			List<DocStruct> children = parent.getAllChildren();
			if (children == null) {
				System.out.println("ERROR: Parent is anchor but has no child");
				return false;
			}
			// Get first child as new parent.
			parent = children.get(0);
		}

		// Calculates the endpages of the children - NOT of the parent we know
		// the parent pages already from the Pagination Sequences.
		CalculateEndPage(parent);

		return true;
	}

	/***************************************************************************
	 * <p>
	 * Calculates the endpages of all DocStruct-instances which are children of
	 * the given inStruct instance. It also creates the references to the pages;
	 * this means, that we must have at least an uppermost logical structure and
	 * a physical structure which contains all pages.
	 * </p>
	 * 
	 * @param inStruct
	 *            parent structure entity
	 * @return true, if everything was okay; otherwise false
	 * @throws MetadataTypeNotAllowedException
	 **************************************************************************/
	public boolean CalculateEndPage(DocStruct inStruct)
			throws MetadataTypeNotAllowedException {

		List<DocStruct> allchildren = inStruct.getAllChildren();
		if (allchildren == null) {
			return true;
		}

		for (int i = 0; i < allchildren.size(); i++) {
			String endpageString = null;
			String startpageString = null;
			int physendpage = 0;
			int physstartpage = 0;

			DocStruct currentchild = allchildren.get(i);

			MetadataType pagenumberstartPhys = this.myPreferences
					.getMetadataTypeByName("_pagephysstart");
			MetadataType pagenumberendPhys = this.myPreferences
					.getMetadataTypeByName("_pagephysend");

			List<? extends Metadata> mdlist = currentchild
					.getAllMetadataByType(pagenumberstartPhys);
			if (mdlist.isEmpty()) {
				System.out
						.println("ERROR: Parent-structure entity has no physical endpage...");
				return false;
			}
			// Get first metadata; there should only be one metadata of this
			// type.
			Metadata md = mdlist.get(0);
			startpageString = md.getValue();
			try {
				physstartpage = Integer.parseInt(startpageString);
			} catch (Exception e) {
				System.out.println("ERROR: Invalid Integer-Format: "
						+ startpageString + " is NOT an integer value.");
			}

			// Check, if there is an additional DocStruct instance, of if thisis
			// the last if it is the last, the endpage is the calculated with
			// the endpage of the parent DocStruct.
			if ((i + 1) == allchildren.size()) {
				// It's the last one, so get the endpage of the parent.
				mdlist = inStruct.getAllMetadataByType(pagenumberendPhys);
				if (mdlist.isEmpty()) {
					System.out
							.println("ERROR: Parent-structure entity has no physical endpage...");
					return false;
				}
				// Get first metadata; there should only be one metadata of this
				// type.
				md = mdlist.get(0);
				endpageString = md.getValue();
				try {
					physendpage = Integer.parseInt(endpageString);
				} catch (Exception e) {
					System.out.println("ERROR: Invalid Integer-Format: "
							+ endpageString + " is NOT an integer value.");
				}
			} else {
				// It's not the lastone; so the endpage is the startpage of the
				// next one or one page before (depends on overlapping).
				DocStruct nextstruct = allchildren.get(i + 1);
				mdlist = nextstruct.getAllMetadataByType(pagenumberstartPhys);
				if (mdlist == null) {
					System.out
							.println("ERROR: Next structure entity has no physical startpage...");
					return false;
				}
				// Get first metadata; there should only be one metadata of this
				// type.
				md = mdlist.get(0);
				endpageString = md.getValue();
				try {
					physendpage = Integer.parseInt(endpageString);
				} catch (Exception e) {
					System.out.println("ERROR: Invalid Integer-Format: "
							+ endpageString + " is NOT an integer value.");
				}
				// Check, if chapters overlap; startpage of next chapter is
				// endpage of the chapter before.
				MetadataType overlapmdt = this.myPreferences
						.getMetadataTypeByName("_overlapping");
				// Get overlapping information from next entity.
				mdlist = nextstruct.getAllMetadataByType(overlapmdt);
				if (mdlist == null) {
					System.out
							.println("ERROR: Next structure entity has no physical startpage...");
					return false;
				}
				Metadata overlapmd = mdlist.get(0);
				String overlapString = overlapmd.getValue();
				if ((overlapString != null)
						&& (overlapString.equalsIgnoreCase("x"))) {
					// The two chapters overlap; endpage is startpage of next
					// chapter.
				} else {
					// It does not overlap; endpage is the page before the
					// startpage of next chapter.
					physendpage = physendpage - 1;
				}
			}

			MetadataType endpagemd = this.myPreferences
					.getMetadataTypeByName("_pagephysend");
			Metadata mdnew = new Metadata(endpagemd);
			mdnew.setValue(Integer.toString(physendpage));
			try {
				// Add physical page number.
				currentchild.addMetadata(mdnew);
			} catch (MetadataTypeNotAllowedException mtnaae) {
				System.err
						.println("ERROR: CalculateEndPage: can't add metadata");
				System.err.println("       " + mdnew.getType().getName()
						+ " can't be added to "
						+ currentchild.getType().getName());
				return false;
			}
			// Create references from startpage to physendpage get uppermost
			// physical structure.
			DocStruct boundbook = this.mydoc.getPhysicalDocStruct();
			List<DocStruct> allpages = boundbook.getAllChildren();
			for (int x = physstartpage; x < physendpage + 1; x++) {
				// x is physical pagenumber.
				for (int y = 0; y < allpages.size(); y++) {
					DocStruct page = allpages.get(y);
					MetadataType physpagetype = this.myPreferences
							.getMetadataTypeByName("physPageNumber");
					List<? extends Metadata> allmds = page
							.getAllMetadataByType(physpagetype);
					if ((allmds == null) || (allmds.size() > 1)) {
						// Error occurred; every page MUST HAVE a physical page
						// number.
						System.out
								.println("ERROR: Calculate EndPage: page does not have a physical pagenumber or more than one physical pagenumber");
						return false;
					}
					// Get first metadata.
					Metadata mymd = allmds.get(0);
					String mdvalue = mymd.getValue();
					try {
						int physInt = Integer.parseInt(mdvalue);
						if (physInt == x) {
							// It's the correct page; so add reference, but
							// only, if it's not a monograph or volume etc..
							// (not the topmost logical docstruct).
							if (!(currentchild.getType().isTopmost())) {
								currentchild.addReferenceTo(page,
										"logical_physical");
							} else {
								// Set a single reference to the boundbook
								// (physical struct).
								List<Reference> refs = currentchild
										.getAllReferences("to");
								if (refs.size() == 0) {
									// No references set, so set one to the
									// bound book.
									DocStruct topphys = this.mydoc
											.getPhysicalDocStruct();
									currentchild.addReferenceTo(topphys,
											"logical_physical");
								}
							}
						}
					} catch (Exception e) {
						System.out
								.println("FUNNY: CalcualteEndPage: physical page number seems not to be an integer...");
					}
				}
			}
		}

		// New for loop; call CalculateEndPage for every child.
		for (int i = 0; i < allchildren.size(); i++) {
			DocStruct currentdoc = allchildren.get(i);
			if (!CalculateEndPage(currentdoc)) {
				// Error occurred.
				return false;
			}
		}
		return true;
	}

	/***************************************************************************
	 * Calculates the physical page number and read
	 * 
	 * @param inSequence
	 * @return physical page number as integer; or -1 if it cannot be
	 **************************************************************************/
	public int CalculatePhysicalNumber(PaginationSequence inSequence,
			int logical) {
		// Check if it's an uncounted page.
		if (inSequence.logcountedstart != 0) {
			// It's a counted page.
			if ((logical < inSequence.logcountedstart)
					|| (logical > inSequence.logcountedend)) {
				return -1;
			}
			int diff = logical - inSequence.logcountedstart;
			int returnvalue = inSequence.physicalstart + diff;
			return (returnvalue);
		}
		if (inSequence.lognotcountedstart != 0) {
			// It's an uncounted page.
			if ((logical < inSequence.lognotcountedstart)
					|| (logical > inSequence.lognotcountedend)) {
				return -1;
			}
			int diff = logical - inSequence.lognotcountedstart;
			int returnvalue = inSequence.physicalstart + diff;
			return (returnvalue);
		}
		return 0;
	}

	/***************************************************************************
	 * @param inSheet
	 * @param Type
	 * @param Version
	 * @return
	 **************************************************************************/
	private String ReadValueInBib(HSSFSheet inSheet, String Type, String Version) {
		for (int x = 0; x < inSheet.getPhysicalNumberOfRows(); x++) {
			org.apache.poi.hssf.usermodel.HSSFRow currentRow = inSheet
					.getRow(x);
			org.apache.poi.hssf.usermodel.HSSFCell currentCell = currentRow
					.getCell((short) 0);

			if ((currentCell != null)
					&& (currentCell.getCellType() == HSSFCell.CELL_TYPE_STRING)
					&& (currentCell.getStringCellValue().equals(Type))) {
				// Cell found; get value.
				org.apache.poi.hssf.usermodel.HSSFCell valuecell = currentRow
						.getCell((short) 1);
				if (valuecell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
					return valuecell.getStringCellValue();
				}
				if (valuecell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
					String res = Double.toString(valuecell
							.getNumericCellValue());
					// Decimal dot; change it for internationalisation.
					int pos = res.indexOf(".");
					res = res.substring(0, pos);
					return res;
				}
			}
		}

		return null;
	}

	/***************************************************************************
	 * @param inString
	 * @return
	 * @throws MetadataTypeNotAllowedException
	 **************************************************************************/
	private List<Metadata> ReadAuthors(String inString)
			throws MetadataTypeNotAllowedException {
		List<Metadata> result = new LinkedList<Metadata>();
		// Position from where to look for the ";".
		int pos = 0;
		int oldpos = 0;
		if (inString == null) {
			System.err.println("ERROR: No author-string available...");
			return null;
		}

		inString = inString + ";";
		while (inString.indexOf(";") > 0) {
			oldpos = 0;
			pos = inString.indexOf(";");
			// No additional ";" was found.
			if (pos == 0) {
				pos = inString.length();
			}
			String author = inString.substring(oldpos, pos);
			// Delete first character if it's a blank.
			if (author.startsWith(" ")) {
				author = author.substring(1, author.length());
			}
			// Delete last character if it's a blank.
			if (author.endsWith(" ")) {
				author = author.substring(0, author.length() - 1);
			}
			inString = inString.substring(pos + 1, inString.length());
			if (author != null) {
				// Create new Metadata object.
				MetadataType mdtype = getMDTypeByName("AUTOREN/HERAUSGEBER",
						"ExcelName");
				if (mdtype == null) {
					System.err
							.println("ERROR: Can't find AUTOREN/HERAUSGEBER for table \"Bibliographie\"!");
					return null;
				}
				Metadata md = new Metadata(mdtype);
				md.setValue(author);
				result.add(md);
			}
			if (inString == null) {
				break;
			}
		}

		return result;
	}

	/***************************************************************************
	 * @param inString
	 * @return
	 * @throws MetadataTypeNotAllowedException
	 **************************************************************************/
	private List<Metadata> ReadPlacePub(String inString)
			throws MetadataTypeNotAllowedException {
		List<Metadata> result = new LinkedList<Metadata>();
		// Position from where to look for the ";".
		int pos = 0;
		int oldpos = 0;
		if (inString == null) {
			System.err
					.println("ERROR: No place of publication-string available...");
			return null;
		}
		inString = inString + ";";
		while (inString.indexOf(";") > 0) {
			oldpos = 0;
			pos = inString.indexOf(";");
			// No additional ";" was found.
			if (pos == 0) {
				pos = inString.length();
			}
			String author = inString.substring(oldpos, pos);
			// Delete first character if it's a blank.
			if (author.startsWith(" ")) {
				author = author.substring(1, author.length());
			}
			// Delete last character if it's a blank.
			if (author.endsWith(" ")) {
				author = author.substring(0, author.length() - 1);
			}
			inString = inString.substring(pos + 1, inString.length());
			if (author != null) {
				// Create new Metadata object.
				MetadataType mdtype = getMDTypeByName("ERSCHEINUNGSORT",
						"ExcelName");
				if (mdtype == null) {
					System.err
							.println("ERROR: Can't find ERSCHEINUNGSORT for language \"excel\"!");
					return null;
				}
				Metadata md = new Metadata(mdtype);
				md.setValue(author);
				result.add(md);
			}
			if (inString == null) {
				break;
			}
		}

		return result;
	}

	/***************************************************************************
	 * @param mdName
	 * @param sheetName
	 * @return
	 **************************************************************************/
	private MetadataType getMDTypeByName(String mdName, String sheetName) {
		MetadataType result = null;
		String mdtName = null;

		// Get internal Name.
		if (sheetName.equals("ExcelName")) {
			mdtName = this.excelNamesMD.get(mdName);
		}
		if (sheetName.equals("ExcelGliederung")) {
			mdtName = this.excelGliederungMD.get(mdName);
		}
		if (sheetName.equals("ExcelAbb_Karten")) {
			mdtName = this.excelAbbMD.get(mdName);
		}

		if (mdtName == null) {
			// mdtName is null, which means, no type with this name is
			// available.
			return null;
		}
		// Get MetadataType from internalName.
		result = this.myPreferences.getMetadataTypeByName(mdtName);

		return result;
	}

	/***************************************************************************
	 * @param dsName
	 * @param sheetName
	 * @return
	 **************************************************************************/
	private DocStructType getDSTypeByName(String dsName, String sheetName) {
		DocStructType result = null;
		String dstName = null;

		// Get internal Name.
		if (sheetName.equals("ExcelName")) {
			dstName = this.excelNamesDS.get(dsName);
		}
		if (sheetName.equals("ExcelGliederung")) {
			dstName = this.excelGliederungDS.get(dsName);
		}
		if (sheetName.equals("ExcelAbb_Karten")) {
			dstName = this.excelAbbDS.get(dsName);
		}

		if (dstName == null) {
			// mdtName is null, which means, no type with this name is
			// available.
			return null;
		}
		// Get MetadataType from internalName.
		result = this.myPreferences.getDocStrctTypeByName(dstName);

		return result;
	}

	/***************************************************************************
	 * All methods to read preferences for Excel Sheet are handled here.
	 * Preferences are read from XML-file in which all preferences are. When
	 * creating the Excelfile-instance, the preferences are read usually
	 * Instance is created during start-up or by a factory class.
	 * 
	 * @param inNode
	 * @return
	 * @throws PreferencesException
	 **************************************************************************/
	public boolean readPrefs(Node inNode) throws PreferencesException {
		NodeList childlist = inNode.getChildNodes();
		for (int i = 0; i < childlist.getLength(); i++) {
			// Get single node.
			Node currentNode = childlist.item(i);
			if ((currentNode.getNodeType() == ELEMENT_NODE)
					&& (currentNode.getNodeName().equals("Metadata"))) {
				// Read information about a single metadata matching.
				if (!readMetadataPrefs(currentNode)) {
					PreferencesException pe = new PreferencesException(
							"ERROR - can't read preferences for Excel module (Metadata section)");
					throw pe;
				}
			}
			if ((currentNode.getNodeType() == ELEMENT_NODE)
					&& (currentNode.getNodeName().equals("DocStruct"))) {
				// Read information about a single docstruct matching.
				if (!readDocStructPrefs(currentNode)) {
					System.err
							.println("ERROR: Excelfile.readPrefs: error occurred while reading docstructs for excel");
					PreferencesException pe = new PreferencesException(
							"ERROR - can't read preferences for Excel module (DocStruct section)");
					throw pe;
				}
			}
		}

		return true;
	}

	/***************************************************************************
	 * @param inNode
	 * @return
	 **************************************************************************/
	private boolean readMetadataPrefs(Node inNode) {
		String internalName = null;
		String excelName = null;
		String gliederungName = null;
		String abbName = null;

		// Read information from XML.
		NodeList childlist = inNode.getChildNodes();
		for (int i = 0; i < childlist.getLength(); i++) {
			// Get single node.
			Node currentNode = childlist.item(i);
			if ((currentNode.getNodeType() == ELEMENT_NODE)
					&& (currentNode.getNodeName().equals("Name"))) {
				internalName = getTextNodeValue(currentNode);
			}
			if ((currentNode.getNodeType() == ELEMENT_NODE)
					&& (currentNode.getNodeName().equals("ExcelName"))) {
				excelName = getTextNodeValue(currentNode);
			}
			if ((currentNode.getNodeType() == ELEMENT_NODE)
					&& (currentNode.getNodeName().equals("ExcelGliederung"))) {
				gliederungName = getTextNodeValue(currentNode);
			}
			if ((currentNode.getNodeType() == ELEMENT_NODE)
					&& (currentNode.getNodeName().equals("ExcelAbb_Karten"))) {
				abbName = getTextNodeValue(currentNode);
			}
		}
		// Check, if internal Name is really available.
		if (this.myPreferences.getMetadataTypeByName(internalName) == null) {
			// No metadatatype with internalName is available.
			System.err.println("ERROR: Metadata with internal name "
					+ internalName + " isn't available!");
			return false;
		}
		if (excelName != null) {
			// Add it to Hashtable excelNames.
			this.excelNamesMD.put(excelName, internalName);
		}
		if (gliederungName != null) {
			// Add it to Hashtable excelGliederung.
			this.excelGliederungMD.put(gliederungName, internalName);
		}
		if (abbName != null) {
			// Add it to Hashtable excelGliederung.
			this.excelAbbMD.put(abbName, internalName);
		}

		return true;
	}

	/***************************************************************************
	 * @param inNode
	 * @return
	 **************************************************************************/
	private boolean readDocStructPrefs(Node inNode) {
		String internalName = null;
		String excelName = null;
		String gliederungName = null;
		String abbName = null;

		// Read information from XML.
		NodeList childlist = inNode.getChildNodes();
		for (int i = 0; i < childlist.getLength(); i++) {
			// Get single node.
			Node currentNode = childlist.item(i);
			if ((currentNode.getNodeType() == ELEMENT_NODE)
					&& (currentNode.getNodeName().equals("Name"))) {
				internalName = getTextNodeValue(currentNode);
			}
			if ((currentNode.getNodeType() == ELEMENT_NODE)
					&& (currentNode.getNodeName().equals("ExcelName"))) {
				excelName = getTextNodeValue(currentNode);
			}
			if ((currentNode.getNodeType() == ELEMENT_NODE)
					&& (currentNode.getNodeName().equals("ExcelGliederung"))) {
				gliederungName = getTextNodeValue(currentNode);
			}
			if ((currentNode.getNodeType() == ELEMENT_NODE)
					&& (currentNode.getNodeName().equals("ExcelAbb_Karten"))) {
				abbName = getTextNodeValue(currentNode);
			}
		}
		// Check, if internal Name is really available.
		if (this.myPreferences.getDocStrctTypeByName(internalName) == null) {
			// No metadatatype with internalName is available.
			System.err.println("Excelfile.readDocStructPrefs: " + internalName
					+ " not found!");
			return false;
		}
		if (excelName != null) {
			// Add it to Hashtable excelNames.
			this.excelNamesDS.put(excelName, internalName);
		}
		if (gliederungName != null) {
			// Add it to Hashtable excelGliederung.
			this.excelGliederungDS.put(gliederungName, internalName);
		}
		if (abbName != null) {
			// Add it to Hashtable excelGliederung.
			this.excelAbbDS.put(abbName, internalName);
		}

		return true;
	}

	/***************************************************************************
	 * @param inNode
	 * @return
	 **************************************************************************/
	private String getTextNodeValue(Node inNode) {
		String retValue = null;
		NodeList textnodes = inNode.getChildNodes();
		if (textnodes != null) {
			Node textnode = textnodes.item(0);
			if (textnode.getNodeType() != Node.TEXT_NODE) {
				// No text node available; maybe it's another element etc..
				// anyhow: an error.
				return null;
			}
			retValue = textnode.getNodeValue();
		}
		return retValue;
	}

	/***************************************************************************
	 * funny things happen when reading excel files, saved with OpenOffice 1.x
	 * Strings may have funny characters at the end; this method removes those
	 * characters...
	 * 
	 * @param inString
	 * @return trimmed String
	 **************************************************************************/
	private String TrimString(String inString) {
		char lastchar = inString.charAt(inString.length() - 1);

		while (Character.getNumericValue(lastchar) == -1) {
			// Remove character.
			inString = inString.substring(0, (inString.length()) - 1);
			lastchar = inString.charAt(inString.length() - 1);
		}

		return inString;
	}

}
