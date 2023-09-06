package org.noear.solon.boot.smarthttp;

import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.boot.ServerConstants;
import org.noear.solon.boot.ServerProps;
import org.noear.solon.boot.prop.impl.HttpServerProps;
import org.noear.solon.core.*;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.util.LogUtil;

public final class XPluginImp implements Plugin {
    private static Signal _signal;

    public static Signal signal() {
        return _signal;
    }


    public static String solon_boot_ver() {
        return "smart http 1.2/" + Solon.version();
    }

    private SmHttpServerComb _server;

    @Override
    public void start(AppContext context) {
        if (Solon.app().enableHttp() == false) {
            return;
        }

        context.lifecycle(ServerConstants.SIGNAL_LIFECYCLE_INDEX, () -> {
            start0(Solon.app());
        });
    }

    private void start0(SolonApp app) throws Throwable {
        //初始化属性
        ServerProps.init();

        HttpServerProps props = new HttpServerProps();
        final String _host = props.getHost();
        final int _port = props.getPort();
        final String _name = props.getName();

        long time_start = System.currentTimeMillis();


        _server = new SmHttpServerComb();
        _server.enableWebSocket(app.enableWebSocket());
        _server.setCoreThreads(props.getCoreThreads());
        if (props.isIoBound()) {
            //如果是io密集型的，加二段线程池
            _server.setWorkExecutor(props.getBioExecutor("smarthttp-"));
        }

        _server.setHandler(Solon.app()::tryHandle);

        //尝试事件扩展
        EventBus.publish(_server);
        _server.start(_host, _port);


        final String _wrapHost = props.getWrapHost();
        final int _wrapPort = props.getWrapPort();
        _signal = new SignalSim(_name, _wrapHost, _wrapPort, "http", SignalType.HTTP);
        app.signalAdd(_signal);

        long time_end = System.currentTimeMillis();

        String connectorInfo = "solon.connector:main: smarthttp: Started ServerConnector@{HTTP/1.1,[http/1.1]";
        if (app.enableWebSocket()) {
            LogUtil.global().info(connectorInfo + "[WebSocket]}{0.0.0.0:" + _port + "}");
        }

        String serverUrl = (_server.isSecure() ? "https" : "http") + "://localhost:" + _port;
        LogUtil.global().info(connectorInfo + "}{"+ serverUrl +"}");
        LogUtil.global().info("Server:main: smarthttp: Started (" + solon_boot_ver() + ") @" + (time_end - time_start) + "ms");
    }

    @Override
    public void stop() throws Throwable {
        if (_server != null) {
            _server.stop();
            _server = null;

            LogUtil.global().info("Server:main: smarthttp: Has Stopped (" + solon_boot_ver() + ")");
        }
    }
}