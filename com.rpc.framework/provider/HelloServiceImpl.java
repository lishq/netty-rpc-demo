package provider;

import publicInterface.HelloService;

/**
 * TODO 实现服务
 * RpcProvider
 *
 * @author lishq
 */
public class HelloServiceImpl implements HelloService {

  @Override
  public String hello(String name) {
    return "Hello " + name;
  }
}