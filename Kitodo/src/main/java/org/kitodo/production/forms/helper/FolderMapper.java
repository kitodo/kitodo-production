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

package org.kitodo.production.forms.helper;

import org.kitodo.data.database.beans.Folder;
import org.kitodo.production.forms.dto.FolderDTO;

public class FolderMapper {

    /**
     * Maps an Entity to a DTO.
     */
    public static FolderDTO toDto(Folder entity) {
        FolderDTO dto = new FolderDTO();
        dto.setId(entity.getId());
        dto.setFileGroup(entity.getFileGroup());
        dto.setMimeType(entity.getMimeType());
        dto.setPath(entity.getPath());
        dto.setUrlStructure(entity.getUrlStructure());
        dto.setLinkingMode(entity.getLinkingMode());
        entity.getImageSize().ifPresent(dto::setImageSize);
        entity.getDpi().ifPresent(dto::setDpi);
        entity.getDerivative().ifPresent(dto::setDerivative);
        dto.setCopyFolder(entity.isCopyFolder());
        dto.setCreateFolder(entity.isCreateFolder());
        dto.setValidateFolder(entity.isValidateFolder());

        if (entity.getLtpValidationConfiguration() != null) {
            dto.setLtpValidationConfigurationId(entity.getLtpValidationConfiguration().getId());
        }
        return dto;
    }

    /**
     * Updates an existing Entity from a DTO.
     */
    public static void updateEntityFromDto(FolderDTO dto, Folder entity) {
        entity.setFileGroup(dto.getFileGroup());
        entity.setMimeType(dto.getMimeType());
        entity.setPath(dto.getPath());
        entity.setUrlStructure(dto.getUrlStructure());
        entity.setLinkingMode(dto.getLinkingMode());
        entity.setImageSize(dto.getImageSize());
        entity.setDpi(dto.getDpi());
        entity.setDerivative(dto.getDerivative());
        entity.setCopyFolder(dto.isCopyFolder());
        entity.setCreateFolder(dto.isCreateFolder());
        entity.setValidateFolder(dto.isValidateFolder());
    }
}
