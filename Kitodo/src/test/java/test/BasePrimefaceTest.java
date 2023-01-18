package test;

import static org.mockito.Mockito.when;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class BasePrimefaceTest extends BaseMockitoTest{

    @Mock
    protected FacesContext facesContext;

    @Mock
    protected ExternalContext externalContext;


    @Before
    public void initPrimeface() {
        when(facesContext.getExternalContext()).thenReturn(externalContext);
    }
}
