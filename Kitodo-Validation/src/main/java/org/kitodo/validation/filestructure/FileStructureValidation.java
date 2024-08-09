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

package org.kitodo.validation.filestructure;

import java.net.URI;

import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.filestructure.FileStructureValidationInterface;
import org.kitodo.exceptions.NotImplementedException;

public class FileStructureValidation implements FileStructureValidationInterface {

    @Override
    public ValidationResult validate(URI xmlFileUri, URI xsdFileUri) {
        throw new NotImplementedException();
    }

}
