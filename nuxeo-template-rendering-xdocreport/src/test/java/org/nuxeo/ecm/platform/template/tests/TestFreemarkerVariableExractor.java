package org.nuxeo.ecm.platform.template.tests;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.runtime.test.NXRuntimeTestCase;
import org.nuxeo.template.XMLSerializer;
import org.nuxeo.template.api.TemplateInput;
import org.nuxeo.template.processors.xdocreport.XDocReportProcessor;

public class TestFreemarkerVariableExractor extends NXRuntimeTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.template.manager.api");
        deployContrib("org.nuxeo.template.manager",
                "OSGI-INF/templateprocessor-service.xml");
        deployContrib("org.nuxeo.template.manager",
                "OSGI-INF/templateprocessor-contrib.xml");
    }

    @Test
    public void testDocXParamExtraction() throws Exception {
        XDocReportProcessor processor = new XDocReportProcessor();
        File file = FileUtils.getResourceFileFromContext("data/testDoc.docx");

        List<TemplateInput> inputs = processor.getInitialParametersDefinition(new FileBlob(
                file));

        // String[] expectedVars = new String[]{"StringVar","DateVar",
        // "Description","picture", "BooleanVar"};
        String[] expectedVars = new String[] { "StringVar", "DateVar",
                "Description", "BooleanVar" };

        assertEquals(expectedVars.length, inputs.size());
        for (String expected : expectedVars) {
            boolean found = false;
            for (TemplateInput input : inputs) {
                if (expected.equals(input.getName())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        String xmlParams = XMLSerializer.serialize(inputs);

        for (TemplateInput input : inputs) {
            assertTrue(xmlParams.contains("name=\"" + input.getName() + "\""));
        }

        List<TemplateInput> inputs2 = XMLSerializer.readFromXml(xmlParams);

        assertEquals(inputs.size(), inputs2.size());
        for (TemplateInput input : inputs) {
            boolean found = false;
            for (TemplateInput input2 : inputs2) {
                if (input2.getName().equals(input.getName())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

    @Test
    public void testODTParamExtraction() throws Exception {
        XDocReportProcessor processor = new XDocReportProcessor();
        File file = FileUtils.getResourceFileFromContext("data/testDoc.odt");

        List<TemplateInput> inputs = processor.getInitialParametersDefinition(new FileBlob(
                file));

        String[] expectedVars = new String[] { "StringVar", "DateVar",
                "Description", "BooleanVar" };

        assertEquals(expectedVars.length, inputs.size());
        for (String expected : expectedVars) {
            boolean found = false;
            for (TemplateInput input : inputs) {
                if (expected.equals(input.getName())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }

        String xmlParams = XMLSerializer.serialize(inputs);

        for (TemplateInput input : inputs) {
            assertTrue(xmlParams.contains("name=\"" + input.getName() + "\""));
        }

        List<TemplateInput> inputs2 = XMLSerializer.readFromXml(xmlParams);

        assertEquals(inputs.size(), inputs2.size());
        for (TemplateInput input : inputs) {
            boolean found = false;
            for (TemplateInput input2 : inputs2) {
                if (input2.getName().equals(input.getName())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

}
