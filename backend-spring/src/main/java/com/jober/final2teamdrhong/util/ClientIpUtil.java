package com.jober.final2teamdrhong.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class ClientIpUtil {
    
    private static final String[] IP_HEADERS = {
        "X-Forwarded-For",
        "X-Real-IP", 
        "X-Original-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"
    };
    
    public static String getClientIpAddress(HttpServletRequest request) {
        return getClientIpAddress(request, false); // 기본적으로 운영환경으로 처리
    }
    
    /**
     * 클라이언트 IP 주소를 가져옵니다.
     * @param request HTTP 요청 객체
     * @param isDevelopment 개발환경 여부 (true: 로컬 IP 허용, false: 로컬 IP 차단)
     * @return 클라이언트 IP 주소
     */
    public static String getClientIpAddress(HttpServletRequest request, boolean isDevelopment) {
        for (String header : IP_HEADERS) {
            String ipAddress = request.getHeader(header);
            if (isValidIpAddress(ipAddress, isDevelopment)) {
                if (ipAddress.contains(",")) {
                    return ipAddress.split(",")[0].trim();
                }
                return ipAddress;
            }
        }
        return request.getRemoteAddr();
    }
    
    /**
     * IP 주소의 유효성을 검증합니다.
     * @param ipAddress 검증할 IP 주소
     * @param isDevelopment 개발환경 여부
     * @return 유효한 IP 주소인지 여부
     */
    private static boolean isValidIpAddress(String ipAddress, boolean isDevelopment) {
        if (!StringUtils.hasText(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
            return false;
        }
        
        // 개발환경에서는 로컬 IP 허용
        if (isDevelopment) {
            return true;
        }
        
        // 운영환경에서는 로컬 IP 차단
        return !"127.0.0.1".equals(ipAddress) && 
               !"0:0:0:0:0:0:0:1".equals(ipAddress) &&
               !"::1".equals(ipAddress) &&
               !ipAddress.startsWith("192.168.") &&
               !ipAddress.startsWith("10.") &&
               !(ipAddress.startsWith("172.") && isPrivateClassB(ipAddress));
    }
    
    /**
     * 172.16.0.0 ~ 172.31.255.255 (Class B 사설 IP) 범위인지 확인
     */
    private static boolean isPrivateClassB(String ipAddress) {
        try {
            String[] parts = ipAddress.split("\\.");
            if (parts.length == 4 && parts[0].equals("172")) {
                int second = Integer.parseInt(parts[1]);
                return second >= 16 && second <= 31;
            }
        } catch (NumberFormatException e) {
            // IP 파싱 오류 시 안전하게 false 반환
        }
        return false;
    }
}