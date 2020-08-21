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

package org.kitodo.production.services.index;

import javax.faces.push.PushContext;

import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;

public class IndexAllThread extends Thread {

    private final PushContext context;
    private final IndexingService indexingService;

    IndexAllThread(PushContext pushContext, IndexingService service) {
        context = pushContext;
        indexingService = service;
    }

    @Override
    public void run() {
        indexingService.setIndexingAll(true);

        for (ObjectType objectType : ObjectType.getIndexableObjectTypes()) {
            try {
                indexingService.startIndexing(objectType, context);
            } catch (DataException | CustomResponseException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), IndexingService.getLogger(), e);
                Thread.currentThread().interrupt();
            }
        }
        try {
            sleep(IndexingService.PAUSE);
        } catch (InterruptedException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), IndexingService.getLogger(), e);
            Thread.currentThread().interrupt();
        }

        indexingService.resetCurrentIndexState();
        indexingService.setIndexingAll(false);

        context.send(IndexingService.INDEXING_FINISHED_MESSAGE);
    }
}
