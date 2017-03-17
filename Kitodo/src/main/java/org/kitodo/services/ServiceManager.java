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

package org.kitodo.services;

import org.kitodo.services.data.BatchService;
import org.kitodo.services.data.DocketService;
import org.kitodo.services.data.HistoryService;
import org.kitodo.services.data.LdapGroupService;
import org.kitodo.services.data.ProcessPropertyService;
import org.kitodo.services.data.ProcessService;
import org.kitodo.services.data.ProjectFileGroupService;
import org.kitodo.services.data.ProjectService;
import org.kitodo.services.data.RulesetService;
import org.kitodo.services.data.TaskService;
import org.kitodo.services.data.TemplatePropertyService;
import org.kitodo.services.data.TemplateService;
import org.kitodo.services.data.UserGroupService;
import org.kitodo.services.data.UserPropertyService;
import org.kitodo.services.data.UserService;
import org.kitodo.services.data.WorkpiecePropertyService;
import org.kitodo.services.data.WorkpieceService;

public class ServiceManager {

    private BatchService batchService = new BatchService();
    private DocketService docketService = new DocketService();
    private HistoryService historyService = new HistoryService();
    private LdapGroupService ldapGroupService = new LdapGroupService();
    private ProcessPropertyService processPropertyService = new ProcessPropertyService();
    private ProcessService processService = new ProcessService();
    private ProjectFileGroupService projectFileGroupService = new ProjectFileGroupService();
    private ProjectService projectService = new ProjectService();
    private RulesetService rulesetService = new RulesetService();
    private TaskService taskService = new TaskService();
    private TemplatePropertyService templatePropertyService = new TemplatePropertyService();
    private TemplateService templateService = new TemplateService();
    private UserGroupService userGroupService = new UserGroupService();
    private UserPropertyService userPropertyService = new UserPropertyService();
    private UserService userService = new UserService();
    private WorkpiecePropertyService workpiecePropertyService = new WorkpiecePropertyService();
    private WorkpieceService workpieceService = new WorkpieceService();
}
