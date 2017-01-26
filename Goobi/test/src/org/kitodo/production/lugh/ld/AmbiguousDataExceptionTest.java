package org.kitodo.production.lugh.ld;

import org.junit.Test;

public class AmbiguousDataExceptionTest {

    @Test(expected = AmbiguousDataException.class)
    public void testAmbiguousDataExceptionCanBeThrown() throws AmbiguousDataException {
        throw new AmbiguousDataException();
    }

}
