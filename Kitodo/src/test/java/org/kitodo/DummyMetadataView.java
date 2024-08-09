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

package org.kitodo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;

public class DummyMetadataView implements ComplexMetadataViewInterface {

    public DummyMetadataView() {
    }

    @Override
    public Collection<MetadataViewInterface> getAddableMetadata(Collection<Metadata> entered,
            Collection<String> additionallySelected) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<MetadataViewInterface> getAllowedMetadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<MetadataViewWithValuesInterface> getSortedVisibleMetadata(Collection<Metadata> entered,
                                                                                 Collection<String> additionallySelected) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Domain> getDomain() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLabel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxOccurs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMinOccurs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUndefined() {
        throw new UnsupportedOperationException();
    }
}
