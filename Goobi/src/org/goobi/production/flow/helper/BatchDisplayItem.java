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

package org.goobi.production.flow.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.sub.goobi.beans.Schritt;
import de.sub.goobi.helper.enums.StepStatus;

public class BatchDisplayItem implements Comparable<BatchDisplayItem>{

    private String stepTitle = "";
    private Integer stepOrder = null;
    private StepStatus stepStatus = StepStatus.DONE;
    private HashMap<String, String> scripts = new HashMap<String, String>();
    private boolean exportDMS = false;

    public BatchDisplayItem(Schritt s) {
        this.stepTitle = s.getTitel();
        this.stepOrder = s.getReihenfolge();
        this.stepStatus = s.getBearbeitungsstatusEnum();
        this.scripts.putAll(s.getAllScripts());
        this.exportDMS = s.isTypExportDMS();
    }

    public String getStepTitle() {
        return this.stepTitle;
    }

    public void setStepTitle(String stepTitle) {
        this.stepTitle = stepTitle;
    }

    public Integer getStepOrder() {
        return this.stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public StepStatus getStepStatus() {
        return this.stepStatus;
    }

    public void setStepStatus(StepStatus stepStatus) {
        this.stepStatus = stepStatus;
    }

    @Override
    public int compareTo(BatchDisplayItem o) {

        return this.getStepOrder().compareTo(o.getStepOrder());
    }

    public HashMap<String, String> getScripts() {
        return this.scripts;
    }

    public void setScripts(HashMap<String, String> scripts) {
        this.scripts = scripts;
    }

    public int getScriptSize() {
        return this.scripts.size();
    }

    public List<String> getScriptnames() {
        List<String> answer = new ArrayList<String>();
        answer.addAll(this.scripts.keySet());
        return answer;
    }

    public boolean getExportDMS() {
        return this.exportDMS;
    }

    public void setExportDMS(boolean exportDMS) {
        this.exportDMS = exportDMS;
    }
}
