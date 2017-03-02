package uk.ac.ebi.biosamples.model.entities;

import uk.ac.ebi.biosamples.model.enums.BioSamplesRelationType;

import java.util.List;
import java.util.Map;

public class Group implements BioSamplesEntity {

    private String accession;
    private String name;
    private String description;
    private String updateDate;
    private String releaseDate;
    private List<String> samples;
    private Map<BioSamplesRelationType, List<BioSamplesRelation>> relations;


    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    @Override
    public Map<BioSamplesRelationType, List<BioSamplesRelation>> getRelations() {
        return this.relations;
    }

    @Override
    public Class getEntityType() {
        return Group.class;
    }

    public void setRelations(Map<BioSamplesRelationType, List<BioSamplesRelation>> relations) {
        this.relations = relations;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<String> getSamples() {
        return samples;
    }

    public void setSamples(List<String> samples) {
        this.samples = samples;
    }
}
