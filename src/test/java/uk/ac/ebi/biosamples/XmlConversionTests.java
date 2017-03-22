package uk.ac.ebi.biosamples;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Node;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.xpath.JAXPXPathEngine;
import org.xmlunit.xpath.XPathEngine;
import uk.ac.ebi.biosamples.model.entities.Sample;
import uk.ac.ebi.biosamples.service.SamplesResourceService;
import uk.ac.ebi.biosamples.service.XmlService;

import javax.xml.transform.Source;
import java.io.File;
import java.net.URL;
import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( classes = Application.class, initializers = ConfigFileApplicationContextInitializer.class)
@Ignore
//@SpringBootTest
public class XmlConversionTests {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private XmlService xmlService;

    @Autowired
    private SamplesResourceService samplesResourceService;

    @Autowired
    private SamplesResourceService iteratorService;

    private Sample testSample;
    private Sample manualTestSample;
    private Source sampleSource;
    private Source documentSource;
    private Source manualTestDocumentSource;
    private Source testSource;
    private final XPathEngine xpath = new JAXPXPathEngine();
    private final String sampleAccession = "SAMEA2590957";
    private final String manualTestSampleAccession = "SAME44348";
    private ElementSelector selector;



    @Before
    public void initTest() {
        String testDocumentPath = "/document.test.xml";
        URL fileUrl = this.getClass().getResource(testDocumentPath);

        File testFile = new File(fileUrl.getFile());
        testSample = samplesResourceService.getSample(sampleAccession).getContent();
        Element sampleEntry = xmlService.getEntryForSample(testSample);
        Document doc = xmlService.produceDocumentForEntries(asList(sampleEntry));
        Document manualTestDoc = xmlService.produceDocumentForEntries(
                asList(xmlService.getEntryForSample(
                        samplesResourceService.getSample(manualTestSampleAccession).getContent()
                ))
        );

        org.w3c.dom.Document w3cDoc = null;
//        org.w3c.dom.Document w3cManualTestDoc = null;
//        try {
//            w3cDoc = new DOMOutputter().output(doc);
//            w3cManualTestDoc = new DOMOutputter().output(manualTestDoc);
//        } catch (JDOMException e) {
//            e.printStackTrace();
//        }


        sampleSource = Input.fromString(xmlService.prettyPrint(sampleEntry)).build();
        testSource = Input.fromFile(testFile).build();
        
//        documentSource = Input.fromDocument(w3cDoc).build();
//        manualTestDocumentSource = Input.fromDocument(w3cManualTestDoc).build();

//        selector = ElementSelectors.conditionalBuilder()
//                .whenElementIsNamed("field").thenUse(new FieldNodeMatcher())
//                .build();
    }

    @Test
    public void accessionElement() {

        Iterable<Node> allMatches = xpath.selectNodes("/entry", sampleSource);
        Iterator<Node> it = allMatches.iterator();
        assertThat(it.hasNext()).withFailMessage("Entry should exist");
        Node node = it.next();
        String id = xpath.evaluate("@id", node);
        assertThat(id).isNotNull().isEqualTo(sampleAccession).withFailMessage("Entry id " + id + " is not the expected");
        assertThat(it.hasNext()).isFalse().withFailMessage("Only one entry per element");
    }

    @Test
    public void countCharacteristics() {
        Iterable<Node> allMatches = xpath.selectNodes("//field", sampleSource);
        assertThat(allMatches).isNotNull().hasSize(8).withFailMessage("Entry should have 8 fields");
    }

    @Test
    public void checkDates() {
        Iterable<Node> matchingDates = xpath.selectNodes("//date", sampleSource);
        assertThat(matchingDates).isNotNull().hasSize(2).withFailMessage("Entry should have 2 date fields");
        String releaseDate = xpath.evaluate("//date[@type='release']/@value", sampleSource);
        String updateDate = xpath.evaluate("//date[@type='update']/@value", sampleSource);
        assertThat(releaseDate).isNotEmpty().isEqualTo(testSample.getReleaseDate());
        assertThat(updateDate).isNotEmpty().isEqualTo(testSample.getUpdateDate());
    }

//    @Test
//    @Ignore
//    public void compareEntries() {
//
//        Diff diff = DiffBuilder.compare(manualTestDocumentSource)
//                .withTest(testSource)
//                .checkForSimilar()
//                .ignoreWhitespace()
//                .normalizeWhitespace()
//                .withNodeFilter(new NodeFilters())
//                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes))
//                .build();
//        try {
//            assertThat(diff.hasDifferences())
//                    .as("Check build and test document aren't different")
//                    .isFalse();
//        } catch (AssertionError e) {
//            diff.getDifferences().forEach(d -> {
//                log.warn("A difference has been found:\n"+d.toString());
//            });
//            throw e;
//        }
//    }


}
