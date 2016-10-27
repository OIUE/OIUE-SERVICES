package org.oiue.service.debug.httpclient;

import java.util.Map;

import org.oiue.service.http.client.HttpClientService;
import org.oiue.service.tcp.Handler;
import org.oiue.service.tcp.Session;
import org.oiue.tools.json.JSONUtil;

public class ServerHandler implements Handler {
    private HttpClientService httpClient = null;

    public ServerHandler(HttpClientService httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void received(Session session, String line, byte[] bytes) throws Exception {
        if (line.startsWith("g")) {
            String cmdArray[] = line.split(" ", 2);
            if (cmdArray.length == 2) {
                if("g".equals(cmdArray[0])){
                Map<?, ?> r = httpClient.getGetData(cmdArray[1]);
                session.write(r + "");
                }else{
                    String t = cmdArray[1];
                    String cmdArrays[] = t.split(",",2);
                    String p = cmdArrays[1];
                    Map<?, ?> r = httpClient.getGetData(cmdArrays[0],p.startsWith("{")?JSONUtil.parserStrToMap(p):p);
                    session.write(r + "");
                }
            }
        } else if (line.startsWith("p")) {
            String cmdArray[] = line.split(" ", 3);
            if (cmdArray.length == 3) {
                if("pj".equals(cmdArray[0])){
                Map<?, ?> r = httpClient.getPostDataByJson(cmdArray[1],cmdArray[2]);
                session.write(r + "");
                }else{
                    Map<?, ?> r = httpClient.getPostData(cmdArray[1],cmdArray[2]);
                    session.write(r + "");
                    
                }
            }
        } else {
            session.write("");
        }
    }

    @Override
    public void closed(Session session) throws Exception {
        
    }

    @Override
    public void opened(Session session) throws Exception {
        session.write("debug Cache Tree Service");
    }

    @Override
    public void idled(Session session) throws Exception {
        session.close();
    }

    @Override
    public void sent(Session session) throws Exception {

    }

    @Override
    public int getReaderIdleCount() {
        return 0;
    }
}
