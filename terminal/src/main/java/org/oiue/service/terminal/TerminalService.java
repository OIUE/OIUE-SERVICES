package org.oiue.service.terminal;

import java.io.Serializable;
import java.util.Map;

public interface TerminalService extends Serializable {
	String getNewVersion(Map data, Map event, String tokenid);
	
	Map getFOTAInfo(Map data, Map event, String tokenid);
}