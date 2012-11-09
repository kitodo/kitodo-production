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

package de.sub.goobi.config;

//TODO: Move this into the GetOPAC Package
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.goobi.webapi.beans.Label;
import org.goobi.webapi.beans.Label.KeyAttribute;

public class ConfigOpacDoctype {
	private String title = "";
	private String rulesetType = "";
	private String tifHeaderType = "";
	private boolean periodical = false;
	private boolean multiVolume = false;
	private boolean containedWork = false;
	private HashMap<String, String> labels;
	private ArrayList<String> mappings;

	public ConfigOpacDoctype() { // stupid Jersey API requires no-arg default constructor which is never used
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public ConfigOpacDoctype(String inTitle, String inRulesetType, String inTifHeaderType, boolean inPeriodical, boolean inMultiVolume,
			boolean inContainedWork, HashMap<String, String> inLabels, ArrayList<String> inMappings) {
		title = inTitle;
		rulesetType = inRulesetType;
		tifHeaderType = inTifHeaderType;
		periodical = inPeriodical;
		multiVolume = inMultiVolume;
		containedWork = inContainedWork;
		labels = inLabels;
		mappings = inMappings;
	}

	@XmlAttribute(name="key")
	public String getTitle() {
		return title;
	}

	public String getRulesetType() {
		return rulesetType;
	}

	@XmlElement(name="tiffHeaderTag")
	public String getTifHeaderType() {
		return tifHeaderType;
	}

	public boolean isPeriodical() {
		return periodical;
	}

	public boolean isMultiVolume() {
		return multiVolume;
	}

	public boolean isContainedWork() {
		return containedWork;
	}

	public HashMap<String, String> getLabels() {
		return labels;
	}

	@XmlElement(name="label")
	public List<Label> getLabelsForJerseyApi() {
		return Label.toListOfLabels(labels, KeyAttribute.LANGUAGE);
	}
	
	@XmlElement(name="receivingValue")
	public ArrayList<String> getMappings() {
		return mappings;
	}

	public void setMappings(ArrayList<String> mappings) {
		this.mappings = mappings;
	}

	public String getLocalizedLabel() {
		String currentLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
		if (currentLocale != null && !currentLocale.equals("")) {
			String answer = labels.get(currentLocale);
			if (answer != null && !answer.equals("")) {
				return answer;
			}
		}
		return labels.get(labels.keySet().iterator().next());
	}

}
