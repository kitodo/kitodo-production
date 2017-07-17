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

package de.sub.goobi.forms;

import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.services.ServiceManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.IOException;

/**
 * Created by solth on 17.07.2017.
 */
@Named
@ApplicationScoped
public class IndexingForm {

    private transient ServiceManager serviceManager = new ServiceManager();

    public void startBatchIndexing() throws InterruptedException, CustomResponseException, IOException {
        serviceManager.getBatchService().addAllObjectsToIndex();
    }

    public void startDocketIndexing() throws InterruptedException, CustomResponseException, IOException {
        serviceManager.getDocketService().addAllObjectsToIndex();
    }

    public void startHistoryIndexing() throws InterruptedException, CustomResponseException, IOException {
        serviceManager.getHistoryService().addAllObjectsToIndex();
    }

    public void startProcessIndexing() throws InterruptedException, CustomResponseException, IOException {
        serviceManager.getProcessService().addAllObjectsToIndex();
    }

    public void startProjectIndexing() throws InterruptedException, CustomResponseException, IOException {
        serviceManager.getProjectService().addAllObjectsToIndex();
    }

    public void startPropertyIndexing() throws InterruptedException, CustomResponseException, IOException {
        serviceManager.getPropertyService().addAllObjectsToIndex();
    }

    public void startRulesetIndexing() throws InterruptedException, CustomResponseException, IOException {
        serviceManager.getRulesetService().addAllObjectsToIndex();
    }

    public void startTaskIndexing() throws InterruptedException, CustomResponseException, IOException {
        serviceManager.getTaskService().addAllObjectsToIndex();
    }

    public void startTemplateIndexing() throws InterruptedException, CustomResponseException, IOException {
        serviceManager.getTemplateService().addAllObjectsToIndex();
    }

    public void startUserGroupIndexing() throws InterruptedException, CustomResponseException, IOException {
        serviceManager.getUserGroupService().addAllObjectsToIndex();
    }

    public void startUserIndexing() throws InterruptedException, CustomResponseException, IOException {
        serviceManager.getUserService().addAllObjectsToIndex();
    }

    public void startWorkpieceIndexing() throws InterruptedException, CustomResponseException, IOException {
        serviceManager.getWorkpieceService().addAllObjectsToIndex();
    }

    public void startAllIndexing() throws InterruptedException, CustomResponseException, IOException {
        this.startBatchIndexing();
        this.startDocketIndexing();
        this.startHistoryIndexing();
        this.startProcessIndexing();
        this.startProcessIndexing();
        this.startProjectIndexing();
        this.startPropertyIndexing();
        this.startRulesetIndexing();
        this.startTaskIndexing();
        this.startTemplateIndexing();
        this.startUserGroupIndexing();
        this.startUserIndexing();
        this.startWorkpieceIndexing();
    }
}
