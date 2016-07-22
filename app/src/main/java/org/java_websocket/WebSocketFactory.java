package org.java_websocket;

import java.net.Socket;
import java.util.List;

import org.java_websocket.drafts.Draft;

public interface WebSocketFactory {
    WebSocket createWebSocket(WebSocketAdapter a, Draft d, Socket s);

    WebSocket createWebSocket(WebSocketAdapter a, List<Draft> drafts, Socket s);

}
