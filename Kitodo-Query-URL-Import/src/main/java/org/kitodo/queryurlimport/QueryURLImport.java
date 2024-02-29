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

package org.kitodo.queryurlimport;

import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.DataImport;
import org.kitodo.api.externaldatamanagement.ExternalDataImportInterface;
import org.kitodo.api.externaldatamanagement.SearchInterfaceType;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.api.externaldatamanagement.SingleHit;
import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.exceptions.CatalogException;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.exceptions.NoRecordFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class QueryURLImport implements ExternalDataImportInterface {

    private static final Logger logger = LogManager.getLogger(QueryURLImport.class);
    private static final String MODS_RECORD_TAG = "mods";
    private static final String HTTP_PROTOCOL = "http";
    private static final String HTTPS_PROTOCOL = "https";
    private static final String FTP_PROTOCOL = "ftp";
    private static final String EQUALS_OPERAND = "=";
    private static final String AND = "&";
    private static final String OAI_IDENTIFIER = "identifier";
    private final Charset encoding = StandardCharsets.UTF_8;

    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    private final FTPClient ftpClient = new FTPClient();

    @Override
    public DataRecord getFullRecordById(DataImport dataImport, String identifier) throws NoRecordFoundException {
        LinkedHashMap<String, String> queryParameters = new LinkedHashMap<>(dataImport.getUrlParameters());
        SearchInterfaceType interfaceType = dataImport.getSearchInterfaceType();
        try {
            if (SearchInterfaceType.FTP.equals(interfaceType)) {
                return performFTPQueryToRecord(dataImport, identifier);
            } else {
                URI queryURL = createQueryURI(dataImport, queryParameters);
                return performQueryToRecord(dataImport, queryURL.toString(), identifier);
            }
        } catch (URISyntaxException e) {
            throw new ConfigException(e.getLocalizedMessage());
        }
    }

    @Override
    public List<DataRecord> getMultipleFullRecordsFromQuery(DataImport dataImport, String field, String value,
                                                            int rows) {
        HashMap<String, String> searchFields = new HashMap<>();
        searchFields.put(field, value);
        if (dataImport.getSearchFields().containsKey(field)) {
            // Query parameters for HTTP request
            LinkedHashMap<String, String> queryParameters = new LinkedHashMap<>(dataImport.getUrlParameters());
            // Search fields and terms of query
            LinkedHashMap<String, String> searchFieldMap = getSearchFieldMap(dataImport, searchFields);

            try {
                URI queryURL = createQueryURI(dataImport, queryParameters);
                String queryString = queryURL + AND;
                SearchInterfaceType interfaceType = dataImport.getSearchInterfaceType();
                if (Objects.nonNull(interfaceType)) {
                    if (Objects.nonNull(interfaceType.getStartRecordString())
                            && Objects.nonNull(interfaceType.getDefaultStartValue())) {
                        queryString = queryString + interfaceType.getStartRecordString() + EQUALS_OPERAND
                                + interfaceType.getDefaultStartValue() + AND;
                    }
                    if (Objects.nonNull(interfaceType.getMaxRecordsString())) {
                        queryString = queryString + interfaceType.getMaxRecordsString() + EQUALS_OPERAND + rows + AND;
                    }
                    if (Objects.nonNull(interfaceType.getQueryString())) {
                        queryString = queryString + interfaceType.getQueryString() + EQUALS_OPERAND;
                    }
                }
                queryString = queryString + createSearchFieldString(interfaceType, searchFieldMap);
                return performQueryToMultipleRecords(dataImport, queryString);
            } catch (URISyntaxException | IOException | ParserConfigurationException | SAXException
                    | TransformerException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public SearchResult search(DataImport dataImport, String field, String term, int rows) {
        return search(dataImport, field, term, 1, rows);
    }

    @Override
    public SearchResult search(DataImport dataImport, String key, String value, int start, int numberOfRecords) {
        switch (dataImport.getScheme()) {
            case FTP_PROTOCOL:
                return performFtpRequest(dataImport, value, start, numberOfRecords);
            case HTTP_PROTOCOL:
            case HTTPS_PROTOCOL:
                if (dataImport.getSearchFields().containsKey(key)
                        || SearchInterfaceType.OAI.equals(dataImport.getSearchInterfaceType())) {
                    return performHTTPRequest(dataImport, Collections.singletonMap(key, value),
                            start, numberOfRecords);
                }
                return null;
            default:
                throw new CatalogException("Error: unknown protocol '" + dataImport.getScheme()
                        + "' configured in import configuration '" + dataImport.getTitle()
                        + "' (supported protocols are http, https and ftp)!");
        }
    }

    @Override
    public Collection<SingleHit> getMultipleEntriesById(Collection<String> ids, String catalogId) {
        return Collections.emptyList();
    }

    private void reinitializeHttpClient(String username, String password) throws IOException {
        httpClient.close();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            provider.setCredentials(AuthScope.ANY, credentials);
            httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        } else {
            httpClient = HttpClientBuilder.create().build();
        }
    }

    private SearchResult performQuery(DataImport dataImport, String queryURL) {
        try {
            this.reinitializeHttpClient(dataImport.getUsername(), dataImport.getPassword());
            logger.debug("Requesting: {}", queryURL);
            HttpResponse response = httpClient.execute(new HttpGet(queryURL));
            int responseStatusCode = response.getStatusLine().getStatusCode();
            if (Objects.equals(responseStatusCode, SC_OK)) {
                return XmlResponseHandler.getSearchResult(response, dataImport);
            } else {
                throw new CatalogException(response.getStatusLine().getReasonPhrase() + " (Http status code "
                        + responseStatusCode + ")");
            }
        } catch (UnknownHostException e) {
            throw new CatalogException("Unknown host: " + e.getMessage());
        } catch (ClientProtocolException e) {
            throw new CatalogException("ClientProtocolException: " + e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getLocalizedMessage());
        }
    }

    private DataRecord performFTPQueryToRecord(DataImport dataImport, String filename) {
        if (StringUtils.isBlank(dataImport.getHost()) || StringUtils.isBlank(dataImport.getPath())) {
            throw new CatalogException("Missing host or path configuration for FTP import in OPAC configuration "
                    + "for import configuration '" + dataImport.getTitle() + "'");
        }
        if (StringUtils.isBlank(dataImport.getUsername()) || StringUtils.isBlank(dataImport.getPassword())) {
            throw new CatalogException("Incomplete credentials configured for FTP import in OPAC configuration "
                    + "for import configuration '" + dataImport.getTitle() + "'");
        }
        try {
            ftpLogin(dataImport);
            String filepath = dataImport.getPath() + "/" + filename;
            InputStream inputStream = ftpClient.retrieveFileStream(filepath);
            if (Objects.isNull(inputStream)) {
                throw new CatalogException("Unable to load file '" + filepath + "' from configured FTP source!");
            }
            String stringContent = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputStream.close();
            DataRecord dataRecord = createRecordFromXMLElement(dataImport, stringContent);
            if (!ftpClient.completePendingCommand()) {
                throw new CatalogException("Unable to import '" + filename + "'!");
            }
            ftpLogout();
            return dataRecord;
        } catch (IOException e) {
            throw new CatalogException(e.getLocalizedMessage());
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    private DataRecord performQueryToRecord(DataImport dataImport, String queryURL, String identifier)
            throws NoRecordFoundException {
        String fullUrl = queryURL;
        if (!dataImport.getUrlParameters().isEmpty()) {
            fullUrl = fullUrl + AND;
        }
        SearchInterfaceType interfaceType = dataImport.getSearchInterfaceType();
        if (Objects.nonNull(interfaceType)) {
            if (Objects.nonNull(interfaceType.getMaxRecordsString())) {
                fullUrl = fullUrl + interfaceType.getMaxRecordsString() + EQUALS_OPERAND + "1&";
            }
            if (Objects.nonNull(interfaceType.getQueryString())) {
                fullUrl = fullUrl + interfaceType.getQueryString() + EQUALS_OPERAND;
            }
        }
        String idPrefix = dataImport.getIdPrefix();
        String prefix = Objects.nonNull(idPrefix) && !identifier.startsWith(idPrefix) ? idPrefix : "";
        String idParameter = SearchInterfaceType.OAI.equals(dataImport.getSearchInterfaceType()) ? OAI_IDENTIFIER
                : dataImport.getIdParameter();
        String queryParameter = idParameter + EQUALS_OPERAND + prefix + identifier;
        if (SearchInterfaceType.SRU.equals(interfaceType)) {
            fullUrl += URLEncoder.encode(queryParameter, encoding);
        } else {
            fullUrl += queryParameter;
        }
        try {
            this.reinitializeHttpClient(dataImport.getUsername(), dataImport.getPassword());
            logger.debug("Requesting: {}", fullUrl);
            HttpResponse response = httpClient.execute(new HttpGet(fullUrl));
            if (Objects.equals(response.getStatusLine().getStatusCode(), SC_OK)) {
                HttpEntity httpEntity = response.getEntity();
                if (Objects.isNull(httpEntity)) {
                    throw new NoRecordFoundException("No record with ID \"" + identifier + "\" found!");
                }
                try (InputStream inputStream = httpEntity.getContent()) {
                    String content = IOUtils.toString(inputStream, Charset.defaultCharset());
                    if (Objects.nonNull(interfaceType.getNumberOfRecordsString())
                            && XmlResponseHandler.extractNumberOfRecords(content, interfaceType) < 1) {
                        throw new NoRecordFoundException("No record with ID \"" + identifier + "\" found!");
                    }
                    return createRecordFromXMLElement(dataImport, content);
                }
            }
            throw new ConfigException("Search Query Request Failed");
        } catch (IOException e) {
            throw new ConfigException(e.getLocalizedMessage());
        }
    }


    private List<DataRecord> performQueryToMultipleRecords(DataImport dataImport, String queryURL)
            throws IOException, ParserConfigurationException, SAXException, TransformerException {
        List<DataRecord> records = new LinkedList<>();
        HttpGet request = new HttpGet(queryURL);
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectionRequestTimeout(3000);
        requestConfigBuilder.setConnectTimeout(3000);
        request.setConfig(requestConfigBuilder.build());
        try {
            logger.debug("Requesting: {}", queryURL);
            HttpResponse response = httpClient.execute(request);
            int responseStatusCode = response.getStatusLine().getStatusCode();
            if (Objects.equals(responseStatusCode, SC_OK)) {
                String xmlContent = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
                Document document = stringToDocument(xmlContent);
                NodeList recordNodes = document.getElementsByTagName(MODS_RECORD_TAG);
                for (int i = 0; i < recordNodes.getLength(); i++) {
                    records.add(createRecordFromXMLElement(dataImport, nodeToString(recordNodes.item(i))));
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

    private SearchResult performHTTPRequest(DataImport dataImport, Map<String, String> searchParameters,
                                            int start, int numberOfRecords) {
        // Query parameters for search request
        LinkedHashMap<String, String> queryParameters = new LinkedHashMap<>(dataImport.getUrlParameters());
        // Search fields and terms of query
        LinkedHashMap<String, String> searchFieldMap = getSearchFieldMap(dataImport, searchParameters);
        SearchInterfaceType interfaceType = dataImport.getSearchInterfaceType();
        try {
            URI queryURL = createQueryURI(dataImport, queryParameters);
            String queryString = queryURL + AND;
            if (Objects.nonNull(interfaceType)) {
                if (start > 0 && Objects.nonNull(interfaceType.getStartRecordString())) {
                    queryString += interfaceType.getStartRecordString() + EQUALS_OPERAND + start + AND;
                }
                if (Objects.nonNull(interfaceType.getMaxRecordsString())) {
                    queryString = queryString + interfaceType.getMaxRecordsString() + EQUALS_OPERAND + numberOfRecords
                            + AND;
                }
                if (Objects.nonNull(interfaceType.getQueryString())) {
                    queryString = queryString + interfaceType.getQueryString() + EQUALS_OPERAND;
                }
            }
            return performQuery(dataImport, queryString + createSearchFieldString(interfaceType, searchFieldMap));
        } catch (URISyntaxException e) {
            throw new CatalogException(e.getLocalizedMessage());
        }
    }

    private SearchResult performFtpRequest(DataImport dataImport, String filenamePart, int startIndex, int rows) {
        if (StringUtils.isBlank(dataImport.getUsername()) || StringUtils.isBlank(dataImport.getPassword())) {
            throw new CatalogException("Incomplete credentials configured for FTP import in import configuration '"
                    + dataImport.getTitle() + "'");
        }
        SearchResult searchResult = new SearchResult();
        FTPFileFilter searchFilter = file -> file.isFile() && file.getName().contains(filenamePart);
        try {
            ftpLogin(dataImport);
            FTPFile[] files = ftpClient.listFiles(dataImport.getPath(), searchFilter);
            searchResult.setNumberOfHits(files.length);
            LinkedList<SingleHit> hits = new LinkedList<>();
            for (int i = startIndex; i < Math.min(startIndex + rows, files.length); i++) {
                hits.add(new SingleHit(files[i].getName(), files[i].getName()));
            }
            searchResult.setHits(hits);
            ftpLogout();
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return searchResult;
    }

    private DataRecord createRecordFromXMLElement(DataImport dataImport, String xmlContent) {
        DataRecord record = new DataRecord();
        record.setMetadataFormat(MetadataFormat.getMetadataFormat(dataImport.getMetadataFormat().name()));
        record.setFileFormat(FileFormat.getFileFormat(dataImport.getReturnFormat().name()));
        record.setOriginalData(xmlContent);
        return record;
    }

    private URI createQueryURI(DataImport dataImport, LinkedHashMap<String, String> searchFields)
            throws URISyntaxException {
        if (dataImport.getPort() > 0) {
            return new URI(dataImport.getScheme(), null, dataImport.getHost(), dataImport.getPort(),
                    dataImport.getPath(), createQueryParameterString(searchFields), null);
        } else {
            return new URI(dataImport.getScheme(), dataImport.getHost(), dataImport.getPath(),
                    createQueryParameterString(searchFields), null);
        }
    }

    private String createQueryParameterString(LinkedHashMap<String, String> searchFields) {
        List<BasicNameValuePair> nameValuePairList = searchFields.entrySet().stream()
                .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return URLEncodedUtils.format(nameValuePairList, StandardCharsets.UTF_8);
    }

    private String createSearchFieldString(SearchInterfaceType interfaceType, LinkedHashMap<String, String> searchFields) {
        List<String> searchOperands = searchFields.entrySet().stream()
                .map(entry -> entry.getKey() + EQUALS_OPERAND + entry.getValue())
                .collect(Collectors.toList());
        String searchString = String.join(" AND ", searchOperands);
        if (SearchInterfaceType.SRU.equals(interfaceType)) {
            return URLEncoder.encode(searchString, encoding);
        } else {
            return searchString;
        }
    }

    private Document stringToDocument(String xmlContent) throws ParserConfigurationException, IOException,
            SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(new InputSource(new StringReader(xmlContent)));
    }

    private String nodeToString(Node node) throws TransformerException {
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }

    private LinkedHashMap<String, String> getSearchFieldMap(DataImport dataImport, Map<String, String> searchFields) {
        LinkedHashMap<String, String> searchFieldMap = new LinkedHashMap<>();
        String idPrefix = dataImport.getIdPrefix();
        if (SearchInterfaceType.OAI.equals(dataImport.getSearchInterfaceType()) && searchFields.size() == 1) {
            String value = new LinkedList<>(searchFields.values()).getFirst();
            if (StringUtils.isBlank(idPrefix) || value.startsWith(idPrefix)) {
                searchFieldMap.put(OAI_IDENTIFIER, value);
            } else {
                searchFieldMap.put(OAI_IDENTIFIER, idPrefix + value);
            }
            return searchFieldMap;
        }
        String idParameter = dataImport.getIdParameter();
        for (Map.Entry<String, String> entry : searchFields.entrySet()) {
            String searchField = dataImport.getSearchFields().get(entry.getKey());
            if (StringUtils.isNotBlank(idPrefix) && StringUtils.isNotBlank(idParameter)
                    && idParameter.equals(searchField) && !entry.getValue().startsWith(idPrefix)) {
                searchFieldMap.put(searchField, idPrefix + entry.getValue());
            } else {
                searchFieldMap.put(searchField, entry.getValue());
            }
        }
        return searchFieldMap;
    }

    private void ftpLogin(DataImport dataImport) throws IOException {
        if (dataImport.getPort() > 0) {
            ftpClient.connect(dataImport.getHost(), dataImport.getPort());
        } else {
            ftpClient.connect(dataImport.getHost());
        }
        boolean loginSuccessful = ftpClient.login(dataImport.getUsername(), dataImport.getPassword());
        if (!loginSuccessful) {
            String replyString = ftpClient.getReplyString();
            int replyCode = ftpClient.getReplyCode();
            ftpClient.logout();
            ftpClient.disconnect();
            throw new CatalogException("FTP server login failed: " + replyString + " (" + replyCode + ")");
        }
    }

    private void ftpLogout() throws IOException {
        ftpClient.logout();
        ftpClient.disconnect();
    }
}
