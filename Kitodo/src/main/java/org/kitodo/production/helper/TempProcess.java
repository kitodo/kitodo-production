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

package org.kitodo.production.helper;

import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;

/**
 * This class is used during import.
 */
public class TempProcess {

    private Workpiece workpiece;

    private Process process;

    public TempProcess(Process process, Workpiece workpiece) {
        this.process = process;
        this.workpiece = workpiece;
    }

    /**
     * Get workpiece.
     *
     * @return workpiece
     */
    public Workpiece getWorkpiece() {
        return workpiece;
    }

    /**
     * Set workpiece.
     *
     * @param workpiece new Workpiece
     */
    public void setWorkpiece(Workpiece workpiece) {
        this.workpiece = workpiece;
    }

    /**
     * Get process.
     *
     * @return process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Set process.
     *
     * @param process new Process
     */
    public void setProcess(Process process) {
        this.process = process;
    }
}
