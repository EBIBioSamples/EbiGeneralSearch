package uk.ac.ebi.spot.biosamples.Service.xml;

import org.jdom2.Attribute;
import org.jdom2.Element;
import uk.ac.ebi.spot.biosamples.Model.BioSamplesCharacteristic;
import uk.ac.ebi.spot.biosamples.Model.Sample;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SampleToEntryConverter {


    public static Element produceEntryFor(Sample sample) {

        Element entry = new Element("entry");
        entry.setAttribute("id", sample.getAccession());

        entry = addGeneralInfo(entry, sample);
        entry = addDates(entry, sample);
        entry = addFields(entry, sample);
        entry = addCrossReferences(entry, sample);

        return entry;
    }


    private static Element addGeneralInfo(Element entry, Sample sample) {

        Element newEntry = entry.clone();

        Element name = new Element("name");
        name.setText(sample.getName());
        newEntry.addContent(name);

        String sampleDescription = sample.getDescription();
        if ( sampleDescription != null && ! sampleDescription.isEmpty() ) {
            Element description = new Element("description");
            description.setText(sample.getDescription());
            newEntry.addContent(description);
        }

        return newEntry;
    }

    private static Element addDates(Element entry, Sample sample) {
        Element newEntry = entry.clone();
        Element dates = new Element("dates");
        Element releaseDate = new Element("date");
        releaseDate.setAttribute(new Attribute("type","release"));
        releaseDate.setAttribute(new Attribute("value",sample.getReleaseDate()));

        dates.addContent(releaseDate);

        Element updateDate = new Element("date");
        updateDate.setAttribute(new Attribute("type", "update"));
        updateDate.setAttribute(new Attribute("value", sample.getUpdateDate()));
        dates.addContent(updateDate);

        newEntry.addContent(dates);
        return newEntry;
    }

    private static Element addFields(Element entry, Sample sample) {
        Element newEntry = entry.clone();

        Element additionalFields = new Element("additional_fields");
        List<BioSamplesCharacteristic> characteristics = sample.getCharacteristics();
        for(BioSamplesCharacteristic c: characteristics) {
            Element field = new Element("field");
            field.setAttribute(new Attribute("name", c.getType()));
            field.addContent(c.getValue());
            additionalFields.addContent(field);
        }

        newEntry.addContent(additionalFields);
        return newEntry;
    }

    private static Element addCrossReferences(Element entry, Sample sample) {
        Element newEntry = entry.clone();

        Element crossReferences = new Element("cross_references");

        // Add taxonomies
        String uriRegexp = "[^/]+(?=/$|$)";
        String taxonRegexp = "(\\w+)_(\\d+)";
        Pattern p = Pattern.compile(uriRegexp);
        List<BioSamplesCharacteristic> characteristics = sample.getCharacteristics();
        for(BioSamplesCharacteristic c: characteristics) {
            List<String> uris = c.getOntologyTerms();
            for(String uri: uris) {
                Matcher uriMatcher = p.matcher(uri);
                if(uriMatcher.find()) {
                    String taxonID = uriMatcher.group();
                    Matcher taxonMatcher = Pattern.compile(taxonRegexp).matcher(taxonID);
                    if(taxonMatcher.find()){
                        String taxonomy = taxonMatcher.group();
                        String dbName = taxonomy.toUpperCase();
                        if(taxonomy.equals("NCBITaxon")) {
                            dbName = "TAXONOMY";
                        }
                        Element taxonElement = new Element("ref");
                        taxonElement.setAttribute("dbname", dbName);
                        taxonElement.setAttribute("dbkey", taxonID);
                        crossReferences.addContent(taxonElement);
                    }
                }
            }
        }

        // Add references to other samples/groups
        // TODO
        if(crossReferences.getContentSize() > 0) {
            newEntry.addContent(crossReferences);
        }

        return newEntry;
    }

}
