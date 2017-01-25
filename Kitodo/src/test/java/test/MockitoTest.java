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

package test;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import de.sub.goobi.converter.ProcessConverter;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Matchers;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ProcessService;

public class MockitoTest {

    /*@Test
    public void testMock() throws DAOException{
        ProcessService processService = mock(ProcessService.class);
        Process newProzess = new Process();

        ProcessConverter converter = spy(new ProcessConverter());
        when(converter.getProzessService()).thenReturn(processService);
        when(processService.find(any(Integer.class))).thenReturn(newProzess);

        Object nullObject = converter.getAsObject(null,null, null);
        Assert.assertNull(nullObject);
        Object object = converter.getAsObject(null, null, "1");
        Assert.assertEquals(newProzess, object);

    }*/

}
