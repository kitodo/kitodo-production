package org.goobi.production.properties;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - http://gdz.sub.uni-goettingen.de - http://www.intranda.com
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.sub.goobi.Beans.Prozesseigenschaft;
import de.sub.goobi.Beans.Schritt;

public class ProcessProperty implements IProperty {

	private String name;
	private Integer container;
	private String validation;
	private Type type;
	private String value;
	private List<String> possibleValues;
	private List<String> projects;
	private List<ShowStepCondition> showStepConditions;
	private AccessCondition showProcessGroupAccessCondition;
	private Prozesseigenschaft prozesseigenschaft;

	public ProcessProperty() {
		this.possibleValues = new ArrayList<String>();
		this.projects = new ArrayList<String>();
		this.showStepConditions = new ArrayList<ShowStepCondition>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#getContainer()
	 */
	@Override
	public int getContainer() {
		return this.container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#setContainer(int)
	 */
	@Override
	public void setContainer(int container) {
		this.container = container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#getValidation()
	 */
	@Override
	public String getValidation() {
		return this.validation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#setValidation(java.lang.String)
	 */
	@Override
	public void setValidation(String validation) {
		this.validation = validation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#getType()
	 */
	@Override
	public Type getType() {
		return this.type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#setType(org.goobi.production.properties.Type)
	 */
	@Override
	public void setType(Type type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#getValue()
	 */
	@Override
	public String getValue() {
		return this.value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#getPossibleValues()
	 */
	@Override
	public List<String> getPossibleValues() {
		return this.possibleValues;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#setPossibleValues(java.util.ArrayList)
	 */
	@Override
	public void setPossibleValues(List<String> possibleValues) {
		this.possibleValues = possibleValues;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#getProjects()
	 */
	@Override
	public List<String> getProjects() {
		return this.projects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#setProjects(java.util.ArrayList)
	 */
	@Override
	public void setProjects(List<String> projects) {
		this.projects = projects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#getShowStepConditions()
	 */
	@Override
	public List<ShowStepCondition> getShowStepConditions() {
		return this.showStepConditions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#setShowStepConditions(java.util.ArrayList)
	 */
	@Override
	public void setShowStepConditions(List<ShowStepCondition> showStepConditions) {
		this.showStepConditions = showStepConditions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#getShowProcessGroupAccessCondition()
	 */
	@Override
	public AccessCondition getShowProcessGroupAccessCondition() {
		return this.showProcessGroupAccessCondition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#setShowProcessGroupAccessCondition(org.goobi.production.properties.AccessCondition)
	 */
	@Override
	public void setShowProcessGroupAccessCondition(AccessCondition showProcessGroupAccessCondition) {
		this.showProcessGroupAccessCondition = showProcessGroupAccessCondition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#isValid()
	 */
	@Override
	public boolean isValid() {
		if (this.validation != null && this.validation.length() > 0) {
			Pattern pattern = Pattern.compile(this.validation);
			Matcher matcher = pattern.matcher(this.value);
			return matcher.matches();
		} else {
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#save(de.sub.goobi.Beans.Schritt)
	 */

	public void save(Schritt step) {
		if (this.prozesseigenschaft != null) {

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#getProzesseigenschaft()
	 */

	public Prozesseigenschaft getProzesseigenschaft() {
		return this.prozesseigenschaft;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#setProzesseigenschaft(de.sub.goobi.Beans.Prozesseigenschaft)
	 */

	public void setProzesseigenschaft(Prozesseigenschaft prozesseigenschaft) {
		this.prozesseigenschaft = prozesseigenschaft;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#getClone()
	 */
	@Override
	public ProcessProperty getClone(int containerNumber) {
		ProcessProperty p = new ProcessProperty();
		// if (this.container.intValue()==0){
		p.setContainer(containerNumber);
		// }else{
		// p.setContainer(this.container.intValue()+1);
		// }
		p.setName(this.name);
		p.setValidation(this.validation);
		p.setType(this.type);
		p.setValue(this.value);
		p.setShowProcessGroupAccessCondition(this.showProcessGroupAccessCondition);
		p.setShowStepConditions(new ArrayList<ShowStepCondition>(getShowStepConditions()));
		p.setPossibleValues(new ArrayList<String>(getPossibleValues()));
		p.setProjects(new ArrayList<String>(getProjects()));
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#transfer()
	 */
	@Override
	public void transfer() {
		if (this.value != null && this.value.length() > 0) {
			this.prozesseigenschaft.setWert(this.value);
			this.prozesseigenschaft.setTitel(this.name);
			this.prozesseigenschaft.setContainer(this.container);
		}
	}

	public List<String> getValueList() {
		String[] values = this.value.split("; ");
		List<String> answer = new ArrayList<String>();
		for (String val : values) {
			answer.add(val);
		}
		return answer;
	}

	public void setValueList(List<String> valueList) {
		this.value = "";
		for (String val : valueList) {
			this.value = this.value + val + "; ";
		}
	}

	public boolean getBooleanValue() {
		if (this.value.equalsIgnoreCase("true")) {
			return true;
		} else {
			return false;
		}
	}

	public void setBooleanValue(boolean val) {
		if (val) {
			this.value = "true";
		} else {
			this.value = "false";
		}
	}

	public static class CompareProperties implements Comparator<ProcessProperty>, Serializable {

		private static final long serialVersionUID = 8047374873015931547L;

		@Override
		public int compare(ProcessProperty o1, ProcessProperty o2) {

			return new Integer(o1.getContainer()).compareTo(new Integer(o2.getContainer()));
		}

	}
}
