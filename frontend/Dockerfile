# frontend/Dockerfile
FROM node:18-alpine as build
WORKDIR /app

# 빌드 인자 선언 (기본값은 로컬호스트)
ARG REACT_APP_API_BASE_URL=http://localhost:8080/api
ARG REACT_APP_AI_API_BASE_URL=http://localhost:8000/ai

# 빌드 인자를 환경변수로 설정
ENV REACT_APP_API_BASE_URL=$REACT_APP_API_BASE_URL
ENV REACT_APP_AI_API_BASE_URL=$REACT_APP_AI_API_BASE_URL

# package.json과 package-lock.json 복사
COPY frontend/package*.json ./

# 의존성 설치
RUN npm ci

# 소스 코드 복사
COPY frontend/ .

# React 앱 빌드 (환경변수가 번들에 포함됨)
RUN npm run build

# Nginx로 정적 파일 서빙
FROM nginx:alpine
ARG NGINX_CONFIG
COPY --from=build /app/build /usr/share/nginx/html
COPY frontend/${NGINX_CONFIG} /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
