package netty;

import conf.ServerConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NettyClient {

    private static ExecutorService executor = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static ClientHandler client;

    private static final int MAX_RETRY = ServerConstants.MAX_RETRY;

    /**
     * TODO 引用服务
     *
     * @param <T> 接口泛型
     * @param interfaceClass 接口类型
     * @param host 服务器主机名
     * @param port 服务器端口
     * @return 远程服务
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T refer(final Class<T> interfaceClass, final String providerName, final String host, final int port) throws Exception {
        if (interfaceClass == null)
            throw new IllegalArgumentException("Interface class == null");
        if (!interfaceClass.isInterface())
            throw new IllegalArgumentException("The " + interfaceClass.getName() + " must be interface class!");
        if (host == null || host.length() == 0)
            throw new IllegalArgumentException("Host == null!");
        if (port <= 0 || port > 65535)
            throw new IllegalArgumentException("Invalid port " + port);
        System.out.println("Get remote service " + interfaceClass.getName() + " from server " + host + ":" + port);
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] {interfaceClass}, (proxy, method, args) -> {
            if (client == null) {
                CountDownLatch latch = new CountDownLatch(1);
                initClient(host,port,latch);
                latch.await();
            }
            // 设置参数
            client.setParam(providerName + args[0]);
            return executor.submit(client).get();
        });
    }

    private static void initClient(final String host, final int port, CountDownLatch latch) {
        client = new ClientHandler();
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new StringDecoder());
                        p.addLast(new StringEncoder());
                        p.addLast(client);
                    }
                });
        connect(bootstrap, host, port, MAX_RETRY,latch);
    }

    private static void connect(Bootstrap bootstrap, final String host, final int port, int retry,CountDownLatch latch) {
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println(now + ": 连接成功");
                latch.countDown();
            } else if (retry == 0) {
                System.err.println(now + ": 重试次数已用完，放弃连接！");
                latch.countDown();
            } else {
                // 第几次重连
                int order = (MAX_RETRY - retry) + 1;
                // 本次重连的间隔
                int delay = 1 << order;
                System.err.println(now + ": 连接失败，第" + order + "次重连……");
                bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retry - 1,latch),
                        delay, TimeUnit.SECONDS);
            }
        });
    }
}
