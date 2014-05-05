package floobits.common.handlers;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import floobits.FlooContext;
import floobits.common.FlooUrl;
import floobits.common.NettyFlooConn;
import io.netty.channel.ChannelHandler;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
abstract public class BaseHandler {
    public FlooUrl url;
    public boolean isJoined = false;
    protected NettyFlooConn conn;
    public FlooContext context;

    public BaseHandler(FlooContext context) {
        this.context = context;
    }

    public abstract void on_data(String name, JsonObject obj);
    public abstract void on_connect();

    public Project getProject() {
        return context.project;
    }

    public FlooUrl getUrl() {
        return url;
    }

    public abstract void go();

    public void shutdown() {
        if (conn != null) {
            conn.shutdown();
            conn = null;
        }
        isJoined = false;
    }
}
