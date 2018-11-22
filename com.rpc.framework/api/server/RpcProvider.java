package api.server;

import conf.ServerConstants;
import netty.NettyServer;

/**
 * TODO 暴露服务
 * RpcProvider
 *
 * @author lishq
 */
public class RpcProvider implements ServerConstants {

  public static void main(String[] args) {
    NettyServer.startServer(HOST, PORT);
  }

}
