/*
 * Copyright by intranda GmbH 2013. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.kitodo.production.plugin.importer.massimport;

import de.sub.goobi.helper.exceptions.ImportPluginException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.goobi.production.enums.ImportReturnValue;
import org.goobi.production.enums.ImportType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.importer.DocstructElement;
import org.goobi.production.importer.ImportObject;
import org.goobi.production.importer.Record;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.properties.ImportProperty;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.MetsModsInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.data.database.beans.Property;
import org.kitodo.legacy.UghImplementation;
import org.kitodo.production.plugin.importer.massimport.sru.SRUHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@PluginImplementation
public class PicaMassImport implements IImportPlugin, IPlugin {

    private static final Logger logger = LogManager.getLogger(PicaMassImport.class);
    private static final String NAME = "intranda Pica Massenimport";
    private String data = "";
    private String importFolder = "";
    private File importFile;
    private PrefsInterface prefs;
    private String currentIdentifier;
    private List<String> currentCollectionList;
    private String opacCatalogue;
    private String configDir;
    private static final String PPN_PATTERN = "\\d+X?";
    private static final String[] SERIAL_TOTALITY_IDENTIFIER_FIELD = new String[] {"036F", "9" };
    private static final String[] TOTALITY_IDENTIFIER_FIELD = new String[] {"036D", "9" };

    protected String ats;
    protected List<Property> workpieceProperties = new ArrayList<>();
    protected List<Property> templateProperties = new ArrayList<>();

    protected String currentTitle;
    protected String author = "";
    protected String volumeNumber = "";

    public String getId() {
        return NAME;
    }

    @Override
    public PluginType getType() {
        return PluginType.IMPORT;
    }

    @Override
    public String getTitle() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return NAME;
    }

    @Override
    public void setPrefs(PrefsInterface prefs) {
        this.prefs = prefs;
    }

    @Override
    public void setData(Record r) {
        this.data = r.getData();
    }

    @Override
    public FileformatInterface convertData() throws ImportPluginException {

        currentIdentifier = data;

        logger.debug("retrieving pica record for {} with server address: {}", this.currentIdentifier, getOpacAddress());
        String search = SRUHelper.search(currentIdentifier, this.getOpacAddress());
        logger.trace(search);
        try {
            Node pica = SRUHelper.parseResult(search);
            if (pica == null) {
                String mess = "PICA record for " + currentIdentifier + " does not exist in catalogue.";
                logger.error(mess);
                throw new ImportPluginException(mess);

            }
            pica = addParentDataForVolume(pica);
            FileformatInterface fileformat = SRUHelper.parsePicaFormat(pica, prefs);
            DigitalDocumentInterface digitalDocument = fileformat.getDigitalDocument();
            boolean multivolue = false;
            DocStructInterface logicalDS = digitalDocument.getLogicalDocStruct();
            DocStructInterface child = null;
            if (logicalDS.getDocStructType().getAnchorClass() != null) {
                child = logicalDS.getAllChildren().get(0);
                multivolue = true;
            }
            // reading title
            MetadataTypeInterface titleType = prefs.getMetadataTypeByName("TitleDocMain");
            List<? extends MetadataInterface> mdList = logicalDS.getAllMetadataByType(titleType);
            if (mdList != null && mdList.size() > 0) {
                MetadataInterface title = mdList.get(0);
                currentTitle = title.getValue();

            }
            // reading identifier
            MetadataTypeInterface identifierType = prefs.getMetadataTypeByName("CatalogIDDigital");
            List<? extends MetadataInterface> childMdList = null;
            if (child != null) {
                childMdList = child.getAllMetadataByType(identifierType);
            }
            if (childMdList != null) {
                mdList = childMdList;
            } else {
                mdList = logicalDS.getAllMetadataByType(identifierType);
            }
            if (mdList != null && mdList.size() > 0) {
                MetadataInterface identifier = mdList.get(0);
                currentIdentifier = identifier.getValue();
            } else {
                currentIdentifier = String.valueOf(System.currentTimeMillis());
            }

            // reading author

            MetadataTypeInterface authorType = prefs.getMetadataTypeByName("Author");
            List<PersonInterface> personList = logicalDS.getAllPersonsByType(authorType);
            if (personList != null && personList.size() > 0) {
                PersonInterface authorMetadata = personList.get(0);
                author = authorMetadata.getDisplayName();

            }

            // reading volume number
            if (child != null) {
                MetadataTypeInterface mdt = prefs.getMetadataTypeByName("CurrentNoSorting");
                mdList = child.getAllMetadataByType(mdt);
                if (mdList != null && mdList.size() > 0) {
                    MetadataInterface md = mdList.get(0);
                    volumeNumber = md.getValue();
                } else {
                    mdt = prefs.getMetadataTypeByName("DateIssuedSort");
                    mdList = child.getAllMetadataByType(mdt);
                    if (mdList != null && mdList.size() > 0) {
                        MetadataInterface md = mdList.get(0);
                        volumeNumber = md.getValue();
                    }
                }
            }

            // reading ats
            MetadataTypeInterface atsType = prefs.getMetadataTypeByName("TSL_ATS");
            mdList = logicalDS.getAllMetadataByType(atsType);
            if (mdList != null && mdList.size() > 0) {
                MetadataInterface atstsl = mdList.get(0);
                ats = atstsl.getValue();
            } else {
                // generating ats
                ats = createAtstsl(currentTitle, author);
                MetadataInterface atstsl = UghImplementation.INSTANCE.createMetadata(atsType);
                atstsl.setStringValue(ats);
                logicalDS.addMetadata(atstsl);
            }

            templateProperties.add(prepareProperty("Titel", currentTitle));

            if (StringUtils.isNotBlank(volumeNumber) && multivolue) {
                templateProperties.add(prepareProperty("Bandnummer", volumeNumber));
            }

                MetadataTypeInterface identifierAnalogType = prefs.getMetadataTypeByName("CatalogIDSource");
            mdList = logicalDS.getAllMetadataByType(identifierAnalogType);
            if (mdList != null && mdList.size() > 0) {
                String analog = mdList.get(0).getValue();
                templateProperties.add(prepareProperty("Identifier", analog));
            }

            if (child != null) {
                mdList = child.getAllMetadataByType(identifierType);
                if (mdList != null && mdList.size() > 0) {
                        MetadataInterface identifier = mdList.get(0);
                    workpieceProperties.add(prepareProperty("Identifier Band", identifier.getValue()));
                }
            }

            workpieceProperties.add(prepareProperty("Artist", author));
            workpieceProperties.add(prepareProperty("ATS", ats));
            workpieceProperties.add(prepareProperty("Identifier", currentIdentifier));

            try {
                // pathimagefiles
                MetadataTypeInterface mdt = prefs.getMetadataTypeByName("pathimagefiles");
                MetadataInterface newmd = UghImplementation.INSTANCE.createMetadata(mdt);
                newmd.setStringValue("/images/");
                digitalDocument.getPhysicalDocStruct().addMetadata(newmd);

                // collections
                if (this.currentCollectionList != null) {
                    MetadataTypeInterface mdTypeCollection = this.prefs.getMetadataTypeByName("singleDigCollection");
                    DocStructInterface topLogicalStruct = digitalDocument.getLogicalDocStruct();
                    List<DocStructInterface> volumes = topLogicalStruct.getAllChildren();
                    if (volumes == null) {
                        volumes = Collections.emptyList();
                    }
                    for (String collection : this.currentCollectionList) {
                        MetadataInterface mdCollection = UghImplementation.INSTANCE.createMetadata(mdTypeCollection);
                        mdCollection.setStringValue(collection);
                        topLogicalStruct.addMetadata(mdCollection);
                        for (DocStructInterface volume : volumes) {
                            try {
                                MetadataInterface mdCollectionForVolume = UghImplementation.INSTANCE
                                        .createMetadata(mdTypeCollection);
                                mdCollectionForVolume.setStringValue(collection);
                                volume.addMetadata(mdCollectionForVolume);
                            } catch (MetadataTypeNotAllowedException e) {
                                logger.error(this.currentIdentifier + ": " + e.getMessage(), e);
                            }
                        }
                    }
                }

            } catch (MetadataTypeNotAllowedException e) {
                logger.error(this.currentIdentifier + ": " + e.getMessage(), e);
            }

            return fileformat;
        } catch (PreferencesException | ReadException | IOException | JDOMException | ParserConfigurationException
                | MetadataTypeNotAllowedException e) {
            logger.error(this.currentIdentifier + ": " + e.getMessage(), e);
            throw new ImportPluginException(e);
        }
    }

    private Property prepareProperty(String title, String value) {
        Property property = new Property();
        property.setTitle(title);
        property.setValue(value);
        return property;
    }

    /**
     * If the record contains a volume of a serial publication, then the series
     * data will be prepended to it.
     *
     * @param volumeRecord
     *            hitlist with one hit which will may be a volume of a serial
     *            publication
     * @return hitlist that may have been extended to contain two hits, first
     *         the series record and second the volume record
     * @throws ImportPluginException
     *             if something goes wrong in {@link #getOpacAddress()}
     */
    private Node addParentDataForVolume(Node volumeRecord) throws ImportPluginException {
        try {
            org.jdom.Document volumeAsJDOMDoc = new DOMBuilder().build(volumeRecord.getOwnerDocument());
            Element volumeAsJDOM = volumeAsJDOMDoc.getRootElement().getChild("record");
            String parentPPN = getFieldValueFromRecord(volumeAsJDOM, SERIAL_TOTALITY_IDENTIFIER_FIELD);
            if (parentPPN.isEmpty()) {
                parentPPN = getFieldValueFromRecord(volumeAsJDOM, TOTALITY_IDENTIFIER_FIELD);
            }
            if (!parentPPN.isEmpty()) {
                Node parentRecord = SRUHelper.parseResult(SRUHelper.search(parentPPN, getOpacAddress()));
                if (parentRecord == null) {
                    String mess = "Could not retrieve superordinate record " + parentPPN;
                    logger.error(mess);
                    throw new ImportPluginException(mess);
                }
                org.jdom.Document resultAsJDOM = new DOMBuilder().build(parentRecord.getOwnerDocument());
                volumeAsJDOM.getParent().removeContent(volumeAsJDOM);
                resultAsJDOM.getRootElement().addContent(volumeAsJDOM);
                return new DOMOutputter().output(resultAsJDOM).getFirstChild();
            }
        } catch (RuntimeException | ImportPluginException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportPluginException(e.getMessage(), e);
        }
        return volumeRecord;
    }

    /**
     * Reads a field value from an XML record. Returns the empty string if not
     * found.
     *
     * @param record
     *            record data to parse
     * @param field
     *            field value to return
     * @return field value, or ""
     *
     */
    @SuppressWarnings("unchecked")
    private static String getFieldValueFromRecord(Element record, String[] field) {
        for (Element child : (List<Element>) record.getChildren()) {
            String fieldName = child.getAttributeValue("tag");
            if (fieldName.equals(field[0])) {
                return getSubelementValue(child, field[1]);
            }
        }
        return "";
    }

    /**
     * Reads a sub field value from an XML node. Returns the empty string if not
     * found.
     *
     * @param inElement
     *            XML node to look into
     * @param attributeValue
     *            attribute to locate
     * @return value, or "" if not found
     * 
     */
    @SuppressWarnings("unchecked")
    private static String getSubelementValue(Element inElement, String attributeValue) {
        String result = "";
        for (Element subElement : (List<Element>) inElement.getChildren()) {
            if (subElement.getAttributeValue("code").equals(attributeValue)) {
                result = subElement.getValue();
            }
        }
        return result;
    }

    @Override
    public String getImportFolder() {
        return this.importFolder;
    }

    @Override
    public String getProcessTitle() {
        String answer;
        if (StringUtils.isNotBlank(this.ats)) {
            answer = ats.toLowerCase() + "_" + this.currentIdentifier;
        } else {
            answer = this.currentIdentifier;
        }
        if (StringUtils.isNotBlank(volumeNumber)) {
            answer = answer + "_" + volumeNumber;
        }
        return answer;
    }

    @Override
    public List<ImportObject> generateFiles(List<Record> records) {
        List<ImportObject> answer = new ArrayList<>();

        for (Record r : records) {
            this.data = r.getData();
            this.currentCollectionList = r.getCollections();
            ImportObject io = new ImportObject();
            FileformatInterface ff = null;
            try {
                ff = convertData();
            } catch (ImportPluginException e1) {
                io.setErrorMessage(e1.getMessage());
            }
            io.setProcessTitle(getProcessTitle());
            if (ff != null) {
                r.setId(this.currentIdentifier);
                try {
                    MetsModsInterface mm = UghImplementation.INSTANCE.createMetsMods(this.prefs);
                    mm.setDigitalDocument(ff.getDigitalDocument());
                    String fileName = getImportFolder() + getProcessTitle() + ".xml";
                    logger.debug("Writing '{}' into given folder...", fileName);
                    mm.write(fileName);
                    io.setMetsFilename(new File(fileName).toURI());
                    io.setImportReturnValue(ImportReturnValue.ExportFinished);

                } catch (PreferencesException e) {
                    logger.error(currentIdentifier + ": " + e.getMessage(), e);
                    io.setImportReturnValue(ImportReturnValue.InvalidData);
                } catch (WriteException e) {
                    logger.error(currentIdentifier + ": " + e.getMessage(), e);
                    io.setImportReturnValue(ImportReturnValue.WriteError);
                }
            } else {
                io.setImportReturnValue(ImportReturnValue.InvalidData);
            }
            answer.add(io);
        }

        return answer;
    }

    @Override
    public void setImportFolder(String folder) {
        this.importFolder = folder;
    }

    @Override
    public List<Record> splitRecords(String records) {
        return new ArrayList<>();
    }

    @Override
    public List<Record> generateRecordsFromFile() {
        List<Record> records = new ArrayList<>();

        try (InputStream myxls = new FileInputStream(importFile)) {
            if (importFile.getName().endsWith(".xlsx")) {
                XSSFWorkbook wb = new XSSFWorkbook(myxls);
                XSSFSheet sheet = wb.getSheetAt(0); // first sheet
                // loop over all rows
                for (int j = 0; j <= sheet.getLastRowNum(); j++) {
                    // loop over all cells
                    XSSFRow row = sheet.getRow(j);

                    if (row != null) {
                        for (int i = 0; i < row.getLastCellNum(); i++) {
                            XSSFCell cell = row.getCell(i);
                            // changing all cell types to String
                            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                            Record record = changeCellTypeToString(cell, i, j);
                            if (record != null) {
                                records.add(record);
                            }
                        }
                    }
                }
            } else {
                HSSFWorkbook wb = new HSSFWorkbook(myxls);
                HSSFSheet sheet = wb.getSheetAt(0); // first sheet
                // loop over all rows
                for (int j = 0; j <= sheet.getLastRowNum(); j++) {
                    // loop over all cells
                    HSSFRow row = sheet.getRow(j);

                    if (row != null) {
                        for (int i = 0; i < row.getLastCellNum(); i++) {
                            HSSFCell cell = row.getCell(i);
                            // changing all cell types to String
                            if (cell != null) {
                                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                                Record record = changeCellTypeToString(cell, i, j);
                                if (record != null) {
                                    records.add(record);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        //
        // Workbook w = Workbook.getWorkbook(importFile);
        // // Get the first sheet
        // Sheet sheet = w.getSheet(0);
        // // loop over all rows in first column
        // for (int i = 0; i < sheet.getRows(); i++) {
        // Cell cell = sheet.getCell(0, i);
        // // get content
        // String value = cell.getContents();
        // // test if content is a valid PPN
        // if (cell.getType().equals(CellType.NUMBER)) {
        // // found numbers only
        // Record r = new Record();
        // r.setId(value.trim());
        // r.setData(value.trim());
        // records.add(r);
        // } else if (cell.getType().equals(CellType.LABEL)) {
        // // found letters in it
        // if (value != null && value.trim().matches(PPN_PATTERN)) {
        // logger.debug("matched: " + value);
        // // found numbers and character 'X' as last sign
        // Record r = new Record();
        // r.setId(value.trim());
        // r.setData(value.trim());
        // records.add(r);
        // }
        // }
        // }
        // } catch (BiffException e) {
        // logger.error(e.getMessage(), e);
        // } catch (IOException e) {
        // logger.error(e.getMessage(), e);
        // }
        return records;
    }

    private Record changeCellTypeToString(Cell cell, int i, int j) {
        Record record = new Record();

        if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
            int value = (int) cell.getNumericCellValue();
            record.setId(String.valueOf(value));
            record.setData(String.valueOf(value));
            return record;
        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
            String value = cell.getStringCellValue();
            if (value.trim().matches(PPN_PATTERN) && value.length() > 6) {
                // remove date and time from list
                logger.debug("matched: " + value + " in row " + (j + 1) + " cell " + i);
                // found numbers and character 'X' as last sign
                record.setId(value.trim());
                record.setData(value.trim());
                return record;
            }
        }
        return null;
    }

    @Override
    public List<Record> generateRecordsFromFilenames(List<String> filenames) {
        return new ArrayList<>();
    }

    @Override
    public void setFile(File importFile) {
        this.importFile = importFile;
    }

    @Override
    public List<String> splitIds(String ids) {
        return new ArrayList<>();
    }

    @Override
    public List<ImportType> getImportTypes() {
        List<ImportType> answer = new ArrayList<>();
        answer.add(ImportType.FILE);
        return answer;
    }

    @Override
    public List<ImportProperty> getProperties() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getAllFilenames() {
        return new ArrayList<>();
    }

    @Override
    public void deleteFiles(List<String> selectedFilenames) {
    }

    @Override
    public List<DocstructElement> getCurrentDocStructs() {
        return new ArrayList<>();
    }

    @Override
    public String deleteDocstruct() {
        return null;
    }

    @Override
    public String addDocstruct() {
        return null;
    }

    @Override
    public List<String> getPossibleDocstructs() {
        return new ArrayList<>();
    }

    @Override
    public DocstructElement getDocstruct() {
        return null;
    }

    @Override
    public void setDocstruct(DocstructElement dse) {
    }

    private String createAtstsl(String myTitle, String author) {
        StringBuilder atsTsl = new StringBuilder();
        if (author != null && !author.equals("")) {
            /* author */
            if (author.length() > 4) {
                atsTsl.append(author.substring(0, 4));
            } else {
                atsTsl.append(author);
                /* titel */
            }

            if (myTitle.length() > 4) {
                atsTsl.append(myTitle.substring(0, 4));
            } else {
                atsTsl.append(myTitle);
            }
        }

        /*
         * bei Zeitschriften Tsl berechnen
         */
        if (author == null || author.equals("")) {
            atsTsl = new StringBuilder();
            StringTokenizer tokenizer = new StringTokenizer(myTitle);
            int counter = 1;
            while (tokenizer.hasMoreTokens()) {
                String tok = tokenizer.nextToken();
                if (counter == 1) {
                    if (tok.length() > 4) {
                        atsTsl.append(tok.substring(0, 4));
                    } else {
                        atsTsl.append(tok);
                    }
                }
                if (counter == 2 || counter == 3) {
                    if (tok.length() > 2) {
                        atsTsl.append(tok.substring(0, 2));
                    } else {
                        atsTsl.append(tok);
                    }
                }
                if (counter == 4) {
                    if (tok.length() > 1) {
                        atsTsl.append(tok.substring(0, 1));
                    } else {
                        atsTsl.append(tok);
                    }
                }
                counter++;
            }
        }
        /* im ATS-TSL die Umlaute ersetzen */
        if (FacesContext.getCurrentInstance() != null) {
            atsTsl = new StringBuilder(UghUtils.convertUmlaut(atsTsl.toString()));
        }
        return atsTsl.toString().replaceAll("[\\W]", "");
    }

    /**
     * Set OPAC catalogue.
     *
     * @param opacCatalogue
     *            the opac catalogue
     */
    @Override
    public void setOpacCatalogue(String opacCatalogue) {
        this.opacCatalogue = opacCatalogue;
    }

    /**
     * Get OPAC catalogue.
     *
     * @return the opac catalogue
     */
    private String getOpacCatalogue() {
        return this.opacCatalogue;
    }

    /**
     * Set Kitodo config directory.
     *
     * @param configDir
     *            the kitodo config directory
     */
    @Override
    public void setKitodoConfigDirectory(String configDir) {
        this.configDir = configDir;
    }

    /**
     * Get Kitodo config directory.
     *
     * @return the kitodo config directory
     */
    private String getGoobiConfigDirectory() {
        return configDir;
    }

    /**
     * Get OPAC address.
     *
     * @return the address of the opac catalogue
     */
    private String getOpacAddress() throws ImportPluginException {

        String address;

        try (FileInputStream istream = new FileInputStream(
                FilenameUtils.concat(this.getGoobiConfigDirectory(), "kitodo_opac.xml"))) {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document xmlDocument = builder.parse(istream);

            XPath xPath = XPathFactory.newInstance().newXPath();

            Node node = (Node) xPath
                    .compile("/opacCatalogues/catalogue[@title='" + this.getOpacCatalogue() + "']/config")
                    .evaluate(xmlDocument, XPathConstants.NODE);

            address = node.getAttributes().getNamedItem("address").getNodeValue();

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            logger.error(e.getMessage(), e);
            throw new ImportPluginException(e);
        }
        return address;
    }
}
