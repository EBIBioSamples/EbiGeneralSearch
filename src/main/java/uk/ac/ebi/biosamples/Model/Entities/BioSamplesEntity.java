package uk.ac.ebi.biosamples.Model.Entities;

import uk.ac.ebi.biosamples.Model.Relations.BioSamplesRelationType;
import uk.ac.ebi.biosamples.Model.Relations.BioSamplesRelation;

import java.util.List;
import java.util.Map;

public interface BioSamplesEntity {

    public String getAccession();
    public String getName();
    public String getDescription();
    public String getUpdateDate();
    public String getReleaseDate();
    public Map<BioSamplesRelationType, List<BioSamplesRelation>> getRelations();
    public void setRelations(Map<BioSamplesRelationType, List<BioSamplesRelation>> relations);
    public Class getEntityType();

}
