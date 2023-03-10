package org.bgee.model.expressiondata.baseelements;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Frederic Bastian
 * @version Bgee 15.0, Jan. 2023
 * @since Bgee 15.0, Jan. 2023
 */
public class PValue {
    private final static Logger log = LogManager.getLogger(PValue.class.getName());

    private final BigDecimal pValue;

    public PValue(BigDecimal pValue) {
        if (pValue == null || pValue.compareTo(new BigDecimal(0)) == -1 ||
                pValue.compareTo(new BigDecimal(1)) == 1) {
            throw log.throwing(new IllegalArgumentException(
                    "pValue cannot be null or less than 0 or greater than 1"));
        }
        this.pValue = pValue;
    }

    public BigDecimal getPValue() {
        return pValue;
    }

    /**
     * Format the p-value returned by {@link #getPValue()}.
     *
     * @return  The formatted {code String} representation of the p-value 
     */
    public String getFormattedPValue() {
        log.traceEntry();
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        // do not use scientific notation when FDR pValue is bigger than 0.001 or equal
        // to 0
        if(pValue.compareTo(new BigDecimal(0.001)) >= 0 || 
                pValue.compareTo(new BigDecimal(0)) == 0) {
            formatter.setMaximumFractionDigits(3);
            formatter.setMinimumFractionDigits(0);
        } else if (formatter instanceof DecimalFormat) {
            ((DecimalFormat) formatter).applyPattern("0.00E0");
        } else {
            throw log.throwing(new IllegalStateException("No formatter could be defined "
                    + "for " + pValue));
        }
        //In Bgee 15 we limited the precision to 14 digits
        return log.traceExit((pValue.compareTo(new BigDecimal("0.00000000000001")) <= 0? "<= ": "")
                + formatter.format(pValue).toLowerCase(Locale.US));
    }

    @Override
    public int hashCode() {
        return Objects.hash(pValue);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PValue other = (PValue) obj;
        return Objects.equals(pValue, other.pValue);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PValue [pValue=").append(pValue).append("]");
        return builder.toString();
    }
}
