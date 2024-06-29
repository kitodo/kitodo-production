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

package org.kitodo.api.schemaconverter;

public class DataRecord {

    private Object originalData;

    private MetadataFormat metadataFormat;

    private FileFormat fileFormat;

    /**
     * Get originalData.
     *
     * @return value of originalData
     */
    public Object getOriginalData() {
        return originalData;
    }

    /**
     * Set originalData.
     *
     * @param originalData as java.lang.Object
     */
    public void setOriginalData(Object originalData) {
        this.originalData = originalData;
    }

    /**
     * Get metadataFormat.
     *
     * @return value of metadataFormat
     */
    public MetadataFormat getMetadataFormat() {
        return metadataFormat;
    }

    /**
     * Set metadataFormat.
     *
     * @param metadataFormat as org.kitodo.api.schemaconverter.MetadataFormat
     */
    public void setMetadataFormat(MetadataFormat metadataFormat) {
        this.metadataFormat = metadataFormat;
    }

    /**
     * Get fileFormat.
     *
     * @return value of fileFormat
     */
    public FileFormat getFileFormat() {
        return fileFormat;
    }

    /**
     * Set fileFormat.
     *
     * @param fileFormat as org.kitodo.api.schemaconverter.FileFormat
     */
    public void setFileFormat(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }
}
