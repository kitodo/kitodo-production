package gov.loc.mets;

/*******************************************************************************
 * gov.loc.mets / HelperTest.java
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

import gov.loc.mets.MetsType.MetsHdr;
import gov.loc.mets.MetsType.MetsHdr.Agent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.xmlbeans.XmlOptions;

/***************************************************************************
 * @author Markus Enders
 *
 *         TODO: This class should probably be covered by a unit test.
 **************************************************************************/
public class HelperTest {

	/**************************************************************************
	 * @param args
	 **************************************************************************/
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HelperTest ht = new HelperTest();
		ht.createMets();
	}

	/***************************************************************************
	 * <p>
	 * Constructor.
	 * </p>
	 **************************************************************************/
	public HelperTest() {
		//
	}

	/**************************************************************************
	 *
	 **************************************************************************/
	public void createMets() {
		// HashMap to store prefixes for different namespaces.
		HashMap<String, String> suggestedPrefixes = new HashMap<String, String>();
		XmlOptions opts = new XmlOptions();

		// Create a METS wrapper
		//
		MetsDocument metsDocument = MetsDocument.Factory.newInstance();

		// This is the <mets> element.
		MetsType myMets = metsDocument.addNewMets();

		// create header
		//
		// <MetsHdr>
		// <agent>
		// <name></name>
		// <note></note>
		// </agent>
		// </MetsHdr>

		MetsHdr header = myMets.addNewMetsHdr();
		Agent metsAgent = header.addNewAgent();
		metsAgent.setROLE(MetsType.MetsHdr.Agent.ROLE.CREATOR);
		metsAgent.setTYPE(MetsType.MetsHdr.Agent.TYPE.INDIVIDUAL);
		metsAgent.setName("Markus Enders");
		metsAgent.addNote("He is the the METS-api to create this file");

		// Create first div; therefore we have to add a new struct map; while
		// adding this structMap a StructMapType object is created.
		//
		// Create a new <StructMap> element.
		StructMapType sm = myMets.addNewStructMap();

		// Create first div.
		DivType monograph_div = sm.addNewDiv();
		// This div is a monograph.
		monograph_div.setTYPE("Monograph");
		// This ID must be XML compliant.
		monograph_div.setID("MAINDIV01");
		monograph_div.setLABEL("Monograph");

		// Create metadata for the first div.
		//
		// Ceate the xml metadata first.
		StringBuffer xmlmetadata = new StringBuffer();
		// Set the namespace for the MODS part.
		suggestedPrefixes.put("http://purl.org/DC#", "dc");
		xmlmetadata
				.append("<dc:identifier xmlns:dc=\"http://purl.org/DC#\"></dc:identifier>");
		xmlmetadata.append("<dc:title></dc:title>");

		// create binary metadata (e.g. orig. MARC record)
		StringBuffer binmetadata = new StringBuffer();
		binmetadata.append("Binary Data");

		// Creating metadata is difficult, since it can be therefore we use the
		// helper class from level 2 api.
		Helper h = new Helper(myMets);

		h.addDmdSecType(monograph_div, xmlmetadata.toString());
		h.addDmdSecType(monograph_div, binmetadata.toString().getBytes());

		// Save METS as file, but create options.
		opts.setSaveSuggestedPrefixes(suggestedPrefixes);

		// Save file using the given options.
		File outputFile = new File("C:/myMets.xml");

		try {
			metsDocument.save(outputFile, opts);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**************************************************************************
	 *
	 **************************************************************************/
	public void createMetsHeader() {
		//
	}

}
