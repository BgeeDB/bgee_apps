package org.bgee.model;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.topanat.TopAnatRealTest;
import org.junit.Test;

public class TaskManagerTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(TopAnatRealTest.class.getName());
    @Override
    protected Logger getLogger() {
        return log;
    } 

    @Test
    public void shouldRegisterTaskManager() {
        TaskManager manager = null;
        try {
            TaskManager.registerTaskManager(Thread.currentThread().getId());
            manager = TaskManager.getTaskManager();
            assertSame(manager, TaskManager.getTaskManager(Thread.currentThread().getId()));
            
            manager.release();
            
            assertNull(TaskManager.getTaskManager());
            TaskManager.registerTaskManager(Thread.currentThread().getId());
            assertNotSame(manager, TaskManager.getTaskManager());
        } finally {
            if (TaskManager.getTaskManager() != null) {
                TaskManager.getTaskManager().release();
            }
        }
    }
}
