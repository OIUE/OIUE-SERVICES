package org.oiue.service.file.upload.impl;

import java.util.ArrayList;
import java.util.Map;

import org.oiue.service.file.upload.FileUploadListener;
import org.oiue.service.file.upload.FileUploadService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;

public class FileUploadServiceImpl implements FileUploadService {
	private ArrayList<FileUploadListener> listenerList = new ArrayList<FileUploadListener>();
	private Logger logger;

	public FileUploadServiceImpl(LogService logService) {
		logger = logService.getLogger(this.getClass());
	}

	public void receive(String uploadFile, Map<?, ?> userInfo) {
		for (FileUploadListener e : listenerList) {
			try {
				e.receive(uploadFile, userInfo);
			} catch (Exception ex) {
				logger.error("FileUploadListener receive error:"+e.getClass().getName()+"|"+ex.getMessage(), ex);
			}
		}
	}

	@Override
	public boolean registerListener(FileUploadListener listener) {
		logger.info("register listener, listener = " + listener);
		if (listenerList.contains(listener)) {
			return false;
		} else {
			listenerList.add(listener);
			return true;
		}
	}

	@Override
	public void unregisterListener(FileUploadListener listener) {
		logger.info("unregister listener, listener = " + listener);
		listenerList.remove(listener);
	}

	@Override
	public void unregisterAllListener() {
		logger.info("unregister all listener");
		listenerList.clear();
	}
}
