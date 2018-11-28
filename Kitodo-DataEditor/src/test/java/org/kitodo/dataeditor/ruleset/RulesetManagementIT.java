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

package org.kitodo.dataeditor.ruleset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.time.Month;
import java.time.MonthDay;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.DatesSimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.InputType;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;

/**
 * Here, the ruleset management is put through its paces.
 */
public class RulesetManagementIT {
    /**
     * English as the only language. This is the default case we use all the
     * time, except for tests that explicitly refer to the language.
     */
    private static final List<LanguageRange> ENGL = LanguageRange.parse("en");

    /**
     * This test checks if a ruleset can be loaded from a file. It loads a
     * complex rule set that contains all sorts of possible cases to test
     * whether the parser has been implemented correctly. The only purpose of
     * this test is that the load command does not throw an exception.
     *
     * <p>
     * The ruleset for this test is more practice-oriented and also includes
     * documentation, if you are looking for it.
     */
    @Test
    public void testAnExtensiveRulesetCanBeLoaded() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testAnExtensiveRulesetCanBeLoaded.xml"));
    }

    /**
     * The ruleset contains presets. This test checks if the presets in the view
     * can be retrieved. That needs to be tested for the different codomain
     * types.
     */
    @Test
    public void testAvailabilityOfPresets() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testAvailabilityOfPresets.xml"));

        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        List<MetadataViewWithValuesInterface<Void>> mvwviList = sevi.getSortedVisibleMetadata(Collections.emptyMap(),
            Arrays.asList("defaultStringKey", "anyURIKey", "booleanKey", "dateKey", "namespaceDefaultAnyURIKey",
                "namespaceStringKey"));

        MetadataViewInterface booleanMvi = mvwviList.get(0).getMetadata().get();
        assertThat(booleanMvi.isComplex(), is(false));
        assertThat(booleanMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface booleanSmvi = (SimpleMetadataViewInterface) booleanMvi;
        assertThat(booleanSmvi.getBooleanDefaultValue(), is(true));

        MetadataViewInterface dateMvi = mvwviList.get(1).getMetadata().get();
        assertThat(dateMvi.isComplex(), is(false));
        assertThat(dateMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface dateSmvi = (SimpleMetadataViewInterface) dateMvi;
        assertThat(dateSmvi.getDefaultValue(), is(equalTo("1993-09-01")));

        MetadataViewInterface defaultStringMvi = mvwviList.get(2).getMetadata().get();
        assertThat(defaultStringMvi.isComplex(), is(false));
        assertThat(defaultStringMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface defaultStringSmvi = (SimpleMetadataViewInterface) defaultStringMvi;
        assertThat(defaultStringSmvi.getDefaultValue(), is(equalTo("Hello World!")));

        MetadataViewInterface namespaceDefaultAnyURIMvi = mvwviList.get(3).getMetadata().get();
        assertThat(namespaceDefaultAnyURIMvi.isComplex(), is(false));
        assertThat(namespaceDefaultAnyURIMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface namespaceDefaultAnyURISmvi = (SimpleMetadataViewInterface) namespaceDefaultAnyURIMvi;
        assertThat(namespaceDefaultAnyURISmvi.getDefaultValue(),
            is(equalTo("http://test.example/non-existent-namespace/")));

        MetadataViewInterface namespaceStringMvi = mvwviList.get(4).getMetadata().get();
        assertThat(namespaceStringMvi.isComplex(), is(false));
        assertThat(namespaceStringMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface namespaceStringSmvi = (SimpleMetadataViewInterface) namespaceStringMvi;
        assertThat(namespaceStringSmvi.getDefaultValue(), is(equalTo("http://test.example/non-existent-namespace/")));

        MetadataViewInterface anyURIMvi = mvwviList.get(5).getMetadata().get();
        assertThat(anyURIMvi.isComplex(), is(false));
        assertThat(anyURIMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface anyURISmvi = (SimpleMetadataViewInterface) anyURIMvi;
        assertThat(anyURISmvi.getDefaultValue(),
            is(equalTo("https://en.wikipedia.org/wiki/%22Hello,_World!%22_program")));
    }

    /**
     * Check that a subkey view is available for subkeys.
     */
    @Test
    public void testAvailabilityOfSubkeyViews() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testAvailabilityOfSubkeyViews.xml"));

        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        List<MetadataViewWithValuesInterface<Void>> mvwviList = sevi.getSortedVisibleMetadata(Collections.emptyMap(),
            Arrays.asList("contributor"));

        MetadataViewInterface contributorMvi = mvwviList.get(0).getMetadata().get();
        assertThat(contributorMvi.getId(), is(equalTo("contributor")));
        assertThat(contributorMvi.getLabel(), is(equalTo("Contributor ‹person›")));
        assertThat(contributorMvi.isComplex(), is(true));
        assertThat(contributorMvi, is(instanceOf(ComplexMetadataViewInterface.class)));
        ComplexMetadataViewInterface contributorCmvi = (ComplexMetadataViewInterface) contributorMvi;

        Collection<MetadataViewInterface> mvic = contributorCmvi.getAddableMetadata(Collections.emptyMap(),
            Collections.emptyList());

        Iterator<MetadataViewInterface> mvici = mvic.iterator();
        for (int i = 0; i < mvic.size(); i++) {
            if (!mvici.hasNext()) {
                break;
            }
            MetadataViewInterface mvi = mvici.next();

            assertThat(mvi.isComplex(), is(false));
            assertThat(mvi, is(instanceOf(SimpleMetadataViewInterface.class)));
            SimpleMetadataViewInterface smvi = (SimpleMetadataViewInterface) mvi;

            /*
             * We do not have a restriction rule here, so the subkeys appear in
             * alphabetical order after the translated label. That’s wanted.
             */
            switch (i) {
                case 0:
                    assertThat(smvi.getId(), is(equalTo("givenName")));
                    assertThat(smvi.getLabel(), is(equalTo("Given name")));
                    break;
                case 1:
                    assertThat(smvi.getId(), is(equalTo("role")));
                    assertThat(smvi.getLabel(), is(equalTo("Role")));
                    assertThat(smvi.getSelectItems(), hasEntry("aut", "Author"));
                    assertThat(smvi.getSelectItems(), hasEntry("edt", "Editor"));
                    break;
                case 2:
                    assertThat(smvi.getId(), is(equalTo("surname")));
                    assertThat(smvi.getLabel(), is(equalTo("Surname")));
                    break;
                default:
                    fail("Too many elements in the view!");
            }
        }
    }

    /**
     * Checks if the options are returned correctly. On the one hand there is
     * the translation, on the other hand the DisplayMode has to be set to
     * MULTI_LINE_SINGLE_SELECTION, MULTIPLE_SELECTION or
     * ONE_LINE_SINGLE_SELECTION accordingly. In cases
     * MULTI_LINE_SINGLE_SELECTION and ONE_LINE_SINGLE_SELECTION, the
     * distinction is made as to whether minOccurs is 1 or not, if not, there
     * must not be an empty selection element for no input, otherwise not.
     */
    @Test
    public void testCorrectReturnOfOptions() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testCorrectReturnOfOptions.xml"));

        // 1. options are sorted as to their labels alphabetically

        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        List<MetadataViewWithValuesInterface<Void>> mvwviList = sevi.getSortedVisibleMetadata(Collections.emptyMap(),
            Collections.emptyList());

        StructuralElementViewInterface seviDe = underTest.getStructuralElementView("book", "",
            LanguageRange.parse("de"));
        List<MetadataViewWithValuesInterface<Void>> mvwviListDe = seviDe
                .getSortedVisibleMetadata(Collections.emptyMap(), Collections.emptyList());

        assertThat(((SimpleMetadataViewInterface) mvwviList.get(0).getMetadata().get()).getSelectItems().keySet(),
            contains("dan", "dut", "eng", "fre", "ger"));
        assertThat(((SimpleMetadataViewInterface) mvwviListDe.get(0).getMetadata().get()).getSelectItems().keySet(),
            contains("dan", "ger", "eng", "fre", "dut"));

        // 2. The input elements that have a minimum occurrence greater than
        // zero will appear on the display by themselves, the others may be
        // added.

        assertThat(ids(mvwviList), contains("mandatoryMultiLineSingleSelection", "mandatoryOneLineSingleSelection",
            "mandatoryMultipleSelection"));

        Collection<MetadataViewInterface> mviColl = sevi.getAddableMetadata(Collections.emptyMap(),
            Collections.emptyList());

        assertThat(ids(mviColl), contains("optionalMultiLineSingleSelection", "optionalOneLineSingleSelection",
            "optionalMultipleSelection"));

        // 3. The input types have been calculated correctly

        assertThat(
            mvwviList.stream().map(mvwvi -> ((SimpleMetadataViewInterface) mvwvi.getMetadata().get()).getInputType())
                    .collect(Collectors.toList()),
            contains(InputType.MULTI_LINE_SINGLE_SELECTION, InputType.ONE_LINE_SINGLE_SELECTION,
                InputType.MULTIPLE_SELECTION));

        assertThat(
            mviColl.stream().map(mvi -> ((SimpleMetadataViewInterface) mvi).getInputType())
                    .collect(Collectors.toList()),
            contains(InputType.MULTI_LINE_SINGLE_SELECTION, InputType.ONE_LINE_SINGLE_SELECTION,
                InputType.MULTIPLE_SELECTION));

        // 4. In the optional single selections, one empty element is in it, not
        // in the others.

        assertThat(((SimpleMetadataViewInterface) mvwviList.get(0).getMetadata().get()).getSelectItems(),
            not(hasEntry("", "")));
        assertThat(((SimpleMetadataViewInterface) mvwviList.get(1).getMetadata().get()).getSelectItems(),
            not(hasEntry("", "")));
        assertThat(((SimpleMetadataViewInterface) mvwviList.get(2).getMetadata().get()).getSelectItems(),
            not(hasEntry("", "")));
        List<MetadataViewInterface> mviList = (List<MetadataViewInterface>) mviColl;
        assertThat(((SimpleMetadataViewInterface) mviList.get(0)).getSelectItems(), hasEntry("", ""));
        assertThat(((SimpleMetadataViewInterface) mviList.get(1)).getSelectItems(), hasEntry("", ""));
        assertThat(((SimpleMetadataViewInterface) mviList.get(2)).getSelectItems(), not(hasEntry("", "")));
    }

    /**
     * The test verifies that only the options that are valid according to the
     * ruleset are valid.
     */
    @Test
    public void testCorrectValidationOfOptions() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testCorrectValidationOfOptions.xml"));

        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        Collection<MetadataViewInterface> mviColl = sevi.getAddableMetadata(Collections.emptyMap(),
            Collections.emptyList());
        SimpleMetadataViewInterface smvi = (SimpleMetadataViewInterface) mviColl.iterator().next();
        assertThat(smvi.isValid("opt1"), is(true));
        assertThat(smvi.isValid("Opt1"), is(false));
    }

    /**
     * The test checks if the validation by regular expressions works.
     */
    @Test
    public void testCorrectValidationOfRegularExpressions() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testCorrectValidationOfRegularExpressions.xml"));

        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        List<MetadataViewWithValuesInterface<Void>> mvwviList = sevi.getSortedVisibleMetadata(Collections.emptyMap(),
            Arrays.asList("defaultStringKey", "anyURIKey", "booleanKey", "dateKey", "namespaceDefaultAnyURIKey",
                "namespaceStringKey", "integerKey", "optionsKey"));

        MetadataViewInterface booleanMvi = mvwviList.get(0).getMetadata().get();
        assertThat(booleanMvi.isComplex(), is(false));
        assertThat(booleanMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface booleanSmvi = (SimpleMetadataViewInterface) booleanMvi;
        assertThat(booleanSmvi.isValid(booleanSmvi.convertBoolean(true).get()), is(true));
        assertThat(booleanSmvi.isValid(""), is(false));
        assertThat(booleanSmvi.isValid("botch"), is(false));

        MetadataViewInterface dateMvi = mvwviList.get(1).getMetadata().get();
        assertThat(dateMvi.isComplex(), is(false));
        assertThat(dateMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface dateSmvi = (SimpleMetadataViewInterface) dateMvi;
        assertThat(dateSmvi.isValid(""), is(false));
        assertThat(dateSmvi.isValid("1803-05-12"), is(false));
        assertThat(dateSmvi.isValid("1993-09-01"), is(true));

        MetadataViewInterface defaultStringMvi = mvwviList.get(2).getMetadata().get();
        assertThat(defaultStringMvi.isComplex(), is(false));
        assertThat(defaultStringMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface defaultStringSmvi = (SimpleMetadataViewInterface) defaultStringMvi;
        assertThat(defaultStringSmvi.isValid("Hello World!"), is(false));
        assertThat(defaultStringSmvi.isValid("1234567X"), is(true));

        MetadataViewInterface integerMvi = mvwviList.get(3).getMetadata().get();
        assertThat(integerMvi.isComplex(), is(false));
        assertThat(integerMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface integerSmvi = (SimpleMetadataViewInterface) integerMvi;
        assertThat(integerSmvi.isValid("22"), is(false));
        assertThat(integerSmvi.isValid("1748"), is(true));

        MetadataViewInterface namespaceDefaultAnyURIMvi = mvwviList.get(4).getMetadata().get();
        assertThat(namespaceDefaultAnyURIMvi.isComplex(), is(false));
        assertThat(namespaceDefaultAnyURIMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface namespaceDefaultAnyURISmvi = (SimpleMetadataViewInterface) namespaceDefaultAnyURIMvi;
        assertThat(namespaceDefaultAnyURISmvi
                .isValid("http://test.example/non-existent-namespace/%22Hello,_World!%22_program"),
            is(false));
        assertThat(namespaceDefaultAnyURISmvi.isValid("http://test.example/non-existent-namespace/Hello_World_program"),
            is(true));

        MetadataViewInterface namespaceStringMvi = mvwviList.get(5).getMetadata().get();
        assertThat(namespaceStringMvi.isComplex(), is(false));
        assertThat(namespaceStringMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface namespaceStringSmvi = (SimpleMetadataViewInterface) namespaceStringMvi;
        assertThat(
            namespaceStringSmvi.isValid("http://test.example/non-existent-namespace/%22Hello,_World!%22_program"),
            is(false));
        assertThat(namespaceStringSmvi.isValid("http://test.example/non-existent-namespace/Hello_World_program"),
            is(true));

        MetadataViewInterface optionsMvi = mvwviList.get(6).getMetadata().get();
        assertThat(optionsMvi.isComplex(), is(false));
        assertThat(optionsMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface optionsSmvi = (SimpleMetadataViewInterface) optionsMvi;
        assertThat(optionsSmvi.isValid("opt88"), is(false));
        assertThat(optionsSmvi.isValid("opt2"), is(true));

        MetadataViewInterface anyURIMvi = mvwviList.get(7).getMetadata().get();
        assertThat(anyURIMvi.isComplex(), is(false));
        assertThat(anyURIMvi, is(instanceOf(SimpleMetadataViewInterface.class)));
        SimpleMetadataViewInterface anyURISmvi = (SimpleMetadataViewInterface) anyURIMvi;
        assertThat(anyURISmvi.isValid("mailto:e-mail@example.org"), is(false));
        assertThat(anyURISmvi.isValid("https://en.wikipedia.org/wiki/%22Hello,_World!%22_program"), is(true));
    }

    /**
     * This test verifies that acquisition stage edit settings correctly
     * override the common edit settings.
     */
    @Test
    public void testDisplaySettingsWithAcquisitionStage() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testDisplaySettingsWithAcquisitionStage.xml"));

        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "The acquisition stage", ENGL);

        // always showing

        List<MetadataViewWithValuesInterface<Void>> mvwviListAlwaysShowing = sevi
                .getSortedVisibleMetadata(Collections.emptyMap(), Collections.emptyList());
        assertThat(
            mvwviListAlwaysShowing.stream().map(mvwvi -> mvwvi.getMetadata().get().getId())
                    .collect(Collectors.toList()),
            containsInAnyOrder("alwaysShowingUnchangedTrue", "alwaysShowingTrueUnchanged",
                "alwaysShowingTrueOtherchanges", "alwaysShowingTrueTrue", "multilineTrueOtherchanges"));

        // excluded

        Map<Object, String> metadataForExcluded = new HashMap<>();
        metadataForExcluded.put("exclude1", "excludedUnchangedTrue");
        metadataForExcluded.put("exclude2", "excludedTrueUnchanged");
        metadataForExcluded.put("exclude3", "excludedTrueOtherchanges");
        metadataForExcluded.put("exclude4", "excludedTrueTrue");
        metadataForExcluded.put("n#*703=]", "excludedTrueFalse");
        List<MetadataViewWithValuesInterface<Object>> mvwviListExcluded = sevi
                .getSortedVisibleMetadata(metadataForExcluded, Collections.emptyList());
        assertThat(mvwviListExcluded.stream().filter(mvwvi -> mvwvi.getMetadata().isPresent())
                .map(mvwvi -> mvwvi.getMetadata().get().getId()).filter(keyId -> keyId.startsWith("excluded"))
                .collect(Collectors.toList()),
            contains("excludedTrueFalse"));
        assertThat(
            mvwviListExcluded.stream().filter(mvwvi -> !mvwvi.getMetadata().isPresent())
                    .flatMap(mvwvi -> mvwvi.getValues().stream()).collect(Collectors.toList()),
            containsInAnyOrder("exclude1", "exclude2", "exclude3", "exclude4"));
        Collection<MetadataViewInterface> mviCollExcluded = sevi.getAddableMetadata(metadataForExcluded,
            Collections.emptyList());
        assertThat(mviCollExcluded.stream().map(mvi -> mvi.getId()).filter(keyId -> keyId.startsWith("excluded"))
                .collect(Collectors.toList()),
            contains("excludedTrueFalse"));

        // editable

        List<MetadataViewWithValuesInterface<Object>> mvwviList = sevi.getSortedVisibleMetadata(Collections.emptyMap(),
            Arrays.asList("editableUnchangedFalse", "editableFalseUnchanged", "editableFalseOtherchanges",
                "editableFalseFalse", "editableFalseTrue", "multilineUnchangedTrue", "multilineTrueUnchanged",
                "multilineTrueOtherchanges", "multilineTrueTrue", "multilineTrueFalse"));

        SimpleMetadataViewInterface editableUnchangedFalse = getSmvi(mvwviList, "editableUnchangedFalse");
        assertThat(editableUnchangedFalse.isEditable(), is(false));
        SimpleMetadataViewInterface editableFalseUnchanged = getSmvi(mvwviList, "editableFalseUnchanged");
        assertThat(editableFalseUnchanged.isEditable(), is(false));
        SimpleMetadataViewInterface editableFalseOtherchanges = getSmvi(mvwviList, "editableFalseOtherchanges");
        assertThat(editableFalseOtherchanges.isEditable(), is(false));
        SimpleMetadataViewInterface editableFalseFalse = getSmvi(mvwviList, "editableFalseFalse");
        assertThat(editableFalseFalse.isEditable(), is(false));
        SimpleMetadataViewInterface editableFalseTrue = getSmvi(mvwviList, "editableFalseTrue");
        assertThat(editableFalseTrue.isEditable(), is(true));

        // multi-line

        SimpleMetadataViewInterface multilineUnchangedTrue = getSmvi(mvwviList, "multilineUnchangedTrue");
        assertThat(multilineUnchangedTrue.getInputType(), is(equalTo(InputType.MULTI_LINE_TEXT)));
        SimpleMetadataViewInterface multilineTrueUnchanged = getSmvi(mvwviList, "multilineTrueUnchanged");
        assertThat(multilineTrueUnchanged.getInputType(), is(equalTo(InputType.MULTI_LINE_TEXT)));
        SimpleMetadataViewInterface multilineTrueOtherchanges = getSmvi(mvwviList, "multilineTrueOtherchanges");
        assertThat(multilineTrueOtherchanges.getInputType(), is(equalTo(InputType.MULTI_LINE_TEXT)));
        SimpleMetadataViewInterface multilineTrueTrue = getSmvi(mvwviList, "multilineTrueTrue");
        assertThat(multilineTrueTrue.getInputType(), is(equalTo(InputType.MULTI_LINE_TEXT)));
        SimpleMetadataViewInterface multilineTrueFalse = getSmvi(mvwviList, "multilineTrueFalse");
        assertThat(multilineTrueFalse.getInputType(), is(equalTo(InputType.ONE_LINE_TEXT)));
    }

    /**
     * This test verifies the working of the editing settings, including their
     * interaction and nesting.
     */
    @Test
    public void testDisplaySettingsWithoutAcquisitionStage() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testDisplaySettingsWithoutAcquisitionStage.xml"));
        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);

        // 1. Without meta-data, and without additional fields being selected,
        // you should see exactly the fields that are always showing, minus
        // those that are excluded (excluded overrules always showing).

        List<MetadataViewWithValuesInterface<Void>> mvwviListNoMetadata = sevi
                .getSortedVisibleMetadata(Collections.emptyMap(), Collections.emptyList());
        assertThat(ids(mvwviListNoMetadata),
            containsInAnyOrder("testAlwaysShowing", "testAlwaysShowingEditable", "testAlwaysShowingMultiline"));

        // 2. All fields except those that are excluded should be allowed to be
        // added.

        Collection<MetadataViewInterface> mviCollNoMetadata = sevi.getAddableMetadata(Collections.emptyMap(),
            Collections.emptyList());
        assertThat(ids(mviCollNoMetadata), containsInAnyOrder("testAlwaysShowing", "testEditable", "testMultiline",
            "testAlwaysShowingEditable", "testAlwaysShowingMultiline", "testEditableMultiline", "testNestedSettings"));

        // 1a. In the nested meta-data field, the only value that should be
        // visible is the one marked as always showing. The field has to be
        // added first:

        List<MetadataViewWithValuesInterface<Void>> mvwviListNestedSettings = sevi
                .getSortedVisibleMetadata(Collections.emptyMap(), Arrays.asList("testNestedSettings"));
        ComplexMetadataViewInterface nestedSettings = (ComplexMetadataViewInterface) mvwviListNestedSettings.stream()
                .filter(mvwvi -> mvwvi.getMetadata().get().getId().equals("testNestedSettings")).findAny().get()
                .getMetadata().get();
        List<MetadataViewWithValuesInterface<Void>> nestedMvwviList = nestedSettings
                .getSortedVisibleMetadata(Collections.emptyMap(), Collections.emptyList());
        assertThat(nestedMvwviList, hasSize(1));
        assertThat(nestedMvwviList.get(0).getMetadata().get().getId(), is(equalTo("testAlwaysShowing")));

        // 2a. Also with nested meta-data, all fields except those that are
        // excluded should be allowed to be added.

        Collection<MetadataViewInterface> nestedMviColl = nestedSettings.getAddableMetadata(Collections.emptyMap(),
            Collections.emptyList());
        assertThat(ids(nestedMviColl), containsInAnyOrder("testAlwaysShowing", "testEditable", "testMultiline"));

        // 3. With meta-data, all fields should be visible except those that are
        // excluded. There should be an entry without a key in the list
        // containing the values of the excluded keys.

        Map<Object, String> metadata = new HashMap<>();
        metadata.put("udv-q@bC", "testAlwaysShowing");
        metadata.put("/F5Mu=/1", "testEditable");
        metadata.put("exclude1", "testExcluded");
        metadata.put("WP&~O$YV", "testMultiline");
        metadata.put("n#*703=]", "testAlwaysShowingEditable");
        metadata.put("exclude2", "testAlwaysShowingExcluded");
        metadata.put("Mu{lp'n1", "testAlwaysShowingMultiline");
        metadata.put("exclude3", "testEditableExcluded");
        metadata.put("qP'Jc:.R", "testEditableMultiline");
        metadata.put("exclude4", "testExcludedMultiline");
        metadata.put("4J[~UgHp", "testNestedSettings");

        List<MetadataViewWithValuesInterface<Object>> mvwviListWithMetadata = sevi.getSortedVisibleMetadata(metadata,
            Collections.emptyList());
        assertThat(ids(mvwviListWithMetadata), containsInAnyOrder("testAlwaysShowing", "testEditable", "testMultiline",
            "testAlwaysShowingEditable", "testAlwaysShowingMultiline", "testEditableMultiline", "testNestedSettings"));
        assertThat(
            mvwviListWithMetadata.stream().filter(mvwvi -> !mvwvi.getMetadata().isPresent())
                    .flatMap(mvwvi -> mvwvi.getValues().stream()).collect(Collectors.toList()),
            containsInAnyOrder("exclude1", "exclude2", "exclude3", "exclude4"));

        // 3a. Also with nested meta-data, that should work that way.

        Map<Object, String> nestedMetadata = new HashMap<>();
        nestedMetadata.put("udv-q@bC", "testAlwaysShowing");
        nestedMetadata.put("/F5Mu=/1", "testEditable");
        nestedMetadata.put("excluded", "testExcluded");
        nestedMetadata.put("WP&~O$YV", "testMultiline");

        List<MetadataViewWithValuesInterface<Object>> nestedMvwviListWithMetadata = nestedSettings
                .getSortedVisibleMetadata(nestedMetadata, Collections.emptyList());
        assertThat(ids(nestedMvwviListWithMetadata),
            containsInAnyOrder("testAlwaysShowing", "testEditable", "testMultiline"));
        assertThat(nestedMvwviListWithMetadata.stream().filter(mvwvi -> !mvwvi.getMetadata().isPresent())
                .flatMap(mvwvi -> mvwvi.getValues().stream()).collect(Collectors.toList()),
            contains("excluded"));

        // 4. The property ‘multiline’ should manipulate the input type

        SimpleMetadataViewInterface testAlwaysShowing = getSmvi(mvwviListWithMetadata, "testAlwaysShowing");
        assertThat(testAlwaysShowing.getInputType(), is(equalTo(InputType.ONE_LINE_TEXT)));
        SimpleMetadataViewInterface testEditable = getSmvi(mvwviListWithMetadata, "testEditable");
        assertThat(testEditable.getInputType(), is(equalTo(InputType.ONE_LINE_TEXT)));
        SimpleMetadataViewInterface testMultiline = getSmvi(mvwviListWithMetadata, "testMultiline");
        assertThat(testMultiline.getInputType(), is(equalTo(InputType.MULTI_LINE_TEXT)));
        SimpleMetadataViewInterface testAlwaysShowingEditable = getSmvi(mvwviListWithMetadata,
            "testAlwaysShowingEditable");
        assertThat(testAlwaysShowingEditable.getInputType(), is(equalTo(InputType.ONE_LINE_TEXT)));
        SimpleMetadataViewInterface testAlwaysShowingMultiline = getSmvi(mvwviListWithMetadata,
            "testAlwaysShowingMultiline");
        assertThat(testAlwaysShowingMultiline.getInputType(), is(equalTo(InputType.MULTI_LINE_TEXT)));
        SimpleMetadataViewInterface testEditableMultiline = getSmvi(mvwviListWithMetadata, "testEditableMultiline");
        assertThat(testEditableMultiline.getInputType(), is(equalTo(InputType.MULTI_LINE_TEXT)));

        SimpleMetadataViewInterface nestedTestAlwaysShowing = getSmvi(nestedMvwviListWithMetadata, "testAlwaysShowing");
        assertThat(nestedTestAlwaysShowing.getInputType(), is(equalTo(InputType.ONE_LINE_TEXT)));
        SimpleMetadataViewInterface nestedTestEditable = getSmvi(nestedMvwviListWithMetadata, "testEditable");
        assertThat(nestedTestEditable.getInputType(), is(equalTo(InputType.ONE_LINE_TEXT)));
        SimpleMetadataViewInterface nestedTestMultiline = getSmvi(nestedMvwviListWithMetadata, "testMultiline");
        assertThat(nestedTestMultiline.getInputType(), is(equalTo(InputType.MULTI_LINE_TEXT)));

        // 5. The property ‘editable’ should be reflected in the value of
        // isEditable()

        assertThat(testAlwaysShowing.isEditable(), is(true));
        assertThat(testEditable.isEditable(), is(false));
        assertThat(testMultiline.isEditable(), is(true));
        assertThat(testAlwaysShowingEditable.isEditable(), is(false));
        assertThat(testAlwaysShowingMultiline.isEditable(), is(true));
        assertThat(testEditableMultiline.isEditable(), is(false));

        assertThat(nestedTestAlwaysShowing.isEditable(), is(true));
        assertThat(nestedTestEditable.isEditable(), is(false));
        assertThat(nestedTestMultiline.isEditable(), is(true));
    }

    /**
     * This test verifies that the labels for divisions are translated
     * correctly. For this purpose, various cases with different language
     * requirements (existing, nonexistent and overspecified) are performed.
     */
    @Test
    public void testDivisionsAreCorrectlyTranslated() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testDivisionsAreCorrectlyTranslated.xml"));
        assertThat(underTest.getAcquisitionStages(), is(empty()));

        // Here we test the translation, whether the expected language always
        // comes.

        Map<String, String> divisionsNoLanguage = underTest.getStructuralElements(Collections.emptyList());
        assertThat(divisionsNoLanguage.entrySet(), hasSize(1));
        assertThat(divisionsNoLanguage, hasKey("book"));
        assertThat(divisionsNoLanguage, hasValue("Book"));

        Map<String, String> divisionsDe = underTest
                .getStructuralElements(LanguageRange.parse("de;q=1.0,cn;q=0.75,fr;q=0.5,ru;q=0.25"));
        assertThat(divisionsDe.entrySet(), hasSize(1));
        assertThat(divisionsDe, hasKey("book"));
        assertThat(divisionsDe, hasValue("Buch"));

        Map<String, String> divisionsDeDe = underTest
                .getStructuralElements(LanguageRange.parse("de-DE;q=1.0,cn;q=0.75,fr;q=0.5,ru;q=0.25"));
        assertThat(divisionsDeDe.entrySet(), hasSize(1));
        assertThat(divisionsDeDe, hasKey("book"));
        assertThat(divisionsDeDe, hasValue("Buch"));

        Map<String, String> divisionsEn = underTest
                .getStructuralElements(LanguageRange.parse("en;q=1.0,fr;q=0.75,de;q=0.5,cn;q=0.25"));
        assertThat(divisionsEn.entrySet(), hasSize(1));
        assertThat(divisionsEn, hasKey("book"));
        assertThat(divisionsEn, hasValue("Book"));

        Map<String, String> divisionsEnUs = underTest
                .getStructuralElements(LanguageRange.parse("en-US;q=1.0,de;q=0.667,fr;q=0.333"));
        assertThat(divisionsEnUs.entrySet(), hasSize(1));
        assertThat(divisionsEnUs, hasKey("book"));
        assertThat(divisionsEnUs, hasValue("Book"));

        Map<String, String> divisionsCnRu = underTest.getStructuralElements(LanguageRange.parse("cn;q=1.0,ru;q=0.5"));
        assertThat(divisionsCnRu.entrySet(), hasSize(1));
        assertThat(divisionsCnRu, hasKey("book"));
        assertThat(divisionsCnRu, hasValue("Book"));

        // Now a first view on a book

        StructuralElementViewInterface view = underTest.getStructuralElementView("book", "", ENGL);
        assertThat(view.getAllowedSubstructuralElements().entrySet(), hasSize(1));
        assertThat(view.getAddableMetadata(Collections.emptyMap(), Collections.emptyList()), hasSize(5));
        assertThat(view.isComplex(), is(true));
        assertThat(view.isUndefined(), is(false));

        // Now a nonsense view

        StructuralElementViewInterface nonsenseView = underTest.getStructuralElementView("bosh", "", ENGL);
        assertThat(nonsenseView.getAllowedSubstructuralElements().entrySet(), hasSize(1));
        assertThat(nonsenseView.getAddableMetadata(Collections.emptyMap(), Collections.emptyList()), hasSize(5));
        assertThat(nonsenseView.isUndefined(), is(true));
    }

    /**
     * This test verifies that the selection of child divisions in divisions by
     * date works correctly.
     */
    @Test
    public void testDivisionsSubdividedByDateHaveOnlyTheCorrectChildren() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testDivisionsSubdividedByDateHaveOnlyTheCorrectChildren.xml"));

        // Also the divisions structuring by date are to come out in the total
        // list:
        assertThat(underTest.getStructuralElements(ENGL).keySet(),
            containsInAnyOrder("newspaper", "newspaperYear", "newspaperMonth", "newspaperDay", "newspaperIssue",
                "newspaperLimitedTest", "newspaperYearLimitedTest", "newspaperMonthLimitedTest",
                "newspaperDayLimitedTest"));
        assertThat(underTest.getStructuralElements(ENGL).values(),
            containsInAnyOrder("Newspaper ‹complete edition›", "Year’s issues ‹newspaper›",
                "Month’s issues ‹newspaper›", "Day’s issues ‹newspaper›", "Issue ‹newspaper›",
                "Newspaper ‹complete edition›—limited test", "Year’s issues ‹newspaper›—limited test",
                "Month’s issues ‹newspaper›—limited test", "Day’s issues ‹newspaper›—limited test"));

        // It should always come the right children:

        StructuralElementViewInterface newspaperSevi = underTest.getStructuralElementView("newspaper", "", ENGL);
        Map<String, String> newspaperAse = newspaperSevi.getAllowedSubstructuralElements();
        assertThat(newspaperAse.entrySet(), hasSize(1));
        assertThat(newspaperAse, hasEntry("newspaperYear", "Year’s issues ‹newspaper›"));

        StructuralElementViewInterface yearSevi = underTest.getStructuralElementView("newspaperYear", "", ENGL);
        Map<String, String> yearAse = yearSevi.getAllowedSubstructuralElements();
        assertThat(yearAse.entrySet(), hasSize(1));
        assertThat(yearAse, hasEntry("newspaperMonth", "Month’s issues ‹newspaper›"));

        StructuralElementViewInterface monthSevi = underTest.getStructuralElementView("newspaperMonth", "", ENGL);
        Map<String, String> monthAse = monthSevi.getAllowedSubstructuralElements();
        assertThat(monthAse.entrySet(), hasSize(1));
        assertThat(monthAse, hasEntry("newspaperDay", "Day’s issues ‹newspaper›"));

        StructuralElementViewInterface daySevi = underTest.getStructuralElementView("newspaperDay", "", ENGL);
        Map<String, String> dayAse = daySevi.getAllowedSubstructuralElements();
        assertThat(dayAse.entrySet(), hasSize(3));
        assertThat(dayAse, hasEntry("newspaper", "Newspaper ‹complete edition›"));
        assertThat(dayAse, hasEntry("newspaperLimitedTest", "Newspaper ‹complete edition›—limited test"));
        assertThat(dayAse, hasEntry("newspaperIssue", "Issue ‹newspaper›"));

        StructuralElementViewInterface newspaperLimitedTestSevi = underTest
                .getStructuralElementView("newspaperDayLimitedTest", "", ENGL);
        Map<String, String> newspaperLimitedTestAse = newspaperLimitedTestSevi.getAllowedSubstructuralElements();
        assertThat(newspaperLimitedTestAse.entrySet(), hasSize(1));
        assertThat(newspaperLimitedTestAse, hasEntry("newspaperIssue", "Issue ‹newspaper›"));
    }

    /**
     * This test checks whether the date scheme and the field are correctly read
     * out for divisions that are subdivided by date.
     */
    @Test
    public void testDivisionsSubdividedByDateReportDateSchemeAndField() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testDivisionsSubdividedByDateReportDateSchemeAndField.xml"));

        StructuralElementViewInterface playtimeSevi = underTest.getStructuralElementView("playtime", "", ENGL);
        assertThat(playtimeSevi.getDatesSimpleMetadata(), is(Optional.empty()));

        StructuralElementViewInterface yearSevi = underTest.getStructuralElementView("playtimeYear", "", ENGL);
        assertThat(yearSevi.getDatesSimpleMetadata(), is(not(Optional.empty())));
        DatesSimpleMetadataViewInterface yearDsmvi = yearSevi.getDatesSimpleMetadata().get();
        assertThat(yearDsmvi.getId(), is(equalTo("ORDERLABEL")));
        assertThat(yearDsmvi.getScheme(), is(equalTo("yyyy/yyyy")));
        assertThat(yearDsmvi.getYearBegin(), is(equalTo(MonthDay.of(Month.AUGUST, 1))));

        StructuralElementViewInterface monthSevi = underTest.getStructuralElementView("playtimeMonth", "", ENGL);
        assertThat(monthSevi.getDatesSimpleMetadata(), is(not(Optional.empty())));
        DatesSimpleMetadataViewInterface monthDsmvi = monthSevi.getDatesSimpleMetadata().get();
        assertThat(monthDsmvi.getId(), is(equalTo("ORDERLABEL")));
        assertThat(monthDsmvi.getScheme(), is(equalTo("yyyy-MM")));

        StructuralElementViewInterface daySevi = underTest.getStructuralElementView("playtimeDay", "", ENGL);
        assertThat(daySevi.getDatesSimpleMetadata(), is(not(Optional.empty())));
        DatesSimpleMetadataViewInterface dayDsmvi = daySevi.getDatesSimpleMetadata().get();
        assertThat(dayDsmvi.getId(), is(equalTo("ORDERLABEL")));
        assertThat(dayDsmvi.getScheme(), is(equalTo("yyyy-MM-dd")));
    }

    /**
     * The test verifies that fields with minOccurs greater than zero appear at
     * least as often as they need to be completed. An exception exists for
     * multiple selection, which of course nevertheless only once may be
     * displayed (here only several must be selected).
     */
    @Test
    public void testFieldsWithMinOccursGreaterZeroAreAlwaysShown() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testFieldsWithMinOccursGreaterZeroAreAlwaysShown.xml"));

        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        List<MetadataViewWithValuesInterface<Object>> mvwviList = sevi.getSortedVisibleMetadata(Collections.emptyMap(),
            Collections.emptyList());

        assertThat(ids(mvwviList), contains("test1", "test2", "test2", "test2options"));
    }

    /**
     * The test verifies that all keys pass the domain correctly.
     */
    @Test
    public void testKeysReturnTheSpecifiedDomain() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testKeysReturnTheSpecifiedDomain.xml"));

        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        Map<Object, String> metadata = new HashMap<>();
        metadata.put("....", "unspecifiedKey");
        List<MetadataViewWithValuesInterface<Object>> mvwviList = sevi.getSortedVisibleMetadata(metadata, Arrays.asList(
            "description", "digitalProvenance", "noDomainSpecified", "rights", "source", "technical", "metsDiv"));

        SimpleMetadataViewInterface description = getSmvi(mvwviList, "description");
        assertThat(description.getDomain().get(), is(equalTo(Domain.DESCRIPTION)));

        SimpleMetadataViewInterface digitalProvenance = getSmvi(mvwviList, "digitalProvenance");
        assertThat(digitalProvenance.getDomain().get(), is(equalTo(Domain.DIGITAL_PROVENANCE)));

        SimpleMetadataViewInterface metsDiv = getSmvi(mvwviList, "metsDiv");
        assertThat(metsDiv.getDomain().get(), is(equalTo(Domain.METS_DIV)));

        SimpleMetadataViewInterface noDomainSpecified = getSmvi(mvwviList, "noDomainSpecified");
        assertThat(noDomainSpecified.getDomain(), is(Optional.empty()));

        SimpleMetadataViewInterface rights = getSmvi(mvwviList, "rights");
        assertThat(rights.getDomain().get(), is(equalTo(Domain.RIGHTS)));

        SimpleMetadataViewInterface source = getSmvi(mvwviList, "source");
        assertThat(source.getDomain().get(), is(equalTo(Domain.SOURCE)));

        SimpleMetadataViewInterface technical = getSmvi(mvwviList, "technical");
        assertThat(technical.getDomain().get(), is(equalTo(Domain.TECHNICAL)));

        SimpleMetadataViewInterface unspecifiedKey = getSmvi(mvwviList, "unspecifiedKey");
        assertThat(unspecifiedKey.getDomain(), is(Optional.empty()));
    }

    /**
     * This test verifies that different rules (key-based and division-based)
     * are merged correctly.
     */
    @Test
    public void testRulesAreCorrectlyMerged() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testRulesAreCorrectlyMerged.xml"));

        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        List<MetadataViewWithValuesInterface<Object>> mvwviList = sevi.getSortedVisibleMetadata(Collections.emptyMap(),
            Arrays.asList("personContributor"));
        ComplexMetadataViewInterface personContributor = getCmvi(mvwviList, "personContributor");
        List<MetadataViewWithValuesInterface<Object>> visible = personContributor
                .getSortedVisibleMetadata(Collections.emptyMap(), Collections.emptyList());
        assertThat(ids(visible), contains("role", "gndRecord", "givenName", "surname"));
        assertThat(getSmvi(visible, "role").getSelectItems().keySet(), contains("author", "editor"));
    }

    /**
     * This test verifies that unspecified unrestricted rules remove keys that
     * have a maxOccurs of zero.
     */
    @Test
    public void testRulesRemoveKeysWithZeroMaxOccurs() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testRulesRemoveKeysWithZeroMaxOccurs.xml"));
        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        Collection<MetadataViewInterface> mviColl = sevi.getAddableMetadata(Collections.emptyMap(),
            Collections.emptyList());
        assertThat(ids(mviColl), contains("keep"));
    }

    /**
     * This test verifies that the input type is set depending on the codomain.
     */
    @Test
    public void testTheDisplayModeIsSetUsingTheCodomain() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testTheDisplayModeIsSetUsingTheCodomain.xml"));

        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        List<MetadataViewWithValuesInterface<Object>> mvwviList = sevi.getSortedVisibleMetadata(Collections.emptyMap(),
            Arrays.asList("defaultString", "anyURI", "boolean", "date", "integer", "namespace", "namespaceString"));

        assertThat(getSmvi(mvwviList, "defaultString").getInputType(), is(equalTo(InputType.ONE_LINE_TEXT)));
        assertThat(getSmvi(mvwviList, "anyURI").getInputType(), is(equalTo(InputType.ONE_LINE_TEXT)));
        assertThat(getSmvi(mvwviList, "boolean").getInputType(), is(equalTo(InputType.BOOLEAN)));
        assertThat(getSmvi(mvwviList, "date").getInputType(), is(equalTo(InputType.DATE)));
        assertThat(getSmvi(mvwviList, "integer").getInputType(), is(equalTo(InputType.INTEGER)));
        assertThat(getSmvi(mvwviList, "namespace").getInputType(), is(equalTo(InputType.ONE_LINE_TEXT)));
        assertThat(getSmvi(mvwviList, "namespaceString").getInputType(), is(equalTo(InputType.ONE_LINE_TEXT)));
    }

    /**
     * This test verifies that unspecified forbidden rules restrict the allowed
     * divisions.
     */
    @Test
    public void testUnspecifiedForbiddenRulesRestrictDivisions() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testUnspecifiedForbiddenRulesRestrictDivisions.xml"));
        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        assertThat(sevi.getAllowedSubstructuralElements().keySet(), contains("chapter"));
    }

    /**
     * This test verifies that unspecified forbidden rules restrict the allowed
     * keys.
     */
    @Test
    public void testUnspecifiedForbiddenRulesRestrictKeys() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testUnspecifiedForbiddenRulesRestrictKeys.xml"));
        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        Collection<MetadataViewInterface> mviColl = sevi.getAddableMetadata(Collections.emptyMap(),
            Collections.emptyList());
        assertThat(ids(mviColl), contains("allowed"));
    }

    /**
     * This test verifies that unspecified forbidden rules restrict the allowed
     * options.
     */
    @Test
    public void testUnspecifiedForbiddenRulesRestrictOptions() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testUnspecifiedForbiddenRulesRestrictOptions.xml"));
        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);

        List<MetadataViewWithValuesInterface<Object>> mvwviList = sevi.getSortedVisibleMetadata(Collections.emptyMap(),
            Arrays.asList("test"));
        SimpleMetadataViewInterface test = getSmvi(mvwviList, "test");
        assertThat(test.getSelectItems().keySet(), contains("opt1", "opt3", "opt5", "opt7"));

    }

    /**
     * This test verifies that unspecified forbidden rules restrict the allowed
     * options during validation.
     */
    @Test
    public void testUnspecifiedForbiddenRulesRestrictOptionsAlsoInTheValidation() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(
            new File("src/test/resources/testUnspecifiedForbiddenRulesRestrictOptionsAlsoInTheValidation.xml"));
        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);

        List<MetadataViewWithValuesInterface<Object>> mvwviList = sevi.getSortedVisibleMetadata(Collections.emptyMap(),
            Arrays.asList("test"));
        SimpleMetadataViewInterface test = getSmvi(mvwviList, "test");
        assertThat(test.isValid("opt1"), is(true));
        assertThat(test.isValid("opt2"), is(false));
        assertThat(test.isValid("opt3"), is(true));
        assertThat(test.isValid("opt4"), is(false));
        assertThat(test.isValid("opt5"), is(true));
        assertThat(test.isValid("opt6"), is(false));
        assertThat(test.isValid("opt7"), is(true));
        assertThat(test.isValid("mischief"), is(false));
    }

    /**
     * This test verifies that unspecified unrestricted rules are resorting but
     * not restricting the divisions.
     */
    @Test
    public void testUnspecifiedUnrestrictedRulesSortDivisionsWithoutRestrictingThem() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(
            new File("src/test/resources/testUnspecifiedUnrestrictedRulesSortDivisionsWithoutRestrictingThem.xml"));

        StructuralElementViewInterface sevi = underTest.getStructuralElementView("box", "", ENGL);
        assertThat(sevi.getAllowedSubstructuralElements().keySet(), contains(
            // in order as in the ruleset
            "phonographicRecord", "cassette", "cd", "gramophoneRecord", "pianoRoll",

            // the rest in alphabetical order by its label
            "article", "book", "box", "chalcography", "colorChart", "dvd", "frontCover", "film", "multiVolume",
            "serial", "side", "videoTape", "wireRecording"));
    }

    /**
     * This test verifies that unspecified unrestricted rules are resorting but
     * not restricting the keys.
     */
    @Test
    public void testUnspecifiedUnrestrictedRulesSortKeysWithoutRestrictingThem() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(
            new File("src/test/resources/testUnspecifiedUnrestrictedRulesSortKeysWithoutRestrictingThem.xml"));
        StructuralElementViewInterface sevi = underTest.getStructuralElementView("article", "", ENGL);
        Collection<MetadataViewInterface> mviColl = sevi.getAddableMetadata(Collections.emptyMap(),
            Collections.emptyList());

        assertThat(ids(mviColl), contains("author", "year", "title", "journal", "journalAbbr", "issue", "abstract"));
    }

    /**
     * The test verifies that unspecified unrestricted rules are resorting but
     * not restricting the options.
     */
    @Test
    public void testUnspecifiedUnrestrictedRulesSortOptionsWithoutRestrictingThem() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(
            new File("src/test/resources/testUnspecifiedUnrestrictedRulesSortOptionsWithoutRestrictingThem.xml"));
        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        List<MetadataViewWithValuesInterface<Object>> mvwviList = sevi.getSortedVisibleMetadata(Collections.emptyMap(),
            Arrays.asList("test"));
        SimpleMetadataViewInterface test = getSmvi(mvwviList, "test");
        assertThat(test.getSelectItems().keySet(), contains("opt4", "opt7", "opt1", "opt2", "opt3", "opt5", "opt6"));
    }

    /**
     * The test verifies that the codomain is validated correctly.
     */
    @Test
    public void testValidationByCodomain() throws IOException {
        RulesetManagement underTest = new RulesetManagement();
        underTest.load(new File("src/test/resources/testValidationByCodomain.xml"));

        StructuralElementViewInterface sevi = underTest.getStructuralElementView("book", "", ENGL);
        List<MetadataViewWithValuesInterface<Object>> mvwviList = sevi.getSortedVisibleMetadata(Collections.emptyMap(),
            Arrays.asList("default", "defaultOpt", "anyUri", "boolean", "date", "integer", "namespaceDefault",
                "namespaceString", "namespaceDefaultOpt", "namespaceStringOpt", "namespaceDefaultExternal",
                "namespaceStringExternal"));

        SimpleMetadataViewInterface defaultMv = getSmvi(mvwviList, "default");
        assertThat(defaultMv.isValid("Hello World!"), is(true));

        SimpleMetadataViewInterface defaultOpt = getSmvi(mvwviList, "defaultOpt");
        assertThat(defaultOpt.isValid("val1"), is(true));
        assertThat(defaultOpt.isValid("val9"), is(false));

        SimpleMetadataViewInterface anyUri = getSmvi(mvwviList, "anyUri");
        assertThat(anyUri.isValid("https://www.kitodo.org/software/kitodoproduction/"), is(true));
        assertThat(anyUri.isValid("mailto:contact@kitodo.org"), is(true));
        assertThat(anyUri.isValid("urn:nbn:de-9999-12345678X"), is(true));
        assertThat(anyUri.isValid("Hello World!"), is(false));

        SimpleMetadataViewInterface booleanMv = getSmvi(mvwviList, "boolean");
        assertThat(booleanMv.isValid("on"), is(true));
        assertThat(booleanMv.isValid("Hello World!"), is(false));

        SimpleMetadataViewInterface date = getSmvi(mvwviList, "date");
        assertThat(date.isValid("1492-10-12"), is(true));
        assertThat(date.isValid("1900-02-29"), is(false));

        SimpleMetadataViewInterface integer = getSmvi(mvwviList, "integer");
        assertThat(integer.isValid("1234567"), is(true));
        assertThat(integer.isValid("1 + 1i"), is(false));

        SimpleMetadataViewInterface namespaceDefault = getSmvi(mvwviList, "namespaceDefault");
        assertThat(namespaceDefault.isValid("http://test.example/testValidation/alice"), is(true));
        assertThat(namespaceDefault.isValid("http://test.example/testValidation#bob"), is(false));
        assertThat(namespaceDefault.isValid("https://www.wdrmaus.de/"), is(false));

        SimpleMetadataViewInterface namespaceString = getSmvi(mvwviList, "namespaceString");
        assertThat(namespaceString.isValid("http://test.example/testValidation/alice"), is(true));
        assertThat(namespaceString.isValid("{http://test.example/testValidation/}bob"), is(true));
        assertThat(namespaceString.isValid("https://www.wdrmaus.de/"), is(false));

        SimpleMetadataViewInterface namespaceOpt = getSmvi(mvwviList, "namespaceDefaultOpt");
        assertThat(namespaceOpt.isValid("http://test.example/testValidation/value1"), is(true));
        assertThat(namespaceOpt.isValid("http://test.example/testValidation/value4"), is(false));

        SimpleMetadataViewInterface namespaceStrOpt = getSmvi(mvwviList, "namespaceStringOpt");
        assertThat(namespaceStrOpt.isValid("http://test.example/testValidation/value1"), is(true));
        assertThat(namespaceStrOpt.isValid("http://test.example/testValidation/value4"), is(false));

        SimpleMetadataViewInterface namespaceExt = getSmvi(mvwviList, "namespaceDefaultExternal");
        assertThat(namespaceExt.isValid("http://test.example/testValidationByCodomainNamespace#val1"), is(true));
        assertThat(namespaceExt.isValid("http://test.example/testValidationByCodomainNamespace#val4"), is(false));

        SimpleMetadataViewInterface namespaceStrExt = getSmvi(mvwviList, "namespaceStringExternal");
        assertThat(namespaceStrExt.isValid("http://test.example/testValidationByCodomainNamespace#val1"), is(true));
        assertThat(namespaceStrExt.isValid("http://test.example/testValidationByCodomainNamespace#val4"), is(false));
    }

    /**
     * The method provides a simple access to a meta-data key in a list of
     * MetadataViewWithValuesInterface.
     * 
     * @param mvwviList
     *            list of MetadataViewWithValuesInterface to extract from
     * @param keyId
     *            ID of key to extract
     * @return meta-data key
     */
    private SimpleMetadataViewInterface getSmvi(List<MetadataViewWithValuesInterface<Object>> mvwviList, String keyId) {
        return (SimpleMetadataViewInterface) mvwviList.stream().filter(mvwvi -> mvwvi.getMetadata().isPresent())
                .filter(mvwvi -> keyId.equals(mvwvi.getMetadata().get().getId())).findAny().get().getMetadata().get();
    }

    /**
     * The method provides a simple access to a meta-data key in a list of
     * MetadataViewWithValuesInterface.
     * 
     * @param metadataViewWithValuesInterfaceList
     *            list of MetadataViewWithValuesInterface to extract from
     * @param keyId
     *            ID of key to extract
     * @return meta-data key
     */
    private ComplexMetadataViewInterface getCmvi(
            List<MetadataViewWithValuesInterface<Object>> metadataViewWithValuesInterfaceList, String keyId) {
        return (ComplexMetadataViewInterface) metadataViewWithValuesInterfaceList.stream()
                .filter(mvwvi -> mvwvi.getMetadata().isPresent())
                .filter(metadataViewWithValuesInterface -> keyId
                        .equals(metadataViewWithValuesInterface.getMetadata().get().getId()))
                .findAny().get().getMetadata().get();
    }

    /**
     * Returns the IDs of the meta-data keys in a collection of meta-data view
     * interfaces.
     * 
     * @param metadataViewWithValuesInterfaceList
     *            collection of meta-data view interfaces to return the IDs of
     *            the meta-data keys from
     * @return the IDs of the meta-data keys
     */
    private List<String> ids(Collection<MetadataViewInterface> mviColl) {
        return mviColl.stream().map(metadataViewInterface -> metadataViewInterface.getId())
                .collect(Collectors.toList());
    }

    /**
     * Returns the IDs of the meta-data keys in a meta-data view with values
     * interface list.
     * 
     * @param metadataViewWithValuesInterfaceList
     *            meta-data view with values interface list to return the IDs of
     *            the meta-data keys from
     * @return the IDs of the meta-data keys
     */
    private <T> List<String> ids(List<MetadataViewWithValuesInterface<T>> metadataViewWithValuesInterfaceList) {
        return metadataViewWithValuesInterfaceList.stream()
                .filter(metadataViewWithValuesInterface -> metadataViewWithValuesInterface.getMetadata().isPresent())
                .map(metadataViewWithValuesInterface -> metadataViewWithValuesInterface.getMetadata().get().getId())
                .collect(Collectors.toList());
    }
}
