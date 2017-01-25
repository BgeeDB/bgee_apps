package org.bgee.model.expressiondata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.CallType.DiffExpression;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DiffExpressionFactor;
import org.bgee.model.gene.Gene;

/**
 * A {@code CallData} represents the expression state of a {@link Gene}, in a {@link Condition}. 
 * This class only manages the expression state part, not the spatio-temporal location, 
 * or gene definition part. It represents the expression state of a baseline present/absent call, 
 * or a differential expression call; a call represents an overall summary 
 * of the expression data contained in Bgee (for instance, the expression state of a gene 
 * summarized over all Affymetrix chips studied in a given organ at a given stage).
 * <p>
 * For a class also managing the gene and condition definitions, and managing 
 * expression data from different data types for a given call, see the class {@link Call}. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Jan. 2017
 * @since   Bgee 13 Sept. 2015
 */
//XXX: examples of attributes that could be managed by this class: 
//* count of experiments supporting and contradicting the CallType.
//this is meaningful both from a "query filter" perspective and a "data retrieval" perspective, 
//and this could be common to baseline present/absent and diff. expression analyses 
//(even if we currently store the information only for diff. expression analyses). 
public abstract class CallData<T extends Enum<T> & CallType> {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CallData.class.getName());

    //**********************************************
    //   INNER CLASSES
    //**********************************************

    //XXX: attributes to be added in the future: min p-value, min/max fold change, ...
    //XXX: where to manage the DiffExpressionFactor? Here, or only in a "Call" class? 
    //But then, we could not use this CallData in query filters to specify the factor to use.
    public static class DiffExpressionCallData extends CallData<DiffExpression> {
        //XXX: I'm not very happy about this field, as it is redundant as compared to the field in 
        //DiffExpressionCall, and as it is not something specific to a data type, 
        //which is what this class is supposed to be about.
        //This field was created only to be able to parameterize queries to a CallService, 
        //though a CallFilter, to request diff. expression calls produced from analyzes 
        //over anatomy, and/or over development.
        //But maybe we can argue that it is always useful to be able to know from which type 
        //of analysis a DiffExpressionCallData comes from...
        private final DiffExpressionFactor diffExpressionFactor;
        
        private final DiffExpression callType;

        private final DataQuality dataQuality;

        private final DataPropagation dataPropagation;

        public DiffExpressionCallData(DiffExpressionFactor factor, DiffExpression callType) {
            this(factor, callType, null);
        }
        public DiffExpressionCallData(DiffExpressionFactor factor, DiffExpression callType, 
                DataType dataType) {
            this(factor, callType, DataQuality.LOW, dataType);
        }
        public DiffExpressionCallData(DiffExpressionFactor factor, DiffExpression callType, 
                DataQuality dataQual, DataType dataType) {
            this(factor, callType, dataQual, dataType, new DataPropagation());
        }
        public DiffExpressionCallData(DiffExpressionFactor factor, DiffExpression callType, 
                DataQuality dataQual, DataType dataType, DataPropagation dataPropagation) {
            super(dataType);
            
            log.entry(factor, callType, dataQual, dataType, dataPropagation);
            
            if (callType == null || dataQual == null || DataQuality.NODATA.equals(dataQual) || 
                dataPropagation == null) {
                        throw log.throwing(new IllegalArgumentException("A DiffExpressionFactor, "
                            + "a CallType, a DataQuality, and a DataPropagation must be defined "
                            + "to instantiate a CallData."));
            }
            callType.checkDataPropagation(dataPropagation);
            if (dataType != null) {
                callType.checkDataType(dataType);
            }

            this.callType = callType;
            this.dataQuality = dataQual;
            this.dataPropagation = dataPropagation;
            this.diffExpressionFactor = factor;            
            log.exit();
        }
        
        public DiffExpressionFactor getDiffExpressionFactor() {
            return diffExpressionFactor;
        }
        
        public DiffExpression getCallType() {
            return callType;
        }
        public DataQuality getDataQuality() {
            return dataQuality;
        }
        public DataPropagation getDataPropagation() {
            return dataPropagation;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((diffExpressionFactor == null) ? 0 : diffExpressionFactor.hashCode());
            result = prime * result + ((callType == null) ? 0 : callType.hashCode());
            result = prime * result + ((dataPropagation == null) ? 0 : dataPropagation.hashCode());
            result = prime * result + ((dataQuality == null) ? 0 : dataQuality.hashCode());
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DiffExpressionCallData other = (DiffExpressionCallData) obj;
            if (diffExpressionFactor != other.diffExpressionFactor) {
                return false;
            }
            if (callType == null) {
                if (other.callType != null) {
                    return false;
                }
            } else if (!callType.equals(other.callType)) {
                return false;
            }
            if (dataPropagation == null) {
                if (other.dataPropagation != null) {
                    return false;
                }
            } else if (!dataPropagation.equals(other.dataPropagation)) {
                return false;
            }
            if (dataQuality != other.dataQuality) {
                return false;
            }
            return true;
        }
        
        @Override
        public String toString() {
            return super.toString() + " - Call type: " + callType + " - Data quality: " + dataQuality +
                " - Data propagation: " + dataPropagation + " - Diff. expression factor: " + diffExpressionFactor;
        }
    }
    
    public static class ExpressionCallData extends CallData<Expression> {

        private final int presentHighSelfExpCount;
        
        private final int presentLowSelfExpCount;
        
        private final int absentHighSelfExpCount;
        
        private final int presentHighDescExpCount;
        
        private final int presentLowDescExpCount;
        
        private final int absentHighParentExpCount;

        private final int presentHighTotalCount;
        
        private final int presentLowTotalCount;
        
        private final int absentHighTotalCount;
        
        public ExpressionCallData(DataType dataType) {
            this(dataType, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        public ExpressionCallData(DataType dataType, int presentHighSelfExpCount, 
            int presentLowSelfExpCount, int absentHighSelfExpCount, int presentHighDescExpCount,
            int presentLowDescExpCount, int absentHighParentExpCount, int presentHighTotalCount,
            int presentLowTotalCount, int absentHighTotalCount) {
            super(dataType);
            this.presentHighSelfExpCount = presentHighSelfExpCount;
            this.presentLowSelfExpCount = presentLowSelfExpCount;
            this.absentHighSelfExpCount = absentHighSelfExpCount;
            this.presentHighDescExpCount = presentHighDescExpCount;
            this.presentLowDescExpCount = presentLowDescExpCount;
            this.absentHighParentExpCount = absentHighParentExpCount;
            this.presentHighTotalCount = presentHighTotalCount;
            this.presentLowTotalCount = presentLowTotalCount;
            this.absentHighTotalCount = absentHighTotalCount;
        }

        public int getPresentHighSelfExpCount() {
            return presentHighSelfExpCount;
        }

        public int getPresentLowSelfExpCount() {
            return presentLowSelfExpCount;
        }

        public int getAbsentHighSelfExpCount() {
            return absentHighSelfExpCount;
        }

        public int getPresentHighDescExpCount() {
            return presentHighDescExpCount;
        }

        public int getPresentLowDescExpCount() {
            return presentLowDescExpCount;
        }

        public int getAbsentHighParentExpCount() {
            return absentHighParentExpCount;
        }

        public int getPresentHighTotalCount() {
            return presentHighTotalCount;
        }

        public int getPresentLowTotalCount() {
            return presentLowTotalCount;
        }

        public int getAbsentHighTotalCount() {
            return absentHighTotalCount;
        }

        @Override
        // FIXME check implementation
        public Expression getCallType() {
            log.entry();
            if (this.getPresentHighTotalCount() > 0 || this.getPresentLowDescExpCount() > 0) {
                return log.exit(Expression.EXPRESSED);
            }
            if (this.getAbsentHighTotalCount() > 0) {
                return log.exit(Expression.NOT_EXPRESSED);
            }
            return log.exit(null);
        }

        @Override
        // FIXME check implementation
        public DataQuality getDataQuality() {
            log.entry();
            if (this.getPresentHighTotalCount() > 0 || 
                this.getPresentHighSelfExpCount() > 0 || this.getPresentHighDescExpCount() > 0) {
                return log.exit(DataQuality.HIGH);
            }
            if (this.getPresentLowTotalCount() > 0 || 
                this.getPresentLowSelfExpCount() > 0 || this.getPresentLowDescExpCount() > 0) {
                return log.exit(DataQuality.LOW);
            }
            if (this.getPresentLowTotalCount() > 0) {
                return log.exit(DataQuality.HIGH);
            }
            return log.exit(DataQuality.NODATA);
        }

        @Override
        // FIXME check implementation
        public DataPropagation getDataPropagation() {
            log.entry();
            throw log.throwing(new UnsupportedOperationException("How to define DataPropagation?"));
        }


        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + absentHighParentExpCount;
            result = prime * result + absentHighSelfExpCount;
            result = prime * result + absentHighTotalCount;
            result = prime * result + presentHighDescExpCount;
            result = prime * result + presentHighSelfExpCount;
            result = prime * result + presentHighTotalCount;
            result = prime * result + presentLowDescExpCount;
            result = prime * result + presentLowSelfExpCount;
            result = prime * result + presentLowTotalCount;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            ExpressionCallData other = (ExpressionCallData) obj;
            if (absentHighParentExpCount != other.absentHighParentExpCount)
                return false;
            if (absentHighSelfExpCount != other.absentHighSelfExpCount)
                return false;
            if (absentHighTotalCount != other.absentHighTotalCount)
                return false;
            if (presentHighDescExpCount != other.presentHighDescExpCount)
                return false;
            if (presentHighSelfExpCount != other.presentHighSelfExpCount)
                return false;
            if (presentHighTotalCount != other.presentHighTotalCount)
                return false;
            if (presentLowDescExpCount != other.presentLowDescExpCount)
                return false;
            if (presentLowSelfExpCount != other.presentLowSelfExpCount)
                return false;
            if (presentLowTotalCount != other.presentLowTotalCount)
                return false;
            return true;
        }

        @Override
        public String toString() {
            return super.toString() + " - Present high self exp count: " + presentHighSelfExpCount +
                " - Present low self exp count: " + presentLowSelfExpCount + 
                " - Absent high self exp count: " + absentHighSelfExpCount + 
                " - Present high desc exp count: " + presentHighDescExpCount +
                " - Present low descendant exp count: " + presentLowDescExpCount +
                " - Absent high parent exp count: " + absentHighParentExpCount +
                " - Present low total count: " + presentLowTotalCount +
                " - Present high total count: "+ presentHighTotalCount +
                " - Absent high total count: " + absentHighTotalCount;
        }
    }

    //**********************************************
    //   INSTANCE ATTRIBUTES AND METHODS
    //**********************************************
    
    private final DataType dataType;
    
    /**
     * Constructor allowing to specify a {@code DataType}. 
     * 
     * @param dataType  The {@code DataType} that allowed to generate the {@code CallType}.
     * @throws IllegalArgumentException    If {@code dataType} is not {@code null}.
     */
    protected CallData(DataType dataType) throws IllegalArgumentException {
        log.entry(dataType);
        
        if (dataType == null) {
            throw log.throwing(new IllegalArgumentException
                ("A DataType must be defined to instantiate a CallData."));
        }

        this.dataType = dataType;

        log.exit();
    }
	
    public abstract T getCallType();

    public abstract DataQuality getDataQuality();
    
    public abstract DataPropagation getDataPropagation();
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CallData<?> other = (CallData<?>) obj;
        if (dataType != other.dataType) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "Data type: " + dataType;
    }
}
