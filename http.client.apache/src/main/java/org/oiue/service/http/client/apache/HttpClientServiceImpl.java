package org.oiue.service.http.client.apache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.oiue.service.http.client.HttpClientService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class HttpClientServiceImpl implements HttpClientService {
	
	Logger logger;
	public static final int cache = 10 * 1024;
	
	public HttpClientServiceImpl(LogService logService) {
		logger = logService.getLogger(this.getClass());
	}
	
	@Override
	public Map getPostData(String url, Map<String, String> para) {
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
			CloseableHttpResponse response = null;
			try {
				response = httpclient.execute(httppost);
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
		} catch (Exception e) {
			throw new OIUEException(StatusResult._conn_error, url, e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return returnMap;
	}
	
	@Override
	public Map httpDownload(String url, Map<String, String> para, String path) {
		if (!StringUtil.isEmptys(path)) {
			String filepath = this.download(url, path);
			Map rtn = new HashMap();
			rtn.put("filePath", filepath);
			rtn.put("status", 200);
			return rtn;
		}
		return null;
	}
	
	@Override
	public Map<?, ?> getGetData(String url) {
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
		} catch (Throwable e) {
			throw new OIUEException(StatusResult._conn_error, url, e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return returnMap;
	}
	
	@Override
	public Map<?, ?> getGetData(String url, Map<String, String> para) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		Map returnMap = new HashMap();
		try {
			RequestBuilder rb = RequestBuilder.post().setUri(new URI(url));
			try {
				if (para != null) {
					for (Iterator iterator = para.keySet().iterator(); iterator.hasNext();) {
						String key = (String) iterator.next();
						
						Object ov = para.get(key);
						if (ov instanceof Integer) {
							ov = String.valueOf(ov);
						}
						rb.addParameter(key, ov + "");
					}
					
				}
			} catch (Throwable e) {
				throw new RuntimeException("para=" + para, e);
			}
			HttpUriRequest login = rb.build();
			CloseableHttpResponse response = httpclient.execute(login);
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
		} catch (Throwable e) {
			throw new OIUEException(StatusResult._conn_error, url, e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return returnMap;
	}
	
	@Override
	public Map<?, ?> getGetData(String url, Object object) {
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
		} catch (Throwable e) {
			throw new OIUEException(StatusResult._conn_error, url, e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return returnMap;
	}
	
	@Override
	public Map<?, ?> getPostData(String url, String str) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		Map returnMap = new HashMap();
		try {
			HttpPost httppost = new HttpPost(url);
			try {
				HttpEntity reqEntity = MultipartEntityBuilder.create().setBoundary(str).build();
				httppost.setEntity(reqEntity);
			} catch (Throwable e) {
				throw new RuntimeException("para=" + str, e);
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
				
			} finally {
				response.close();
			}
		} catch (Throwable e) {
			throw new OIUEException(StatusResult._conn_error, url, e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return returnMap;
	}
	
	@Override
	public Map<?, ?> getPostDataByJson(String url, String str) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		Map<String, Object> returnMap = new HashMap();
		try {
			HttpPost httppost = new HttpPost(url);
			try {
				HttpEntity reqEntity = new StringEntity(str, "utf-8");
				httppost.addHeader("Content-type", "application/json; charset=utf-8");
				httppost.setHeader("Accept", "application/json");
				httppost.setEntity(reqEntity);
			} catch (Throwable e) {
				throw new RuntimeException("para=" + str, e);
			}
			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				int status = response.getStatusLine().getStatusCode();
				returnMap.put("status", status);
				// 如果成功
				if (status == HttpStatus.SC_OK) {
					boolean isjson = false;
					try {
						String accept = Arrays.asList(response.getAllHeaders()).toString();
						isjson = accept.indexOf("application/json") > 0;
					} catch (Exception e) {
						logger.error("get page type is error:" + e.getMessage(), e);
					}
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
						buffer.deleteCharAt(buffer.length() - 1).deleteCharAt(0);
						// end 读取整个页面内容
						returnMap.put("data", isjson ? JSONUtil.parserStrToMap(buffer.toString().replace("\\\"", "\"")) : buffer.toString());
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
		} catch (Throwable e) {
			throw new OIUEException(StatusResult._conn_error, url, e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return returnMap;
	}
	
	@SuppressWarnings("unused")
	@Override
	public String download(String url, String filepath) {
		if (filepath == null)
			throw new RuntimeException("filepath can't null");
		try {
			String fileName = null;
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			
			fileName = getFileName(response, url);
			
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			InputStreamReader dis = new InputStreamReader(is);
			java.io.BufferedReader bf = new java.io.BufferedReader(dis);
			File file = new File(filepath);
			file.getParentFile().mkdirs();
			file = new File(filepath + "/" + fileName);
			FileOutputStream fileout = new FileOutputStream(file);
			/**
			 * 根据实际运行效果 设置缓冲区大小
			 */
			byte[] buffer = new byte[cache];
			int ch = 0;
			while ((ch = is.read(buffer)) != -1) {
				fileout.write(buffer, 0, ch);
			}
			is.close();
			fileout.flush();
			fileout.close();
			return filepath + "/" + fileName;
			
		} catch (Exception e) {
			throw new OIUEException(StatusResult._conn_error, url, e);
		}
	}
	
	/**
	 * 获取response header中Content-Disposition中的filename值
	 * @param response
	 * @return
	 */
	public static String getFileName(HttpResponse response, String url) {
		Header contentHeader = response.getFirstHeader("Content-Disposition");
		String filename = null;
		if (contentHeader != null) {
			HeaderElement[] values = contentHeader.getElements();
			if (values.length == 1) {
				NameValuePair param = values[0].getParameterByName("filename");
				if (param != null) {
					try {
						// filename = new String(param.getValue().toString().getBytes(), "utf-8");
						// filename=URLDecoder.decode(param.getValue(),"utf-8");
						filename = param.getValue();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		if (StringUtil.isEmptys(filename)) {
			String[] s = url.split("/");
			filename = s[s.length - 1];
		}
		return filename;
	}
	
	/**
	 * 获取随机文件名
	 * @return
	 */
	public static String getRandomFileName() {
		return String.valueOf(System.currentTimeMillis());
	}
	
}
