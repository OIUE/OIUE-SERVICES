package org.oiue.service.driver.sms.alisms;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.oiue.service.driver.api.Driver;
import org.oiue.service.driver.api.DriverListener;
import org.oiue.service.driver.api.DriverService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.map.MapUtil;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

@SuppressWarnings({ "unused" })
public class AliSMS implements Driver {
	public final String _DriverName = "aliSMS";
	private Map<?, ?> props;
	private DriverService smsService;
	private DriverListener listener;
	public Logger logger;
	
	// 初始化ascClient需要的几个参数
	final String product = "Dysmsapi";// 短信API产品名称（短信产品名固定，无需修改）
	final String domain = "dysmsapi.aliyuncs.com";// 短信API产品域名（接口地址固定，无需修改）
	// 替换成你的AK
	String accessKeyId="LTAIXwPhhOrWJl5q";// 你的accessKeyId,参考本文档步骤2
	String accessKeySecret="O5RQVKompXeZ2ofd3Ym2NgIZZTZr1b";// 你的accessKeySecret，参考本文档步骤2
	
	public AliSMS(DriverService smsService, LogService logService) {
		this.smsService = smsService;
		this.logger = logService.getLogger(this.getClass());
		logger.info("start AliSMS");
	}
	
	@Override
	public void unregistered() {
		smsService.unregisterDriver(_DriverName);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public StatusResult send(Map paramSMS) {
		Map<String, Object> rtn = null;
		StatusResult sr = new StatusResult();
		try {
			if (logger.isInfoEnabled()) {
				logger.info("send sms :" + paramSMS);
			}
			String tel = MapUtil.getString(paramSMS, "phoneNo");
			String code = MapUtil.getString(paramSMS, "smsCode");
//			String type = MapUtil.getString(paramSMS, "smsType");
			String signature = MapUtil.getString(paramSMS, "signature");
			String productName = MapUtil.getString(paramSMS, "product");
			String templateCode = MapUtil.getString(paramSMS, "templateCode");
			
			IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
			DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
			IAcsClient client = new DefaultAcsClient(profile);
			
			SendSmsRequest request = new SendSmsRequest();
			// 待发送手机号
			request.setPhoneNumbers(tel);
			// 短信签名
			request.setSignName(signature);
			// 短信模板
			request.setTemplateCode(templateCode);
			
			String param = "{\"code\":\"" + code + "\",\"product\":\"" + productName + "\"}";
			request.setTemplateParam(param);
			
			SendSmsResponse httpResponse = client.getAcsResponse(request);
			
			Map data = new HashMap<>();
			data.put("RequestId", httpResponse.getRequestId());
			data.put("Message", httpResponse.getMessage());
			data.put("BizId", httpResponse.getBizId());
			data.put("Code", httpResponse.getCode());
			sr.setData(data);
			
			if ("OK".equals(httpResponse.getCode())) {
				sr.setResult(StatusResult._SUCCESS);
				return sr;
			}else
				throw new OIUEException(StatusResult._ncriticalAbnormal, httpResponse.getMessage());
			
		} catch (Exception e) {
			logger.error("send sms is error:" + e.getMessage(), e);
			sr.setResult(StatusResult._ncriticalAbnormal);
			sr.setDescription("send sms is error:" + e.getMessage());
			return sr;
		}
	}
	
	public void updateConfigure(Map<?, ?> props) {
		if (props == null) {
			logger.error("config is null!");
			return;
		}
		logger.info("updateConfigure:"+props);
		try {
			this.props = props;
			this.accessKeyId = props.get("keyId") + "";
			this.accessKeySecret = props.get("keySecret") + "";
		} catch (Exception e) {
			logger.error("AliSMS update configure is error:" + e.getMessage(), e);
		}
		try {
			this.unregistered();
		} catch (Throwable e) {
			logger.error("AliSMS unregistered is error:" + e.getMessage(), e);
		}
		smsService.registerDriver(_DriverName, this);
	}
	
	@Override
	public void registered(DriverListener listener) {
		this.listener = listener;
	}
	
}
