package com.botox.util;

import jakarta.servlet.http.HttpServletRequest;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CustomIpUtil {
    public static String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getHeader("X-FORWARDED-FOR");

        if (remoteAddr == null || remoteAddr.isEmpty()) {
            remoteAddr = request.getRemoteAddr();
        } else {
            // 여러 IP가 포함된 경우 첫 번째 IP 사용
            remoteAddr = remoteAddr.split(",")[0].trim();
        }

        // IPv6 로컬 주소를 IPv4로 변환 숏컷 (localhost)
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            return "127.0.0.1";
        }

        // IPv6 주소를 IPv4 주소로 변환
        try {
            InetAddress inetAddress = InetAddress.getByName(remoteAddr);
            if (inetAddress instanceof Inet6Address) {
                byte[] ipv4Bytes = extractIPv4FromIPv6(inetAddress.getAddress());
                if (ipv4Bytes != null) {
                    return InetAddress.getByAddress(ipv4Bytes).getHostAddress();
                }
            }
        } catch (UnknownHostException e) {
            System.err.print(
                    e.fillInStackTrace().toString()
            );
        }

        return remoteAddr;  // IPv4 주소 또는 그 외의 경우
    }

    private static byte[] extractIPv4FromIPv6(byte[] ipv6) {
        // IPv4로 매핑된 IPv6 주소인지 확인 (예: ::ffff:192.168.0.1)
        if (ipv6.length == 16) {
            for (int i = 0; i < 10; i++) {
                if (ipv6[i] != 0) return null;
            }
            if (ipv6[10] == (byte) 0xff && ipv6[11] == (byte) 0xff) {
                byte[] ipv4 = new byte[4];
                System.arraycopy(ipv6, 12, ipv4, 0, 4);
                return ipv4;
            }
        }
        return null;
    }
}