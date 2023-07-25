package org.kitodo.production.services.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TemplateService;

public final class ImportProcesses extends EmptyTask {
    private static final Logger logger = LogManager.getLogger(ImportProcesses.class);

    private final TemplateService templateService = ServiceManager.getTemplateService();

    private final Path importRootPath;
    private final Template templateForProcesses;
    private final Path errorPath;

    public ImportProcesses(String indir, String template, String errors) throws IllegalArgumentException {
        super(indir);
        this.importRootPath = checkIndir(indir);
        this.templateForProcesses = checkTemplate(template);
        this.errorPath = checkErros(errors);
    }

    private final Path checkIndir(String indir) {
        if (Objects.isNull(indir)) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.indir.isNull");
        }
        Path importRoot = Paths.get(indir);
        if (!Files.isDirectory(importRoot)) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.indir.isNoDirectory");
        }
        if (!Files.isExecutable(importRoot)) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.indir.cannotExecute");
        }
        return importRoot;
    }

    private final Template checkTemplate(String template) {
        if (Objects.isNull(template)) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.template.isNull");
        }
        if (!template.matches("[\\d]+")) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.template.isNoTemplateID");
        }
        Integer templateInteger = Integer.valueOf(template);
        try {
            return templateService.getById(templateInteger);
        } catch (DAOException e) {
            logger.catching(e);
            throw new IllegalArgumentException("kitodoScript.importProcesses.template.noTemplateWithID");
        }
    }

    private final Path checkErros(String errors) {
        if (Objects.isNull(errors)) {
            return null;
        }
        Path errorDir = Paths.get(errors);
        if (!Files.isDirectory(errorDir)) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.errors.isNoDirectory");
        }
        if (!Files.isWritable(errorDir)) {
            throw new IllegalArgumentException("kitodoScript.importProcesses.errors.cannotWrite");
        }
        return errorDir;
    }
}
