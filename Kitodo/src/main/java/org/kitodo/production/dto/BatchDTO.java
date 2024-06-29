/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.production.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Batch DTO object.
 */
public class BatchDTO extends BaseDTO {

    private String title;
    private List<ProcessDTO> processes = new ArrayList<>();

    /**
     * Get title.
     *
     * @return title as String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     *
     * @param title
     *            as String
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get processes.
     *
     * @return List of processes as ProcessDTO
     */
    public List<ProcessDTO> getProcesses() {
        return processes;
    }

    /**
     * Set processes.
     *
     * @param processes
     *            as List of processes as ProcessDTO
     */
    public void setProcesses(List<ProcessDTO> processes) {
        this.processes = processes;
    }
}
