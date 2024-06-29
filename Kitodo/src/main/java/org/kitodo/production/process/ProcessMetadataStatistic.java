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

package org.kitodo.production.process;

public class ProcessMetadataStatistic {

    private String processTitle;
    private int numberOfImages;
    private int numberOfStructuralElements;
    private int numberOfMetadata;

    /**
     * Constructor.
     * @param processTitle the processTitle
     * @param numberOfImages the number of Images
     * @param numberOfStructuralElements the number of structural Elements
     * @param numberOfMetadata the number of metadata
     */
    public ProcessMetadataStatistic(String processTitle, int numberOfImages, int numberOfStructuralElements,
            int numberOfMetadata) {
        this.processTitle = processTitle;
        this.numberOfImages = numberOfImages;
        this.numberOfStructuralElements = numberOfStructuralElements;
        this.numberOfMetadata = numberOfMetadata;
    }

    public String getProcessTitle() {
        return processTitle;
    }

    public void setProcessTitle(String processTitle) {
        this.processTitle = processTitle;
    }

    public int getNumberOfImages() {
        return numberOfImages;
    }

    public void setNumberOfImages(int numberOfImages) {
        this.numberOfImages = numberOfImages;
    }

    public int getNumberOfStructuralElements() {
        return numberOfStructuralElements;
    }

    public void setNumberOfStructuralElements(int numberOfStructuralElements) {
        this.numberOfStructuralElements = numberOfStructuralElements;
    }

    public int getNumberOfMetadata() {
        return numberOfMetadata;
    }

    public void setNumberOfMetadata(int numberOfMetadata) {
        this.numberOfMetadata = numberOfMetadata;
    }
}
