package de.unigoettingen.sub.search.opac;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.goobi.webapi.beans.Label;
import org.goobi.webapi.beans.Label.KeyAttribute;

import com.sharkysoft.util.NotImplementedException;

public class ConfigOpacDoctype {
	private String title = "";
	private String rulesetType = "";
	private String tifHeaderType = "";
	private boolean periodical = false;
	private boolean multiVolume = false;
	private HashMap<String, String> labels;
	private ArrayList<String> mappings;
	private boolean newspaper;

	public ConfigOpacDoctype() {
		throw new NotImplementedException("Jersey API requires no-arg constructor which is never used");
	}
	
	ConfigOpacDoctype(String inTitle, String inRulesetType, String inTifHeaderType, boolean inPeriodical,
			boolean inMultiVolume, boolean inContainedWork, boolean newspaper, HashMap<String, String> inLabels,
			ArrayList<String> inMappings) {
		this.title = inTitle;
		this.rulesetType = inRulesetType;
		this.tifHeaderType = inTifHeaderType;
		this.periodical = inPeriodical;
		this.multiVolume = inMultiVolume;
		this.newspaper = newspaper;
		this.labels = inLabels;
		this.mappings = inMappings;
	}

	@XmlAttribute(name="key")
	public String getTitle() {
		return this.title;
	}

	public String getRulesetType() {
		return this.rulesetType;
	}

	@XmlElement(name="tiffHeaderTag")
	public String getTifHeaderType() {
		return this.tifHeaderType;
	}

	public boolean isPeriodical() {
		return this.periodical;
	}

	public boolean isMultiVolume() {
		return this.multiVolume;
	}

	public boolean isNewspaper() {
		return this.newspaper;
	}

	@XmlElement(name = "label")
	public List<Label> getLabelsForJerseyApi() {
		return Label.toListOfLabels(labels, KeyAttribute.LANGUAGE);
	}

	@XmlElement(name = "receivingValue")
	public ArrayList<String> getMappings() {
		return this.mappings;
	}

	public String getLocalizedLabel() {
		String currentLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
		if (currentLocale != null && !currentLocale.equals("")) {
			String answer = this.labels.get(currentLocale);
			if (answer != null && !answer.equals("")) {
				return answer;
			}
		}
		return this.labels.get(this.labels.keySet().iterator().next());
	}

}
