package handler;

import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class JavaNioHandler implements Runnable {
    private volatile static Selector selector;
    private final SocketChannel channel; // Channel is bi-directional
    private SelectionKey key;
    private final ByteBuffer input = ByteBuffer.allocate(1024);
    private final ByteBuffer output = ByteBuffer.allocate(1024);

    public JavaNioHandler(SocketChannel channel) throws IOException {
        this.channel = channel;
        channel.configureBlocking(false);
        selector = Selector.open();
        key = channel.register(selector, SelectionKey.OP_READ);  // listen to read events
    }

    @Override
    public void run() {
        try {
            while (selector.isOpen() && channel.isOpen()) {
                Set<SelectionKey> keys = select();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                        write(key);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // to handle a JDK bug : select will return empty keys sometimes
    private Set<SelectionKey> select() throws IOException {
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
        if (keys.isEmpty()) {
            int interestOps = key.interestOps();
            selector = Selector.open();
            key = channel.register(selector, interestOps);
            return select();
        }

        return keys;
    }

    private void read(SelectionKey key) throws IOException {
        channel.read(input);
        if (input.position() == 0) {
            return;
        }

        input.flip();
        process();  // handle business logic
        input.clear();
        key.interestOps(SelectionKey.OP_WRITE);  // listen to write events after read
    }

    private void write(SelectionKey key) throws IOException {
        output.flip();
        if (channel.isOpen()) {
            channel.write(output);
            key.channel();
            channel.close();
            output.clear();
        }
    }

    // Business Logic should be async and added to task queue
    // result should be returned as Future class, then respond to client in the callback
    private void process() {
        byte[] bytes = new byte[input.remaining()];
        input.get(bytes);
        String message = new String(bytes, CharsetUtil.UTF_8);
        System.out.println("receive message from client: \n" + message);

        output.put("hello client".getBytes());
    }
}
