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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.xeoh.plugins.base.annotations.PluginImplementation;

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
import org.jdom.JDOMException;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.exceptions.ImportPluginException;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
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
    private LegacyPrefsHelper prefs;
    private String currentIdentifier;
    private List<String> currentCollectionList;
    private String opacCatalogue;
    private String configDir;
    private static final String PPN_PATTERN = "\\d+X?";

    protected String ats;

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
    public void setPrefs(LegacyPrefsHelper prefs) {
        this.prefs = prefs;
    }

    @Override
    public void setData(Record r) {
        this.data = r.getData();
    }

    @Override
    public LegacyMetsModsDigitalDocumentHelper convertData() throws ImportPluginException {
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

            throw new UnsupportedOperationException("Dead code pending removal");
        } catch (IOException | JDOMException | ParserConfigurationException e) {
            logger.error(this.currentIdentifier + ": " + e.getMessage(), e);
            throw new ImportPluginException(e);
        }
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
            LegacyMetsModsDigitalDocumentHelper ff = null;
            try {
                ff = convertData();
            } catch (ImportPluginException e1) {
                io.setErrorMessage(e1.getMessage());
            }
            io.setProcessTitle(getProcessTitle());
            if (ff != null) {
                r.setId(this.currentIdentifier);
                try {
                    LegacyMetsModsDigitalDocumentHelper mm = new LegacyMetsModsDigitalDocumentHelper(
                            this.prefs.getRuleset());
                    mm.setDigitalDocument(ff.getDigitalDocument());
                    String fileName = getImportFolder() + getProcessTitle() + ".xml";
                    logger.debug("Writing '{}' into given folder...", fileName);
                    mm.write(fileName);
                    io.setMetsFilename(new File(fileName).toURI());
                    io.setImportReturnValue(ImportReturnValue.EXPORT_FINISHED);

                } catch (IOException e) {
                    logger.error(currentIdentifier + ": " + e.getMessage(), e);
                    io.setImportReturnValue(ImportReturnValue.WRITE_ERROR);
                }
            } else {
                io.setImportReturnValue(ImportReturnValue.INVALID_DATA);
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

        try (InputStream xls = new FileInputStream(importFile)) {
            if (importFile.getName().endsWith(".xlsx")) {
                records = getRecordsForXLSX(xls);
            } else {
                records = getRecordsForXLS(xls);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return records;
    }

    private List<Record> getRecordsForXLSX(InputStream xls) throws IOException {
        List<Record> records = new ArrayList<>();

        XSSFWorkbook wb = new XSSFWorkbook(xls);
        XSSFSheet sheet = wb.getSheetAt(0); // first sheet
        // loop over all rows
        for (int j = 0; j <= sheet.getLastRowNum(); j++) {
            // loop over all cells
            XSSFRow row = sheet.getRow(j);
            if (Objects.nonNull(row)) {
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    XSSFCell cell = row.getCell(i);
                    // changing all cell types to String
                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                    Record record = changeCellTypeToString(cell, i, j);
                    if (Objects.nonNull(record)) {
                        records.add(record);
                    }
                }
            }
        }

        return records;
    }

    private List<Record> getRecordsForXLS(InputStream xls) throws IOException {
        List<Record> records = new ArrayList<>();

        HSSFWorkbook wb = new HSSFWorkbook(xls);
        HSSFSheet sheet = wb.getSheetAt(0); // first sheet
        // loop over all rows
        for (int j = 0; j <= sheet.getLastRowNum(); j++) {
            // loop over all cells
            HSSFRow row = sheet.getRow(j);
            if (Objects.nonNull(row)) {
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    HSSFCell cell = row.getCell(i);
                    // changing all cell types to String
                    if (Objects.nonNull(cell)) {
                        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                        Record record = changeCellTypeToString(cell, i, j);
                        if (Objects.nonNull(record)) {
                            records.add(record);
                        }
                    }
                }
            }
        }

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
                logger.debug("matched: {} in row {} cell {}", value, j + 1, i);
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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
     * Get OPAC address.
     *
     * @return the address of the opac catalogue
     */
    private String getOpacAddress() throws ImportPluginException {

        String address;

        try (FileInputStream istream = new FileInputStream(KitodoConfigFile.OPAC_CONFIGURATION.getFile())) {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

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
