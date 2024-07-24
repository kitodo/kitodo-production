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

package org.kitodo.data.database.beans;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.hibernate.Hibernate;
import org.kitodo.data.database.persistence.BaseDAO;

/**
 * Base bean class.
 */
@MappedSuperclass
public abstract class BaseBean implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @SuppressWarnings("unchecked")
    void initialize(BaseDAO baseDAO, List<? extends BaseBean> list) {
        if (Objects.nonNull(this.id) && !Hibernate.isInitialized(list)) {
            baseDAO.initialize(this, list);
        }
    }
}
