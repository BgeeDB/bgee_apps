package org.bgee.pipeline.expression;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeEnum;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.expressiondata.call.ExpressionCallLoader;
import org.bgee.pipeline.CommandRunner;


public class OnTheFlyPropagationTest extends CallService {

    private final static Logger log = LogManager.getLogger(OnTheFlyPropagationTest.class.getName());

    public OnTheFlyPropagationTest(ServiceFactory serviceFactory) {
        super(serviceFactory);
        // TODO Auto-generated constructor stub
    }
    
    public static void main(String[] args) {
        List<String> geneIds = CommandRunner.parseListArgument(args[1]);
        //As it is a test class I will not check validity of all arguments here.
        List<String> selectedDatatypes = CommandRunner.parseListArgument(args[2]);
        List<String> selectedCondParams = CommandRunner.parseListArgument(args[3]);
        if (geneIds == null || geneIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("at least one gene should be provided" + args[1]));
        }
        
        //validate condition parameters
        LinkedHashSet<ConditionParameter<?, ?>> condParams = selectedCondParams.isEmpty()?
                //default value
                ConditionParameter.allOf():
                //otherwise retrieve condition parameters from request
                ConditionParameter.allOf()
                    .stream().filter(a -> selectedCondParams.contains(a.getParameterName()))
                    .collect(Collectors.toCollection(() -> new LinkedHashSet<>()));
        
        // validate datatypes
        if (selectedDatatypes != null && !BgeeEnum.areAllInEnum(DataType.class, selectedDatatypes)) {
            throw log.throwing(new IllegalArgumentException("Incorrect data types provided: " + selectedDatatypes));
        }
        EnumSet<DataType> dataTypes = DataType.convertToDataTypeSet(selectedDatatypes);
        OnTheFlyPropagationTest.retrieveCalls(geneIds, dataTypes, condParams,
                DAOManager::getDAOManager, ServiceFactory::new);
    }

    private static void retrieveCalls(List<String> geneIds, EnumSet<DataType> dataTypes,
            LinkedHashSet<ConditionParameter<?, ?>> condParams, final Supplier<DAOManager> daoManager,
            final Function<DAOManager, ServiceFactory> serviceFactory) {
        log.traceEntry("{}, {}, {}, {}, {}",geneIds, dataTypes, condParams, daoManager, serviceFactory);
//        ExpressionCallLoader callLoader = this.loadExprCallLoader(true, condParams, dataTypes);
        
    }

}
