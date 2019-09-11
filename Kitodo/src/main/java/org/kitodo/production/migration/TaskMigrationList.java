package org.kitodo.production.migration;

import java.util.ArrayList;
import java.util.Iterator;

import org.kitodo.data.database.beans.Task;

public class TaskMigrationList extends ArrayList<Task> {

    @Override
    public boolean equals(Object o) {
        TaskComparator taskComparator = new TaskComparator();

        Iterator<Task> firstTaskIterator = this.iterator();
        Iterator<Task> secondTaskIterator = ((TaskMigrationList) o).iterator();
        while (firstTaskIterator.hasNext()) {
            Task firstTask = firstTaskIterator.next();
            Task secondTask = secondTaskIterator.next();
            if (taskComparator.compare(firstTask, secondTask) != 0) {
                return false;
            }
        }
        return true;
    }
}
