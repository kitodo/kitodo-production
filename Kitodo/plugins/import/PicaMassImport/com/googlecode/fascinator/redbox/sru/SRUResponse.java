/* 
 * The Fascinator - ReDBox/Mint SRU Client - Response Object
 * Copyright (C) 2012 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.googlecode.fascinator.redbox.sru;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A trivial wrapper for response Objects, allowing access to common
 * information without having to parse continually.</p>
 * 
 * @author Greg Pendlebury
 * 
 */
public class SRUResponse {
    /** Logging **/
    private static Logger log = LoggerFactory.getLogger(SRUResponse.class);

    /** Record counts **/
    private int totalRecords = 0;
    private int recordsReturned = 0;

    /** Results **/
    private List<Node> resultsList;

    /**
     * <p>Default Constructor. Extract some basic information.</p>
     * 
     * @param searchResponse A parsed DOM4J Document
     * @throws SRUException If any of the XML structure does not look like expected
     */
   
	@SuppressWarnings("unchecked")
	public SRUResponse(Document searchResponse) throws SRUException {
        // Results total
        Node number = searchResponse.selectSingleNode("//srw:numberOfRecords");
        if (number == null) {
            throw new SRUException("Unable to get result numbers from response XML.");
        }
        totalRecords = Integer.parseInt(number.getText());
        log.debug("SRU Search found {} results(s)", totalRecords);

        // Results List
        if (totalRecords == 0) {
            resultsList = new ArrayList<Node>();
        } else {
        	
            resultsList = searchResponse.selectNodes("//srw:recordData");
        }
        recordsReturned = resultsList.size();
    }

    /**
     * <p>Get the number of rows returned by this search. Not the total results
     * that match the search.</p>
     * 
     * @return int The number of rows returned from this search.
     */
    public int getRows() {
        return recordsReturned;
    }

    /**
     * <p>Get the number of records that match this search. A subset of this
     * will be returned if the total is higher then the number of rows requested
     * (or defaulted).</p>
     * 
     * @return int The number of records that match this search.
     */
    public int getTotalResults() {
        return totalRecords;
    }

    /**
     * <p>Return the List of DOM4J Nodes extracted from the SRU XML wrapping
     * it.</p>
     * 
     * @return int The number of records that match this search.
     */
    public List<Node> getResults() {
        return resultsList;
    }
}
