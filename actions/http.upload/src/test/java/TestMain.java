
/**
 * 
 */

/**
 * @author Every
 * 
 */
public class TestMain {
//	
//	@Test
//	public void test2(){
////		int t = 96;
////		while (t!=0) {
////			System.out.println(t&1);
////			t=t>>1;
////		}
//		
//		int week=4;
//		StringBuffer sb = new StringBuffer();
//		for (int i = 0; week!=0; i++) {
//			System.out.println(week+"|"+(week&1));
//			if((week&1)>0){
//				sb.append(",").append(i+1);
//			}
//			week=week>>1;
//		}
//		if(sb.length()>1)
//			sb.deleteCharAt(0);
//		System.out.println(sb);
//	}
//
//	public String testLogin() {
//		try {
//
////			URL postUrl = new URL("http://172.17.12.144:8888/action");
//			URL postUrl = new URL("http://127.0.0.1:8006/action");
//			;
//			// 打开连接
//			HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
//
//			// 设置是否向connection输出，因为这个是post请求，参数要放在
//			// http正文内，因此需要设为true
//			connection.setDoOutput(true);
//			// Read from the connection. Default is true.
//			connection.setDoInput(true);
//			// 默认是 GET方式
//			connection.setRequestMethod("POST");
//
//			// Post 请求不能使用缓存
//			connection.setUseCaches(false);
//
//			connection.setInstanceFollowRedirects(true);
//
//			// 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
//			// 意思是正文是urlencoded编码过的form参数，下面我们可以看到我们对正文内容使用URLEncoder.encode
//			// 进行编码
//			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//			// 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
//			// 要注意的是connection.getOutputStream会隐含的进行connect。
//			connection.connect();
//			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
//			// The URL-encoded contend
//			// 正文，正文内容其实跟get的URL中 '? '后的参数字符串一致
//			String content = "parameter=" + URLEncoder.encode("{\"modulename\":\"login\",\"tag\":\"exttag\",\"operation\":\"login\",\"data\":{\"userName\":\"root\",\"userPass\":\"LtsmartPoc\"},\"tokenid\":\"login\"}", "UTF-8");
//			// DataOutputStream.writeBytes将字符串中的16位的unicode字符以8位的字符形式写到流里面
//			out.writeBytes(content);
//
//			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//			String line;
//			String returnStr = null;
//			while ((line = reader.readLine()) != null) {
//				returnStr = line;
//				System.out.println(line);
//			}
//			if (!StringUtil.isEmptys(returnStr)) {
//				Map rtn = JSONUtil.parserStrToMap(returnStr);
//				if ((Integer) rtn.get("status") > 0) {
//					return MapUtil.getString(rtn, "tokenid");
//				}
//			}
//
//			reader.close();
//			connection.disconnect();
//		} catch (Throwable e) {
//			// TODO: handle exception
//		}
//		return null;
//	}
//
//	@Test
//	public void testObjectClass(){
//		Object[]  arg = new Object[]{LogServiceImpl.class}; 
//		Class[] c = new Class[arg.length];
//		
//		c[0]=arg[0].getClass().getInterfaces()[0];
//		System.out.println(Arrays.asList(c));
//		
//	}
//	@Test
//	public void convertJson() {
//		// String json="{\"key\":{\"keys\":\"value\"}}";
//		// JsonUtil.parserToMap(JSONObject.fromObject(json));
//
//		try {
//			// URL postUrl = new URL("http://172.17.12.144:8888/action");
//			// URL postUrl = new URL("http://172.17.12.144:8888/upload");
//			// // 打开连接
//			// HttpURLConnection connection = (HttpURLConnection)
//			// postUrl.openConnection();
//			//
//			// // 设置是否向connection输出，因为这个是post请求，参数要放在
//			// // http正文内，因此需要设为true
//			// connection.setDoOutput(true);
//			// // Read from the connection. Default is true.
//			// connection.setDoInput(true);
//			// // 默认是 GET方式
//			// connection.setRequestMethod("POST");
//			//
//			// // Post 请求不能使用缓存
//			// connection.setUseCaches(false);
//			//
//			// connection.setInstanceFollowRedirects(true);
//			//
//			// // 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
//			// // 意思是正文是urlencoded编码过的form参数，下面我们可以看到我们对正文内容使用URLEncoder.encode
//			// // 进行编码
//			// connection.setRequestProperty("Content-Type",
//			// "application/x-www-form-urlencoded");
//			// // 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
//			// // 要注意的是connection.getOutputStream会隐含的进行connect。
//			// connection.connect();
//			// DataOutputStream out = new
//			// DataOutputStream(connection.getOutputStream());
//			// // The URL-encoded contend
//			// // 正文，正文内容其实跟get的URL中 '? '后的参数字符串一致
//			// String content = "parameter=" +
//			// URLEncoder.encode("{\"modulename\":\"login\",\"tag\":\"exttag\",\"operation\":\"login\",\"data\":{\"userName\":\"root\",\"userPass\":\"LtsmartPoc\"},\"tokenid\":\"login\"}",
//			// "UTF-8");
//			// // DataOutputStream.writeBytes将字符串中的16位的unicode字符以8位的字符形式写到流里面
//			// out.writeBytes(content);
//			// String BOUNDARY = "---------------------------7d4a6d158c9"; //
//			// 分隔符
//			//
//			//
//			// StringBuffer sb = new StringBuffer();
//			// sb.append("--");
//			// sb.append(BOUNDARY);
//			// sb.append("\r\n");
//			// sb.append("Content-Disposition: form-data; name=\"myfile\"; filename=\"test.txt\"\r\n");
//			// sb.append("Content-Type: application/octet-stream\r\n\r\n");
//			//
//			// byte[] data = sb.toString().getBytes();
//			// byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
//			//
//			// connection.setRequestProperty("Content-Type",
//			// "multipart/form-data; boundary="+BOUNDARY); //设置表单类型和分隔符
//			// connection.setRequestProperty("Content-Length",
//			// String.valueOf(data.length + s.length + end_data.length));
//			// //设置内容长度
//			//
//			// out.write(data);
//			//
//			// FileInputStream fis = new FileInputStream(new
//			// File("/workspace/1.jpg")); //要上传的文件
//			//
//			// int rn2;
//			// byte[] buf2 = new byte[1024];
//			// while((rn2=fis.read(buf2, 0, 1024))>0)
//			// {
//			// out.write(buf2,0,rn2);
//			//
//			// }
//			//
//			// InputStream in = connection.getInputStream();
//			// BufferedReader rd = new BufferedReader(new InputStreamReader(in,
//			// "UTF-8"));
//			// String tempLine = rd.readLine();
//			// StringBuffer temp = new StringBuffer();
//			// String crlf = System.getProperty("line.separator");
//			// while (tempLine != null) {
//			// temp.append(tempLine);
//			// temp.append(crlf);
//			// tempLine = rd.readLine();
//			// }
//			// System.out.println(temp.toString());
//			// rd.close();
//			// in.close();
//			//
//			// out.flush();
//			// out.close();
//			//
//			// BufferedReader reader = new BufferedReader(new
//			// InputStreamReader(connection.getInputStream()));
//			// String line;
//			//
//			// while ((line = reader.readLine()) != null) {
//			// System.out.println(line);
//			// }
//			//
//			// reader.close();
//			// connection.disconnect();
//
//			URL url = new URL("http://172.17.12.144:8888/upload"); // 文件接收的CGI,不一定是JSP的
//
//			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//			conn.setRequestMethod("POST");
//			conn.setDoOutput(true);
//
//			String BOUNDARY = "---------------------------7d4a6d158c9"; // 分隔符
//
//			StringBuffer sb = new StringBuffer();
//			sb.append("--");
//			sb.append(BOUNDARY);
//			sb.append("\r\n");
//			sb.append("Content-Disposition: form-data; name=\"myfile\"; filename=\"test.txt\"\r\n");
//			sb.append("Content-Type: application/octet-stream\r\n\r\n");
//
//			byte[] data = sb.toString().getBytes();
//			byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
//
//			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY); // 设置表单类型和分隔符
//			// conn.setRequestProperty("Content-Length",
//			// String.valueOf(data.length + buf.length + end_data.length));
//			// //设置内容长度
//
//			OutputStream os = conn.getOutputStream();
//			os.write(data);
//
//			FileInputStream fis = new FileInputStream(new File("/workspace/1.jpg")); // 要上传的文件
//
//			int rn2;
//			byte[] buf2 = new byte[1024];
//			while ((rn2 = fis.read(buf2, 0, 1024)) > 0) {
//				os.write(buf2, 0, rn2);
//
//			}
//
//			os.write(end_data);
//			os.flush();
//			os.close();
//			fis.close();
//
//			// 得到返回的信息
//			InputStream is = conn.getInputStream();
//
//			byte[] inbuf = new byte[1024];
//			int rn;
//			while ((rn = is.read(inbuf, 0, 1024)) > 0) {
//
//				System.out.write(inbuf, 0, rn);
//
//			}
//			is.close();
//		} catch (Throwable e) {
//			// TODO: handle exception
//		}
//	}
//
//	@Test
//	public void testFile() {
//		try {
//			TestMain tm = new TestMain();
//			String boundary = "------WebKitFormBoundaryUey8ljRiiZqhZHBu";
//			String url = "http://127.0.0.1:8006/upload";
//			URL u = new URL(url);
//			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
//			conn.setDoOutput(true);
//			conn.setDoInput(true);
//			conn.setUseCaches(false);
//			conn.setRequestMethod("POST");
//			conn.setRequestProperty("connection", "Keep-Alive");
//			conn.setRequestProperty("Charsert", "UTF-8");
//			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//			// 指定流的大小，当内容达到这个值的时候就把流输出
//			conn.setChunkedStreamingMode(10240);
//			OutputStream out = new DataOutputStream(conn.getOutputStream());
//			byte[] end_data = ("\r\n--" + boundary + "--\r\n").getBytes();// 定义最后数据分隔线
//			List<String> list = new ArrayList<String>();
//			list.add("/workspace/1.jpg");
//			// list.add("e:/email.html");
//			StringBuilder sb = new StringBuilder();
//			// 添加form属性
//			sb.append("--");
//			sb.append(boundary);
//			sb.append("\r\n");
//			sb.append("Content-Disposition: form-data; name=\"tokenid\"");
//			sb.append("\r\n\r\n");
//			sb.append(tm.testLogin());
//			// sb.append("Content-Disposition: form-data; name=\"parameter\"");
//			// sb.append("\r\n\r\n");
//			// sb.append("{\"tokenid\":\"824f2de2cbea4e1fb0e4174cfb90c8f9\",\"modulename\":\"information_map_insert\",\"tag\":\"ext-gen659\"}");
//			out.write(sb.toString().getBytes("utf-8"));
//			out.write("\r\n".getBytes("utf-8"));
//
//			int leng = list.size();
//			for (int i = 0; i < leng; i++) {
//				String fname = list.get(i);
//				File file = new File(fname);
//				sb = new StringBuilder();
//				sb.append("--");
//				sb.append(boundary);
//				sb.append("\r\n");
//				sb.append("Content-Disposition: form-data;name=\"file" + i + "\";filename=\"" + file.getName() + "\"\r\n");
//				sb.append("Content-Type:application/octet-stream\r\n\r\n");
//				byte[] data = sb.toString().getBytes();
//				out.write(data);
//				DataInputStream in = new DataInputStream(new FileInputStream(file));
//				int bytes = 0;
//				byte[] bufferOut = new byte[1024];
//				while ((bytes = in.read(bufferOut)) != -1) {
//					out.write(bufferOut, 0, bytes);
//				}
//				out.write("\r\n".getBytes()); // 多个文件时，二个文件之间加入这个
//				in.close();
//			}
//			out.write(end_data);
//			out.flush();
//			out.close();
//			// 定义BufferedReader输入流来读取URL的响应
//			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//			String line = null;
//			while ((line = reader.readLine()) != null) {
//				System.out.println(line);
//			}
//		} catch (Exception e) {
//			System.out.println("发送POST请求出现异常！" + e);
//			e.printStackTrace();
//		}
//	}
//	
//	
//	@Test
//	public void testseconds()  {
//		long seconds=System.currentTimeMillis()/1000;
//		System.out.println(seconds);
//		java.util.Date d = new java.util.Date();
//		Calendar calendar = Calendar.getInstance();
//		
////		calendar.setTime(DateUtil.getSecondOfDate(p_date));
////        TimeZone tztz = TimeZone.getTimeZone("GMT");       
////        calendar1.setTimeZone(tztz);
//		Date da =new Date();
//        System.out.println(calendar.getTime());
//        System.out.println(calendar.getTimeInMillis());
//        System.out.println();
//	}
}
