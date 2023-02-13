package handler;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaNioAcceptor implements Runnable {
    private final ExecutorService executor = Executors.newFixedThreadPool(20);

    private final ServerSocketChannel serverSocket;

    public JavaNioAcceptor(ServerSocketChannel serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            SocketChannel channel = serverSocket.accept();
            if (channel != null) {
                executor.execute(new JavaNioHandler(channel));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
