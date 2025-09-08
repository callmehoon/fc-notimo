# RDB 원격 연결 테스트 가이드

## 테스트 환경 설정

### 1. 환경변수 설정
`.env` 파일에 다음 환경변수들이 설정되어야 합니다:
```bash
# RDS 데이터베이스 비밀번호
DB_PASSWORD=your_rds_password

# SSH 터널용 PEM 키 경로
DRHONG_PEM_KEY_PATH=/path/to/your/key.pem

# JWT 시크릿 (테스트용)
JWT_SECRET_KEY=your-test-jwt-secret-key
```

### 2. SSH 터널 실행
```bash
# macOS/Linux
./start-tunnel.sh

# Windows
./start-tunnel.ps1
```

### 3. 터널 연결 확인
```bash
# 포트 3307이 열려있는지 확인
lsof -i :3307

# 또는 netstat 사용
netstat -an | grep 3307
```

## 테스트 실행 방법

### 1. 원격 RDB 저장소 단위 테스트
```bash
# IDE에서 실행하거나 gradle 명령어 사용
./gradlew test --tests "RemoteRdbStorageTest" -Dspring.profiles.active=rdb-test
```

**테스트 항목:**
- ✅ 원격 RDB 연결 테스트
- ✅ 트랜잭션 롤백 테스트  
- ✅ 대량 데이터 처리 (50건)
- ✅ 동시성 테스트
- ✅ 만료 시간 정확성 테스트
- ✅ 성능 테스트 (100회 작업)

### 2. RDB 폴백 통합 테스트
```bash
./gradlew test --tests "RdbFallbackIntegrationTest" -Dspring.profiles.active=rdb-test
```

**테스트 시나리오:**
- ✅ 이메일 발송 → RDB 저장 → 조회 검증
- ✅ 회원가입 시 RDB에서 인증 코드 검증
- ✅ 대용량 데이터 처리 성능 (20건)
- ✅ 동시 접근 시뮬레이션 (5개 스레드)
- ✅ 에러 복구 테스트

## 테스트 프로파일 설정

### application-rdb-test.properties
- MySQL RDS 연결 설정
- 디버그 로깅 활성화
- Redis/이메일 선택적 비활성화
- 테스트용 JWT 시크릿

## 실행 전 체크리스트

- [ ] `.env` 파일에 모든 환경변수 설정 완료
- [ ] SSH 터널 스크립트 실행 완료 
- [ ] 포트 3307에서 터널 연결 확인
- [ ] RDS 인스턴스가 실행 중인지 확인
- [ ] 보안 그룹에서 EC2 → RDS 접근 허용 확인

## 예상 결과

### 성공 시
```
원격 RDB 연결 성공
RDB에 저장된 인증 코드: 123456
RDB를 통한 회원가입 성공 및 인증 코드 삭제 완료
RDB 대용량 처리 성능: 20회 작업에 2847ms 소요
RDB 동시 접근 테스트 완료: 5개 이메일 처리 성공
```

### 실패 시 대표적인 원인
1. **SSH 터널 미실행**: `Connection refused` 에러
2. **환경변수 미설정**: `DB_PASSWORD not found` 에러
3. **RDS 접근 권한**: `Access denied` 에러
4. **보안 그룹 설정**: `Timeout` 에러

## 문제 해결

### SSH 터널 재시작
```bash
# 기존 터널 종료
lsof -i :3307 | awk 'NR>1 {print $2}' | xargs kill -9

# 터널 재시작
./start-tunnel.sh
```

### 연결 테스트
```bash
# MySQL 클라이언트로 직접 연결 테스트
mysql -h localhost -P 3307 -u admin -p notimo
```

### 로그 확인
테스트 실행 시 다음 로그 레벨을 확인:
- `com.jober.final2teamdrhong=DEBUG`
- `org.springframework.transaction=DEBUG`
- `org.springframework.orm.jpa=INFO`