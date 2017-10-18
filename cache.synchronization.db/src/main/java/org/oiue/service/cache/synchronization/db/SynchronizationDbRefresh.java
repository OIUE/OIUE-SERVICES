package org.oiue.service.cache.synchronization.db;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.cache.Type;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.dmo.CallBack;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.service.task.Task;
import org.oiue.service.task.TaskService;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public class SynchronizationDbRefresh implements Task {
	private CacheServiceManager cacheService;
	private TaskService taskService;
	private FactoryService factoryService;
	private Logger logger;
	private List<String> jobNameList = new ArrayList<String>();

	public SynchronizationDbRefresh(CacheServiceManager cacheService, TaskService taskService, FactoryService factoryService, LogService logService) {
		this.cacheService = cacheService;
		this.taskService = taskService;
		this.factoryService = factoryService;
		this.logger = logService.getLogger(this.getClass());
	}
	private String data_source_name;
	public void updateProps(Dictionary props) {
		logger.info("update property, props = " + props);
		if (props == null) {
			logger.error("property is null, please check configure file");
			return;
		}

		for (String jobName : jobNameList) {
			taskService.unregister(jobName);
		}
		jobNameList.clear();

		try {
			String event_id = (String) props.get("task_event_id");
			data_source_name = (String) props.get("data_source_name");

			if(StringUtil.isEmptys(event_id))
				return;

			if(StringUtil.isEmptys(data_source_name))
				data_source_name=null;

			IResource iresource = factoryService.getBmo(IResource.class.getName());
			List<Map> object = (List) iresource.callEvent(event_id, data_source_name, new HashMap<>());

			for (Map event : object) {
				String quartz = MapUtil.getString(event, "quartz");
				String task_event_id = MapUtil.getString(event, "task_event_id");

				String jobName = this.getClass().getName() + "_" + task_event_id;
				if (taskService.registerCron(jobName, quartz, this, event)) {
					jobNameList.add(jobName);
					logger.info("create refresh job successed, task = " + event);
				} else {
					logger.warn("register cron error, task = " + event);
				}
			}

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void execute(Map context) {
		synchronized (context) {
			if (logger.isDebugEnabled())
				logger.debug("running:" + context);
			try {
				this.processRefreshTask(context);
			} catch (Throwable e) {
				logger.error(context + e.getMessage(), e);
			}
		}
	}

	public void shutdown() {
		logger.info("shutdown");
		for (String jobName : jobNameList) {
			taskService.unregister(jobName);
		}
		jobNameList.clear();
	}

	public Object max(Object a, Object b) {
		if ((a == null) && (b == null)) {
			return null;
		}

		if (a == null) {
			return b;
		}

		if (b == null) {
			return a;
		}

		if (a instanceof Number) {
			if (((Number) a).doubleValue() > ((Number) b).doubleValue()) {
				return a;
			} else {
				return b;
			}
		} else if (a instanceof Date) {
			if (((Date) a).after((Date) b)) {
				return a;
			} else {
				return b;
			}
		} else if (a instanceof Timestamp) {
			if (((Timestamp) a).after((Timestamp) b)) {
				return a;
			} else {
				return b;
			}
		} else if (a instanceof String) {
			if (a.toString().compareTo(b.toString()) > 0) {
				return a;
			} else {
				return b;
			}
		}
		return b;
	}

	private void processRefreshTask(Map task) {
		String event = (String) task.get("task_event");
		String event_id = (String) task.get("task_event_id");

		Object incrementReference = task.get("task_incrementReference");
		String incrementEvent_id = (String) task.get("task_incrementEvent_id");

		String cacheName= MapUtil.getString(task, "task_cache_service");

		String cacheKey= MapUtil.getString(task, "task_cachename");
		String command = MapUtil.getString(task, "task_command");
		String type = MapUtil.getString(task, "task_type");

		boolean isPut = command.equalsIgnoreCase("p");
		boolean isSwap = command.equalsIgnoreCase("s");
		String bufferValueKey = (type.length() >= 2 ? type.substring(1, 2) : null);

		String swapCacheKey = null;
		if (isSwap == true) {
			isPut = true;
			swapCacheKey = cacheKey;
			cacheKey = cacheKey + "_" + UUID.randomUUID().toString().replace("-", "");
		}

		Type bufferType;
		switch (type.charAt(0)) {
		case 'o':
			bufferType = Type.ONE;
			break;
		case 'm':
			bufferType = Type.MANY;
			break;
		case 's':
			bufferType = Type.SPATIAL;
			break;
		default:
			logger.error("sync action error, refresh = " + event);
			return;
		}

		try {
			String[] incrementReferences = null;
			if (incrementReference != null)
				if (incrementReference instanceof String) {
					incrementReferences = ((String) incrementReference).split(",");
					task.put("task_incrementReference", incrementReferences);
				} else {
					incrementReferences = (String[]) incrementReference;
					event_id = incrementEvent_id;
				}
			IResource iresource = factoryService.getBmo(IResource.class.getName());
			iresource.callEvent(event_id, this.data_source_name, task,new cb(incrementReferences, incrementReferences, task, isPut, cacheName, isSwap?swapCacheKey:cacheKey,bufferType,bufferValueKey));

			if (swapCacheKey != null) {
				cacheService.getCacheService(cacheName).swap(swapCacheKey, cacheKey);
				cacheService.getCacheService(cacheName).delete(cacheKey);
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}

	}
	class cb extends CallBack {
		String[] incrementReferences;
		Object incrementReference;
		Map task;

		Type bufferType;
		boolean isPut = true;
		String bufferValueKey ;
		String cacheName;
		String cacheKey;
		public cb(String[] incrementReferences,Object incrementReference,Map task,boolean isPut,String cacheName,String cacheKey,Type bufferType,String bufferValueKey){
			this.bufferType=bufferType;
			this.incrementReferences=incrementReferences;
			this.incrementReference=incrementReference;
			this.task=task;
			this.isPut=isPut;
			this.cacheKey=cacheKey;
			this.cacheName=cacheName;
			this.bufferValueKey=bufferValueKey;
		}
		@Override
		public boolean callBack(Map record) {
			try {
				if (incrementReference != null) {
					for (int i = 0; i < incrementReferences.length; i++) {
						task.put(incrementReferences[i], max(MapUtil.get(task, incrementReferences[i]), MapUtil.get(record, incrementReferences[i])));
					}
				}

				if (isPut == true) {
					if (bufferType == Type.SPATIAL) {
						//							if (record.containsKey("x")) {
						//								if (bufferValueKey != null) {
						//									cacheService.put(bufferName, (String) record.get("k"), (Double) record.get("x"), (Double) record.get("y"), record.get(bufferValueKey));
						//								} else {
						//									cacheService.put(bufferName, (String) record.get("k"), (Double) record.get("x"), (Double) record.get("y"), record);
						//								}
						//							}
					} else {
						if (bufferValueKey != null) {
							cacheService.getCacheService(cacheName).put(cacheKey, (String) record.get("k"), record.get(bufferValueKey), bufferType);
						} else {
							cacheService.getCacheService(cacheName).put(cacheKey, (String) record.get("k"), record, bufferType);
						}
					}
				} else {
					if (bufferType == Type.MANY) {
						//							if (bufferValueKey != null) {
						//								cacheService.delete(bufferName, (String) record.get("k"), record.get(bufferValueKey));
						//							} else {
						//								cacheService.delete(bufferName, (String) record.get("k"), record);
						//							}
					} else {
						cacheService.getCacheService(cacheName).delete(cacheKey, (String) record.get("k"));
					}
				}

			} catch (Exception e) {
				logger.error("cacheName:{} cacheKey:{} record:{} error:{}",cacheName,cacheKey,record,e.getMessage());
				logger.error(e.getMessage(),e);
			}
			return false;
		}
	}

}
