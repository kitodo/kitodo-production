package org.goobi.production.properties;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2012, intranda GmbH, Göttingen
 * 
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
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportProperty implements IProperty{

	private String name = "";
	private Integer container = 0;
	private String validation = "";
	private Type type = Type.TEXT;
	private String value = "";
	private List<String> possibleValues = new ArrayList<String>();
	private List<String> projects = new ArrayList<String>();
	
	public ImportProperty() {
		this.possibleValues = new ArrayList<String>();
		this.projects = new ArrayList<String>();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getContainer() {
		return this.container;
	}

	@Override
	public void setContainer(int container) {
		this.container = container;
	}

	@Override
	public String getValidation() {
		return this.validation;
	}

	@Override
	public void setValidation(String validation) {
		this.validation = validation;
	}

	@Override
	public Type getType() {
		return this.type;
	}

	@Override
	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public List<String> getPossibleValues() {
		return this.possibleValues;
	}

	@Override
	public void setPossibleValues(List<String> possibleValues) {
		this.possibleValues = possibleValues;
	}

	@Override
	public List<String> getProjects() {
		return this.projects;
	}
	
	@Override
	public void setProjects(List<String> projects) {
		this.projects = projects;
	}
	
	@Override
	public ArrayList<ShowStepCondition> getShowStepConditions() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setShowStepConditions(List<ShowStepCondition> showStepConditions) {
	}
	
	@Override
	public AccessCondition getShowProcessGroupAccessCondition() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setShowProcessGroupAccessCondition(AccessCondition showProcessGroupAccessCondition) {
	}

	@Override
	public boolean isValid(){
		Pattern pattern = Pattern.compile(this.validation);
		Matcher matcher = pattern.matcher(this.value);
		return matcher.matches();
	}
		
	
	
	
	@Override
	public ImportProperty getClone(int containerNumber){
		ImportProperty p = new ImportProperty();
		return p;
	}
	
	@Override
	public void transfer(){
		
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

	public void setDateValue(Date inDate) {
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		value= format.format(inDate);
	}

	
	public Date getDateValue() {
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(format.parse(value));
			cal.set(Calendar.HOUR, 12);
			return cal.getTime();
		} catch (ParseException e) {
			return new Date();
		} catch (NullPointerException e) {
			return new Date();
		}
	}
}
