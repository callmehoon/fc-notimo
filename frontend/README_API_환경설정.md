# 🔧 API 환경설정 가이드

## 📋 변경사항 요약
프론트엔드 API URL을 환경변수 방식으로 변경했습니다. 이제 개발/배포 환경에 따라 자동으로 올바른 서버에 연결됩니다.

## 🚀 로컬 개발 시 실행 방법

### 1. 백엔드 서버 실행
```bash
# Spring Boot 서버 실행 (8080 포트)
./gradlew bootRun

# AI 서버 실행 (8000 포트) - 필요시
cd backend-ai
python main.py
```

### 2. 프론트엔드 실행
```bash
cd frontend
npm start
```

**✅ 자동으로 로컬 서버에 연결됩니다!**
- Spring Boot: `http://localhost:8080/api`
- AI 서버: `http://localhost:8000`

## 📦 배포 시 동작
```bash
npm run build
```
**✅ 자동으로 배포 서버에 연결됩니다!**
- Spring Boot: `http://dev.notimo.kro.kr/api`
- AI 서버: `http://dev.notimo.kro.kr:8000`

## 📁 파일 구조
```
frontend/
├── .env.development    # 로컬 개발용 (npm start)
├── .env.production     # 배포용 (npm run build)
├── src/services/
│   ├── api.js         # Spring Boot API (환경변수 적용)
│   └── apiAi.js       # AI API (환경변수 적용)
```

## ⚠️ 주의사항

### 1. .env 파일 수정 금지
- `.env.development`, `.env.production` 파일을 임의로 수정하지 마세요
- URL 변경이 필요하면 팀에서 논의 후 수정

### 2. 로컬 테스트 전 서버 확인
- `npm start` 전에 백엔드 서버가 실행 중인지 확인
- Spring Boot: `http://localhost:8080/api/swagger-ui.html` 접속 확인

### 3. 환경변수 우선순위
1. `.env.development` (개발 모드)
2. `.env.production` (프로덕션 빌드)
3. 코드 내 기본값 (`localhost`)

## 🔍 트러블슈팅

### 문제: API 연결 안 됨
**해결방법:**
1. 백엔드 서버 실행 상태 확인
2. 브라우저 개발자도구 → Network 탭에서 요청 URL 확인
3. CORS 에러 시 백엔드 CORS 설정 확인

### 문제: 환경변수 인식 안 됨
**해결방법:**
1. 파일명 확인: `.env.development` (점으로 시작)
2. 변수명 확인: `REACT_APP_` 접두사 필수
3. 서버 재시작: `npm start` 종료 후 다시 실행

## 📞 문의사항
- API 연결 문제: 백엔드 팀
- 환경설정 문제: 프론트엔드 팀
- 배포 관련: DevOps 팀

---
💡 **이제 개발할 때 더 이상 URL을 수동으로 바꿀 필요가 없습니다!**