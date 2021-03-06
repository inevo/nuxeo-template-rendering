package org.nuxeo.ecm.platform.template.tests;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.junit.Test;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.template.api.TemplateInput;
import org.nuxeo.template.api.adapters.TemplateBasedDocument;
import org.nuxeo.template.processors.xdocreport.XDocReportProcessor;
import org.nuxeo.template.processors.xdocreport.ZipXmlHelper;
import static org.junit.Assert.*;

public class TestXDocReportODTProcessing extends SimpleTemplateDocTestCase {

    @Test
    public void testFileUpdateFromParams() throws Exception {

        TemplateBasedDocument adapter = setupTestDocs();
        DocumentModel testDoc = adapter.getAdaptedDoc();
        assertNotNull(testDoc);

        List<TemplateInput> params = getTestParams();

        testDoc = adapter.saveParams(TEMPLATE_NAME, params, true);
        session.save();

        XDocReportProcessor processor = new XDocReportProcessor();

        Blob newBlob = processor.renderTemplate(adapter, TEMPLATE_NAME);

        // System.out.println(((FileBlob) newBlob).getFile().getAbsolutePath());

        String xmlContent = ZipXmlHelper.readXMLContent(newBlob,
                ZipXmlHelper.OOO_MAIN_FILE);

        assertTrue(xmlContent.contains("John Smith"));
        assertTrue(xmlContent.contains("some description"));
        assertTrue(xmlContent.contains("The boolean var is false"));
        assertTrue(xmlContent.contains("xlink:href=\"Pictures/xdocreport_0.jpg\""));

        // System.out.println(xmlContent);

    }

    @Test
    public void testEmptyPicture() throws Exception {

        TemplateBasedDocument adapter = setupTestDocs();
        DocumentModel testDoc = adapter.getAdaptedDoc();
        assertNotNull(testDoc);

        // remove the picture !
        testDoc.setPropertyValue("files:files", (Serializable) null);
        testDoc = session.saveDocument(testDoc);
        session.save();

        adapter = testDoc.getAdapter(TemplateBasedDocument.class);

        List<TemplateInput> params = getTestParams();

        testDoc = adapter.saveParams(TEMPLATE_NAME, params, true);
        session.save();

        XDocReportProcessor processor = new XDocReportProcessor();

        Blob newBlob = processor.renderTemplate(adapter, TEMPLATE_NAME);

        // System.out.println(((FileBlob) newBlob).getFile().getAbsolutePath());

        String xmlContent = ZipXmlHelper.readXMLContent(newBlob,
                ZipXmlHelper.OOO_MAIN_FILE);

        assertTrue(xmlContent.contains("John Smith"));
        assertTrue(xmlContent.contains("some description"));
        assertTrue(xmlContent.contains("The boolean var is false"));
        assertFalse(xmlContent.contains("xlink:href=\"Pictures/xdocreport_0.jpg\""));

    }

    @Override
    protected Blob getTemplateBlob() {
        File file = FileUtils.getResourceFileFromContext("data/testDoc.odt");
        Blob fileBlob = new FileBlob(file);
        fileBlob.setFilename("testDoc.odt");
        return fileBlob;
    }

}
