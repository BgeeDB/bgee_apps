package org.bgee.pipeline.expression;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.gene.GeneFilter;

public class AnatEntityExpression {
    private final static Logger log = LogManager.getLogger(AnatEntityExpression.class.getName());

    private final ServiceFactory serviceFactory;

    public AnatEntityExpression() {
    	this(new ServiceFactory());
    }
    public AnatEntityExpression(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }


	public static void main(String[] args) throws IOException {
    	AnatEntityExpression generator = new AnatEntityExpression();
    	//Human: 9606 : 5026
    	//Mouse: 10090 : 133
    	//Horse: 9796 : 6
    	//Zebrafish : 7955 : 67
    	//D.melanogaster : 7227 : 13
    	// M.maculata : 9544 : 90
    	//X. tropicalis : 8364 : 66 
    	//C. elegans : 6239 : 50
    	//Gallus gallus : 9031 : 45 	
    	//10116 : 36
    	//9913 : 33
    	//13616 :m 19
    	//7249 :17
    	//9598 : 15
    	//7237 : 14
    	//7227 : 14
    	//9593 : 13
    	//9597 : 12
    	//9823 : 10
    	//9685 : 9
    	//10141 : 9
    	//7230 : 8
    	//9796 : 8
    	//9986 : 6
    	//9615 : 6
    	//9365 : 6
    	//7245 : 4
    	//7217 : 4
    	//7244 : 4
    	//28777 : 4
    	
    	generator.generateFile(Integer.parseInt(args[0]), false,
    			args[1]);
    }

    public void generateFile(int speciesId, boolean rnaSeqOnly, String outputDirectory) throws IOException {
        log.entry(speciesId, rnaSeqOnly, outputDirectory);

        log.info("Species ID: {}", speciesId);
        CallService callService = this.serviceFactory.getCallService();
        //We want observed data only for any call type
        Map<CallType.Expression, Boolean> obsDataFilter = new HashMap<>();
        obsDataFilter.put(null, true);
        //Create the parameters of the query
        ExpressionCallFilter filter = new ExpressionCallFilter(
                null, //we want expressed/not-expressed calls of any quality
                //all genes for the requested species
                Collections.singleton(new GeneFilter(speciesId)),
                //calls for the requested anat. entities and dev. stages
                null,
                rnaSeqOnly? EnumSet.of(DataType.RNA_SEQ): null, //data requested by OncoMX only
                obsDataFilter, //only observed data
                //no filter on observed data in anat. entity and stage,
                //it will anyway be both from the previous filter
                // true,true for rank -  null,null for counts
                null, null
                );
        //Attributes to retrieve
        Collection<CallService.Attribute> attrs = EnumSet.of(CallService.Attribute.GENE,
                CallService.Attribute.ANAT_ENTITY_ID,
                CallService.Attribute.DEV_STAGE_ID,
                CallService.Attribute.CALL_TYPE, CallService.Attribute.DATA_QUALITY,
                CallService.Attribute.MEAN_RANK, CallService.Attribute.ANAT_ENTITY_QUAL_EXPR_LEVEL);
        //Ordering Attributes
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.DEV_STAGE_ID, Service.Direction.ASC);
       
        
        File file = new File(outputDirectory, speciesId + ".tsv");
        // override any existing file
        if (file.exists()) {
        	file.delete();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        	log.info("Retrieving calls");
        	callService.loadExpressionCalls(filter, 
            		attrs,
            		serviceOrdering)
        	.forEach(c -> {
        		try {
        			String call = "";
        			if (c.getGene().getEnsemblGeneId() != null) {
        				call = call + c.getGene().getEnsemblGeneId() + "\t";
        			} else {
        				call = call + "NA\t";
        			}
        			if (c.getGene().getName() != null) {
        				call = call + c.getGene().getName() + "\t";
        			} else {
        				call = call + "NA\t";
        			}
        			if (c.getGene().getDescription() != null) {
        				call = call + c.getGene().getDescription() + "\t";
        			} else {
        				call = call + "NA\t";
        			}
        			if (c.getGene().getGeneBioType() != null) {
        				call = call + c.getGene().getGeneBioType() + "\t";
        			} else {
        				call = call + "NA\t";
        			}
        			if (c.getCondition().getAnatEntity().getId() != null) {
        				call = call + c.getCondition().getAnatEntity().getId() + "\t";
        			} else {
        				call = call + "NA\t";
        			}
        			if (c.getCondition().getAnatEntity().getName() != null) {
        				call = call + c.getCondition().getAnatEntity().getName() + "\t";
        			} else {
        				call = call + "NA\t";
        			}
        			if (c.getCondition().getDevStage().getId() != null) {
        				call = call + c.getCondition().getDevStage().getId() + "\t";
        			} else {
        				call = call + "NA\t";
        			}
        			if (c.getCondition().getDevStage().getName() != null) {
        				call = call + c.getCondition().getDevStage().getName() + "\t";
        			} else {
        				call = call + "NA\t";
        			}
        			if (c.getSummaryCallType() != null) {
        				call = call + c.getSummaryCallType() + "\t";
        			} else {
        				call = call + "NA\t";
        			}
        			if (c.getSummaryQuality() != null) {
        				call = call + c.getSummaryQuality() + "\t";
        			} else {
        				call = call + "NA\t";
        			}
        			if (c.getFormattedMeanRank() != null) {
        				call = call + c.getFormattedMeanRank() + "\t";
        			} else {
        				call = call + "NA\t";
        			}
        			writer.write(call);
                    writer.newLine();
                    writer.flush();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            });
        	log.info("Done");
        }

        log.exit();
    }
}
