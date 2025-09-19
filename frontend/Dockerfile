# frontend/Dockerfile
FROM node:18-alpine as build

WORKDIR /app

# package.json과 package-lock.json 복사
COPY frontend/package*.json ./

# 의존성 설치
RUN npm ci

# 소스 코드 복사
COPY frontend/ .

# React 앱 빌드
RUN npm run build

# Nginx로 정적 파일 서빙
FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY frontend/nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]