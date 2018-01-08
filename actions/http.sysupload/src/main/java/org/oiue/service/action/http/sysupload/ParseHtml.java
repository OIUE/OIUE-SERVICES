package org.oiue.service.action.http.sysupload;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * 取得request对象中 所有的参数值并生成 一个相应的对象返回
 * 
 * @author Every
 * 
 */
@SuppressWarnings({ "rawtypes"})
public class ParseHtml {
	/**
	 * 将request中参数封装成Map
	 * @param request 存储着表单的HttpServletRequest对象
	 * @return 封装好的表单Map
	 */
	public static Map parseRequest(HttpServletRequest request) {
		HashMap<Object,Object> hashMap=new HashMap<Object,Object>();
		Map map=request.getParameterMap();
		Set keSet=map.entrySet(); 
		for(Iterator itr=keSet.iterator();itr.hasNext();){ 
		    Map.Entry me=(Map.Entry)itr.next(); 
		    Object ok=me.getKey(); 
		    Object ov=me.getValue(); 
		    String[] value=new String[1]; 
		    if(ov instanceof String[]){ 
		    	value=(String[])ov; 
		    	if(value.length==1){
		    		hashMap.put(ok,value[0]);
		    	}else{
		    		hashMap.put(ok,value);
		    	}
		    }else if(ov instanceof String){ 
		        hashMap.put(ok,ov);
		    }else{
		    	hashMap.put(ok,ov);
		    	System.out.println("request参数["+ok+"]类型为["+ov.getClass()+"]值为："+ov);
		    }
		}
		return hashMap;
	}
//	/**
//	 * 
//	 * @param request存储着表单的HttpServletRequest对象
//	 * @param bean要封装的表单Bean
//	 * @return 封装好的表单Bean
//	 */
//	public static Object parseRequest(HttpServletRequest request, Object bean) {
//		// 取得所有参数列表
//		Enumeration enums = request.getParameterNames();
//		// 遍历所有参数列表
//		while (enums.hasMoreElements()) {
//			Object obj = enums.nextElement();
//			try {
//				// 取得这个参数在Bean中的数据类型
//				Class cls = PropertyUtils.getPropertyType(bean, obj.toString());
//				// 把相应的数据转换成对应的数据类型
//				Object beanValue = ConvertUtils.convert(request.getParameter(obj.toString()), cls);
//				// 填充Bean值
//				PropertyUtils.setProperty(bean, obj.toString(), beanValue);
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				e.printStackTrace();
//			} catch (NoSuchMethodException e) {
//				e.printStackTrace();
//			}
//		}
//		return bean;
//	}
}