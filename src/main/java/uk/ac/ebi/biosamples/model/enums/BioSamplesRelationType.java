package uk.ac.ebi.biosamples.model.enums;

public enum BioSamplesRelationType {
    RECURATED_TO("recuratedTo"),
    PARENT_OF("parentOf"),
    DERIVED_TO("derivedTo"),
    DERIVED_FROM("derivedFrom"),
    RECURATED_FROM("recuratedFrom"),
    GROUPS("groups"),
    CHILD_OF("childOf"),
    SAME_AS("sameAs"),
    EXTERNAL_LINKS("externalLinks");


    private String rel;
//    private Class relClass;

    BioSamplesRelationType(String relName) {
        this.rel = relName;
//        this.relClass = relClass;
    }

    public String getRelationName() {
        return this.rel;
    }

}
