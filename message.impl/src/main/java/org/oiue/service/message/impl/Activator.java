package org.oiue.service.message.impl;

import java.util.Dictionary;

import org.oiue.service.bytes.api.BytesService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.log.LogService;
import org.oiue.service.message.MessageService;
import org.oiue.service.online.OfflineHandler;
import org.oiue.service.online.OnlineHandler;
import org.oiue.service.online.OnlineService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;

public class Activator extends FrameActivator {

    @Override
    public void start() throws Exception {
        this.start(new MulitServiceTrackerCustomizer() {
            private MessageServiceImpl messageService;
            private OnlineService onlineService;

            @Override
            public void removedService() {
                onlineService.unRegisterOnlineHandler("messageService");
                onlineService.unRegisterOfflineHandler("messageService");
            }

            @Override
            public void addingService() {
                LogService logService = getService(LogService.class);
                CacheServiceManager cache = getService(CacheServiceManager.class);
                BytesService bytesService = getService(BytesService.class);
                onlineService = getService(OnlineService.class);

                messageService = new MessageServiceImpl(logService, cache, onlineService, bytesService);
                registerService(MessageService.class, messageService);

                onlineService.registerOnlineHandler("messageService", (OnlineHandler) messageService, 1);
                onlineService.registerOfflineHandler("messageService", (OfflineHandler) messageService, 1);
            }

            @Override
            public void updated(Dictionary<String, ?> props) {
                messageService.updated(props);
            }
        }, LogService.class, CacheServiceManager.class, OnlineService.class, BytesService.class);
    }

    @Override
    public void stop() throws Exception {}
}
