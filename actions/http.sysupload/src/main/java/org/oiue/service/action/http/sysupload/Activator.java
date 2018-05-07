package org.oiue.service.action.http.sysupload;

import java.util.Dictionary;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.file.upload.FileUploadService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
import org.osgi.service.http.HttpService;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private String url = getProperty("org.oiue.service.action.http.root") + "/sysupload";
			private HttpService httpService;
			private UploadPostServlet upload;
			
			@Override
			public void removedService() {
				httpService.unregister(url);
			}
			
			@Override
			public void addingService() {
				httpService = getService(HttpService.class);
				LogService logService = getService(LogService.class);
				ActionService actionService = getService(ActionService.class);
				OnlineService onlineService = getService(OnlineService.class);
				FileUploadService fileUploadService = getService(FileUploadService.class);
				FactoryService factoryService = getService(FactoryService.class);
				
				upload = new UploadPostServlet(actionService, onlineService, logService, fileUploadService, factoryService, getProperty("user.dir"));
				Logger log = logService.getLogger(this.getClass());
				log.debug("绑定url：" + url);
				try {
					httpService.registerServlet(url, upload, null, null);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			
			@Override
			public void updated(Dictionary<String, ?> props) {
				try {
					upload.updated(props);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}, HttpService.class, ActionService.class, LogService.class, FileUploadService.class, OnlineService.class, FactoryService.class, IResource.class);
	}
	
	@Override
	public void stop() {}
}
