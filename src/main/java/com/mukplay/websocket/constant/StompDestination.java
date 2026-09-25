package com.mukplay.websocket.constant;

public final class StompDestination {

    private StompDestination() {}

    // Prefix definitions
    public static final String APP_PREFIX = "/app";
    public static final String TOPIC_PREFIX = "/topic";

    // Client Inbound destinations (Send to server via /app/...)
    public static final String APP_MOVE = "/game/move";

    // Broadcast Outbound destinations (Subscribe via /topic/...)
    public static final String TOPIC_ROOM_STATE = "/topic/room/{roomId}/state";
    public static final String TOPIC_ROOM_POSITIONS = "/topic/room/{roomId}/positions";
    public static final String TOPIC_ROOM_EVENT = "/topic/room/{roomId}/event";

    public static String getRoomStateDestination(String roomId) {
        return "/topic/room/" + roomId + "/state";
    }

    public static String getRoomPositionsDestination(String roomId) {
        return "/topic/room/" + roomId + "/positions";
    }

    public static String getRoomEventDestination(String roomId) {
        return "/topic/room/" + roomId + "/event";
    }
}
