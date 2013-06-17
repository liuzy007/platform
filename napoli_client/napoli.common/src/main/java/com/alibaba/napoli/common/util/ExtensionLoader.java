package com.alibaba.napoli.common.util;

import com.alibaba.napoli.common.Extension;
import com.alibaba.napoli.receiver.NapoliReceiver;
import com.alibaba.napoli.receiver.NapoliReceiverContext;
import com.alibaba.napoli.receiver.impl.ReceiverMq;
import com.alibaba.napoli.sender.impl.SenderMq;
import com.alibaba.napoli.sender.NapoliSenderContext;
import com.alibaba.napoli.sender.NapoliResult;
import com.alibaba.napoli.sender.NapoliSender;
import com.alibaba.napoli.spi.ReceiverFilter;
import com.alibaba.napoli.spi.SenderFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * User: heyman Date: 2/17/12 Time: 12:01 下午
 */
public class ExtensionLoader {
    public static <T> Map<String, T> getExtensionLoader(final Class<T> type) {
        Map<String, T> extensionMap = new HashMap<String, T>();
        ServiceLoader<T> services = ServiceLoader.load(type);
        for (T service : services) {
            if (service.getClass().isAnnotationPresent(Extension.class)) {
                String name = service.getClass().getAnnotation(Extension.class).value();
                if (name == null || name.length() == 0) {
                    throw new IllegalStateException("Illegal @Extension annotation in class " + type);
                }
                if (extensionMap.get(name) != null){
                    throw new IllegalStateException("Duplicate extension " + name + " name  and class " + type);
                }
                extensionMap.put(name, service);
            }
        }
        return extensionMap;
    }

    public static NapoliSender buildSenderFilterChain(String[] filterNames,NapoliSender last) {
        if (filterNames == null || filterNames.length == 0) {
            throw new IllegalArgumentException("filterNames is empty");
        }
        //NapoliSender last = new SenderMq();
        Map<String, SenderFilter> senderFilterMap = getExtensionLoader(SenderFilter.class);
        for (int i = filterNames.length - 1; i >= 0; i--) {
            final SenderFilter filter = senderFilterMap.get(filterNames[i]);
            if (filter != null) {
                final NapoliSender next = last;
                last = new NapoliSender() {
                    public NapoliResult sendMessage(NapoliSenderContext senderContext) {
                        return filter.doSend(next, senderContext);
                    }
                };
            }
        }
        return last;
    }

    public static NapoliReceiver buildReceiverFilterChain(String[] filterNames){
        if (filterNames == null || filterNames.length == 0) {
            throw new IllegalArgumentException("filterNames is empty");
        }
        NapoliReceiver last = new ReceiverMq();
        Map<String, ReceiverFilter> receiverFilterMap = getExtensionLoader(ReceiverFilter.class);
        for (int i = filterNames.length - 1; i >= 0; i--) {
            final ReceiverFilter filter = receiverFilterMap.get(filterNames[i]);
            if (filter != null) {
                final NapoliReceiver next = last;
                last = new NapoliReceiver() {
                    public NapoliResult onMessage(NapoliReceiverContext receiverContext) {
                        return  filter.doMessage(next,receiverContext);
                    }
                };
            }
        }
        return last;
    }
}
