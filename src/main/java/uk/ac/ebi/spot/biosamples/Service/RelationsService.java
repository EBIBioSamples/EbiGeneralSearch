package uk.ac.ebi.spot.biosamples.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.client.Traverson;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.biosamples.Model.Relations.BioSamplesRelation;
import uk.ac.ebi.spot.biosamples.Model.Relations.BioSamplesRelationType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.ac.ebi.spot.biosamples.Model.Relations.BioSamplesRelationType.values;

@Service
public class RelationsService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${biosamples.api.root:'https://www.ebi.ac.uk/biosamples/api'}")
    private String apiRoot;

    public Map<BioSamplesRelationType, List<BioSamplesRelation>> getSampleRelations(String accession) {
        Map<BioSamplesRelationType, List<BioSamplesRelation>> sampleRelations = new HashMap<>();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("apiRoot", apiRoot);
        parameters.put("accession", accession);
        UriTemplate template = new UriTemplate("{apiRoot}/samples/{accession}");

        Traverson traverson = new Traverson(template.expand(parameters), MediaTypes.HAL_JSON);
        ParameterizedTypeReference<Resources<BioSamplesRelation>> relation =
                new ParameterizedTypeReference<Resources<BioSamplesRelation>>() {};
        for(BioSamplesRelationType e: values()) {
            Resources<BioSamplesRelation> ir = traverson.follow("relations",e.getRelationName()).toObject(relation);
            sampleRelations.put(e, ir.getContent().stream().collect(Collectors.toList()));
        }

        return sampleRelations;

    }

    public Map<BioSamplesRelationType, List<BioSamplesRelation>> getGroupsRelations(String accession) {
        Map<BioSamplesRelationType, List<BioSamplesRelation>> groupRelations = new HashMap<>();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("apiRoot", apiRoot);
        parameters.put("accession", accession);
        UriTemplate template = new UriTemplate("{apiRoot}/groups/{accession}");

        Traverson traverson = new Traverson(template.expand(parameters), MediaTypes.HAL_JSON);
        ParameterizedTypeReference<Resources<BioSamplesRelation>> rel = new ParameterizedTypeReference<Resources<BioSamplesRelation>>() {};
        for(BioSamplesRelationType e: values()) {
            Resources<BioSamplesRelation> relations = traverson.follow("relations",e.getRelationName()).toObject(rel);
            if(!relations.getContent().isEmpty()) {
                groupRelations.put(e, relations.getContent().stream().collect(Collectors.toList()));
            }
        }

        return groupRelations;
    }



}
