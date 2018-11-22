package netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import protocol.PacketCodeC;
import provider.HelloServiceImpl;

/**
 * 用于处理请求数据
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {

    // 屏蔽非本协议的客户端
    String _msg = msg.toString();
    if (!_msg.startsWith(PacketCodeC.MAGIC_NUMBER)) {
      ctx.channel().close();
      return;
    }
    String result = new HelloServiceImpl()
            .hello(_msg.substring(_msg.lastIndexOf("#") + 1));
    ctx.writeAndFlush(result);
  }
}
