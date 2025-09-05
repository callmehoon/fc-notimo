package com.jober.final2teamdrhong.service.storage;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 로컬 개발 및 테스트 환경을 위한 인메모리(In-Memory) 저장소 구현체입니다.
 * 애플리케이션이 실행되는 동안에만 데이터를 메모리에 저장합니다.
 */
@Component("inMemoryStorage") // 이 구현체의 이름(Bean Name)을 "inMemoryStorage"로 지정
public class InMemoryVerificationStorage implements VerificationStorage {

    // 동시성 문제를 해결하기 위해 ConcurrentHashMap 사용
    private final Map<String, String> storage = new ConcurrentHashMap<>();

    @Override
    public void save(String key, String value) {
        storage.put(key, value);
        // 실제 운영용이 아니므로, TTL(만료 시간) 로직은 단순화하거나 생략합니다.
    }

    @Override
    public Optional<String> find(String key) {
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public void delete(String key) {
        storage.remove(key);
    }
}
