package uk.ac.ebi.biosamples.model.enums;

public enum EntityType {
    GROUP("groups"), SAMPLES("samples");
    private String type;
    EntityType(String type) {
        this.type = type;
    }
    public String getType() {
        return this.type;
    }
}
