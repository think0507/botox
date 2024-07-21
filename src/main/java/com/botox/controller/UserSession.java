package com.botox.controller;


import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.WebSocketSession;

public class UserSession {

    private final WebSocketSession session;
    private final WebRtcEndpoint webRtcEndpoint;


    public UserSession(WebSocketSession session) {
        this.session = session;
        this.webRtcEndpoint = null;
    }

    public UserSession(WebSocketSession session, MediaPipeline pipeline) {
        this.session = session;
        this.webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
        this.webRtcEndpoint.setStunServerAddress("stun.l.google.com");
        this.webRtcEndpoint.setStunServerPort(19302);
    }

    public WebSocketSession getSession() {
        return session;
    }

    public WebRtcEndpoint getWebRtcEndpoint() {
        return webRtcEndpoint;
    }

    public void release() {
        if (webRtcEndpoint != null) {
            webRtcEndpoint.release();
        }
    }
}