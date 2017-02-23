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

package org.goobi.production.properties;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.sub.goobi.beans.Prozesseigenschaft;

public class ProcessProperty implements IProperty, Serializable {

    private static final long serialVersionUID = 6413183995622426678L;
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
    private AccessCondition currentStepAccessCondition;
    private boolean currentStepDuplicationAllowed = false;

    public ProcessProperty() {
        this.container = 0;
        this.value = "";
        this.possibleValues = new ArrayList<String>();
        this.projects = new ArrayList<String>();
        this.showStepConditions = new ArrayList<ShowStepCondition>();
        this.prozesseigenschaft = new Prozesseigenschaft();
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


    @Override
    public void setDateValue(Date inDate) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        value= format.format(inDate);
    }


    @Override
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
        p.setContainer(containerNumber);
        p.setName(this.name);
        p.setValidation(this.validation);
        p.setType(this.type);
        p.setValue(this.value);
        p.setShowProcessGroupAccessCondition(this.showProcessGroupAccessCondition);
        p.setDuplicationAllowed(this.getDuplicationAllowed());
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
            this.prozesseigenschaft.setWert(this.value);
            this.prozesseigenschaft.setTitel(this.name);
            this.prozesseigenschaft.setContainer(this.container);
        // }
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
        if (this.value != null && this.value.equalsIgnoreCase("true")) {
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

            return Integer.compare(o1.getContainer(), o2.getContainer());
        }

    }

    public boolean getIsNew() {
        if (this.name == null || this.name.length() == 0) {
            return true;
        }
        return false;
    }

    public AccessCondition getCurrentStepAccessCondition() {
        return currentStepAccessCondition;
    }

    public void setCurrentStepAccessCondition(AccessCondition currentStepAccessCondition) {
        this.currentStepAccessCondition = currentStepAccessCondition;
    }

    public void setDuplicationAllowed(boolean duplicate) {
        currentStepDuplicationAllowed = duplicate;
    }

    public boolean getDuplicationAllowed() {
        return currentStepDuplicationAllowed;
    }
}
