package PostTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.oiue.tools.string.Md5;

@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public class PostTest {
	
	// 接口地址
	private static String apiURL = "http://localhost:8888/apis/kafka/input?topic=test";
	private HttpClient httpClient = null;
	private HttpPost method = null;
	
	public PostTest() {
		//
		// if (url != null) {
		// this.apiURL = url;
		// }
		// if (apiURL != null) {
		// httpClient = new DefaultHttpClient();
		// method = new HttpPost(apiURL);
		//
		// }
	}
	
	public String post(String apiURL, String parameters) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost method = new HttpPost(apiURL);
		String body = null;
		
		if (method != null & parameters != null && !"".equals(parameters.trim())) {
			try {
				
				// method.addHeader("Content-type","application/json; charset=utf-8");
				// method.setHeader("Accept", "application/json");
				method.setEntity(new StringEntity(parameters));
				
				HttpResponse response = httpclient.execute(method);
				
				// Read the response body
				body = EntityUtils.toString(response.getEntity());
				
			} catch (IOException e) {
				
			} finally {}
			
		}
		return body;
	}
	
	public Map getPostData(String url, Map<String, String> para) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		Map returnMap = new HashMap();
		try {
			HttpPost httppost = new HttpPost(url);
			try {
				if (para != null) {
					List pl = new ArrayList();
					for (Iterator iterator = para.keySet().iterator(); iterator.hasNext();) {
						String key = (String) iterator.next();
						
						Object ov = para.get(key);
						if (ov instanceof Integer) {
							ov = String.valueOf(ov);
						}
						
						pl.add(new BasicNameValuePair(key, ov + ""));
					}
					httppost.setEntity(new UrlEncodedFormEntity(pl));
				}
			} catch (Throwable e) {
				throw new RuntimeException("para=" + para, e);
			}
			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				int status = response.getStatusLine().getStatusCode();
				returnMap.put("status", status);
				// 如果成功
				if (status == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						// start 读取整个页面内容
						InputStream is = entity.getContent();
						BufferedReader in = new BufferedReader(new InputStreamReader(is));
						StringBuffer buffer = new StringBuffer();
						String line = "";
						while ((line = in.readLine()) != null) {
							buffer.append(line);
						}
						// end 读取整个页面内容
						returnMap.put("data", buffer.toString());
					}
					
				}
				// 如果失败
				else {
					returnMap.put("StatusCode", response.getStatusLine().getStatusCode());
					returnMap.put("ReasonPhrase", response.getStatusLine().getReasonPhrase());
					Header[] headers = response.getAllHeaders();
					for (Header header : headers) {
						returnMap.put(header.getName(), header.getValue());
					}
				}
				// System.out.println( EntityUtils.toString(response.getEntity()));
				
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
		return returnMap;
	}
	
	// public static void main(String[] args) {
	// PostTest ac = new PostTest(apiURL);
	// ac.post("{\"key\":\"value\"}");
	// System.out.println("hello world");
	// }
	public Map<?, ?> getGetData(String url, Object object) throws UnsupportedEncodingException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		Map returnMap = new HashMap();
		try {
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = httpclient.execute(httpGet);
			try {
				int status = response.getStatusLine().getStatusCode();
				returnMap.put("status", status);
				// 如果成功
				if (status == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						// start 读取整个页面内容
						InputStream is = entity.getContent();
						BufferedReader in = new BufferedReader(new InputStreamReader(is));
						StringBuffer buffer = new StringBuffer();
						String line = "";
						while ((line = in.readLine()) != null) {
							buffer.append(line);
						}
						// end 读取整个页面内容
						returnMap.put("data", buffer.toString());
					}
					
				}
				// 如果失败
				else {
					returnMap.put("StatusCode", response.getStatusLine().getStatusCode());
					returnMap.put("ReasonPhrase", response.getStatusLine().getReasonPhrase());
					Header[] headers = response.getAllHeaders();
					for (Header header : headers) {
						returnMap.put(header.getName(), header.getValue());
					}
				}
				
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
		return returnMap;
	}
	
	public Map<?, ?> getGetData(String url) throws UnsupportedEncodingException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		Map returnMap = new HashMap();
		try {
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = httpclient.execute(httpGet);
			try {
				int status = response.getStatusLine().getStatusCode();
				returnMap.put("status", status);
				// 如果成功
				if (status == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						// start 读取整个页面内容
						InputStream is = entity.getContent();
						BufferedReader in = new BufferedReader(new InputStreamReader(is));
						StringBuffer buffer = new StringBuffer();
						String line = "";
						while ((line = in.readLine()) != null) {
							buffer.append(line);
						}
						// end 读取整个页面内容
						returnMap.put("data", buffer.toString());
					}
					
				}
				// 如果失败
				else {
					returnMap.put("StatusCode", response.getStatusLine().getStatusCode());
					returnMap.put("ReasonPhrase", response.getStatusLine().getReasonPhrase());
					Header[] headers = response.getAllHeaders();
					for (Header header : headers) {
						returnMap.put(header.getName(), header.getValue());
					}
				}
				
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
		return returnMap;
	}
	//
	// public static void main(String[] args) throws UnsupportedEncodingException {
	// // String url = "http://127.0.0.1:8080/upload";
	// // String url = "http://127.0.0.1:8080/services/version/modulename/operation?parameters={}";
	// String url = "http://124.207.141.82:9999/wap/portalChuxin.do";
	//
	// String key = "test123";
	// String vehicleCity = "110100";
	// String mobile = "12345678901";
	// String licenseNo = "京PY7V99";
	// // String licenseNo = URLEncoder.encode("京A123456", "utf-8");
	// String customerID = URLEncoder.encode("03364c9a-24dc-4d4b-8c2b-6a0e857291b5", "utf-8");
	// String businessID = URLEncoder.encode("0348aac4-6f21-4ffa-9e8a-64a0100281fd", "utf-8");
	// String uuid = URLEncoder.encode("0348aac4-6f21-4ffa-9e8a-64a0100281fs", "utf-8");
	// String channelCode = "120002";
	// String platformType = "APP";
	// String mediaCode = "";
	// String bj = "vehicleCity=" + vehicleCity + "&mobile=" + mobile + "&licenseNo=" + licenseNo + "&customerID=" + customerID + "&businessID=" + businessID + "&uuid=" + uuid + "&channelCode=" + channelCode + "&platformType=" + platformType + "&mediaCode=" + mediaCode;
	// // String bjs = "vehicleCity="+ vehicleCity+"&mobile="+ mobile+"&licenseNo="+
	// // URLEncoder.encode(licenseNo, "utf-8")+"&customerID="+customerID
	// // +"&businessID="+businessID+"&uuid="+uuid+"&channelCode="+channelCode+"&platformType="+platformType
	// // +"&mediaCode="+mediaCode;
	//
	// Map data = new HashMap<>();
	// data.put("vehicleCity", vehicleCity);
	// data.put("mobile", mobile);
	// data.put("licenseNo", licenseNo);
	// data.put("customerID", customerID);
	// data.put("businessID", businessID);
	// data.put("uuid", uuid);
	// data.put("channelCode", channelCode);
	// data.put("platformType", platformType);
	// data.put("mediaCode", mediaCode);
	// try {
	// Md5 md5 = new Md5((key + bj).getBytes("utf-8"));
	// byte b[] = md5.getDigest();
	// System.out.println("key:" + key);
	// System.out.println("bj:" + bj);
	// System.out.println("md5:" + md5.getStringDigest().toLowerCase());
	//
	// PostTest ac = new PostTest();
	// // System.out.println(ac.getPostData(url, data));
	// String pser = bj + "&bjMd5=" + md5.getStringDigest().toLowerCase();
	// // String pser=bj+"&bjMd5=ca13fd460f53af44a55ae9ebf57c22fc";
	// System.out.println(url + "?" +pser);
	// // System.out.println(ac.post(url,pser));
	//// System.out.println(ac.getGetData(url + "?" + pser));
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }
}
