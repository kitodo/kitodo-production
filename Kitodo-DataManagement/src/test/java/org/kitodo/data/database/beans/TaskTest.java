package org.kitodo.data.database.beans;

import org.junit.Assert;
import org.junit.Test;

public class TaskTest {

    @Test
    public void testGetListOfPaths() {
        Task task = new Task();

        Assert.assertTrue(task.getListOfPaths().isEmpty());

        task.setScriptName1("ocr");

        Assert.assertEquals("ocr", task.getListOfPaths());

        task.setScriptName2("empty");

        Assert.assertEquals("ocr; empty", task.getListOfPaths());

        task.setScriptName2(null);

        Assert.assertEquals("ocr", task.getListOfPaths());

        task.setScriptName2("");

        Assert.assertEquals("ocr", task.getListOfPaths());

        task.setScriptName2("     ");

        Assert.assertEquals("ocr", task.getListOfPaths());

    }

}
