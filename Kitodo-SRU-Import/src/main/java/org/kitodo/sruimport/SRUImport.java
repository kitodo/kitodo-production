/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.sruimport;

import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.ExternalDataImportInterface;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.api.externaldatamanagement.SingleHit;
import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.config.OPACConfig;
import org.kitodo.exceptions.CatalogException;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.ParameterNotFoundException;
import org.kitodo.exceptions.ResponseHandlerNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SRUImport implements ExternalDataImportInterface {

    private static final Logger logger = LogManager.getLogger(SRUImport.class);
    private static final String NAME_ATTRIBUTE = "[@name]";
    private static final String VALUE_ATTRIBUTE = "[@value]";
    private static final String LABEL_ATTRIBUTE = "[@label]";
    private static final String HOST_CONFIG = "host";
    private static final String SCHEME_CONFIG = "scheme";
    private static final String PATH_CONFIG = "path";
    private static final String PORT_CONFIG = "port";
    private static final String PARAM_TAG = "param";
    private static final String SEARCHFIELD_TAG = "searchField";
    private static final String RETURN_FORMAT_TAG = "returnFormat";
    private static final String METADATA_FORMAT_TAG = "metadataFormat";
    private static final String MODS_RECORD_TAG = "mods";

    private static String protocol;
    private static String host;
    private static String path;
    private static int port = -1;
    private static String idParameter;
    private static String fileFormat;
    private static String metadataFormat;
    private static LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
    private static HashMap<String, String> searchFieldMapping = new HashMap<>();
    private static String equalsOperand = "=";
    private static HttpClient sruClient = HttpClientBuilder.create().build();

    private static HashMap<String, XmlResponseHandler> formatHandlers;

    static {
        formatHandlers = new HashMap<>();
        formatHandlers.put(MetadataFormat.MODS.name(), new ModsResponseHandler());
        formatHandlers.put(MetadataFormat.MARC.name(), new MarcResponseHandler());
        formatHandlers.put(MetadataFormat.PICA.name(), new PicaResponseHandler());
    }

    @Override
    public DataRecord getFullRecordById(String catalogId, String identifier) throws NoRecordFoundException {
        loadOPACConfiguration(catalogId);
        LinkedHashMap<String, String> queryParameters = new LinkedHashMap<>(parameters);
        try {
            URI queryURL = createQueryURI(queryParameters);
            return performQueryToRecord(queryURL.toString(), identifier);
        } catch (URISyntaxException e) {
            throw new ConfigException(e.getLocalizedMessage());
        }
    }

    @Override
    public List<DataRecord> getMultipleFullRecordsFromQuery(String catalogId, String field, String value, int rows) {
        loadOPACConfiguration(catalogId);
        HashMap<String, String> searchFields = new HashMap<>();
        searchFields.put(field, value);
        if (searchFieldMapping.keySet().containsAll(searchFields.keySet())) {
            // Query parameters for HTTP request
            LinkedHashMap<String, String> queryParameters = new LinkedHashMap<>(parameters);

            // Search fields and terms of query
            LinkedHashMap<String, String> searchFieldMap = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : searchFields.entrySet()) {
                searchFieldMap.put(searchFieldMapping.get(entry.getKey()), entry.getValue());
            }
            try {
                URI queryURL = createQueryURI(queryParameters);
                String queryString = queryURL.toString();
                queryString = queryString + "&startRecord=0&maximumRecords=" + rows + "&query="
                        + createSearchFieldString(searchFieldMap);
                return performQueryToMultipleRecords(queryString);
            } catch (URISyntaxException | IOException | ParserConfigurationException | SAXException
                    | TransformerException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public SearchResult search(String catalogId, String field, String term, int rows) {
        loadOPACConfiguration(catalogId);
        HashMap<String, String> searchFields = new HashMap<>();
        searchFields.put(field, term);
        return search(catalogId, searchFields, 1, rows);
    }

    @Override
    public SearchResult search(String catalogId, String field, String term, int start, int rows) {
        loadOPACConfiguration(catalogId);
        HashMap<String, String> searchFields = new HashMap<>();
        searchFields.put(field, term);
        return search(catalogId, searchFields, start, rows);
    }

    private SearchResult search(String catalogId, Map<String, String> searchParameters, int start, int numberOfRecords) {
        loadOPACConfiguration(catalogId);
        if (searchFieldMapping.keySet().containsAll(searchParameters.keySet())) {

            // Query parameters for HTTP request
            LinkedHashMap<String, String> queryParameters = new LinkedHashMap<>(parameters);

            // Search fields and terms of query
            LinkedHashMap<String, String> searchFieldMap = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : searchParameters.entrySet()) {
                searchFieldMap.put(searchFieldMapping.get(entry.getKey()), entry.getValue());
            }

            try {
                URI queryURL = createQueryURI(queryParameters);
                String queryString = queryURL.toString();
                if (start > 0 ) {
                    queryString += "&startRecord=" + start;
                }
                return performQuery(queryString
                                + "&maximumRecords=" + numberOfRecords
                                + "&query=" + createSearchFieldString(searchFieldMap));
            } catch (URISyntaxException | UnsupportedEncodingException | ResponseHandlerNotFoundException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
        return null;
    }

    @Override
    public Collection<SingleHit> getMultipleEntriesById(Collection<String> ids, String catalogId) {
        return Collections.emptyList();
    }

    private SearchResult performQuery(String queryURL) throws ResponseHandlerNotFoundException {
        try {
            HttpResponse response = sruClient.execute(new HttpGet(queryURL));
            int responseStatusCode = response.getStatusLine().getStatusCode();
            if (Objects.equals(responseStatusCode, SC_OK)) {
                if (formatHandlers.containsKey(metadataFormat)) {
                    return formatHandlers.get(metadataFormat).getSearchResult(response);
                } else {
                    throw new ResponseHandlerNotFoundException("No ResponseHandler found for metadata format "
                            + metadataFormat);
                }
            } else {
                throw new CatalogException(response.getStatusLine().getReasonPhrase() + " (Http status code "
                        + responseStatusCode + ")");
            }
        } catch (UnknownHostException e) {
            throw new CatalogException("Unknown host: " + e.getMessage());
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        return new SearchResult();
    }

    private DataRecord performQueryToRecord(String queryURL, String identifier) throws NoRecordFoundException {
        String fullUrl = queryURL + "&maximumRecords=1&query=" + idParameter + equalsOperand + identifier;
        try {
            HttpResponse response = sruClient.execute(new HttpGet(fullUrl));
            if (Objects.equals(response.getStatusLine().getStatusCode(), SC_OK)) {
                if (Objects.isNull(response.getEntity())) {
                    throw new NoRecordFoundException("No record with ID '" + identifier + "' found!");
                }
                return createRecordFromXMLElement(IOUtils.toString(response.getEntity().getContent(),
                        Charset.defaultCharset()));
            }
            throw new ConfigException("SRU Request Failed");
        } catch (IOException e) {
            throw new ConfigException(e.getLocalizedMessage());
        }
    }

    private List<DataRecord> performQueryToMultipleRecords(String queryURL) throws IOException,
            ParserConfigurationException, SAXException, TransformerException {
        List<DataRecord> records = new LinkedList<>();
        HttpGet request = new HttpGet(queryURL);
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectionRequestTimeout(3000);
        requestConfigBuilder.setConnectTimeout(3000);
        request.setConfig(requestConfigBuilder.build());
        try {
            HttpResponse response = sruClient.execute(request);
            int responseStatusCode = response.getStatusLine().getStatusCode();
            if (Objects.equals(responseStatusCode, SC_OK)) {
                String xmlContent = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
                Document document = stringToDocument(xmlContent);
                NodeList recordNodes = document.getElementsByTagName(MODS_RECORD_TAG);
                for (int i = 0; i < recordNodes.getLength(); i++) {
                    records.add(createRecordFromXMLElement(nodeToString(recordNodes.item(i))));
                }
            } else {
                throw new CatalogException(response.getStatusLine().getReasonPhrase() + " (Http status code "
                        + responseStatusCode + ")");
            }
        } catch (ConnectTimeoutException e) {
            throw new CatalogException("Connection exception: OPAC did not respond within the configured time limit!");
        }

        return records;
    }

    private DataRecord createRecordFromXMLElement(String xmlContent) {
        DataRecord record = new DataRecord();
        record.setMetadataFormat(MetadataFormat.getMetadataFormat(metadataFormat));
        record.setFileFormat(FileFormat.getFileFormat(fileFormat));
        record.setOriginalData(xmlContent);
        return record;
    }

    private URI createQueryURI(LinkedHashMap<String, String> searchFields) throws URISyntaxException {
        return new URI(protocol, null, host, port, path, createQueryParameterString(searchFields), null);
    }

    private String createQueryParameterString(LinkedHashMap<String, String> searchFields) {
        List<BasicNameValuePair> nameValuePairList = searchFields.entrySet().stream()
                .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return URLEncodedUtils.format(nameValuePairList, StandardCharsets.UTF_8);
    }

    private String createSearchFieldString(LinkedHashMap<String, String> searchFields) throws UnsupportedEncodingException {
        List<String> searchOperands = searchFields.entrySet().stream()
                .map(entry -> entry.getKey() + equalsOperand + entry.getValue())
                .collect(Collectors.toList());
        return URLEncoder.encode(String.join(" AND ", searchOperands), StandardCharsets.UTF_8.displayName());
    }

    private static void loadOPACConfiguration(String opacName) {
        try {
            // XML configuration of OPAC
            HierarchicalConfiguration opacConfig = OPACConfig.getOPACConfiguration(opacName);

            for (HierarchicalConfiguration queryConfigParam : opacConfig.configurationsAt(PARAM_TAG)) {
                switch (queryConfigParam.getString(NAME_ATTRIBUTE)) {
                    case SCHEME_CONFIG:
                        protocol = queryConfigParam.getString(VALUE_ATTRIBUTE);
                        break;
                    case HOST_CONFIG:
                        host = queryConfigParam.getString(VALUE_ATTRIBUTE);
                        break;
                    case PATH_CONFIG:
                        path = queryConfigParam.getString(VALUE_ATTRIBUTE);
                        break;
                    case PORT_CONFIG:
                        port = queryConfigParam.getInt(VALUE_ATTRIBUTE);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + queryConfigParam.getString(NAME_ATTRIBUTE));
                }
            }

            idParameter = OPACConfig.getIdentifierParameter(opacName);
            fileFormat = OPACConfig.getConfigValue(opacName, RETURN_FORMAT_TAG);
            metadataFormat = OPACConfig.getConfigValue(opacName, METADATA_FORMAT_TAG);

            HierarchicalConfiguration searchFields = OPACConfig.getSearchFields(opacName);

            for (HierarchicalConfiguration searchField : searchFields.configurationsAt(SEARCHFIELD_TAG)) {
                searchFieldMapping.put(searchField.getString(LABEL_ATTRIBUTE), searchField.getString(VALUE_ATTRIBUTE));
            }

            HierarchicalConfiguration urlParameters = OPACConfig.getUrlParameters(opacName);

            for (HierarchicalConfiguration queryParam : urlParameters.configurationsAt(PARAM_TAG)) {
                parameters.put(queryParam.getString(NAME_ATTRIBUTE), queryParam.getString(VALUE_ATTRIBUTE));
            }
        } catch (IllegalArgumentException | ParameterNotFoundException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    private Document stringToDocument(String xmlContent) throws ParserConfigurationException, IOException,
            SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(new InputSource(new StringReader(xmlContent)));
    }

    private String nodeToString(Node node) throws TransformerException {
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }
}
