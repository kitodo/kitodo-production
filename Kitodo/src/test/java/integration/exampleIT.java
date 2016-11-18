package integration;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by al-huber on 11.11.2016.
 */
public class exampleIT {

    final static Logger logger = LoggerFactory.getLogger(exampleIT.class);

    @Test
    public void exampleIntegration(){
        logger.info("integration tests are running (logger is working too ;) )");

    }

}
