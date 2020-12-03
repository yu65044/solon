package org.noear.solon.boot.jetty.websocket;

import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.noear.solon.Solon;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.message.Message;
import org.noear.solon.core.message.Session;
import org.noear.solon.extend.socketd.ListenerProxy;
import org.noear.solon.extend.socketd.MessageUtils;
import org.noear.solon.extend.socketd.MessageWrapper;

import java.nio.ByteBuffer;

public class WebSocketListenerImp extends WebSocketAdapter {

    @Override
    public void onWebSocketConnect(org.eclipse.jetty.websocket.api.Session sess) {
        super.onWebSocketConnect(sess);
        ListenerProxy.getGlobal().onOpen(_SocketSession.get(getSession()));
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        try {
            ByteBuffer buf = ByteBuffer.wrap(payload, offset, len);
            Session session = _SocketSession.get(getSession());

            Message message = null;

            if (Solon.global().enableWebSocketD()) {
                message = MessageUtils.decode(buf);
            } else {
                message = MessageWrapper.wrap(getSession().getUpgradeRequest().getOrigin(), null,
                        buf.array());
            }

            ListenerProxy.getGlobal().onMessage(session, message, false);
        } catch (Throwable ex) {
            EventBus.push(ex);
        }
    }

    @Override
    public void onWebSocketText(String text) {
        try {
            Session session = _SocketSession.get(getSession());
            Message message = MessageWrapper.wrap(getSession().getUpgradeRequest().getRequestURI().toString(),null,
                    text.getBytes("UTF-8"));

            ListenerProxy.getGlobal().onMessage(session, message, true);

        } catch (Throwable ex) {
            EventBus.push(ex);
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        ListenerProxy.getGlobal().onClose(_SocketSession.get(getSession()));

        _SocketSession.remove(getSession());
        super.onWebSocketClose(statusCode, reason);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        ListenerProxy.getGlobal().onError(_SocketSession.get(getSession()), cause);
    }
}
