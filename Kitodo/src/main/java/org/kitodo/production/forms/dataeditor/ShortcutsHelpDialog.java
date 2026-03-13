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

package org.kitodo.production.forms.dataeditor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.forms.BaseForm;
import org.kitodo.production.forms.user.UserEditViewMetadataTab;
import org.kitodo.production.helper.Helper;

@Named("ShortcutsHelpDialog")
@ViewScoped
public class ShortcutsHelpDialog extends BaseForm {
    
    private static final Logger logger = LogManager.getLogger(ShortcutsHelpDialog.class);

    private SortedMap<String, String> shortcuts;

    /**
     * Initialize ShortcutsHelpDialog.
     */
    @PostConstruct
    public void init() {
        try {
            if (Objects.nonNull(user) && StringUtils.isNotBlank(user.getShortcuts())) {
                shortcuts = UserEditViewMetadataTab.mapShortcuts(new ObjectMapper().readValue(user.getShortcuts(),
                        new TypeReference<TreeMap<String, String>>() {}));
            } else {
                shortcuts = UserEditViewMetadataTab.mapShortcuts(new TreeMap<>());
            }
        } catch (IOException e) {
            Helper.setErrorMessage("Could not parse shortcuts loaded from user with id " + user.getId() + "!", logger, e);
        }
    }

    /**
     * Get shortcuts.
     *
     * @return value of shortcuts
     */
    public SortedMap<String, String> getShortcuts() {
        return shortcuts;
    }

}
