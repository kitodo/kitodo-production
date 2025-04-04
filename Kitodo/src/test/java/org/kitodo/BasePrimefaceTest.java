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

package org.kitodo;

import static org.mockito.Mockito.when;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class BasePrimefaceTest {

    @Mock
    protected FacesContext facesContext;

    @Mock
    protected ExternalContext externalContext;


    @BeforeEach
    public void initPrimeface() {
        when(facesContext.getExternalContext()).thenReturn(externalContext);
    }
}
