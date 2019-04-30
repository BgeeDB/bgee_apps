package org.bgee.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.source.Source;

import java.util.Objects;

/**
 * Class allowing to describe a cross-reference.
 *
 * @author  Valentine Rech de Laval 
 * @version Bgee 14, Apr. 2019
 * @since   Bgee 14, Apr. 2019
 * @param <T> The type of {@code Entity} ID to which the reference corresponds.
 */
public class XRef<T> {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(XRef.class.getName());
    
    /**
     * A {@code String} that is the cross-reference ID.
     */
    private final String xRefId;

    /**
     * A {@code String} that is the cross-reference name.
     */
    private final String xRefName;

    /**
     * A {@code String} that is the entity to which the reference corresponds.
     */
    private final T entityId;

    /**
     * A {@code Source} that is the data source the cross-reference comes from.
     */
    public final Source source;

    public XRef(String xRefId, String xRefName, Source source, T entityId) {
        if (StringUtils.isBlank(xRefId)) {
            throw log.throwing(new IllegalArgumentException("The cross-reference ID must be provided."));
        }
        if (source == null) {
            throw log.throwing(new IllegalArgumentException("The source must be provided."));
        }
        this.xRefId = xRefId;
        this.xRefName = xRefName;
        this.source = source;
        this.entityId = entityId;
    }

    public String getXRefId() {
        return xRefId;
    }

    public String getXRefName() {
        return xRefName;
    }

    public Source getSource() {
        return source;
    }
    
    public T getEntityId() {
        return entityId;
    }

    /**
     * @return  The {@code String} that is the cross-reference URL.
     */
    public String getXRefUrl() {
        log.entry();
        return log.exit(Source.buildXRefUrl(source, xRefId, entityId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XRef xRef = (XRef) o;
        return Objects.equals(xRefId, xRef.xRefId) &&
                Objects.equals(xRefName, xRef.xRefName) &&
                Objects.equals(source, xRef.source) &&
                Objects.equals(entityId, xRef.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xRefId, xRefName, source, entityId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("XRef [xRefId=").append(xRefId)
                .append(", xRefName=").append(xRefName)
                .append(", source=").append(source)
                .append(", entityId=").append(entityId).append("]");
        return sb.toString();
    }
}
