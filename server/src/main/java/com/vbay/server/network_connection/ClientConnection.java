package com.vbay.server.network_connection;

import java.io.PrintWriter;

import com.vbay.shared.Utils.JsonUtils;

public class ClientConnection {
    private final ClientSession session;
    private final PrintWriter out;

    public ClientConnection(ClientSession session, PrintWriter out) {
        this.session = session;
        this.out = out;
    }
    ///sau có thể thêm 1 tầng serialize để không hoàn toàn dựa vào Json
    public synchronized void send(Object message) {
        out.println(JsonUtils.toJson(message));
    }

    public ClientSession getSession() {
        return session;
    }
}
