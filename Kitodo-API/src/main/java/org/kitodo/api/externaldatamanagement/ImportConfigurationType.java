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

package org.kitodo.api.externaldatamanagement;

public enum ImportConfigurationType {

    OPAC_SEARCH("newProcess.catalogueSearch.heading"),
    FILE_UPLOAD("newProcess.fileUpload.heading"),
    PROCESS_TEMPLATE("processTemplate");

    private final String messageKey;

    ImportConfigurationType(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * Get messageKey.
     *
     * @return value of messageKey
     */
    public String getMessageKey() {
        return messageKey;
    }
}
