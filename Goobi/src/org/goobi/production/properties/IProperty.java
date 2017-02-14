package org.goobi.production.properties;
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
import java.util.Date;
import java.util.List;

public interface IProperty {

	public abstract String getName();

	public abstract void setName(String name);

	public abstract int getContainer();

	public abstract void setContainer(int container);

	public abstract String getValidation();

	public abstract void setValidation(String validation);

	public abstract Type getType();

	public abstract void setType(Type type);

	public abstract String getValue();

	public abstract void setValue(String value);

	public abstract List<String> getPossibleValues();

	public abstract void setPossibleValues(List<String> possibleValues);

	public abstract List<String> getProjects();

	public abstract void setProjects(List<String> projects);

	public abstract List<ShowStepCondition> getShowStepConditions();

	public abstract void setShowStepConditions(List<ShowStepCondition> showStepConditions);

	public abstract AccessCondition getShowProcessGroupAccessCondition();

	public abstract void setShowProcessGroupAccessCondition(AccessCondition showProcessGroupAccessCondition);

	public abstract boolean isValid();

	public void setDateValue(Date inDate);
	
	public Date getDateValue();

	public abstract IProperty getClone(int containerNumber);

	public abstract void transfer();

}
