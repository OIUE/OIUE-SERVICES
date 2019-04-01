package org.oiue.service.rectification;

import java.util.Map;
import org.oiue.tools.StatusResult;

public interface RectificationService {
	StatusResult  convert(Map data);
}