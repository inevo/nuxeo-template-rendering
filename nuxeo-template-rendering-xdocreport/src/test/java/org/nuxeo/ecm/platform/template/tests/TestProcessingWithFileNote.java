package org.nuxeo.ecm.platform.template.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.junit.Test;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.template.api.ContentInputType;
import org.nuxeo.template.api.InputType;
import org.nuxeo.template.api.TemplateInput;
import org.nuxeo.template.api.TemplateProcessorService;
import org.nuxeo.template.api.adapters.TemplateBasedDocument;
import org.nuxeo.template.api.adapters.TemplateSourceDocument;
import org.nuxeo.template.processors.xdocreport.ZipXmlHelper;

public class TestProcessingWithFileNote extends SQLRepositoryTestCase {

    private DocumentModel templateDoc;

    private DocumentModel testDoc;

    protected static final String TEMPLATE_NAME = "mytestTemplate";

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(TestProcessingWithFileNote.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.ecm.core.api");
        deployBundle("org.nuxeo.ecm.core");
        deployBundle("org.nuxeo.ecm.core.schema");
        deployBundle("org.nuxeo.ecm.core.event");
        deployBundle("org.nuxeo.ecm.core.convert.api");
        deployBundle("org.nuxeo.ecm.platform.mimetype.api");
        deployBundle("org.nuxeo.ecm.platform.mimetype.core");
        deployBundle("org.nuxeo.ecm.core.convert");
        deployBundle("org.nuxeo.ecm.platform.convert");
        deployBundle("org.nuxeo.ecm.platform.preview");
        deployBundle("org.nuxeo.ecm.platform.dublincore");
        deployBundle("org.nuxeo.template.manager.api");
        deployBundle("org.nuxeo.template.manager");
        deployBundle("org.nuxeo.template.manager.xdocreport");
        openSession();

    }

    protected void setupTestDocs() throws Exception {

        DocumentModel root = session.getRootDocument();

        // create the template
        templateDoc = session.createDocumentModel(root.getPathAsString(),
                "templatedDoc", "TemplateSource");
        templateDoc.setProperty("dublincore", "title", "MyTemplate");
        File file = FileUtils.getResourceFileFromContext("data/Container.odt");
        Blob fileBlob = new FileBlob(file);
        fileBlob.setFilename("Container.odt");
        templateDoc.setProperty("file", "content", fileBlob);
        templateDoc.setPropertyValue("tmpl:templateName", TEMPLATE_NAME);
        templateDoc = session.createDocument(templateDoc);

        // create the note
        testDoc = session.createDocumentModel(root.getPathAsString(),
                "testDoc", "Note");
        testDoc.setProperty("dublincore", "title", "MyTestNote2");
        testDoc.setProperty("dublincore", "description", "Simple note sample");

        File mdfile = FileUtils.getResourceFileFromContext("data/MDSample.md");
        Blob mdfileBlob = new FileBlob(mdfile);

        testDoc.setPropertyValue("note:note", mdfileBlob.getString());
        testDoc.setPropertyValue("note:mime_type", "text/x-web-markdown");

        File imgFile = FileUtils.getResourceFileFromContext("data/android.jpg");
        Blob imgBlob = new FileBlob(imgFile);
        imgBlob.setFilename("android.jpg");
        imgBlob.setMimeType("image/jpeg");

        List<Map<String, Serializable>> blobs = new ArrayList<Map<String, Serializable>>();
        Map<String, Serializable> blob1 = new HashMap<String, Serializable>();
        blob1.put("file", (Serializable) imgBlob);
        blob1.put("filename", "android.jpg");
        blobs.add(blob1);

        testDoc.setPropertyValue("files:files", (Serializable) blobs);

        testDoc = session.createDocument(testDoc);
    }

    @Override
    public void tearDown() throws Exception {
        EventService eventService = Framework.getLocalService(EventService.class);
        eventService.waitForAsyncCompletion();
        closeSession();
        super.tearDown();
    }

    @Test
    public void testNoteWithMasterTemplate() throws Exception {

        setupTestDocs();

        // check the template

        TemplateSourceDocument source = templateDoc.getAdapter(TemplateSourceDocument.class);
        assertNotNull(source);

        // init params
        source.initTemplate(true);

        List<TemplateInput> params = source.getParams();
        // System.out.println(params);
        assertEquals(1, params.size());

        params.get(0).setType(InputType.Content);
        params.get(0).setSource(ContentInputType.HtmlPreview.getValue());

        templateDoc = source.saveParams(params, true);

        // associate Note to template
        TemplateBasedDocument templateBased = testDoc.getAdapter(TemplateBasedDocument.class);
        assertNull(templateBased);
        TemplateProcessorService tps = Framework.getLocalService(TemplateProcessorService.class);
        assertNotNull(tps);
        testDoc = tps.makeTemplateBasedDocument(testDoc, templateDoc, true);
        templateBased = testDoc.getAdapter(TemplateBasedDocument.class);
        assertNotNull(templateBased);

        // render
        testDoc = templateBased.initializeFromTemplate(TEMPLATE_NAME, true);
        Blob blob = templateBased.renderWithTemplate(TEMPLATE_NAME);
        assertNotNull(blob);

        assertEquals("MyTestNote2.odt", blob.getFilename());

        String xmlContent = ZipXmlHelper.readXMLContent(blob,
                ZipXmlHelper.OOO_MAIN_FILE);

        // System.out.println(xmlContent);

        // verify that XML is valid !
        DocumentHelper.parseText(xmlContent);

        // verify that note content has been merged in ODT
        assertTrue(xmlContent.contains("TemplateBasedDocument"));
        assertTrue(xmlContent.contains(testDoc.getTitle()));

        // File testFile = new File("/tmp/testOOo.odt");
        // blob.transferTo(testFile);

    }

}
