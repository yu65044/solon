package org.noear.solon.logging;

import org.noear.solon.SolonApp;
import org.noear.solon.Utils;
import org.noear.solon.core.Plugin;
import org.noear.solon.logging.event.Appender;
import org.noear.solon.logging.event.Level;

import java.util.Properties;


/**
 * @author noear
 * @since 1.3
 */
public class XPluginImp implements Plugin {
    @Override
    public void start(SolonApp app) {
        loadAppenderConfig(app);
        loadLoggerConfig(app);
    }

    private void loadAppenderConfig(SolonApp app) {
        Properties props = app.cfg().getProp("solon.logging.appender");

        if (props.size() > 0) {
            props.forEach((k, v) -> {
                String key = (String) k;
                String val = (String) v;

                if (key.endsWith(".class")) {
                    Appender appender = Utils.newInstance(val);
                    if (appender != null) {
                        AppenderManager.getInstance().register(appender);
                    }
                }
            });
        }
    }

    private void loadLoggerConfig(SolonApp app) {
        Properties props = app.cfg().getProp("solon.logging.logger");

        if (props.size() > 0) {
            props.forEach((k, v) -> {
                String key = (String) k;
                String val = (String) v;

                if (key.endsWith(".level")) {
                    String loggerExpr = key.substring(0, key.length() - 6);

                    LogOptions.addLoggerLevel(loggerExpr, Level.of(val, Level.INFO));
                }
            });
        }
    }
}
