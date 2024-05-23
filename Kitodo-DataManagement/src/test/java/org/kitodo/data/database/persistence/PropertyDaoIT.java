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

package org.kitodo.data.database.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.enums.IndexAction;
import org.kitodo.data.database.enums.PropertyType;
import org.kitodo.data.database.exceptions.DAOException;

public class PropertyDaoIT {

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<Property> properties = getProperties();

        PropertyDAO propertyDAO = new PropertyDAO();
        propertyDAO.save(properties.get(0));
        propertyDAO.save(properties.get(1));
        propertyDAO.save(properties.get(2));

        assertEquals(3, propertyDAO.getAll().size(), "Objects were not saved or not found!");
        assertEquals(2, propertyDAO.getAll(1,2).size(), "Objects were not saved or not found!");

        Property foundProperty = propertyDAO.getById(1);
        assertEquals("first_property", foundProperty.getTitle(), "Object was not saved or not found!");
        assertEquals(PropertyType.UNKNOWN, foundProperty.getDataType(), "Object's type was not converted!");

        propertyDAO.remove(1);
        propertyDAO.remove(properties.get(1));
        assertEquals(1, propertyDAO.getAll().size(), "Objects were not removed or not found!");

        Exception exception = assertThrows(DAOException.class,
            () -> propertyDAO.getById(1));
        assertEquals("Object cannot be found in database", exception.getMessage());
    }

    private List<Property> getProperties() {
        Property firstProperty = new Property();
        firstProperty.setTitle("first_property");
        firstProperty.setDataType(null);
        firstProperty.setIndexAction(IndexAction.DONE);

        Property secondProperty = new Property();
        secondProperty.setTitle("second_property");
        secondProperty.setIndexAction(IndexAction.INDEX);

        Property thirdProperty = new Property();
        thirdProperty.setTitle("third_property");

        List<Property> properties = new ArrayList<>();
        properties.add(firstProperty);
        properties.add(secondProperty);
        properties.add(thirdProperty);
        return properties;
    }
}
