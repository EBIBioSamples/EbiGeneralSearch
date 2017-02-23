package uk.ac.ebi.spot.biosamples.Model.Entities;

import java.util.Collections;
import java.util.List;

/**
 * Created by lucacherubin on 2016/11/07.
 */
public class BioSamplesCharacteristic {

    String type;
    String value;
    List<String> ontologyTerms = Collections.emptyList();
    String unit;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getOntologyTerms() {
        return ontologyTerms;
    }

    public void setOntologyTerms(List<String> ontologyTerms) {
        this.ontologyTerms = ontologyTerms;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean unitAssociatedOntology() {
        return this.unit != null && !this.unit.isEmpty();
    }
}
