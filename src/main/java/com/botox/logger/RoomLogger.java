package com.botox.logger;

import com.botox.controller.RoomApiController;
import com.botox.util.CustomIpUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.logging.Logger;

public class RoomLogger {
    private static final Logger logger = Logger.getLogger(RoomLogger.class.getName());

    public static void RoomLog(
            String logType,
            String action, // join, enter, leave
            Long roomNum,
            String roomContent,
            Long userId,
            HttpServletRequest request
    ) {
        String roomIdentifier;

        // roomNum과 roomContent 중 Null이 아닌 값 추출
        if (roomNum != null) {
            roomIdentifier = "room " + roomNum;
        } else if (roomContent != null && !roomContent.isEmpty()) {
            roomIdentifier = "room " + roomContent;
        } else {
            roomIdentifier = "unknown room";
        }

        // 로그 메시지 형식: "User {userId} {action} {roomIdentifier}"
        String Message = String.format("User %s %-5s %s", userId, action, roomIdentifier);

        String LogMessage = String.format(" %s | %-5s | %-20s | %s | %s",
                logType,
                action,
                Message,
                CustomIpUtil.getClientIp(request),
                request.getHeader("User-Agent")
        );

        logger.info(LogMessage);
    }
}
//2024-08-20 13:57:58 - | join  | User 2 join  room 1  | 127.0.0.1 | PostmanRuntime/7.41.0
//2024-08-20 13:58:01 - | enter | User 1 enter room A  | 127.0.0.1 | PostmanRuntime/7.41.0

