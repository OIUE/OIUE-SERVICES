package org.oiue.service.driver.filter.convert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.map.MapUtil;

@SuppressWarnings({ "rawtypes","unchecked" })
public class ConvertConstants {

	private Logger logger;
	public static List<String> ignoreTargetTerminal = new ArrayList<>();
	public static Map<String, Map<String, String[]>> config = new HashMap<String, Map<String, String[]>>();

	public ConvertConstants(LogService logService) {
		logger = logService.getLogger(this.getClass());
	}

	public void updateConfigure(Map props) {
		logger.info("update configure, properties = " + props);
		try {

			String driverName = null;
			if (props != null){
				Iterator<Map.Entry> kv=props.entrySet().iterator();
				while(kv.hasNext()) {
					try {
						Map.Entry me = kv.next();
						String key = me.getKey()+ "";
						String value = (String) me.getValue();
						if (value != null) {
							String[] values = value.split(",");
							if (values.length == 5) {
								driverName = values[0];
								Map<String, String[]> convert = config.get(driverName);
								if (convert == null) {
									convert = new HashMap<String, String[]>();
									config.put(driverName, convert);
								}
								convert.put(values[1], values);
							} else {
								logger.error("config [" + key + "] is error!");
							}
						}
					} catch (Throwable e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
			logger.debug("over convert");
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		try {
			ignoreTargetTerminal=Arrays.asList(MapUtil.getString(props, "ignoreTargetTerminal","").split(","));
		}catch(Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
}
