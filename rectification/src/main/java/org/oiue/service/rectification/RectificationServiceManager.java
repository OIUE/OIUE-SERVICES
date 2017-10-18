package org.oiue.service.rectification;


public interface RectificationServiceManager extends RectificationService {

	public boolean registerRectificationService(String name,RectificationService rectification);

	public boolean unRegisterRectificationService(String name);

	public RectificationService getRectificationService(String name);
}
