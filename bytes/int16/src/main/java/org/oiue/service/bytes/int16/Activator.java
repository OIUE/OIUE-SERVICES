package org.oiue.service.bytes.int16;

import java.util.Map;

import org.oiue.service.bytes.api.BytesDecodeEncoded;
import org.oiue.service.bytes.api.BytesService;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {
	
	@Override
	public void start() {
		this.start(new MulitServiceTrackerCustomizer() {
			private final static String type = "int";
			private BytesDecodeEncoded intService;
			private BytesService bytesService;
			
			@Override
			public void removedService() {
				bytesService.unRregisterDecodeEncoded(type);
			}
			
			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				bytesService = getService(BytesService.class);
				intService = new IntDecodeEncoded(logService);
				
				bytesService.registerDecodeEncoded(type, intService);
			}
			
			@Override
			public void updatedConf(Map<String, ?> props) {
			
			}
		}, LogService.class, BytesService.class);
	}
	
	@Override
	public void stop() {}
}
