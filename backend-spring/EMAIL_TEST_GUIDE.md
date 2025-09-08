# Gmail 이메일 발송 테스트 가이드

## 🎯 테스트 목적
실제 Gmail SMTP를 통해 **kernelteam2jdi@gmail.com** 계정에서 **자기 자신에게** 이메일을 발송하여 전체 시스템을 검증합니다.

## 📋 사전 준비

### 1. 환경변수 설정 확인
`.env` 파일에 다음이 설정되어 있어야 합니다:
```bash
MAIL_PASSWORD=svaieothjdtxtwyo
JWT_SECRET_KEY=bRmQUS6ug6iZPbR3BCzfSzGByCH2xtZwBZNGZ2jC0Tp1FssGET7Lwkp6XmgBSdTo7IfxCXtwAsE7Wu1UH5oeYg==
```

### 2. Gmail 설정 확인
- **Gmail 계정**: kernelteam2jdi@gmail.com
- **앱 비밀번호**: svaieothjdtxtwyo (2단계 인증 필요)
- **SMTP 서버**: smtp.gmail.com:587

## 🧪 테스트 실행 방법

### 1. 빠른 테스트 (권장)
```bash
# IDE에서 QuickEmailTest 실행
# @ActiveProfiles("email-test") 사용
```

**테스트 내용:**
- ✅ 자신에게 이메일 발송
- ✅ 인증 코드 생성 및 저장
- ✅ 저장소 연동 확인 (Redis/RDB 폴백)

### 2. 전체 통합 테스트
```bash
# IDE에서 EmailSendingIntegrationTest 실행
```

**테스트 내용:**
- ✅ 실제 이메일 발송
- ✅ Rate Limiting 검증
- ✅ 전체 플로우 테스트
- ✅ 이메일 형식 검증

## 📧 예상 결과

### 콘솔 출력
```
🚀 자체 이메일 발송 테스트 시작
📧 발송자: kernelteam2jdi@gmail.com  
📨 수신자: kernelteam2jdi@gmail.com
---
✅ 이메일 발송 성공!
🔢 생성된 인증 코드: 123456
💾 저장소 타입: FallbackVerificationStorage
---
📬 Gmail 받은편지함을 확인하세요!
```

### Gmail 받은편지함
```
제목: [notimo] 회원가입 이메일 인증 코드입니다.

회원가입을 위해 아래 인증 코드를 입력해주세요.

인증 코드: 123456

이 코드는 5분 후에 만료됩니다.
만약 본인이 요청하지 않았다면 이 메일을 무시해주세요.
```

## 🔧 문제 해결

### 1. Authentication 에러
```
535-5.7.8 Username and Password not accepted
```
**해결:** Gmail 앱 비밀번호 재생성 및 2단계 인증 확인

### 2. Connection 에러  
```
Could not connect to SMTP host: smtp.gmail.com, port: 587
```
**해결:** 방화벽 설정 확인 및 인터넷 연결 점검

### 3. 환경변수 에러
```
MAIL_PASSWORD not found
```
**해결:** IDE Run Configuration에서 환경변수 직접 설정

## 🎉 성공 기준
- [x] 콘솔에 "✅ 이메일 발송 성공!" 메시지 출력
- [x] 6자리 숫자 인증 코드 생성
- [x] 저장소에 코드 정상 저장
- [x] Gmail 받은편지함에 이메일 도착
- [x] 이메일 내용 및 형식 정확

## 📝 테스트 체크리스트
- [ ] `.env` 파일 환경변수 설정 완료
- [ ] Gmail 2단계 인증 및 앱 비밀번호 생성 완료  
- [ ] IDE에서 `QuickEmailTest` 실행
- [ ] 콘솔 출력 확인
- [ ] Gmail 받은편지함 확인
- [ ] 인증 코드 일치 확인