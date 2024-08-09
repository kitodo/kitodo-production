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

package org.kitodo.production.services.dataeditor;

import java.util.Objects;

import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class RulesetManagementService {
    private static volatile RulesetManagementService instance = null;
    private final KitodoServiceLoader<RulesetManagementInterface> rulesetManagementLoader;

    /**
     * Return singleton variable of type MetsService.
     *
     * @return unique instance of MetsService
     */
    public static RulesetManagementService getInstance() {
        RulesetManagementService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (RulesetManagementService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new RulesetManagementService();
                    instance = localReference;
                }
            }
        }
        return localReference;
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
