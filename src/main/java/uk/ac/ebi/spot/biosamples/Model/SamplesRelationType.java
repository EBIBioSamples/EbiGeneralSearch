package uk.ac.ebi.spot.biosamples.Model;

public enum SamplesRelationType {
    RECURATED_TO("recuratedTo"), PARENT_OF("parentOf"),
    EXTERNAL_LINKS("externalLinks"), DERIVED_TO("derivedTo"),
    DERIVED_FROM("derivedFrom"), RECURATED_FROM("recuratedFrom"),
    GROUPS("groups"), CHILD_OF("childOf"), SAME_AS("sameAs");


    private String rel;
    SamplesRelationType(String relName) {
        this.rel = relName;
    }

    public String getRel() {
        return this.rel;
    }
}
