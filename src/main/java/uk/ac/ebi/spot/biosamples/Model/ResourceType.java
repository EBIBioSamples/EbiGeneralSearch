package uk.ac.ebi.spot.biosamples.Model;

public enum ResourceType {
    GROUPS("groups"), SAMPLES_RELATIONS("samplesrelations"),
    SAMPLES("samples"), EXTERNAL_LINK_RELATIONS("externallinkrelations"),
    GROUPS_RELATIONS("groupsrelations");

    private String rel;

    ResourceType(String relName) {
        this.rel = relName;
    }

    public String getRel() {
        return rel;
    }
}
