package com.alibaba.napoli.mqImpl.metaq;

import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.NapoliWorker;
import com.alibaba.napoli.client.async.router.AsyncRouterWorker;
import com.alibaba.napoli.client.async.router.RouterWorker;
import com.alibaba.napoli.common.ConnectionParam;
import com.alibaba.napoli.common.NapoliException;
import com.alibaba.napoli.common.util.ExtensionLoader;
import com.alibaba.napoli.common.util.HessianUtil;
import com.alibaba.napoli.metamorphosis.Message;
import com.alibaba.napoli.metamorphosis.client.consumer.MessageListener;
import com.alibaba.napoli.receiver.NapoliReceiver;
import com.alibaba.napoli.receiver.NapoliReceiverContext;
import com.alibaba.napoli.sender.NapoliResult;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: heyman
 * Date: 4/25/12
 * Time: 9:52 上午
 */
public class MetaqListener  implements MessageListener {
    private static final Log logger = LogFactory.getLog(MetaqListener.class);
    private NapoliWorker worker;
    private MetaqConsumerContext consumerContext;
    private final AtomicLong count = new AtomicLong(0);

    public MetaqListener(NapoliWorker worker,MetaqConsumerContext consumerContext) {
        this.worker = worker;
        this.consumerContext = consumerContext;
    }

    @Override
    public void recieveMessages(Message message) {
        NapoliResult result;

        if (worker instanceof AsyncRouterWorker) {
            throw new IllegalStateException("metaq can't support AsyncRouterWorker");
        } else if (worker instanceof RouterWorker) {
            throw new IllegalStateException("metaq can't support RouterWorker");
        }
        byte[] data = message.getData();
        try {
            NapoliMessage napoliMessage = (NapoliMessage)HessianUtil.deserialize(data);
            //fix for NP-265
            consumerContext.getConnectionParam().setStoreEnable(true);
            napoliMessage.setStore2Local(true);
            NapoliReceiver napoliReceiver;
            ConnectionParam connectionParam = consumerContext.getConnectionParam();
            if (connectionParam.getFilterChain() != null && connectionParam.getFilterChain().length() > 0) {
                napoliReceiver = ExtensionLoader.buildReceiverFilterChain(connectionParam.getFilterChain().split(","));
            }else{
                napoliReceiver = ExtensionLoader.buildReceiverFilterChain(new String[]{"monitor", "bdbStore"});
            }
            NapoliReceiverContext context = new NapoliReceiverContext(worker, napoliMessage,
                    consumerContext.getConnectionParam());
            context.setFilterList(consumerContext.getFilterList());
            result = napoliReceiver.onMessage(context);
            result.printError();
        } catch (Exception e) {
            logger.error("Hessian deserialize error!,the message will be discard!!!!["+data+e.getMessage(),e);
            try {
                System.out.println("maybe="+HessianUtil.deserialize(data));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public Executor getExecutor() {
        return null;
    }
}
