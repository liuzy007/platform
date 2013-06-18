package com.taobao.diamond.io;

import java.io.File;

import com.taobao.diamond.io.watch.StandardWatchEventKind;
import com.taobao.diamond.io.watch.WatchEvent;
import com.taobao.diamond.io.watch.WatchKey;
import com.taobao.diamond.io.watch.WatchService;


/**
 * Watch服务实例
 * 
 * @author boyan
 * @date 2010-5-4
 */
public class WatchServiceExample {
    public static void main(String[] args) {
        WatchService watcher = FileSystem.getDefault().newWatchService();

        Path path = new Path(new File("/home/dennis/test"));
        // 注册事件
        path.register(watcher, StandardWatchEventKind.ENTRY_CREATE, StandardWatchEventKind.ENTRY_DELETE,
            StandardWatchEventKind.ENTRY_MODIFY);

        // 无线循环等待事件
        for (;;) {

            // 凭证
            WatchKey key;
            try {
                key = watcher.take();
            }
            catch (InterruptedException x) {
                return;
            }

            /**
             * 获取事件集合
             */
            for (WatchEvent<?> event : key.pollEvents()) {
                // 事件的类型
                WatchEvent.Kind<?> kind = event.kind();

                // 通过context方法得到发生事件的path
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path eventPath = ev.context();

                // 简单打印
                System.out.format("事件触发 file %s,Event %s%n", eventPath.getAbsolutePath(), kind.name());

            }

            // reset，如果无效，跳出循环,无效可能是监听的目录被删除
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
        System.out.println("done");
        watcher.close();
    }
}
