package com.simple.rpc.network.server;

import com.simple.rpc.entity.LocalServerInfo;
import com.simple.rpc.network.codec.RpcDecoder;
import com.simple.rpc.network.codec.RpcEncoder;
import com.simple.rpc.network.msg.Request;
import com.simple.rpc.network.msg.Response;
import com.simple.rpc.util.NetUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.context.ApplicationContext;

/**
 * 功能描述:
 *
 * @author: WuChengXing
 * @create: 2021-12-29 10:33
 **/
public class ServerSocket implements Runnable {

    private ChannelFuture f;

    private transient ApplicationContext applicationContext;

    public ServerSocket(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public boolean isActiveSocketServer() {
        try {
            if (f != null) {
                return f.channel().isActive();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void run() {
        /**
         * 管理连接的事件循环组
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        /**
         * 处理handler，也即处理通道信息等的事件循环组
         */
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new RpcDecoder(Request.class),
                                    new RpcEncoder(Response.class),
                                    new ServerSocketHandler(applicationContext));
                        }
                    });

            //启动初始端口
            int port = 41200;
            while (NetUtil.isPortUsing(port)) {
                port++;
            }
            LocalServerInfo.LOCAL_HOST = NetUtil.getHost();
            LocalServerInfo.LOCAL_PORT = port;
            //注册服务
            this.f = b.bind(port).sync();
            // 返回请求等待结果，异步监听事件
            this.f.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }
}
