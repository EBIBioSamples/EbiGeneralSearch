package uk.ac.ebi.biosamples.service;

import uk.ac.ebi.biosamples.model.entities.Group;
import uk.ac.ebi.biosamples.model.entities.BioSamplesEntity;
import uk.ac.ebi.biosamples.model.entities.Sample;

import java.util.concurrent.Callable;

public class RelationExpander implements Callable {

    private RelationsService relationService;
    private BioSamplesEntity entity;

    public RelationExpander(RelationsService relationsService, BioSamplesEntity entity) {
        this.relationService = relationsService;
        this.entity = entity;
    }

    @Override
    public Object call() throws Exception {
        if (entity instanceof Group) {
            return relationService.getGroupsRelations(entity.getAccession());
        } else if (entity instanceof Sample) {
            return relationService.getSampleRelations(entity.getAccession());
        } else {
            throw new UnsupportedOperationException("Operation not possible for entity of type " + entity.getClass());
        }

    }
}
