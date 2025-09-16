package com.jober.final2teamdrhong.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 타이밍 공격 방지를 위한 유틸리티 클래스
 * 요청 시작 시간을 ThreadLocal에 저장하고 최소 응답 시간을 보장
 */
@Component
@Slf4j
public class TimingAttackProtection {

    // 요청 시작 시간을 저장하는 ThreadLocal
    private static final ThreadLocal<Long> REQUEST_START_TIME = new ThreadLocal<>();

    /**
     * 요청 시작 시간을 ThreadLocal에 설정
     * Controller나 Service 진입점에서 호출
     */
    public void startTiming() {
        REQUEST_START_TIME.set(System.currentTimeMillis());
    }

    /**
     * 최소 응답 시간을 보장 (타이밍 공격 방지)
     * @param minimumMs 최소 대기 시간 (밀리초)
     */
    public void ensureMinimumResponseTime(long minimumMs) {
        try {
            long currentTime = System.currentTimeMillis();
            long startTime = getCurrentRequestStartTime();
            long elapsed = currentTime - startTime;

            if (elapsed < minimumMs) {
                long sleepTime = minimumMs - elapsed;
                log.debug("보안 지연 적용: {}ms 대기 (총 경과시간: {}ms)", sleepTime, elapsed);
                Thread.sleep(sleepTime);
            } else {
                log.debug("최소 응답 시간 이미 만족: {}ms (요구: {}ms)", elapsed, minimumMs);
            }
        } catch (InterruptedException e) {
            // 인터럽트 상태 복원
            Thread.currentThread().interrupt();
            log.warn("응답 시간 지연 중 인터럽트 발생");
        }
    }

    /**
     * ThreadLocal 정리 (메모리 누수 방지)
     * 반드시 finally 블록에서 호출해야 함
     */
    public void clear() {
        REQUEST_START_TIME.remove();
    }

    /**
     * 현재 요청의 시작 시간을 반환
     * @return 시작 시간 (밀리초), ThreadLocal에 값이 없으면 현재 시간
     */
    private long getCurrentRequestStartTime() {
        Long startTime = REQUEST_START_TIME.get();
        if (startTime == null) {
            // ThreadLocal에 값이 없으면 현재 시간을 기본값으로 사용 (fallback)
            long currentTime = System.currentTimeMillis();
            log.warn("요청 시작 시간이 설정되지 않음, 현재 시간을 기본값으로 사용: {}", currentTime);
            return currentTime;
        }
        return startTime;
    }

    /**
     * 현재 요청의 경과 시간을 반환
     * @return 경과 시간 (밀리초)
     */
    public long getElapsedTime() {
        long currentTime = System.currentTimeMillis();
        long startTime = getCurrentRequestStartTime();
        return currentTime - startTime;
    }
}