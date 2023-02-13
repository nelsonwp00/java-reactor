package dispatcher;

import handler.NettyDiscardHandler;
import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Discards any incoming data.
 */
public class NettyDispatcher {
    private final int port;

    public NettyDispatcher(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // Main Reactor
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // Sub Reactor and Worker Thread
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // NIO Model
                    .option(ChannelOption.SO_BACKLOG, 128) //
                    .childOption(ChannelOption.SO_KEEPALIVE, true) //
                    .childHandler(new ChannelInitializer<SocketChannel>() { //
                        @Override
                        public void initChannel(SocketChannel channel) {
                            channel.pipeline().addLast(new NettyDiscardHandler());
                        }
                    });

            // Bind and start to accept incoming connections.
            ChannelFuture future = bootstrap.bind(port).sync(); // default is async

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new NettyDispatcher(port).run();
    }
}