package uk.ac.ebi.biosamples.model.enums;

public enum Sort {
    ASCENDING("asc"), DESCENDING("desc");
    private String type;
    Sort(String type) { this.type = type; }
    public String getType() { return this.type; }
}
