package org.kitodo.lugh;

import org.kitodo.lugh.mem.MemoryStorage;

/**
 * Configure the test package here.
 *
 * @author Matthias Ronge
 */
public class TestConfig {

    /**
     * Storage instances to run the tests against.
     */
    final static Storage[] STORAGES_TO_TEST_AGAINST = {MemoryStorage.INSTANCE };
}
