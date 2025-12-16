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

package org.kitodo.production.forms.user;

import static java.util.Map.entry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.BaseForm;
import org.kitodo.production.forms.dataeditor.GalleryViewMode;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.metadata.pagination.PaginatorType;

@Named("UserEditViewMetadataTab")
@ViewScoped
public class UserEditViewMetadataTab extends BaseForm {

    /**
     * The user object that is being edited (variable "user" references to the user currently logged in, see BaseForm).
     */
    private User userObject;

    private static final Logger logger = LogManager.getLogger(UserEditViewMetadataTab.class);

    private static final List<String> AVAILABLE_SHORTCUTS = Arrays.asList(
            "detailView",
            "help",
            "nextItem",
            "nextItemMulti",
            "previousItem",
            "previousItemMulti",
            "structuredView",
            "downItem",
            "downItemMulti",
            "upItem",
            "upItemMulti");

    private static final LinkedHashMap<String, PaginatorType> paginationTypes = new LinkedHashMap<>(Map.ofEntries(
            entry("arabic", PaginatorType.ARABIC),
            entry("roman", PaginatorType.ROMAN),
            entry("alphabetic", PaginatorType.ALPHABETIC),
            entry("uncounted", PaginatorType.UNCOUNTED),
            entry("paginationFreetext", PaginatorType.FREETEXT),
            entry("paginationAdvanced", PaginatorType.ADVANCED)
    ));

    private SortedMap<String, String> shortcuts;

    /**
     * Return user object currently being edited.
     * 
     * @return the user currently being edited
     */
    public User getUserObject() {
        return this.userObject;
    }

    /**
     * Get shortcuts.
     *
     * @return value of shortcuts
     */
    public SortedMap<String, String> getShortcuts() {
        return shortcuts;
    }

    /**
     * Method that is called from viewAction of user edit form.
     *
     * @param userObject
     *            the user currently being edited
     */
    public void load(User userObject) {
        this.userObject = userObject;
        try {
            if (Objects.nonNull(this.userObject) && StringUtils.isNotBlank(this.userObject.getShortcuts())) {
                shortcuts = mapShortcuts(new ObjectMapper().readValue(this.userObject.getShortcuts(),
                        new TypeReference<TreeMap<String, String>>() {}));
            } else {
                shortcuts = mapShortcuts(new TreeMap<>());
            }
        } catch (IOException e) {
            Helper.setErrorMessage("Could not parse shortcuts loaded from user with id " + user.getId() + "!", logger, e);
        }
    }

    /**
     * Save metadata tab information for user.
     *
     * @return true if information can be saved and was updated on user object
     */
    public boolean save() {

        try {
            ObjectMapper mapper = new ObjectMapper();
            this.userObject.setShortcuts(mapper.writeValueAsString(shortcuts));
        } catch (IOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.USER.getTranslationSingular()}, logger, e);
            return false;
        }

        return true;
    }

    /**
     * Get map of available pagination types.
     *
     * @return a map where keys are instances of PaginatorType and values are their
     *         respective descriptions as strings
     */
    public Map<String, PaginatorType> getPaginationTypes() {
        return  paginationTypes.entrySet().stream()
                .map(e -> entry(Helper.getTranslation(e.getKey()), e.getValue()))
                .sorted(Map.Entry.comparingByKey(Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /**
     * Get map of supported metadata languages.
     *
     * @return map of supported metadata languages
     */
    public Map<String, String> getMetadataLanguages() {
        Map<String, String> metadataLanguages = new HashMap<>();
        String[] availableMetadataLanguages = ConfigCore.getStringArrayParameter(ParameterCore.METADATA_LANGUAGE_LIST);
        for (String availableLanguage : availableMetadataLanguages) {
            String[] language = availableLanguage.split("-");
            metadataLanguages.put(language[0], language[1]);
        }
        return metadataLanguages;
    }

    /**
     * Return sorted list of all available shortcuts by name (even shortcuts that are not assigned yet).
     * 
     * @param loadedShortcuts the shortcuts of a user
     * @return the sorted list of all shortcuts
     */
    public static SortedMap<String, String> mapShortcuts(Map<String, String> loadedShortcuts) {
        SortedMap<String, String> shortcuts = new TreeMap<>();
        for (String shortcut : AVAILABLE_SHORTCUTS) {
            shortcuts.put(shortcut, loadedShortcuts.getOrDefault(shortcut, ""));
        }
        return shortcuts;
    }

    /**
     * Get gallery view modes.
     *
     * @return list of Strings
     */
    public List<String> getGalleryViewModes() {
        return GalleryViewMode.getGalleryViewModes();
    }

    /**
     * Get translation of GalleryViewMode with given enum value 'galleryViewMode'.
     * 
     * @param galleryViewModeValue enum value of GalleryViewMode
     * @return translation of GalleryViewMode
     */
    public String getGalleryViewModeTranslation(String galleryViewModeValue) {
        return GalleryViewMode.getByName(galleryViewModeValue).getTranslation();
    }

}
