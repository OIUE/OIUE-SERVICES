package org.oiue.service.file.upload;

import java.util.Map;

public interface FileUploadService {
	public boolean registerListener(FileUploadListener listener);

	public void unregisterListener(FileUploadListener listener);

	public void unregisterAllListener();

	public void receive(String uploadFile, Map<?, ?> userInfo);
}
