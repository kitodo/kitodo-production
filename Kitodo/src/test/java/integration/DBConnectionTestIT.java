package integration;

/**
 * Created by al-huber on 18.11.2016.
 */

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.persistence.ProzessDAO;
import org.junit.Assert;
import org.junit.Test;

public class DBConnectionTestIT {

    @Test
    public void test() throws Exception {

        Prozess test = new Prozess();
        test.setTitel("TestTitle");
        ProzessDAO dao = new ProzessDAO();
        dao.save(test);

        long counted = dao.count("from Prozess");
        Assert.assertNotNull("No Prozess found",counted);
        Assert.assertEquals(1, counted);

        String title = dao.get(1).getTitel();
        Assert.assertEquals("TestTitle", title);

    }
}