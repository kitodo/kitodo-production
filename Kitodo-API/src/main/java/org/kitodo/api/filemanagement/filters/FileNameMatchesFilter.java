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

package org.kitodo.api.filemanagement.filters;

import java.io.File;
import java.io.FilenameFilter;

public class FileNameMatchesFilter implements FilenameFilter {

    private String name;

    /**
     * Filter file list according to the given name.
     *
     * @param name
     *            String
     * @throws IllegalArgumentException
     *             it is thrown in case parameter is null or empty String
     */
    public FileNameMatchesFilter(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("No filter or empty name is given.");
        }
        this.name = name;
    }

    @Override
    public boolean accept(File directory, String filename) {
        return filename.matches(name);
    }
}
