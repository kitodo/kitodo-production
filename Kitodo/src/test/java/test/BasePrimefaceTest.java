package test;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.primefaces.config.PrimeEnvironment;
import org.primefaces.context.PrimeApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public abstract class BasePrimefaceTest {

    @Mock
    protected FacesContext facesContext;

    @Mock
    protected ExternalContext externalContext;

    @Mock
    protected PrimeApplicationContext primeApplicationContext;

    @Mock
    protected PrimeEnvironment primeEnvironment;

    @Before
    public void setup() throws NoSuchMethodException {
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(primeApplicationContext.getEnvironment()).thenReturn(primeEnvironment);

        Map<String, Object> applicationMap = new HashMap<>();
        applicationMap.put(PrimeApplicationContext.INSTANCE_KEY,primeApplicationContext);
        when(externalContext.getApplicationMap()).thenReturn(applicationMap);
    }
}
