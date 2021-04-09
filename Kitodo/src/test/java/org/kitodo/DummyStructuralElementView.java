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

package org.kitodo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.DatesSimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;

public class DummyStructuralElementView implements StructuralElementViewInterface {

    private String label;

    public DummyStructuralElementView(String label) {
        this.label = label;
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
        return label;
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

    @Override
    public Map<String, String> getAllowedSubstructuralElements() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<DatesSimpleMetadataViewInterface> getDatesSimpleMetadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> getProcessTitle() {
        throw new UnsupportedOperationException();
    }
}
