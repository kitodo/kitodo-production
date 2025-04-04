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

package org.kitodo.data.database.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "DataEditorSetting")
@Table(name = "dataeditor_setting")
public class DataEditorSetting extends BaseBean {

    @Column(name = "user_id")
    private int userId;

    @Column(name = "task_id", nullable = true)
    private Integer taskId;

    @Column(name = "structure_width")
    private float structureWidth;

    @Column(name = "metadata_width")
    private float metadataWidth;

    @Column(name = "gallery_width")
    private float galleryWidth;

    /**
     * Constructor.
     */
    public DataEditorSetting() {
    }

    /**
     * Copy constructor (without id).
     * 
     * @param setting the data editor settings that are copied
     */
    public DataEditorSetting(DataEditorSetting setting) {
        setUserId(setting.getUserId());
        setTaskId(setting.getTaskId());
        setStructureWidth(setting.getStructureWidth());
        setMetadataWidth(setting.getMetadataWidth());
        setGalleryWidth(setting.getGalleryWidth());
    }

    /**
     * Get userId.
     *
     * @return value of userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Set userId.
     *
     * @param userId as int
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Get taskId. Either the id of the task or null, for a task-independent default layout.
     *
     * @return value of taskId
     */
    public Integer getTaskId() {
        return taskId;
    }

    /**
     * Set taskId. Either the id of the task or null, for a task-independent default layout.
     *
     * @param taskId as int
     */
    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    /**
     * Get structureWidth.
     *
     * @return value of structureWidth
     */
    public float getStructureWidth() {
        return structureWidth;
    }

    /**
     * Set structureWidth.
     *
     * @param structureWidth as float
     */
    public void setStructureWidth(float structureWidth) {
        this.structureWidth = structureWidth;
    }

    /**
     * Get metadataWidth.
     *
     * @return value of metadataWidth
     */
    public float getMetadataWidth() {
        return metadataWidth;
    }

    /**
     * Set metadataWidth.
     *
     * @param metadataWidth as float
     */
    public void setMetadataWidth(float metadataWidth) {
        this.metadataWidth = metadataWidth;
    }

    /**
     * Get galleryWidth.
     *
     * @return value of galleryWidth
     */
    public float getGalleryWidth() {
        return galleryWidth;
    }

    /**
     * Set galleryWidth.
     *
     * @param galleryWidth as float
     */
    public void setGalleryWidth(float galleryWidth) {
        this.galleryWidth = galleryWidth;
    }
}
