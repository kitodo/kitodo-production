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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.sub.goobi.Beans.Prozesseigenschaft;
import de.sub.goobi.Beans.Schritt;

public class ProcessProperty {

	private String name;
	private Integer container;
	private String validation;
	private Type type;
	private String value;
	private ArrayList<String> possibleValues;
	private ArrayList<String> projects;
	private ArrayList<ShowStepCondition> showStepConditions;
	private AccessCondition showProcessGroupAccessCondition;
	private Prozesseigenschaft prozesseigenschaft;
	
	public ProcessProperty() {
		possibleValues = new ArrayList<String>();
		projects = new ArrayList<String>();
		showStepConditions = new ArrayList<ShowStepCondition>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getContainer() {
		return container;
	}

	public void setContainer(int container) {
		this.container = container;
	}

	public String getValidation() {
		return validation;
	}

	public void setValidation(String validation) {
		this.validation = validation;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ArrayList<String> getPossibleValues() {
		return possibleValues;
	}

	public void setPossibleValues(ArrayList<String> possibleValues) {
		this.possibleValues = possibleValues;
	}

	public ArrayList<String> getProjects() {
		return projects;
	}
	
	public void setProjects(ArrayList<String> projects) {
		this.projects = projects;
	}
	
	public ArrayList<ShowStepCondition> getShowStepConditions() {
		return showStepConditions;
	}
	
	public void setShowStepConditions(ArrayList<ShowStepCondition> showStepConditions) {
		this.showStepConditions = showStepConditions;
	}
	
	public AccessCondition getShowProcessGroupAccessCondition() {
		return showProcessGroupAccessCondition;
	}
	
	public void setShowProcessGroupAccessCondition(AccessCondition showProcessGroupAccessCondition) {
		this.showProcessGroupAccessCondition = showProcessGroupAccessCondition;
	}

	public boolean isValid(){
		Pattern pattern = Pattern.compile(validation);
		Matcher matcher = pattern.matcher(value);
		return matcher.matches();
	}
	
	public void save(Schritt step){
		if (prozesseigenschaft!=null){
			
		}
	}

	public Prozesseigenschaft getProzesseigenschaft() {
		return prozesseigenschaft;
	}
	
	public void setProzesseigenschaft(Prozesseigenschaft prozesseigenschaft) {
		this.prozesseigenschaft = prozesseigenschaft;
	}
	
	public ProcessProperty getClone(){
		ProcessProperty p = new ProcessProperty();
		if (container.intValue()==0){
			p.setContainer(0);
		}else{
			p.setContainer(container.intValue()+1);
		}
		p.setName(name);
		p.setValidation(validation);
		p.setType(type);
		p.setValue(value);
		p.setShowProcessGroupAccessCondition(showProcessGroupAccessCondition);
		p.setShowStepConditions(new ArrayList<ShowStepCondition>(getShowStepConditions()));
		p.setPossibleValues(new ArrayList<String>(getPossibleValues()));
		p.setProjects(new ArrayList<String>(getProjects()));
		return p;
	}
	
	public void transfer(){
		prozesseigenschaft.setWert(value);
		prozesseigenschaft.setTitel(name);
		prozesseigenschaft.setContainer(container);
	}
}
