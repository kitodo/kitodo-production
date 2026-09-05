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

import java.util.Date;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.kitodo.api.logintask.LoginTaskStatus;
import org.kitodo.api.logintask.LoginTaskType;

/**
 * Storing login tasks for users (e.g. saving credentials to ldpa server, resetting password).
 */
@Entity
@Table(name = "logintask")
public class LoginTask extends BaseBean {

    @Column(name = "type", columnDefinition = "VARCHAR", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoginTaskType type;

    @Column(name = "status", columnDefinition = "VARCHAR", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoginTaskStatus status;

    @Column(name = "createdAt", nullable = false)
    private Date createdAt;

    @Column(name = "executedAt", nullable = true)
    private Date executedAt;

    @Column(name = "error", nullable = true)
    private String error;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_logintask_user_id"))
    private User user;

    /**
     * Returns the type of the login task.
     * 
     * @return the type of the login task
     */
    public LoginTaskType getType() {
        return type;
    }

    /**
     * Set the type of the login task.
     * 
     * @param type the type of the login task
     */
    public void setType(LoginTaskType type) {
        this.type = type;
    }

    /**
     * Returns the status of the login task.
     * 
     * @return the status of the login task
     */
    public LoginTaskStatus getStatus() {
        return status;
    }

    /**
     * Set the status of the login task.
     * 
     * @param status the status of the login task
     */
    public void setStatus(LoginTaskStatus status) {
        this.status = status;
    }

    /**
     * Returns the creation date of the login task.
     * 
     * @return the creation date of the login task
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Set the creation date of the login task.
     * 
     * @param createdAt the creation date of the login task
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the execution date of the login task.
     * 
     * @return the execution date of the login task
     */
    public Date getExecutedAt() {
        return executedAt;
    }

    /**
     * Set the execution date of the login task.
     * 
     * @param executedAt the execution date of the login task
     */
    public void setExecutedAt(Date executedAt) {
        this.executedAt = executedAt;
    }

    /**
     * Returns the error message of the login task in case it failed.
     * 
     * @return the error message of the login task
     */
    public String getError() {
        return error;
    }

    /**
     * Set the error message of the login task in case it failed.
     * 
     * @param error the error message of the login task
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Returns the user associated with the login task.
     * 
     * @return the user associated with the login task
     */
    public User getUser() {
        return user;
    }

    /**
     * Set the user associated with the login task.
     * 
     * @param user the error message of the login task
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Equals implementation based on the database id.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof LoginTask) {
            LoginTask loginTask = (LoginTask) object;
            return Objects.nonNull(this.getId()) && Objects.nonNull(loginTask.getId())
                    && Objects.equals(this.getId(), loginTask.getId());
        }

        return false;
    }

    /**
     * Hash code implementation based on all properties of this condition.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, type, status, createdAt, executedAt);
    }
}
