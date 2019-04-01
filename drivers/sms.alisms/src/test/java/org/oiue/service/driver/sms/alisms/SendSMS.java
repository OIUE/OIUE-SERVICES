package org.oiue.service.driver.sms.alisms;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.oiue.tools.StatusResult;
import org.oiue.tools.map.MapUtil;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

public class SendSMS {

	public SendSMS() {
	}
	// 初始化ascClient需要的几个参数
	final String product = "Dysmsapi";// 短信API产品名称（短信产品名固定，无需修改）
	final String domain = "dysmsapi.aliyuncs.com";// 短信API产品域名（接口地址固定，无需修改）
	// 替换成你的AK
	String accessKeyId="LTAIXwPhhOrWJl5q";// 你的accessKeyId,参考本文档步骤2
	String accessKeySecret="O5RQVKompXeZ2ofd3Ym2NgIZZTZr1b";// 你的accessKeySecret，参考本文档步骤2
	
	private String signature;
	private String templateCode;
	@Test
	public void sendSMS() {
		Map data = new HashMap();
		

		data.put("smsCode", "");
		data.put("product", product);
		data.put("signature", signature);
		data.put("templateCode", templateCode);
		
	}
	
	public void send(Map paramSMS) {
		try{
			String tel = MapUtil.getString(paramSMS, "phoneNo");
			String code = MapUtil.getString(paramSMS, "smsCode");
			String signature = MapUtil.getString(paramSMS, "signature");
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
			
			String param = "{\"code\":\"" + code + "\",\"product\":\"" + product + "\"}";
			request.setTemplateParam(param);
			
			SendSmsResponse httpResponse = client.getAcsResponse(request);
			
			Map data = new HashMap<>();
			data.put("RequestId", httpResponse.getRequestId());
			data.put("Message", httpResponse.getMessage());
			data.put("BizId", httpResponse.getBizId());
			data.put("Code", httpResponse.getCode());
			
			if ("OK".equals(httpResponse.getCode())) {
				
			}
			
		} catch (Exception e) {
			
		}
	}

}
