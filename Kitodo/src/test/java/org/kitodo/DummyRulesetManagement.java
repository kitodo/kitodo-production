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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Optional;

import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalDivision;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;

public class DummyRulesetManagement implements RulesetManagementInterface {

    @Override
    public Collection<String> getAcquisitionStages() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getFunctionalKeys(FunctionalMetadata functionalMetadata) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getFunctionalDivisions(FunctionalDivision functionalDivision) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getDivisionsWithNoWorkflow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getStructuralElements(List<LanguageRange> priorityList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StructuralElementViewInterface getStructuralElementView(String division, String acquisitionStage,
            List<LanguageRange> priorityList) {
        return new DummyStructuralElementView(division);
    }

    @Override
    public ComplexMetadataViewInterface getMetadataView(String metadata, String acquisitionStage, List<LanguageRange> priorityList) {
        return new DummyMetadataView();
    }

    @Override
    public Optional<String> getTranslationForKey(String key, List<LanguageRange> priorityList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void load(File rulesetFile) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAlwaysShowingForKey(String keyId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int updateMetadata(String division, Collection<Metadata> metadata, String acquisitionStage,
            Collection<Metadata> updateItems) {
        throw new UnsupportedOperationException();
    }
}
