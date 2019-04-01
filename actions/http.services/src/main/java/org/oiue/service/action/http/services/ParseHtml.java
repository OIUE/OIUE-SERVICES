package org.oiue.service.action.http.services;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.string.StringUtil;

/**
 * 取得request对象中 所有的参数值并生成 一个相应的对象返回
 * 
 * @author Every
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ParseHtml {
	/**
	 * 将request中参数封装成Map
	 * 
	 * @param request 存储着表单的HttpServletRequest对象
	 * @return 封装好的表单Map
	 */
	public static Map parseRequest(HttpServletRequest request) {
		Map per = null;
		String perStr = null;
		String callBackFn = "";
		try {
			perStr = request.getParameter("parameter");
			try {
				if (StringUtil.isEmptys(perStr)) {
					StringBuffer jb = new StringBuffer();
					String line = null;
					BufferedReader reader = request.getReader();
					while ((line = reader.readLine()) != null)
						jb.append(line);
					perStr = jb.toString();
				} else {
					callBackFn = request.getParameter("callback");
				}
			} catch (Throwable e) {
				// logger.error(e.getMessage(), e);
			}
			if (!StringUtil.isEmptys(perStr)) {
				per = JSONUtil.parserStrToMap(perStr, false);
			}

			{
				Set eSet = request.getParameterMap().entrySet();
				Map temp_data = new HashMap<>();
				for (Iterator itr = eSet.iterator(); itr.hasNext();) {
					Map.Entry me = (Map.Entry) itr.next();
					Object ok = me.getKey();
					Object ov = me.getValue();
					String[] value = new String[1];
					if (ov instanceof String[]) {
						value = (String[]) ov;
						if (value.length == 1) {
							temp_data.put(ok, value[0]);
						} else {
							temp_data.put(ok, value);
						}
					} else if (ov instanceof String) {
						temp_data.put(ok, ov);
					} else {
						temp_data.put(ok, ov);
						System.out.println("request参数[" + ok + "]类型为[" + ov.getClass() + "]值为：" + ov);
					}
				}
				if (temp_data.size() > 0) {
					if (per == null || per.size() == 0) {
						if (per == null)
							per = new HashMap<>();
						per.put("data", temp_data);
						per.putAll(temp_data);
					} else {
						Object tdata = per.get("data");
						if (tdata == null) {
							per.put("data", temp_data);
						} else if (tdata instanceof Map) {
							((Map) tdata).putAll(temp_data);
						} else if (tdata instanceof List) {
							for (Object object : (List) tdata) {
								if (object instanceof Map) {
									((Map) object).putAll(temp_data);
								}
							}
						}
					}
				}
			}
			if (per == null) {
				per = new HashMap<>();
				per.put("data", new HashMap<>());
			}
			String token = request.getHeader("Authorization");
			if (!StringUtil.isEmptys(token)) {
				int index = token.indexOf(" ");
				if (index > 0)
					token = token.substring(index + 1);
				per.put("token", token);
			}
			if (!StringUtil.isEmptys(callBackFn)) {
				per.put("callback", callBackFn);
			}
		} catch (Throwable e) {
			throw new OIUEException(StatusResult._format_error, "参数格式不正确！" + " /n " + perStr + " /n " + per, e);
		}

		return per;
	}
	// /**
	// *
	// * @param request存储着表单的HttpServletRequest对象
	// * @param bean要封装的表单Bean
	// * @return 封装好的表单Bean
	// */
	// public static Object parseRequest(HttpServletRequest request, Object bean) {
	// // 取得所有参数列表
	// Enumeration enums = request.getParameterNames();
	// // 遍历所有参数列表
	// while (enums.hasMoreElements()) {
	// Object obj = enums.nextElement();
	// try {
	// // 取得这个参数在Bean中的数据类型
	// Class cls = PropertyUtils.getPropertyType(bean, obj.toString());
	// // 把相应的数据转换成对应的数据类型
	// Object beanValue = ConvertUtils.convert(request.getParameter(obj.toString()),
	// cls);
	// // 填充Bean值
	// PropertyUtils.setProperty(bean, obj.toString(), beanValue);
	// } catch (IllegalAccessException e) {
	// e.printStackTrace();
	// } catch (InvocationTargetException e) {
	// e.printStackTrace();
	// } catch (NoSuchMethodException e) {
	// e.printStackTrace();
	// }
	// }
	// return bean;
	// }
}