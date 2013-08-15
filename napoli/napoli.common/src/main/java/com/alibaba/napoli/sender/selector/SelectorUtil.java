package com.alibaba.napoli.sender.selector;

import com.alibaba.napoli.domain.client.ClientMachine;
import com.alibaba.napoli.domain.client.ClientQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * User: heyman
 * Date: 12/14/11
 * Time: 1:54 下午
 */
public class SelectorUtil {
    private static final Log log = LogFactory.getLog(SelectorUtil.class);

    /*public static boolean canPhysicalQueueSend(final PhysicalQueue physicalQueue) {
        // 物理Queue的 sendable 属性 == false时，挖掉这个machine的发送
        if (!physicalQueue.isSendable()) {
            return false;
        }

        if (physicalQueue.getState() != PhysicalQueue.STATE_WORKING) {
            return false;
        }

        final Machine machine = physicalQueue.getMachine();
        if (machine.getState() != Machine.STATE_WORKING) {
            // 正常情况下不能执行到这里，能加上物理Queue中的Machine一定是WORKING的状态！
            // 这一点由 Napoli Server的控制台保证。
            if (log.isWarnEnabled()) {
                log.warn("the state of machine(id:" + machine.getId() + ") is NOT working, "
                        + "but this machine belongs to a physical queue(id:"
                        + physicalQueue.getId() + ")!");
            }

            return false;
        }
        return true;
    }

    public static boolean canPhysicalTopicSend(final PhysicalTopic physicalTopic) {
        // 物理Queue的 sendable 属性 == false时，挖掉这个machine的发送
        *//*if (!physicalTopic.isSendable()) {
            return false;
        }*//*
        if (physicalTopic.getState() != PhysicalQueue.STATE_WORKING) {
            return false;
        }

        final Machine machine = physicalTopic.getMachine();
        if (machine.getState() != Machine.STATE_WORKING) {
            // 正常情况下不能执行到这里，能加上物理Queue中的Machine一定是WORKING的状态！
            // 这一点由 Napoli Server的控制台保证。
            if (log.isWarnEnabled()) {
                log.warn("the state of machine(id:" + machine.getId() + ") is NOT working, "
                        + "but this machine belongs to a physical queue(id:"
                        + physicalTopic.getId() + ")!");
            }

            return false;
        }
        return true;
    }*/

    public static Selector<ClientMachine> createSelector(final String strategy, final ClientQueue clientQueue) {
        Selector<ClientMachine> selector = null;
        if (ClientQueue.STRATEGY_RANDOM.equalsIgnoreCase(strategy)) {
            selector = new RandomSelector<ClientMachine>();
        } else if (ClientQueue.STRATEGY_ROUND_ROBIN.equalsIgnoreCase(strategy)) {
            selector = new RoundRobinSelector<ClientMachine>();
        } else if (ClientQueue.BALANCE_ROBIN.equalsIgnoreCase(strategy)) {
            selector = new BalanceSelector<ClientMachine>();
        } else if (ClientQueue.STRATEGY_WEIGHT.equalsIgnoreCase(strategy)) {
            selector = createWeightSelector(strategy, clientQueue);
        }
        // 如果策略未知，则指定 缺省的selector
        else {
            selector = new RoundRobinSelector<ClientMachine>();
        }

        return selector;
    }

    private static Selector<ClientMachine> createWeightSelector(final String strategy, final ClientQueue queueEntity) {
        if (ClientQueue.STRATEGY_WEIGHT.equalsIgnoreCase(strategy)) {
            final WeightSelector<ClientMachine> weightSelector = new WeightSelector<ClientMachine>();
            final Map<ClientMachine, Integer> weightMap = new HashMap<ClientMachine, Integer>();

            for (ClientMachine machine : queueEntity.getMachineSet()) {
                //final PhysicalQueue physicalQueue = entry.getValue();
                Integer w = machine.getWeight();
                // 如果权重的数值不对，则重设成 1
                if (w == null || w < 1) {
                    if (log.isInfoEnabled()) {
                        log.info("the weight of PhysicalQueue(id: " + queueEntity.getName() + ") in the Queue("
                                + queueEntity.getName() + ") is " + w + ", weight MUST be a positive number!!");
                    }
                    w = 1;
                }
                weightMap.put(machine, w);
            }

            weightSelector.setWeightForCandidate(weightMap);
            return weightSelector;
        }
        return null;
    }

}
