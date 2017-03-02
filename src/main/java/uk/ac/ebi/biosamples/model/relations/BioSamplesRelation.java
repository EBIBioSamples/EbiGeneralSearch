package uk.ac.ebi.biosamples.model.relations;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class BioSamplesRelation {
//    private String accession;
//
//    public String getAccession() {
//        return accession;
//    }
//
//    public void setAccession(String accession) {
//        this.accession = accession;
//    }
    private String identifier;

    @JsonGetter("accession")
    public String getRelationIdentifier() {
        return identifier;
    }

    @JsonSetter("accession")
    public void setInternalRelationIdentifier(String accession) {
        this.identifier = accession;
    }

    @JsonSetter("url")
    public void setExternalRelationIdentifier(String url) {
        if (this.identifier == null) {
            this.identifier = url;
        }
    }


//    @Override
//    public String getIdentifier() {
//        return getAccession();
//    }
}
