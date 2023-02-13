package dispatcher;

import handler.JavaNioAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

// Java New IO is a Multiplexing IO Model
// Reactor Pattern has 3 main Components : Dispatcher, Acceptor and Handler
// Dispatcher : listen to events and dispatch the events
// Acceptor : handle accept() events
// Handler : handle read, write, user defined events
public class JavaNioDispatcher implements Runnable {
    private final Selector selector;

    public JavaNioDispatcher(int port) throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open(); // for TCP connections
        serverSocket.configureBlocking(false);
        selector = Selector.open();

        SelectionKey key = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        serverSocket.bind(new InetSocketAddress(port));
        key.attach(new JavaNioAcceptor(serverSocket));
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    dispatch(iterator.next());
                    iterator.remove();
                }

                selector.selectNow(); // this is non-blocking
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dispatch(SelectionKey key) throws IOException {
        Runnable acceptor = (Runnable) key.attachment(); // Acceptor is the attachment
        acceptor.run();
    }

}
