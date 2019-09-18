package org.kitodo.production.migration;

import java.util.Comparator;
import java.util.Objects;

import org.kitodo.data.database.beans.Template;

public class TemplateComparator implements Comparator<Template> {

    @Override
    public int compare(Template firstTemplate, Template secondTemplate) {
        if (Objects.isNull(firstTemplate) || Objects.isNull(secondTemplate)) {
            return 1;
        }
        if (!firstTemplate.getWorkflow().getId().equals(secondTemplate.getWorkflow().getId())) {
            return 1;
        }
        if (!firstTemplate.getRuleset().getId().equals(secondTemplate.getRuleset().getId())) {
            return 1;
        }
        if (!firstTemplate.getDocket().getId().equals(secondTemplate.getDocket().getId())) {
            return 1;
        }
        return 0;
    }
}
