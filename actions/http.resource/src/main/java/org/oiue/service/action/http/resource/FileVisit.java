package org.oiue.service.action.http.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;

public class FileVisit implements Visit{
	private static final long serialVersionUID = 1L;
	private Logger logger;

	public FileVisit(LogService logService) {
		this.logger=logService.getLogger(getClass());
	}

	@Override
	public void visit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String domain_path = (String) request.getAttribute("domain_path");
		String resName = (String) request.getAttribute("resName");

		resName = resName.startsWith("/")?resName:"/"+resName;

		ServletContext httpContext = (ServletContext) request.getAttribute("httpContext");
		final URL url = httpContext.getResource(domain_path+resName);

		if (url == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else {
			handle(request, response, url, resName);
		}
	}

	private void handle(final HttpServletRequest req, final HttpServletResponse res, final URL url, final String resName) throws IOException {
		final long lastModified = getLastModified(url);
		if (lastModified != 0) {
			res.setDateHeader("Last-Modified", lastModified);
		}

		if (!resourceModified(lastModified, req.getDateHeader("If-Modified-Since"))) {
			res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		} else {
			copyResource(url, res);
		}
	}

	private long getLastModified(final URL url) {
		long lastModified = 0;

		try {
			final URLConnection conn = url.openConnection();
			lastModified = conn.getLastModified();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		try {
			if (lastModified == 0) {
				final String filepath = url.getPath();
				if (filepath != null) {
					final File f = new File(filepath);
					if (f.exists()) {
						lastModified = f.lastModified();
					}
				}
			}

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		return lastModified;
	}

	private boolean resourceModified(long resTimestamp, long modSince) {
		modSince /= 1000;
		resTimestamp /= 1000;

		return resTimestamp == 0 || modSince == -1 || resTimestamp > modSince;
	}

	private void copyResource(final URL url, final HttpServletResponse res) throws IOException {
		URLConnection conn = null;
		OutputStream os = null;
		InputStream is = null;

		try {
			conn = url.openConnection();
			is = conn.getInputStream();
			os = res.getOutputStream();
			int len = getContentLength(conn);
			if (len >= 0) {
				res.setContentLength(len);
			}

			byte[] buf = new byte[1024];
			int n;
			while ((n = is.read(buf, 0, buf.length)) >= 0) {
				os.write(buf, 0, n);
			}
		}finally {
			if (is != null) {
				is.close();
			}

			if (os != null) {
				os.close();
			}
		}
	}

	private int getContentLength(final URLConnection conn) {
		int length = -1;

		length = conn.getContentLength();
		if (length < 0) {
			// Unknown, try whether it is a file, and if so, use the file
			// API to get the length of the content...
			String path = conn.getURL().getPath();
			if (path != null) {
				File f = new File(path);
				// In case more than 2GB is streamed
				if (f.length() < Integer.MAX_VALUE) {
					length = (int) f.length();
				}
			}
		}
		return length;
	}
}
