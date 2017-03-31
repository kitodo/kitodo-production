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

package org.kitodo.production.flow.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.services.ServiceManager;

public class BatchDisplayItem implements Comparable<BatchDisplayItem> {

    private String stepTitle = "";
    private Integer stepOrder = null;
    private TaskStatus stepStatus = TaskStatus.DONE;
    private HashMap<String, String> scripts = new HashMap<String, String>();
    private boolean exportDMS = false;
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Constructor.
     *
     * @param s
     *            task
     */
    public BatchDisplayItem(Task s) {
        this.stepTitle = s.getTitle();
        this.stepOrder = s.getOrdering();
        this.stepStatus = s.getProcessingStatusEnum();
        this.scripts.putAll(serviceManager.getTaskService().getAllScripts(s));
        this.exportDMS = s.isTypeExportDMS();
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

    public TaskStatus getStepStatus() {
        return this.stepStatus;
    }

    public void setStepStatus(TaskStatus stepStatus) {
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

    /**
     * Get script names.
     *
     * @return list of script names
     */
    public List<String> getScriptnames() {
        List<String> answer = new ArrayList<>();
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
