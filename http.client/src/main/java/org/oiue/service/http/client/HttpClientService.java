package org.oiue.service.http.client;

import java.io.IOException;
import java.util.Map;


public interface HttpClientService {

    Map<?, ?> getGetData(String url) throws IOException;

    Map<?, ?> getGetData(String url, Map<String, String> para) throws IOException;

    Map<?, ?> getGetData(String url, Object object) throws IOException;

    Map<?, ?> getPostData(String url, Map<String, String> para) throws IOException;

    Map<?, ?> getPostData(String url, String str) throws IOException;

    Map<?, ?> getPostDataByJson(String url, String json) throws IOException;

    Map<?, ?> httpDownload(String url, Map<String, String> para, String saveFile) throws IOException;

    String download(String url, String filepath) throws IOException;
}
