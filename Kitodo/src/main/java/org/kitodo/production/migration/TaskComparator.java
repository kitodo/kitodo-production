package org.kitodo.production.migration;

import org.kitodo.data.database.beans.Task;

import java.util.Comparator;
import java.util.Objects;

public class TaskComparator implements Comparator<Task> {

    @Override
    public int compare(Task firstTask, Task secondTask) {
        if (Objects.isNull(firstTask) || Objects.isNull(secondTask)) {
            return 1;
        }
        if(Objects.isNull(firstTask.getTitle()) ? !Objects.isNull(secondTask.getTitle()) : !firstTask.getTitle().equals(secondTask.getTitle())){
            return 1;
        }
        if(Objects.isNull(firstTask.getOrdering()) ? !Objects.isNull(secondTask.getOrdering()) : !firstTask.getOrdering().equals(secondTask.getOrdering())){
            return 1;
        }
        if(!firstTask.isTypeAutomatic() == secondTask.isTypeAutomatic()){
            return 1;
        }
        if(!firstTask.isTypeMetadata() == secondTask.isTypeMetadata()){
            return 1;
        }
        if(!firstTask.isTypeImagesRead() == secondTask.isTypeImagesRead()){
            return 1;
        }
        if(!firstTask.isTypeImagesWrite() == secondTask.isTypeImagesWrite()){
            return 1;
        }
        if(!firstTask.isTypeExportDMS() == secondTask.isTypeExportDMS()){
            return 1;
        }
        if(!firstTask.isTypeAcceptClose() == secondTask.isTypeAcceptClose()){
            return 1;
        }
        if(!firstTask.isTypeCloseVerify() == secondTask.isTypeCloseVerify()){
            return 1;
        }
        if(Objects.isNull(firstTask.getScriptPath()) ? !Objects.isNull(secondTask.getScriptPath()) : !firstTask.getScriptPath().equals(secondTask.getScriptPath())){
            return 1;
        }
        if(!firstTask.isBatchStep() == secondTask.isBatchStep()){
            return 1;
        }
        return 0;
    }
}
