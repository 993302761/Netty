package Simple.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;


/**
 *  我们自定义Handler需要继承netty某个规定好的Handler的适配器
 *  这时我们自定义的Handler才叫Handler
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    //处理异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //关闭通道
        ctx.close();
    }

    //数据读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

        //把数据写入到一个缓存并刷新
        //一般我们会将发送的数据进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("收到",CharsetUtil.UTF_8));
    }

    //读取客户端发送的消息
    // ChannelHandlerContext ctx 是上下文对象，含有pipeline管道（可以关联很多handler，注重于业务逻辑），channel通道（更注重数据的读写），地址
    // Object msg 是客户端发送的数据
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //ByteBuf是netty提供的，不是nio的byteBuffer，这个的性能更高
        ByteBuf byteBuf= (ByteBuf) msg;

        System.out.println(ctx.channel().remoteAddress()+" msg: "+byteBuf.toString(CharsetUtil.UTF_8));
    }
}
