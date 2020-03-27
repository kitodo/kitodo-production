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

package org.kitodo.api.externaldatamanagement;

public enum SearchInterfaceType {
    SRU("sru", "http://www.loc.gov/zing/srw/", "record", "startRecord",
            "maximumRecords", "query", "numberOfRecords"),
    OAI("oai", "http://www.openarchives.org/OAI/2.0/", "record", null,
            null, null, null);

    private String typeString;
    private String namespace;
    private String recordString;
    private String startRecordString;
    private String maxRecordsString;
    private String queryString;
    private String numberOfRecordsString;

    SearchInterfaceType(String type, String namespace, String record, String startRecord, String maxRecords,
                        String query, String numberOfRecords) {
        this.typeString = type;
        this.namespace = namespace;
        this.recordString = record;
        this.startRecordString = startRecord;
        this.maxRecordsString = maxRecords;
        this.queryString = query;
        this.numberOfRecordsString = numberOfRecords;
    }

    public String getTypeString() {
        return this.typeString;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getRecordString() {
        return this.recordString;
    }

    public String getStartRecordString() {
        return this.startRecordString;
    }

    public String getMaxRecordsString() {
        return this.maxRecordsString;
    }

    public String getQueryString() {
        return this.queryString;
    }

    public String getNumberOfRecordsString() {
        return this.numberOfRecordsString;
    }
}
