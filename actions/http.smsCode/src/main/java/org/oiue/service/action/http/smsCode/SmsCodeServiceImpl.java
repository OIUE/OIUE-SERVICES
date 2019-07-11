package org.oiue.service.action.http.smsCode;

import java.util.Map;

import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.cache.Type;
import org.oiue.service.driver.api.DriverDataField;
import org.oiue.service.driver.api.DriverService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.odp.base.FactoryService;
import org.oiue.service.odp.res.api.IResource;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "unused" })
public class SmsCodeServiceImpl implements SmsCodeService {
	private static final long serialVersionUID = -6327347468651806863L;
	private Logger logger;
	private DriverService driverService;
	protected static FactoryService _factoryService;
	protected static CacheServiceManager _cacheServiceManager;
	
	
	public SmsCodeServiceImpl(LogService logService, DriverService driverService,FactoryService factoryService, CacheServiceManager cacheServiceManager) {
		super();
		this.logger = logService.getLogger(this.getClass());
		this.driverService=driverService;
		_factoryService=factoryService;
		_cacheServiceManager=cacheServiceManager;
	}
	
	public void updated(Map props) {
		try {
			product = MapUtil.getString(props, "product", "Sm@rtMapX");
		} catch (Exception e) {}
		try {
			signature = MapUtil.getString(props, "signature", "傻瓜地图");
		} catch (Exception e) {}
		try {
			templateCode = MapUtil.getString(props, "templateCode", "SMS_25750577");
		} catch (Exception e) {}
		try {
			LENGTH = MapUtil.getInt(props, "codeLength",4);
		} catch (Exception e) {}
		try {
			onlynum = StringUtil.isFalse(MapUtil.getString(props, "onlynum","Y"));
		} catch (Exception e) {}
	}
	private String product;
	private String signature;
	private String templateCode;
	private int LENGTH = 4;
	private boolean onlynum = true;
	@Override
	public Object sendSmsCode(Map data, Map event, String token) {
		String tel = MapUtil.getString(data, "phoneNo");

		String sRand = "";
		for (int i = 0; i < LENGTH; i++) {
			String tmp = getRandomChar();
			while (tmp.equalsIgnoreCase("0") || tmp.equalsIgnoreCase("o") || tmp.equalsIgnoreCase("1") || tmp.equalsIgnoreCase("i") || tmp.equalsIgnoreCase("l") || tmp.equalsIgnoreCase("z") || tmp.equalsIgnoreCase("2")) {
				tmp = getRandomChar();
			}
			sRand += tmp;
		}
		data.put("smsCode", sRand);
		data.put("product", product);
		data.put("signature", signature);
		data.put("templateCode", templateCode);
		data.put(DriverDataField.driverName, "aliSMS");
		
		_cacheServiceManager.put("_system_phone_code_", tel, sRand.toLowerCase(), Type.ONE, 180);
		IResource iresource = _factoryService.getBmo(IResource.class.getName());
//		iresource.executeEvent(event, null, data, null);
		
		return driverService.send(data);
	}

	@Override
	public Object verifySmsCode(Map data, Map event, String token) {
		String tel = MapUtil.getString(data, "phoneNo");
		String code = MapUtil.getString(data, "code");
		if(code!=null)
			code=code.toLowerCase();
		
		Object smscode = _cacheServiceManager.get("_system_phone_code_", tel);
		if (smscode != null && smscode.equals(code)) {
			return 1;
		}else {
			return 0;
		}
	}
	private String getRandomChar() {
		int rand = (int) Math.round(Math.random() * 2);
		long itmp = 0;
		char ctmp = '\u0000';
		switch (rand) {
			case 1:
				itmp = (long) Math.round(Math.random() * 25 + 97);
				ctmp = (char) itmp;
				return String.valueOf(ctmp);
			default:
				itmp = (long) (Math.random() * 9);
				return String.valueOf(itmp);
		}
	}
	
	private String getRandomNum() {
		int rand = (int) Math.round(Math.random() * 9);
		return String.valueOf(rand);
	}

}