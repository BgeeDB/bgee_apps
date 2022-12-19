package org.bgee.model.expressiondata.call;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.SexService;
import org.bgee.model.anatdev.StrainService;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO;
import org.bgee.model.expressiondata.ExpressionDataService;

public class CallServiceParent extends ExpressionDataService {
    private final static Logger log = LogManager.getLogger(CallServiceParent.class.getName());

    /**
     * A {@code BigDecimal} that is the value a FDR-corrected p-value must be less than or equal to
     * for PRESENT LOW QUALITY.
     */
    public final static BigDecimal PRESENT_LOW_LESS_THAN_OR_EQUALS_TO = new BigDecimal("0.05");
    /**
     * A {@code BigDecimal} that is the value a FDR-corrected p-value must be less than or equal to
     * for PRESENT HIGH QUALITY.
     */
    public final static BigDecimal PRESENT_HIGH_LESS_THAN_OR_EQUALS_TO = new BigDecimal("0.01");
    /**
     * A {@code BigDecimal} that is the value a FDR-corrected p-value must be greater than
     * for ABSENT HIGH QUALITY.
     */
    public final static BigDecimal ABSENT_HIGH_GREATER_THAN = new BigDecimal("0.1");
    /**
     * A {@code BigDecimal} that is the value a FDR-corrected p-value must be greater than
     * for ABSENT LOW QUALITY.
     */
    public final static BigDecimal ABSENT_LOW_GREATER_THAN = PRESENT_LOW_LESS_THAN_OR_EQUALS_TO;

    protected final CallServiceUtils utils;

    protected final ConditionDAO conditionDAO;
    protected final GlobalExpressionCallDAO globalExprCallDAO;
    protected final SexService sexService;
    protected final StrainService strainService;
    
    protected CallServiceParent(ServiceFactory serviceFactory, CallServiceUtils utils) {
        super(serviceFactory);

        if (utils == null) {
            throw log.throwing(new IllegalArgumentException("CallServiceUtils cannot be null"));
        }
        this.utils = utils;

        this.conditionDAO = this.getDaoManager().getConditionDAO();
        this.globalExprCallDAO = this.getDaoManager().getGlobalExpressionCallDAO();
        this.sexService = this.getServiceFactory().getSexService();
        this.strainService = this.getServiceFactory().getStrainService();
    }
}
