package org.oiue.service.event.etl.csv;

import java.util.Dictionary;

import org.oiue.service.event.etl.impl.InputStepMetaManger;
import org.oiue.service.log.LogService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
public class Activator extends FrameActivator {

	@Override
	public void start()  {
		this.start(new MulitServiceTrackerCustomizer() {

			@Override
			public void removedService() {}

			@Override
			public void addingService() {
				LogService logService = getService(LogService.class);
				CsvInputStepMeta.logger = logService.getLogger(CsvInputStepMeta.class);

				InputStepMetaManger.registerInputStepMeta("csv", new CsvInputStepMeta());
			}

			@Override
			public void updated(Dictionary<String, ?> props) {
			}
		}, LogService.class);
	}

	@Override
	public void stop()  {}
}
