package Simple.cline;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyCline {
    public static void main(String[] args) throws InterruptedException {

        //客户端需要一个事件循环组
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();

        //创建客户端启动对象
        Bootstrap bootstrap=new Bootstrap();

        try {

            bootstrap.group(eventExecutors)                 //设置线程组
                    .channel(NioSocketChannel.class)        //设置客户端通道的实现类（反射）
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NettyClineHandler());         //设置自己的处理器
                        }
                    });

            System.out.println("客户端已启动");

            //绑定一个端口并且同步，涉及到netty的异步模型
            ChannelFuture sync = bootstrap.connect("127.0.0.1", 8888).sync();

            //对关闭通道进行监听，如果缺少这段代码，则会直接进入finally，它会让线程进入wait状态
            sync.channel().closeFuture().sync();
        }finally {

            eventExecutors.shutdownGracefully();
        }

    }
}
