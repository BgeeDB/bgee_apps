package org.bgee.model.expressiondata;

import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.anatdev.DevStageService;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.ontology.OntologyService;

public class ExpressionDataService extends CommonService {

    protected final GeneDAO geneDAO;
    protected final OntologyService ontService;
    protected final AnatEntityService anatEntityService;
    protected final DevStageService devStageService;

    protected ExpressionDataService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.geneDAO = this.getDaoManager().getGeneDAO();
        this.ontService = serviceFactory.getOntologyService();
        this.anatEntityService = serviceFactory.getAnatEntityService();
        this.devStageService = serviceFactory.getDevStageService();
    }
}
