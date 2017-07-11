package org.kitodo.impl.docket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.kitodo.api.docket.DocketData;
import org.kitodo.api.docket.DocketInterface;

public class DocketImpl implements DocketInterface {

    @Override
    public File generateDocket(DocketData docketData, URI xslFileUri) throws IOException {
        ExportDocket exportDocket = new ExportDocket();

        File file = new File("docket.pdf");
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        exportDocket.startExport(docketData, fileOutputStream, new File(xslFileUri));

        return file;
    }

    @Override
    public File generateMultipleDockets(Collection<DocketData> docketData, URI xslFileUri) throws IOException {
        ExportDocket exportDocket = new ExportDocket();

        File file = new File("docket_multipage.pdf");
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        exportDocket.startExport(docketData, fileOutputStream, new File(xslFileUri));

        return file;
    }
}
