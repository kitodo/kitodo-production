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

public class ExemplarRecord {

    private String owner;
    private String signature;

    public ExemplarRecord(String recordOwner, String recordSignature) {
        this.owner = recordOwner;
        this.signature = recordSignature;
    }

    /**
     * Get owner.
     *
     * @return value of owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Set owner.
     *
     * @param owner as java.lang.String
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Get signature.
     *
     * @return value of signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Set signature.
     *
     * @param signature as java.lang.String
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }
}
