package org.bgee.model.expressiondata.call;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.ExpressionDataService;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter;
import org.bgee.model.ServiceFactory;

public class ExpressionCallService extends ExpressionDataService {
    private final static Logger log = LogManager.getLogger(ExpressionCallService.class.getName());
    private final ConditionDAO condDAO;

    public ExpressionCallService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.condDAO = this.getDaoManager().getConditionDAO();
    }

    public ExpressionCallLoader loadCallLoader(ExpressionCallFilter filter) {
        log.traceEntry("{}", filter);
        //TODO
        return null;
    }

//    public ExpressionCallFilter processExpressionCallFilter(ExpressionCallFilter filter) {
//        log.traceEntry("{}", filter);
//    }
}
