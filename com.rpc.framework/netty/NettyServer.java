package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 服务端
 */
public class NettyServer {

  /**
   * 暴露服务
   */
  public static void startServer(String hostName, int port) {
      if (hostName == null || hostName.length() == 0)
        throw new IllegalArgumentException("Host == null!");
      if (port <= 0 || port > 65535)
        throw new IllegalArgumentException("Invalid port " + port);
      startServer0(hostName, port);
  }

  private static void startServer0(String hostName, int port) {

      ServerBootstrap bootstrap = new ServerBootstrap();
      NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
      bootstrap.group(eventLoopGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline p = ch.pipeline();
                p.addLast(new StringDecoder());
                p.addLast(new StringEncoder());
                p.addLast(new ServerHandler());
            }
          });

      bind(bootstrap, hostName, port);
  }

  private static void bind(final ServerBootstrap serverBootstrap, final String hostName, final int port) {
      String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
      serverBootstrap.bind(hostName,port).addListener(future -> {
        if (future.isSuccess()) {
          System.out.println(now + ": 端口[" + port + "],地址[" + hostName + "]绑定成功!");
        } else {
          System.err.println(now + "：端口[" + port + "],地址[" + hostName + "]绑定失败!");
        }
      });
  }

}
