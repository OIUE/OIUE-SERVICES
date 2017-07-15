package org.oiue.service.action.http.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.file.upload.FileUploadService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.ExceptionUtil;
import org.oiue.tools.file.FileStringUtil;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.string.StringReplace;
import org.oiue.tools.string.StringUtil;
import org.osgi.service.http.HttpService;

@SuppressWarnings({ "unused", "rawtypes" })
public class UploadPostServlet extends HttpServlet{
	private static final long serialVersionUID = -6327347468651806863L;
	private ActionService actionService;
	private Logger logger;
	private Dictionary properties;
	private FileUploadService fileUploadService;
	private String userDir = null;
	private boolean isMultipart = false;
	private HttpService httpService;

	public UploadPostServlet(ActionService actionService, LogService logService, FileUploadService fileUploadService, String userDir) {
		super();
		this.logger = logService.getLogger(this.getClass());
		this.actionService = actionService;
		this.fileUploadService = fileUploadService;
		this.userDir = userDir;
	}

	public void updated(Dictionary props) {
		logger.info("updateConfigure");
		properties = props;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<Object, Object> per = new HashMap<Object, Object>();
		Map<Object, Object> rtn = null;
		String perStr = null;
		String callBackFn = "";
		try {
			per = this.saveFile(req, per);
			try {
				callBackFn = per.get("callback") + "";
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
			rtn = new HashMap<Object, Object>();
			rtn.putAll(per);

			rtn.put("status", StatusResult._SUCCESS);
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			rtn = new HashMap();
			rtn.put("status", StatusResult._permissionDenied);
			rtn.put("excepion", ExceptionUtil.getCausedBySrcMsg(ex));
		}
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter out = resp.getWriter();
		out.print(JSONUtil.parserToStr(rtn));
		out.close();
	}

	/** 文件域列表 */
	private Map<String, FileItem> fileFields = new TreeMap<String, FileItem>();
	/** 表单域列表 */
	private Map formFields = new TreeMap();

	/**
	 * 处理文件项目.
	 *
	 * @param item - FileItem 对象
	 */
	private void processUploadedFile(FileItem item) {
		String name = item.getFieldName();
		fileFields.put(name, item);
	}

	/**
	 * 获取上传的文件项目.
	 *
	 * @param name - String, 文件域名称
	 * @return FileItem - org.apache.commons.fileupload.FileItem 对象
	 */
	public FileItem getFileItem(String name) {
		if (!isMultipart)
			return null;

		return (fileFields.get(name));
	}

	/**
	 * 处理表单项目.
	 *
	 * @param item - FileItem 对象
	 */
	@SuppressWarnings("unchecked")
	private void processFormField(FileItem item) {
		String name = item.getFieldName();
		// NOTE 文件上传统一使用 UTF-8 编码 2005-10-16
		String value = null;

		try {
			value = item.getString("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}

		// 首先尝试获取原来的值
		Object oldValue = formFields.get(name);

		if (oldValue == null) {
			formFields.put(name, value);
		} else {
			// 多个值存储为 List

			// 原来为单个值则添加现有的值
			try {
				String oldString = (String) oldValue;

				List list = new ArrayList();
				list.add(oldString);
				list.add(value);

				formFields.put(name, list);
			} catch (Exception ex) {
				// ex.printStackTrace();
			}

			// 原来为多个值则添加现有的值
			try {
				List list = (List) oldValue;
				list.add(value);
				formFields.put(name, list);
			} catch (Exception ex) {
				// ex.printStackTrace();
			}
		}
	}

	@SuppressWarnings({ "unchecked" })
	public Map saveFile(HttpServletRequest req, Map per) {
		/** 文件域列表 */
		fileFields = new TreeMap();
		/** 表单域列表 */
		formFields = new TreeMap();
		if (logger.isDebugEnabled()) {
			logger.debug("----------->start savefile");
		}

		Map data;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			List<FileItem> items = upload.parseRequest(req); // 解析request请求
			Iterator iter = items.iterator();

			List<FileItem>  tmpFile = new ArrayList<>();
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				if (item.isFormField()) { // 如果是表单域 ，就是非文件上传元素
					processFormField(item);
				} else {
					tmpFile.add(item);
				}
			}
			String perStr = null;
			perStr = formFields.get("parameter") + "";
			if (!StringUtil.isEmptys(perStr)) {
				per = (JSONUtil.parserStrToMap(perStr));
			}
			String token = per.get("token") + "";
			// 如果是文件上传表单
			String dir = properties.get("upload.rootpath." + per.get("modulename")) + "";// File
			// dir
			if (StringUtil.isEmptys(dir)) {
				throw new RuntimeException("请配置存储路径！" + "upload.rootpath." + per.get("modulename"));
			}
			dir = StringReplace.replace(dir, "${userid}", 0 + "", false);
			Map info = new HashMap();

			if (logger.isDebugEnabled()) {
				logger.debug("----------->start loop  file");
			}
			for (FileItem item : tmpFile) {
				String fieldName = item.getFieldName(); // 文件域中name属性的值
				String fileName = item.getName(); // 文件的全路径，绝对路径名加文件名
				String contentType = item.getContentType(); // 文件的类型
				//                  long size = item.getSize(); // 文件的大小，以字节为单位
				// 保存文件
				if (!StringUtil.isEmpty(dir) && item != null && !StringUtil.isEmpty(item.getName()) && item.get() != null) {
					Map map;
					try {
						(new File(dir)).mkdirs();
						File f = new File(dir, FileStringUtil.getShortFileName(item.getName()));
						if (!f.exists()) { // 判断文件是否存在
							logger.debug("文件不存在");
						} else {
							boolean rs = f.delete(); // 调用delete()方法
							if (rs)
								logger.debug("文件删除成功");
							else
								logger.debug("文件删除失败");
						}
						logger.debug(f.getAbsolutePath());
						java.io.FileOutputStream fout = new FileOutputStream(f);
						fout.write(item.get());
						fout.close();
						fileUploadService.receive(f.getAbsolutePath(), info);
						String rtnPath = f.getPath().split("uploadfile")[1];
						rtnPath = rtnPath.replace("\\", "/");
						per.put(fieldName, rtnPath.substring("/".indexOf(rtnPath) + 2));
						try {
							data = (Map) per.get("data");
							if (data == null) {
								data = new HashMap();
								per.put("data", data);
							}
							data.put(fieldName, rtnPath.substring("/".indexOf(rtnPath) + 2));
						} catch (Throwable e) {
						}
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					} finally {
						map = null;
						System.gc();
					}
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("----------->end parse form field");
			}
		} catch (Exception e) {

		}

		if (logger.isDebugEnabled()) {
			logger.debug("----------->end loop  file" + per);
		}
		return per;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	private boolean CopyFile(File from, File to) {
		try {
			int byteread = 0;
			if (from.exists()) {
				InputStream inStream = new FileInputStream(from);
				FileOutputStream fs = new FileOutputStream(to);
				byte[] buffer = new byte[4096];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
				fs.flush();
				fs.close();
				inStream.close();
				return true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}
}
