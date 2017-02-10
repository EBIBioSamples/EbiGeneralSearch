package uk.ac.ebi.spot.biosamples.Model.Util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.spot.biosamples.Model.BioSamplesCharacteristic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CharacteristicsDeserializer extends JsonDeserializer<List<BioSamplesCharacteristic>>{

    private static final Logger log = LoggerFactory.getLogger(CharacteristicsDeserializer.class);

    @Override
    public List<BioSamplesCharacteristic> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        List<BioSamplesCharacteristic> outList = new ArrayList<>();
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        if (node.getNodeType().equals(JsonNodeType.OBJECT)) {
            ObjectNode charsObj = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> it = charsObj.fields();
            while(it.hasNext()) {
                Map.Entry<String, JsonNode> rawCharacteristic = it.next();
                outList.addAll(extractCharacteristic(rawCharacteristic));
            }
        }
        return outList;
    }

    private List<BioSamplesCharacteristic> extractCharacteristic(Map.Entry<String,JsonNode> jsonNodeEntry) {
        List<BioSamplesCharacteristic> multiValuedCharacteristic = new ArrayList<>();
        String multiValueFieldName = jsonNodeEntry.getKey();
        ArrayNode multiValueFieldValues = (ArrayNode) jsonNodeEntry.getValue();
        multiValueFieldValues.forEach(node -> {
            BioSamplesCharacteristic characteristic = new BioSamplesCharacteristic();
            characteristic.setType(multiValueFieldName);
            characteristic.setValue(node.findValue("text").textValue());
            if (node.has("unit")) {
                characteristic.setUnit(node.findValue("unit").textValue());
            }

            if(node.has("ontologyTerms")) {
                List<String> ontologyTerms = new ArrayList<>();
                node.findValue("ontologyTerms").forEach(ontoNode -> {
                    ontologyTerms.add(ontoNode.asText());
                });
                characteristic.setOntologyTerms(ontologyTerms);
            }
            multiValuedCharacteristic.add(characteristic);
        });
        return multiValuedCharacteristic;

    }
}
