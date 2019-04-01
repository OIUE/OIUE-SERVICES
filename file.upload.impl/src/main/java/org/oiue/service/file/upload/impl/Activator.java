package org.oiue.service.file.upload.impl;

import java.util.Map;

import org.oiue.service.file.upload.FileUploadService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private FileUploadServiceImpl fileUploadService;
			
			@Override
			public void removedService() {
				fileUploadService.unregisterAllListener();
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				fileUploadService = new FileUploadServiceImpl(logService);
				
				registerService(FileUploadService.class, fileUploadService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			
			}
		}, LogService.class);
	}
	
	@Override
	public void stop() {}
}
