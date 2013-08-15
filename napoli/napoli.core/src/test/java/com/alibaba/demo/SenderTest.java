package com.alibaba.demo;

import com.alibaba.napoli.ResourceConstant;
import com.alibaba.napoli.client.async.AsyncWorker;
import com.alibaba.napoli.client.async.NapoliMessage;
import com.alibaba.napoli.client.async.impl.DefaultAsyncReceiver;
import com.alibaba.napoli.client.async.impl.DefaultAsyncSender;
import com.alibaba.napoli.client.connector.NapoliConnector;
import com.alibaba.napoli.client.model.Person;
import com.alibaba.napoli.receiver.Receiver;
import com.alibaba.napoli.sender.Sender;
import java.io.Serializable;

/**
 * User: heyman Date: 2/13/12 Time: 10:55 上午
 */
public class SenderTest {
    public static String msg_body_1k = "message body,FD 和 Type 列的含义最为模糊，它们提供了关于文件如何使用的更多信息。"
            + "FD 列表示文件描述符，应用程序通过文件描述符识别该文件。Type 列提供了关于文件格"
            + "式的更多描述。我们来具体研究一下文件描述符列，清单 1 中出现了三种不同的值。cwd 值表示"
            + "应用程序的当前工作目录，这是该应用程序启动的目录，除非它本身对这个目录进行更改。txt 类型的文件是程序代码"
            + "，如应用程序二进制文件本身或共享库，再比如本示例的列表中显示的 init 程序。最后，数值表示应用程序的文件描述符，"
            + "这是打开该文件时返回的一个整数。在清单 1 输出的最后一行中，您可以看到用户正在使用 vi "
            + "编辑 /var/tmp/ExXDaO7d，其文件描述符为 3。u 表示该文件被打开并处于读取/写入模式，而不是只读 (r) "
            + "或只写 (w) 模式。有一点不是很重要但却很有帮助，初始打开每个应用程序时，都具有三个文件描述符，从 0 到 2，";

    public void testSendObjects() throws Exception {
        NapoliConnector consoleConnector = NapoliConnector.createConnector(ResourceConstant.shConsole);
        consoleConnector.init();
        DefaultAsyncSender sender = DefaultAsyncSender.createSender(consoleConnector,"objectQueue", false);
        sender.init();
        Person person = new Person();
        person.setPenName("heymantest");

        for (int i = 0; i < 100; i++) {
            sender.sendMessage(person);
        }

        sender.close();
        consoleConnector.close();
    }

    public void testrecvObjects() throws Exception {
        NapoliConnector consoleConnector = NapoliConnector.createConnector(ResourceConstant.shConsole);
        AsyncWorker worker = new AsyncWorker() {
            public boolean doWork(Serializable message) {
                Person person = (Person) message;
                System.out.println(person.getPenName());
                return true;
            }
        };
        Receiver receiver = DefaultAsyncReceiver.createReceiver(consoleConnector, "objectQueue", false, 5, worker);
        receiver.start();
        System.out.println("start....");
        receiver.stop();
        consoleConnector.close();
    }

    public void testSendString() throws Exception {

        byte[] msgBytes = msg_body_1k.getBytes("utf-8");
        NapoliConnector consoleConnector = NapoliConnector.createConnector(ResourceConstant.shConsole + ":80");
        DefaultAsyncSender sender = DefaultAsyncSender.createSender(consoleConnector,"queue.exampleQueueXX0", false);
        sender.init();
        NapoliMessage napoliMessage = new NapoliMessage(msgBytes);
        napoliMessage.setProperty("prop", "spree");

        for (int i = 0; i < 100; i++) {
            sender.sendMessage(msgBytes);
        }

        sender.close();
        consoleConnector.close();
    }

    public void testrecvString() throws Exception {
        NapoliConnector consoleConnector = NapoliConnector.createConnector(ResourceConstant.shConsole);
        AsyncWorker worker = new AsyncWorker() {
            public boolean doWork(Serializable message) {
                if (message instanceof String) {
                    System.out.println((String) message);
                } else if (message instanceof Person) {
                    System.out.println(((Person) message).getPenName());
                }
                return true;
            }
        };
        Receiver receiver = DefaultAsyncReceiver.createReceiver(consoleConnector,"objectQueue", false, 5, worker);
        receiver.start();
        System.out.println("start....");
        receiver.stop();
        consoleConnector.close();
    }

    public static void main(String[] args) throws Exception {
        SenderTest senderTest = new SenderTest();
        senderTest.testSendObjects();
        senderTest.testrecvObjects();
        senderTest.testSendString();
        senderTest.testrecvString();
    }
}
