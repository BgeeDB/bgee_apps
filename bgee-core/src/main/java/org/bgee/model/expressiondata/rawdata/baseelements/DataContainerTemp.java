package org.bgee.model.expressiondata.rawdata.baseelements;

import java.util.Objects;

public class DataContainerTemp {
    private final boolean resultFound;

    public DataContainerTemp(boolean resultFound) {
        this.resultFound = resultFound;
    }

    public boolean isResultFound() {
        return resultFound;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultFound);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataContainerTemp other = (DataContainerTemp) obj;
        return resultFound == other.resultFound;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DataContainerTemp [resultFound=").append(resultFound).append("]");
        return builder.toString();
    }
}