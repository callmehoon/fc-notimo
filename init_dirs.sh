#!/bin/bash

# =================================================
#  Dr.Hong Project - Initializing Directories...
# =================================================

echo ""
echo "[Spring] Creating service layer directories..."

# Spring Boot 프로젝트의 베이스 디렉토리 경로를 변수로 지정
BASE_DIR="backend-spring/src/main/java/com/jober/final2teamdrhong"

# -p 옵션: 중간 경로가 없으면 자동으로 만들고, 디렉토리가 이미 있어도 에러 없이 넘어감
mkdir -p "${BASE_DIR}/config"
mkdir -p "${BASE_DIR}/controller"
mkdir -p "${BASE_DIR}/dto"
mkdir -p "${BASE_DIR}/entity"
mkdir -p "${BASE_DIR}/repository"
mkdir -p "${BASE_DIR}/service"

echo ""
echo "Directory setup complete!