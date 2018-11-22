package api.client;

import conf.ServerConstants;
import netty.NettyClient;
import protocol.PacketCodeC;
import publicInterface.HelloService;

/**
 * RpcConsumer
 * TODO 引用服务
 *
 * @author lishq
 */
public class RpcConsumer implements ServerConstants {

  public static void main(String[] args) throws Exception {

      // 创建一个代理对象
      HelloService service = NettyClient.refer(HelloService.class, PacketCodeC.MAGIC_NUMBER,HOST, PORT);

      for (int i = 0; i < Integer.MAX_VALUE; i ++) {
        String hello = service.hello("World" + i);
        System.out.println(hello);
        Thread.sleep(1000);
      }
  }

}
