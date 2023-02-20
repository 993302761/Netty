package Simple.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
    public static void main(String[] args) throws InterruptedException {

        // 创建BossGroup和WorkerGroup
        // BossGroup处理accept请求  WorkerGroup处理其他读写请求
        // 两者都是无限循环等待任务执行
        // BossGroup和WorkerGroup的子线程的个数默认是（CPU核数*2）
        NioEventLoopGroup bossGroup=new NioEventLoopGroup();
        NioEventLoopGroup workerGroup=new NioEventLoopGroup();

        // 创建服务端启动对象
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        try {
            /**
             * 在TCP三次握手的这个过程中，服务器内核会用到两个队列：
             *
             *     syn 队列：未完成三次握手的连接，记作 A 队列；
             *     accept 队列：已完成三次握手，内核正等待进程执行 accept 函数的连接，记作 B 队列。
             *
             * ChannelOption.SO_BACKLOG 配置就是控制 A + B 队列总长度的参数，如果这两个队列都满了，那么 Netty 服务将不会再接收新的连接请求了。
             */
            serverBootstrap.group(bossGroup,workerGroup)              //设置两个线程组
                    .channel(NioServerSocketChannel.class)          //使用NioServerSocketChannel作为服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG,128)       //设置线程队列的等待连接的个数
                    .childOption(ChannelOption.SO_KEEPALIVE,true)   //设置保持活动连接状态，当设置为true的时候，TCP会实现监控连接是否有效
                    // 当连接处于空闲状态的时候，超过了2个小时，本地的TCP实现会发送一个数据包给远程的 socket
                    // 如果远程没有发回响应，TCP会持续尝试11分钟，知道响应为止，如果在12分钟的时候还没响应，TCP尝试关闭socket连接。
                    //给workerGroup的 EventLoop 对应的管道设置处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {        //创建一个通道初始化对象
                        //给pipeline设置处理器
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new NettyServerHandler());
                        }
                    });

            System.out.println("服务器已启动");

            ChannelFuture sync = null;

            //绑定一个端口并且同步，涉及到netty的异步模型
            sync = serverBootstrap.bind(8888).sync();

            //对关闭通道进行监听，如果缺少这段代码，则会直接进入finally，它会让线程进入wait状态
            sync.channel().closeFuture().sync();
        }finally {

            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }



    }
}
