package org.oiue.service.task;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface Task extends Serializable {
	void execute(Map context);
}
