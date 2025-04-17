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

package org.kitodo.longtermpreservationvalidation;

import java.net.URI;
import java.util.List;

import org.kitodo.api.validation.longtermpreservation.FileType;
import org.kitodo.api.validation.longtermpreservation.LongTermPreservationValidationInterface;
import org.kitodo.api.validation.longtermpreservation.LtpValidationConditionInterface;
import org.kitodo.api.validation.longtermpreservation.LtpValidationResult;
import org.kitodo.longtermpreservationvalidation.jhove.KitodoJhoveBase;

/**
 * A LongTermPreservationValidationInterface implementation using Jhove.
 */
public class LongTermPreservationValidation implements LongTermPreservationValidationInterface {

    public LtpValidationResult validate(URI fileUri, FileType fileType, List<? extends LtpValidationConditionInterface> conditions) {
        return KitodoJhoveBase.validate(fileUri.getPath(), fileType, conditions);
    }

    public List<String> getPossibleValidationConditionProperties(FileType filetype) {
        return KitodoJhoveBase.getListOfProperties(filetype);
    }
}
