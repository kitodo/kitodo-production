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

package org.kitodo.production.services.dataeditor;

import java.util.Objects;

import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class RulesetManagementService {
    private static volatile RulesetManagementService instance = null;
    private KitodoServiceLoader<RulesetManagementInterface> rulesetManagementLoader;

    /**
     * Return singleton variable of type MetsService.
     *
     * @return unique instance of MetsService
     */
    public static RulesetManagementService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (RulesetManagementService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new RulesetManagementService();
                }
            }
        }
        return instance;
    }
    
    private RulesetManagementService() {
        rulesetManagementLoader = new KitodoServiceLoader<>(RulesetManagementInterface.class);
    }

    /**
     * Returns a new ruleset management.
     * 
     * @return a new ruleset management
     */
    public RulesetManagementInterface getRulesetManagement() {
        return rulesetManagementLoader.loadModule();
    }
}
