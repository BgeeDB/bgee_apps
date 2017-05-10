package org.bgee.model.topanat;

import java.util.Arrays;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TestAncestor;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.job.JobService;
import org.bgee.model.job.exception.ThreadAlreadyWorkingException;
import org.bgee.model.topanat.exception.MissingParameterException;

public class TopAnatRealTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(TopAnatRealTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    } 

    //@Test
    public void test() throws MissingParameterException, ThreadAlreadyWorkingException {
        Properties setProps = new Properties();
        setProps.setProperty(BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY, 
                "/Users/admin/Desktop/topanat/results/");
        setProps.setProperty(BgeeProperties.TOP_ANAT_R_WORKING_DIRECTORY_KEY, 
                "/Users/admin/Desktop/topanat/results/");
        setProps.setProperty(MySQLDAOManager.USER_KEY, "bgee");
        setProps.setProperty(MySQLDAOManager.PASSWORD_KEY, "bgee");
        setProps.setProperty(MySQLDAOManager.JDBC_DRIVER_NAMES_KEY, 
                "com.mysql.jdbc.Driver,net.sf.log4jdbc.sql.jdbcapi.DriverSpy");
        setProps.setProperty(MySQLDAOManager.JDBC_URL_KEY, 
                "jdbc:log4jdbc:mysql://altbioinfo.unil.ch:3306/bgee_v13");
        
        DAOManager manager = DAOManager.getDAOManager(setProps);
        BgeeProperties props = BgeeProperties.getBgeeProperties(setProps);
        ServiceFactory serviceFactory = new ServiceFactory(manager);
        
        TopAnatParams params = new TopAnatParams.Builder(Arrays.asList(
                "ENSXETG00000014994", 
                "ENSXETG00000006965", 
                "ENSXETG00000014206", 
                "ENSXETG00000005746", 
                "ENSXETG00000024006", 
                "ENSXETG00000003484", 
                "ENSXETG00000019568", 
                "ENSXETG00000001992", 
                "ENSXETG00000013496", 
                "ENSXETG00000025008", 
                "ENSXETG00000002868", 
                "ENSXETG00000017807", 
                "ENSXETG00000003378", 
                "ENSXETG00000002761", 
                "ENSXETG00000012899", 
                "ENSXETG00000003600", 
                "ENSXETG00000023793", 
                "ENSXETG00000026423", 
                "ENSXETG00000014474", 
                "ENSXETG00000001573", 
                "ENSXETG00000011784"
                ), 
                8364, SummaryCallType.ExpressionSummary.EXPRESSED).fdrThreshold(1).pvalueThreshold(1).summaryQuality(SummaryQuality.GOLD).build();
        
        JobService jobService = new JobService(props);
        try {
            TopAnatController controller = new TopAnatController(Arrays.asList(params), 
                    props, serviceFactory, jobService.registerNewJob());
            controller.proceedToTopAnatAnalyses().flatMap(e -> {
                try {
                    return e.getRows().stream();
                } catch (Throwable exc) {
                    log.throwing(new RuntimeException(exc));
                }
                return null;
            }).forEach(e -> log.info(e));
        } finally {
            if (jobService.getJob() != null) {
                jobService.getJob().release();
            }
        }
    }
}
