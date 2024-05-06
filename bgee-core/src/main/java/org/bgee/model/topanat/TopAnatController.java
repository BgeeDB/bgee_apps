package org.bgee.model.topanat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.ManageReadWriteLocks;
import org.bgee.model.ServiceFactory;
import org.bgee.model.function.PentaFunction;
import org.bgee.model.job.Job;

/**
 * This class controls the whole topAnat process by running a {@link TopAnatAnalysis} for each
 * provided {@link TopAnatParams} instance and returns {@link TopAnatResults} objects.
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2016
 * @since Bgee 13
 */
//XXX: rename to TopAnatService, to be obtain through the ServiceFactory?
//Or create another TopAnatService class?
//This TopAnatService should receive the JobService instance at instantiation?
public class TopAnatController {

    private final static Logger log = LogManager.getLogger(TopAnatController.class.getName()); 

    /**
     * A {@code List} containing one {@code TopAnatParams} for each analysis to be conducted
     */
    private final List<TopAnatParams> topAnatParams;

    /**
     * A {@code BgeeProperties} instance to provide all properties values
     */
    private final BgeeProperties props;

    /**
     * A {@code ReadWriteLock} instance to manage read/write locks
     */
    private ManageReadWriteLocks readWriteLocks;

    /**
     * A {@code ServiceFactory} to be injected in {@code TopAnatAnalysis} to provide
     * various service instances
     */
    private final ServiceFactory serviceFactory;

    /**
     * A {@code PentaFunction} allowing to obtain new {@code TopAnatAnalysis} instances.
     */
    private final PentaFunction<TopAnatParams, BgeeProperties, ServiceFactory, TopAnatRManager,
    TopAnatController, TopAnatAnalysis> 
    topAnatAnalysisSupplier;
    
    /**
     * An {@code Optional} {@code Job} allowing to track advancement of the analyses.
     */
    private final Optional<Job> job;

    /**
     * Constructor building a {@code TopAnatController} given a list of {@code TopAnatParams}
     * 
     * @param topAnatParams     A {@code List} of {@code TopAnatParams} that will produce one
     *                          {@code TopAnatAnalysis} each.
     */
    public TopAnatController(List<TopAnatParams> topAnatParams) {
        this(topAnatParams, BgeeProperties.getBgeeProperties(), new ServiceFactory());
    }
    /**
     * Constructor building a {@code TopAnatController} given a list of {@code TopAnatParams},
     * a {@code BgeeProperties} instance, and a {@code ServiceFactory} instance.
     * 
     * @param topAnatParams     A {@code List} of {@code TopAnatParams} that will produce one
     *                          {@code TopAnatAnalysis} each.
     * @param props             A {@code BgeeProperties} instance to provide all properties values
     * @param serviceFactory    A {@code ServiceFactory} to be injected in {@code TopAnatAnalysis} 
     *                          to provide various service instances
     */
    public TopAnatController(List<TopAnatParams> topAnatParams, BgeeProperties props, 
            ServiceFactory serviceFactory) {
        this(topAnatParams, props, serviceFactory, TopAnatAnalysis::new);
    }
    /**
     * Constructor building a {@code TopAnatController} given a list of {@code TopAnatParams},
     * a {@code BgeeProperties} instance, a {@code ServiceFactory} instance, and a {@code TaskManager}.
     * 
     * @param topAnatParams     A {@code List} of {@code TopAnatParams} that will produce one
     *                          {@code TopAnatAnalysis} each.
     * @param props             A {@code BgeeProperties} instance to provide all properties values
     * @param serviceFactory    A {@code ServiceFactory} to be injected in {@code TopAnatAnalysis} 
     *                          to provide various service instances
     * @param taskManager       A {@code TaskManager}
     */
    public TopAnatController(List<TopAnatParams> topAnatParams, BgeeProperties props, 
            ServiceFactory serviceFactory, Job job) {
        this(topAnatParams, props, serviceFactory, TopAnatAnalysis::new, job);
    }

    /**
     * Constructor building a {@code TopAnatController} given a list of {@code TopAnatParams},
     * a {@code BgeeProperties} instance, a {@code ServiceFactory} instance,
     * and a custom supplier for obtaining {@code TopAnatAnalysis}.
     * 
     * @param topAnatParams     A {@code List} of {@code TopAnatParams} that will produce one
     *                          {@code TopAnatAnalysis} each.
     * @param props             A {@code BgeeProperties} instance to provide all properties values
     * @param serviceFactory    A {@code ServiceFactory} to be injected in {@code TopAnatAnalysis} 
     *                          to provide various service instances
     * @param topAnatAnalysisSupplier   A {@code PentaFunction} allowing to obtain new 
     *                                  {@code TopAnatAnalysis} instances.
     */   
    public TopAnatController(List<TopAnatParams> topAnatParams, BgeeProperties props, 
            ServiceFactory serviceFactory, 
            PentaFunction<TopAnatParams, BgeeProperties, ServiceFactory, TopAnatRManager, TopAnatController,
            TopAnatAnalysis> topAnatAnalysisSupplier) {
        this(topAnatParams, props, serviceFactory, topAnatAnalysisSupplier, null);
    }

    /**
     * Constructor building a {@code TopAnatController} given a list of {@code TopAnatParams},
     * a {@code BgeeProperties} instance, a {@code ServiceFactory} instance, a {@code TaskManager},
     * and a custom supplier for obtaining {@code TopAnatAnalysis}.
     * 
     * @param topAnatParams     A {@code List} of {@code TopAnatParams} that will produce one
     *                          {@code TopAnatAnalysis} each.
     * @param props             A {@code BgeeProperties} instance to provide all properties values
     * @param serviceFactory    A {@code ServiceFactory} to be injected in {@code TopAnatAnalysis} 
     *                          to provide various service instances
     * @param taskManager       A {@code TaskManager}
     * @param topAnatAnalysisSupplier   A {@code PentaFunction} allowing to obtain new 
     *                                  {@code TopAnatAnalysis} instances.
     */  
    public TopAnatController(List<TopAnatParams> topAnatParams, BgeeProperties props, 
            ServiceFactory serviceFactory, 
            PentaFunction<TopAnatParams, BgeeProperties, ServiceFactory, TopAnatRManager, TopAnatController,
            TopAnatAnalysis> topAnatAnalysisSupplier, Job job) {
        log.traceEntry("{}, {}, {}, {}, {}", topAnatParams, props, serviceFactory,
                topAnatAnalysisSupplier, job);

        if (topAnatParams == null || topAnatParams.isEmpty() || 
                topAnatParams.stream().anyMatch(Objects::isNull)) {
            throw log.throwing(new IllegalArgumentException("At least one TopAnatParams "
                    + "must be provided, and none should be null"));
        }
        if (topAnatAnalysisSupplier == null) {
            throw log.throwing(new IllegalArgumentException("A supplier of TopAnatAnalysis "
                    + "must be provided"));
        }
        if (props == null) {
            throw log.throwing(new IllegalArgumentException("A BgeeProperties object must be provided."));
        }
        if (serviceFactory == null) {
            throw log.throwing(new IllegalArgumentException("A ServiceFactory must be provided."));
        }
        this.topAnatParams = Collections.unmodifiableList(new ArrayList<>(topAnatParams));
        this.topAnatAnalysisSupplier = topAnatAnalysisSupplier;
        this.props = props;
        this.serviceFactory = serviceFactory;
        this.readWriteLocks = new ManageReadWriteLocks();
        this.job = Optional.ofNullable(job);

        log.traceExit();
    }

    /**
     * Proceed to the analysis and return results
     * 
     * @return a {@code Stream} of {@code TopAnatResults}
     */
    public Stream<TopAnatResults> proceedToTopAnatAnalyses() {
        log.traceEntry();

        // Create TopAnatAnalysis for each TopAnatParams
        //TODO: TopAnatAnalysis should be provided with the Job instance to be able to use 
        //'checkInterrupted'

        return log.traceExit(this.topAnatParams.stream()
                .map(params -> this.topAnatAnalysisSupplier.apply(params, this.props, 
                        this.serviceFactory, new TopAnatRManager(this.props, params),this))
                .map(analysis -> {
                    try {
                        //if task in job not yet started (first analysis), start it.
                        if (this.job.map(t -> !t.isStarted()).orElse(false)) {
                            this.job.ifPresent(t -> t.startJob());
                        } else {
                            //otherwise, check if job was interrupted and move to next subtask
                            this.job.ifPresent(t -> {
                                try {
                                    t.checkInterrupted();
                                    t.nextTask();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }

                        TopAnatResults results = analysis.proceedToAnalysis();
                        
                        //end job if last analysis
                        if (this.job.map(t -> t.getCurrentTaskIndex()).orElse(-1) == 
                                (this.topAnatParams.size() - 1)) {
                            this.job.ifPresent(t -> t.completeWithSuccess());
                        }

                        return results;
                    } catch (Throwable e) {
                        //catch and throw this error in DEBUG level because we don't want 
                        //to log those as errors when we requested a Thread interruption
                        log.catching(Level.DEBUG, e);
                        this.job.ifPresent(t -> t.complete());
                        throw log.throwing(Level.DEBUG, new RuntimeException(e));
                    }
                }));
    }

    /**
     * @return A {@code BgeeProperties} instance to provide all properties values
     */
    public BgeeProperties getBgeeProperties() {
        return this.props;
    }
    /**
     * @return A {@code ManageReadWriteLocks} instance to provide all read/write lock info
     */
    public ManageReadWriteLocks getReadWriteLocks() {
        return this.readWriteLocks;
    }
    /**
     * @return  An {@code Optional} {@code Job} allowing to track advancement of the analyses.
     */
    public Optional<Job> getJob() {
        return job;
    }

    /**
     * @return  A {@code List} containing one {@code TopAnatParams} for each analysis to be conducted
     */
    public List<TopAnatParams> getTopAnatParams() {
        return this.topAnatParams;
    }

    /**
     * @return a {@code boolean} that tells whether all analyses are done
     */
    public boolean areAnalysesDone(){
        log.traceEntry();
        return log.traceExit(this.topAnatParams.stream()
                .map(params -> this.topAnatAnalysisSupplier.apply(params, this.props, 
                        this.serviceFactory, new TopAnatRManager(this.props, params), this))
                .allMatch(a -> a.isAnalysisDone()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TopAnatController [readWriteLocks=").append(readWriteLocks).append(", props=")
        .append(props).append(", serviceFactory=").append(serviceFactory)
        .append(", job=").append(job).append(", topAnatAnalysisSupplier=")
        .append("").append(", topAnatParams=").append(topAnatParams)
        .append("]");
        return builder.toString();
    }
}
