package uk.ac.ebi.biosamples.model.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class BioSamplesRelation {
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

}
