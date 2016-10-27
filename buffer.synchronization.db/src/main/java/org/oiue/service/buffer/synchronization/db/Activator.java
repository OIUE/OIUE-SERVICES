package org.oiue.service.buffer.synchronization.db;

import java.util.Dictionary;

import org.oiue.service.buffer.BufferService;
import org.oiue.service.log.LogService;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.task.TaskService;

public class Activator extends FrameActivator {

    @Override
    public void start() throws Exception {
        this.start(new MulitServiceTrackerCustomizer() {
            private SynchronizationDbRefresh refreshDb;

            @Override
            public void removedService() {
                refreshDb.shutdown();
            }

            @SuppressWarnings("unused")
            @Override
            public void addingService() {
                LogService logService = getService(LogService.class);
                FactoryService factoryService = getService(FactoryService.class);
                TaskService taskService = getService(TaskService.class);
                BufferService bufferService = getService(BufferService.class);
                IResource iResource = getService(IResource.class);
                
                refreshDb = new SynchronizationDbRefresh(bufferService, taskService, factoryService, logService);

            }

            @Override
            public void updated(Dictionary<String, ?> props) {
                refreshDb.updateProps(props);
            }
        }, LogService.class,FactoryService.class,TaskService.class,BufferService.class,IResource.class);
    }

    @Override
    public void stop() throws Exception {}
}
