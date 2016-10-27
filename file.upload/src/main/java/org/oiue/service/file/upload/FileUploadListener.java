package org.oiue.service.file.upload;

import java.util.Map;

public interface FileUploadListener {
	public void receive(String uploadFile, Map<?, ?> userInfo);
}
