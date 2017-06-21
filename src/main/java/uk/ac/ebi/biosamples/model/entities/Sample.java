package uk.ac.ebi.biosamples.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import uk.ac.ebi.biosamples.model.enums.BioSamplesRelationType;
import uk.ac.ebi.biosamples.model.util.CharacteristicsDeserializer;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class Sample implements BioSamplesEntity{

    private String accession;
    private String name;
    private String description;
    private String updateDate;
    private String releaseDate;


    @JsonDeserialize(using = CharacteristicsDeserializer.class)
    private List<BioSamplesCharacteristic> characteristics;

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

    @JsonSetter("updateDate")
    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    @JsonSetter("update")
    public void setUpdateDateTime(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    @JsonSetter("releaseDate")
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    @JsonSetter("release")
    public void setReleaseDateTime(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public Map<BioSamplesRelationType, List<BioSamplesRelation>> getRelations() {
        return this.relations;
    }

    @Override
    public Class getEntityType() {
        return Sample.class;
    }

    public void setRelations(Map<BioSamplesRelationType, List<BioSamplesRelation>> relations) {
        this.relations = relations;
    }


    public List<BioSamplesCharacteristic> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<BioSamplesCharacteristic> characteristics) {
        this.characteristics = characteristics;
    }

}
