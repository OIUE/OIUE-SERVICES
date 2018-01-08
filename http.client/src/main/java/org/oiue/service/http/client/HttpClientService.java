package org.oiue.service.http.client;

import java.util.Map;


public interface HttpClientService {

    Map<?, ?> getGetData(String url);

    Map<?, ?> getGetData(String url, Map<String, String> para);

    Map<?, ?> getGetData(String url, Object object);

    Map<?, ?> getPostData(String url, Map<String, String> para);

    Map<?, ?> getPostData(String url, String str);

    Map<?, ?> getPostDataByJson(String url, String json);

    Map<?, ?> httpDownload(String url, Map<String, String> para, String saveFile);

    String download(String url, String filepath);
}
