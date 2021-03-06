package org.nuxeo.ecm.platform.template.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.template.api.TemplateInput;
import org.nuxeo.template.api.adapters.TemplateBasedDocument;

import static org.junit.Assert.*;

public class TestFMTextProcessing extends SimpleTemplateDocTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.ecm.platform.mimetype.api");
        deployBundle("org.nuxeo.ecm.platform.mimetype.core");
    }

    @Test
    public void testDocumentsAttributes() throws Exception {
        TemplateBasedDocument adapter = setupTestDocs();
        DocumentModel testDoc = adapter.getAdaptedDoc();
        assertNotNull(testDoc);

        List<TemplateInput> params = new ArrayList<TemplateInput>();
        TemplateInput input = new TemplateInput("variable1", "YoVar1");
        params.add(input);

        testDoc = adapter.saveParams(TEMPLATE_NAME, params, true);
        session.save();

        String processorType = adapter.getSourceTemplate(TEMPLATE_NAME).getTemplateType();
        assertEquals("Freemarker", processorType);

        Blob newBlob = adapter.renderWithTemplate(TEMPLATE_NAME);

        String txtContent = newBlob.getString();

        assertTrue(newBlob.getMimeType().equals("text/plain"));
        assertTrue(newBlob.getFilename().endsWith(".txt"));

        assertTrue(txtContent.contains(testDoc.getTitle()));
        assertTrue(txtContent.contains(testDoc.getId()));
        assertTrue(txtContent.contains("YoVar1"));

    }

    @Override
    protected Blob getTemplateBlob() {
        File file = FileUtils.getResourceFileFromContext("data/testText.ftl");
        Blob fileBlob = new FileBlob(file);
        fileBlob.setFilename("testText.ftl");
        return fileBlob;
    }

}
