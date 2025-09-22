package com.jober.final2teamdrhong.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    /**
     * @Async 메서드에서 사용할 기본 Executor 설정
     * DelegatingSecurityContextAsyncTaskExecutor를 사용하여 SecurityContext를 비동기 스레드로 전파
     */
    @Override
    public Executor getAsyncExecutor() {
        // 가상 스레드를 사용하는 SimpleAsyncTaskExecutor 생성
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setVirtualThreads(true); // JDK 21 가상 스레드 활성화
        executor.setThreadNamePrefix("async-");

        // SecurityContext를 전파하는 DelegatingSecurityContextAsyncTaskExecutor로 래핑
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }

    /**
     * 비동기 실행 중 발생한 예외를 처리할 핸들러
     */
    @Override
    public org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("비동기 메서드 실행 중 예외 발생: {}, params={}", method.getName(), params, ex);
        };
    }
}