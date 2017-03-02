package uk.ac.ebi.biosamples.service;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biosamples.model.entities.BioSamplesCharacteristic;
import uk.ac.ebi.biosamples.model.entities.Group;
import uk.ac.ebi.biosamples.model.entities.Sample;
import uk.ac.ebi.biosamples.model.entities.BioSamplesRelation;
import uk.ac.ebi.biosamples.model.enums.BioSamplesRelationType;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class XmlService {


    public Element getEntryForSample(Sample sample) {

        Element entry = new Element("entry");
        entry.setAttribute("id", sample.getAccession());

        entry = addSampleGeneralInfo(entry, sample);
        entry = addSampleDates(entry, sample);
        entry = addSampleFields(entry, sample);
        entry = addSampleCrossReferences(entry, sample);

        return entry;
    }

    public Element getEntryForGroup(Group group) {
        Element entry = new Element("entry");

        return entry;
    }

    public Document produceDocumentForEntries(Collection<Element> entriesCollection) {
        Element database = new Element("database");
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");

        List<Element> databaseContent = new ArrayList<>();
        databaseContent.add(new Element("name").setText("biosamples"));
        databaseContent.add(new Element("description").setText(""));
        databaseContent.add(new Element("release").setText("1"));
        databaseContent.add(new Element("release_date").setText(dateformat.format(new Date())));
        databaseContent.add(new Element("entry_count").setText(Integer.toString(entriesCollection.size())));
        databaseContent.add(new Element("entries").addContent(entriesCollection));

        database.addContent(databaseContent);

        Document xmlDoc = new Document(new Element("xml"));
        xmlDoc.setContent(database);
        return xmlDoc;
    }



    private Element addSampleGeneralInfo(Element entry, Sample sample) {

        Element newEntry = entry.clone();

        Element name = new Element("name");
        name.setText(sample.getName());
        newEntry.addContent(name);

        String sampleDescription = sample.getDescription();
        if (sampleDescription != null && !sampleDescription.isEmpty()) {
            Element description = new Element("description");
            description.setText(sample.getDescription());
            newEntry.addContent(description);
        }

        return newEntry;
    }

    private  Element addSampleDates(Element entry, Sample sample) {
        Element newEntry = entry.clone();
        Element dates = new Element("dates");
        Element releaseDate = new Element("date");
        releaseDate.setAttribute(new Attribute("type", "release"));
        releaseDate.setAttribute(new Attribute("value", sample.getReleaseDate()));

        dates.addContent(releaseDate);

        Element updateDate = new Element("date");
        updateDate.setAttribute(new Attribute("type", "update"));
        updateDate.setAttribute(new Attribute("value", sample.getUpdateDate()));
        dates.addContent(updateDate);

        newEntry.addContent(dates);
        return newEntry;
    }

    private  Element addSampleFields(Element entry, Sample sample) {
        Element newEntry = entry.clone();

        Element additionalFields = new Element("additional_fields");
        List<BioSamplesCharacteristic> characteristics = sample.getCharacteristics();
        for (BioSamplesCharacteristic c : characteristics) {
            Element field = new Element("field");
            field.setAttribute(new Attribute("name", camelToSnakeCase(c.getType())));
            field.addContent(c.getValue());
            additionalFields.addContent(field);
        }

        for (Map.Entry<BioSamplesRelationType, List<BioSamplesRelation>> mapEntry: sample.getRelations().entrySet()) {

            if (mapEntry.getKey().equals(BioSamplesRelationType.GROUPS)) continue;

            for(BioSamplesRelation rel: mapEntry.getValue()) {
                Element field = new Element("field");
                field.setAttribute(new Attribute("name", camelToSnakeCase(mapEntry.getKey().getRelationName())));
                field.addContent(rel.getRelationIdentifier());
                additionalFields.addContent(field);
            }
        }

        newEntry.addContent(additionalFields);
        return newEntry;
    }

    private String camelToSnakeCase(String string) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        return string.replaceAll(regex, replacement).toLowerCase();
    }

    private  Element addSampleCrossReferences(Element entry, Sample sample) {
        Element newEntry = entry.clone();

        Element crossReferences = new Element("cross_references");


        // Get taxonomies
        List<Element> sampleTaxonomies = getSampleTaxonomies(sample);
        crossReferences.addContent(sampleTaxonomies);

        // Get references to other samples/groups
        List<Element> sampleRelations = getSampleRelations(sample);
        crossReferences.addContent(sampleRelations);

        if (crossReferences.getContentSize() > 0) {
            newEntry.addContent(crossReferences);
        }

        return newEntry;
    }

    private List<Element> getSampleRelations(Sample sample) {
        Map<BioSamplesRelationType, List<BioSamplesRelation>> relations = sample.getRelations();
        List<Element> sampleRelations = new ArrayList<>();
        relations.entrySet().forEach(entry -> {

            List<Element> tempRelations = entry.getValue().stream().map(rel -> {
                Element relation = new Element("ref");
                switch(entry.getKey()) {
                    case EXTERNAL_LINKS:
                        break;
                    case GROUPS:
                        relation.setAttribute("dbName", "GROUPS");
                        relation.setAttribute("dbkey", rel.getRelationIdentifier());
                        break;
                    default:
                        relation.setAttribute("dbName", "SAMPLES");
                        relation.setAttribute("dbkey", rel.getRelationIdentifier());
                }
                return relation;
            }).collect(Collectors.toList());
            sampleRelations.addAll(tempRelations);
        });
        return sampleRelations;
    }

    private List<Element> getSampleTaxonomies(Sample sample) {
        List<Element> sampleTaxonomies = new ArrayList<>();

        String uriRegexp = "[^/]+(?=/$|$)";
        String taxonRegexp = "(\\w+)_(\\d+)";
        Pattern p = Pattern.compile(uriRegexp);

        for (BioSamplesCharacteristic c : sample.getCharacteristics()) {
            List<String> uris = c.getOntologyTerms();
            for (String uri : uris) {
                Matcher uriMatcher = p.matcher(uri);
                if (uriMatcher.find()) {
                    String taxonID = uriMatcher.group();
                    Matcher taxonMatcher = Pattern.compile(taxonRegexp).matcher(taxonID);
                    if (taxonMatcher.find()) {
                        String taxonomy = taxonMatcher.group(1);
                        String dbName = taxonomy.toUpperCase();
                        if (taxonomy.equals("NCBITaxon")) {
                            dbName = "TAXONOMY";
                            taxonID = taxonMatcher.group(2);
                        }
                        Element taxonElement = new Element("ref");
                        taxonElement.setAttribute("dbname", dbName);
                        taxonElement.setAttribute("dbkey", taxonID);
                        sampleTaxonomies.add(taxonElement);
                    }
                }
            }
        }
        return sampleTaxonomies;
    }

    public String prettyPrint(Document d) {
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        return xmlOutput.outputString(d);
    }

    public String prettyPrint(Element e) {
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        return xmlOutput.outputString(e);
    }
}
