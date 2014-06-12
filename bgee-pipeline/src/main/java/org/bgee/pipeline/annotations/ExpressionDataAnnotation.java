package org.bgee.pipeline.annotations;

import java.util.Collections;
import java.util.Set;

/**
 * Class to perform tasks related to expression data annotations.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class ExpressionDataAnnotation {
    /**
     * An unmodifiable {@code Set} of {@code String}s representing paths to 
     * expression data annotation files, which this object will operate on.
     */
    private final Set<String> annotationFiles;
    /**
     * Default constructor private, this class is designed to operate on 
     * expression data annotation files, provided at instantiation.
     */
    @SuppressWarnings("unused")
    private ExpressionDataAnnotation() {
        this(null);
    }
    public ExpressionDataAnnotation(Set<String> annotationFiles) {
        this.annotationFiles = Collections.unmodifiableSet(annotationFiles);
    }
    
    
    
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s representing paths to 
     *          expression data annotation files, which this object will operate on.
     */
    public Set<String> getAnnotationFiles() {
        return this.annotationFiles;
    }
}
