package org.oiue.service.buffer.sync.db.refresh;

import java.util.Dictionary;

import org.oiue.service.buffer.BufferService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.oiue.service.sql.SqlService;
import org.oiue.service.task.TaskService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private SyncDbRefresh refreshDb;
			
			@Override
			public void removedService() {
				if (refreshDb != null)
					refreshDb.shutdown();
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				TaskService taskService = getService(TaskService.class);
				BufferService bufferService = getService(BufferService.class);
				SqlService sqlService = getService(SqlService.class);
				
				refreshDb = new SyncDbRefresh(bufferService, taskService, sqlService, logService);
			}
			
			@Override
			public void updated(Dictionary<String, ?> props) {
				refreshDb.updateProps(props);
			}
		}, LogService.class, TaskService.class, BufferService.class, SqlService.class);
	}
	
	@Override
	public void stop() {}
}
