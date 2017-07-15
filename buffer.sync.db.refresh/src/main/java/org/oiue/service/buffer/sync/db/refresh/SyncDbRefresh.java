package org.oiue.service.buffer.sync.db.refresh;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.oiue.service.buffer.BufferService;
import org.oiue.service.buffer.Type;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.sql.SqlService;
import org.oiue.service.sql.SqlServiceResult;
import org.oiue.service.task.Task;
import org.oiue.service.task.TaskService;
import org.oiue.tools.map.MapUtil;

@SuppressWarnings("serial")
public class SyncDbRefresh implements Task {
	private BufferService bufferService;
	private TaskService quartzService;
	private SqlService sqlService;
	private Logger logger;
	private List<String> jobNameList = new ArrayList<String>();

	public SyncDbRefresh(BufferService bufferService, TaskService quartzService, SqlService sqlService, LogService logService) {
		this.bufferService = bufferService;
		this.quartzService = quartzService;
		this.sqlService = sqlService;
		this.logger = logService.getLogger(this.getClass());
	}

	@SuppressWarnings("rawtypes")
	public void updateProps(Dictionary props) {
		logger.info("update property, props = " + props);
		if (props == null) {
			logger.error("property is null, please check configure file");
			return;
		}

		for (String jobName : jobNameList) {
			quartzService.unregister(jobName);
		}
		jobNameList.clear();

		for (String refresh : ((String) props.get("refresh")).split(",")) {
			refresh = refresh.trim();
			String quartz = (String) props.get("refresh." + refresh + ".quartz");
			String sql = (String) props.get("refresh." + refresh + ".sql");
			String incrementReference = (String) props.get("refresh." + refresh + ".increment.reference");
			String incrementSql = (String) props.get("refresh." + refresh + ".increment.sql");

			if ((quartz == null) || (sql == null)) {
				logger.warn("refresh configure error, refresh = " + refresh + ", quartz = " + quartz + ", sql = " + sql + ", increment.reference = " + incrementReference + ", increment.sql = "
						+ incrementSql);
				continue;
			}

			Map job = new HashMap<>();
			job.put("Name",refresh);
			job.put("Quartz",quartz);
			job.put("sql",sql);

			if (incrementReference != null) {
				String[] tmp = incrementReference.split(",");
				for (int i = 0; i < tmp.length; i++) {
					tmp[i] = tmp[i].trim();
				}
				job.put("IncrementReference",tmp);
			}

			if (incrementSql != null) {
				job.put("IncrementSql",incrementSql.trim());
			}

			String jobName = this.getClass().getName() + "_" + refresh;
			if (quartzService.registerCron(jobName, quartz , this, job)) {
				jobNameList.add(jobName);
				logger.info("create refresh job successed, job = " + job);
			} else {
				logger.warn("register cron error, job = " + job);
			}
		}
	}

	public void shutdown() {
		logger.info("shutdown");
		for (String jobName : jobNameList) {
			quartzService.unregister(jobName);
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

	@SuppressWarnings("unchecked")
	private void processRefreshJob(Map job) {

		String sql = null;
		List<Object> params = null;
		if (job.get("IncrementReference") == null) {
			sql = MapUtil.getString(job, "sql");
		} else {
			Object incrementReferenceValue[] = (Object[]) job.get("IncrementReferenceValue");
			if (incrementReferenceValue == null) {
				sql = MapUtil.getString(job, "sql");
			} else {
				sql = MapUtil.getString(job, "IncrementSql");
				params = Arrays.asList(incrementReferenceValue);
			}
		}

		// sql = buffer_name,[s,p,r],[o,m,s][v],[data_source],[sql]

		String args[] = sql.split(",", 5);
		String bufferName = args[0].trim();
		boolean isPut = args[1].equalsIgnoreCase("p");
		boolean isSwap = args[1].equalsIgnoreCase("s");
		String bufferValueKey = (args[2].trim().length() >= 2 ? args[2].trim().substring(1, 2) : null);
		String alias = args[3].trim();
		String sqlString = args[4].trim();

		String swapBufferName = null;
		if (isSwap == true) {
			isPut = true;
			swapBufferName = bufferName;
			bufferName = bufferName + "_" + UUID.randomUUID().toString().replace("-", "");
		}

		if (args.length != 5) {
			logger.error("refresh format error, refresh = " + job.get("Name"));
			return;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("runing refresh, refresh = " +  job.get("Name"));
		}

		Type bufferType;
		switch (args[2].trim().charAt(0)) {
		case 'o':
			bufferType = Type.KeyToOne;
			break;
		case 'm':
			bufferType = Type.KeyToMany;
			break;
		case 's':
			bufferType = Type.KeyToSpatial;
			break;
		default:
			logger.error("sync action error, refresh = " +  job.get("Name"));
			return;
		}

		SqlServiceResult result = sqlService.selectMap(alias, sqlString, params);
		if (!result.getResult()) {
			if (logger.isDebugEnabled()) {
				logger.debug("do job sql error, error = " + result.getData() + ", job = " + job);
				return;
			}
		}

		List<Map<String, Object>> records = (List<Map<String, Object>>) result.getData();
		String incrementReference[] = (String[]) job.get("IncrementReference");
		Object tmpIncrementReferenceValue[] = null;
		if (incrementReference != null) {
			tmpIncrementReferenceValue = new Object[incrementReference.length];
		}
		for (Map<String, Object> record : records) {
			if (incrementReference != null) {
				for (int i = 0; i < incrementReference.length; i++) {
					tmpIncrementReferenceValue[i] = max(tmpIncrementReferenceValue[i], MapUtil.get(record, incrementReference[i]));
				}
			}

			if (isPut == true) {
				if (bufferType == Type.KeyToSpatial) {
					if (record.containsKey("x")) {
						if (bufferValueKey != null) {
							bufferService.put(bufferName, (String) record.get("k"), (Double) record.get("x"), (Double) record.get("y"), record.get(bufferValueKey));
						} else {
							bufferService.put(bufferName, (String) record.get("k"), (Double) record.get("x"), (Double) record.get("y"), record);
						}
					}
				} else {
					if (bufferValueKey != null) {
						bufferService.put(bufferName, (String) record.get("k"), record.get(bufferValueKey), bufferType);
					} else {
						bufferService.put(bufferName, (String) record.get("k"), record, bufferType);
					}
				}
			} else {
				if (bufferType == Type.KeyToMany) {
					if (bufferValueKey != null) {
						bufferService.remove(bufferName, (String) record.get("k"), record.get(bufferValueKey));
					} else {
						bufferService.remove(bufferName, (String) record.get("k"), record);
					}
				} else {
					bufferService.remove(bufferName, (String) record.get("k"));
				}
			}
		}
		// update max increment reference value
		if (incrementReference != null) {
			Object[] incrementReferenceValue = (Object[]) job.get("IncrementReferenceValue");
			if (incrementReferenceValue == null) {
				job.put("IncrementReferenceValue",tmpIncrementReferenceValue);
			} else {
				for (int i = 0; i < incrementReferenceValue.length; i++) {
					incrementReferenceValue[i] = max(tmpIncrementReferenceValue[i], incrementReferenceValue[i]);
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("update increment reference value, reference = " + Arrays.toString((String[]) job.get("IncrementReference")) + ", value = " + Arrays.toString((Object[]) job.get("IncrementReferenceValue")));
			}
		}

		if (swapBufferName != null) {
			bufferService.swap(swapBufferName, bufferName);
			bufferService.remove(bufferName);
		}
	}

	@Override
	public void execute(Map context) {
		try {
			synchronized (context) {
				processRefreshJob(context);
				if (logger.isDebugEnabled()) {
					logger.debug("job execute successed, job = " + context);
				}
			}
		} catch (Throwable e) {
			if (logger.isErrorEnabled()) {
				logger.error("process refresh job error, job = " + context, e);
			}
		}
	}
}
