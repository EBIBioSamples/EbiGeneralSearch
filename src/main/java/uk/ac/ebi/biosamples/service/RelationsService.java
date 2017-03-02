package uk.ac.ebi.biosamples.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.client.Traverson;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biosamples.model.entities.BioSamplesEntity;
import uk.ac.ebi.biosamples.model.entities.Group;
import uk.ac.ebi.biosamples.model.entities.Sample;
import uk.ac.ebi.biosamples.model.relations.BioSamplesRelation;
import uk.ac.ebi.biosamples.model.relations.BioSamplesRelationType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class RelationsService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Value("${biosamples.api.root:'https://www.ebi.ac.uk/biosamples/api'}")
    private String apiRoot;

    public Map<BioSamplesRelationType, List<BioSamplesRelation>> getSampleRelations(String accession) {
        Map<BioSamplesRelationType, List<BioSamplesRelation>> sampleRelations = new HashMap<>();

        try {
            sampleRelations = getAllRelationAsync(accession, Sample.class).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while retrieving relations for " + accession, e);
        }
        return sampleRelations;

    }

    public Map<BioSamplesRelationType, List<BioSamplesRelation>> getGroupsRelations(String accession) {

        try {
            return getAllRelationAsync(accession, Group.class).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while retrieving relations for " + accession, e);
        }
        return new HashMap<>();

    }

    @Async
    public CompletableFuture<List<BioSamplesRelation>> getRelationTypeAsync(String accession,
                                                                            Class<? extends BioSamplesEntity> entity,
                                                                            BioSamplesRelationType type) {
        Traverson traverson = getTraverson(accession, entity);
        ParameterizedTypeReference<Resources<BioSamplesRelation>> relation = new ParameterizedTypeReference<Resources<BioSamplesRelation>>() {};
        Resources<BioSamplesRelation> ir = traverson.follow("relations",type.getRelationName()).toObject(relation);
        List<BioSamplesRelation> relations = ir.getContent().stream().collect(Collectors.toList());

        return CompletableFuture.completedFuture(relations);

    }

    @Async
    public CompletableFuture<Map<BioSamplesRelationType,List<BioSamplesRelation>>> getAllRelationAsync(String accession,
                                                                                                    Class<? extends BioSamplesEntity> entity) {
        Map<BioSamplesRelationType, List<BioSamplesRelation>> sampleRelations = new HashMap<>();
        Traverson traverson = getTraverson(accession, entity);
        ParameterizedTypeReference<Resources<BioSamplesRelation>> relation =
                new ParameterizedTypeReference<Resources<BioSamplesRelation>>() {};
        for(BioSamplesRelationType e: BioSamplesRelationType.values()) {
            Resources<BioSamplesRelation> ir = traverson.follow("relations",e.getRelationName()).toObject(relation);
            sampleRelations.put(e, ir.getContent().stream().collect(Collectors.toList()));
        }

        return CompletableFuture.completedFuture(sampleRelations);

    }

    public Map<BioSamplesRelationType, List<BioSamplesRelation>> getAllRelations(String accession, Class<? extends BioSamplesEntity> entity) {
        Map<BioSamplesRelationType, List<BioSamplesRelation>> sampleRelations = new HashMap<>();
        Traverson traverson = getTraverson(accession, entity);
        ParameterizedTypeReference<Resources<BioSamplesRelation>> relation =
                new ParameterizedTypeReference<Resources<BioSamplesRelation>>() {};
        for(BioSamplesRelationType e: BioSamplesRelationType.values()) {
            Resources<BioSamplesRelation> ir = traverson.follow("relations",e.getRelationName()).toObject(relation);
            sampleRelations.put(e, ir.getContent().stream().collect(Collectors.toList()));
        }
        return sampleRelations;
    }


    private Traverson getTraverson(String accession, Class<? extends BioSamplesEntity> type) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("apiRoot", apiRoot);
        parameters.put("accession", accession);
        UriTemplate template = null;
        if(type == Sample.class) {
            template = new UriTemplate("{apiRoot}/samples/{accession}");
        } else {
            template = new UriTemplate("{apiRoot}/groups/{accession}");
        }

        return new Traverson(template.expand(parameters), MediaTypes.HAL_JSON);
    }



}
