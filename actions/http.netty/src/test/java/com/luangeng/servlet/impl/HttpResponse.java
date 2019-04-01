package com.luangeng.servlet.impl;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by LG on 2017/12/4.
 */
public class HttpResponse implements HttpServletResponse {

    private String contentType;

    private int contentLength = -1;

    private int status;

    private String encoding = "ISO-8859-1";

    private StringWriter writer;

    private Map<String, String> headers = new HashMap<String, String>();

    public HttpResponse() {
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getContentLength() {
        return contentLength;
    }

    @Override
    public void setContentLength(int i) {
        contentLength = i;
        setIntHeader("Content-Length", i);
    }

    public String getResult() {
        if (writer == null) {
            return "";
        }
        return writer.toString();
    }

    @Override
    public void addCookie(Cookie cookie) {
    }

    @Override
    public boolean containsHeader(String s) {
        return headers.get(s) != null;
    }

    @Override
    public String encodeURL(String s) {
        return s;
    }

    @Override
    public String encodeRedirectURL(String s) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param s
     * @deprecated
     */
    @Override
    public String encodeUrl(String s) {
        return s;
    }

    /**
     * @param s
     * @deprecated
     */
    @Override
    public String encodeRedirectUrl(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int i, String s) throws IOException {
        sendError(i, s);
    }

    @Override
    public void sendError(int i) throws IOException {
        sendError(i, null);
    }

    @Override
    public void sendRedirect(String s) throws IOException {
        setStatus(302);
        setHeader("Location", s);
    }

    @Override
    public void setDateHeader(String s, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addDateHeader(String s, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(String s, String s1) {
        headers.put(s, s1);
    }

    @Override
    public void addHeader(String s, String s1) {
        headers.put(s, s1);
    }

    @Override
    public void setIntHeader(String s, int i) {
        setHeader(s, "" + i);
    }

    @Override
    public void addIntHeader(String s, int i) {
        setHeader(s, "" + i);
    }

    /**
     * @param i
     * @param s
     * @deprecated
     */
    @Override
    public void setStatus(int i, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int i) {
        status = i;
    }

    @Override
    public String getHeader(String s) {
        return headers.get(s);
    }

    @Override
    public Collection<String> getHeaders(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public String getCharacterEncoding() {
        return encoding;
    }

    @Override
    public void setCharacterEncoding(String s) {
        encoding = s;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String s) {
        setHeader("Content-Type", s);
        contentType = s;
        String charset = "charset=";
        int i = s.indexOf(charset);
        if (i >= 0) {
            encoding = s.substring(i + charset.length());
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        writer = new StringWriter();
        return new PrintWriter(writer);
    }

    @Override
    public int getBufferSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBufferSize(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushBuffer() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCommitted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException();
    }

	@Override
	public void setContentLengthLong(long len) {
		// TODO Auto-generated method stub
		
	}
}