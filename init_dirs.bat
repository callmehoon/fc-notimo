:: [Dr.Hong Project] 초기 디렉토리 생성 스크립트
::
:: 사용 방법:
:: 1. 이 파일을 프로젝트 최상위 디렉토리에 위치시킵니다.
:: 2. 파일 탐색기에서 이 파일을 더블클릭하거나, cmd 창에서 직접 실행합니다.
::
:: 이 스크립트는 Spring Boot 프로젝트에 필요한 패키지 디렉토리들을 자동으로 생성합니다.
:: 디렉토리가 이미 존재할 경우에는 아무 작업도 수행하지 않습니다.


:: cmd에서 실행되는 명령어가 화면에 표시되지 않도록 설정합니다.
@echo off

:: 화면에 프로젝트 시작 메시지를 출력합니다.
echo.
echo  =================================================
echo   Dr.Hong Project - Initializing Directories...
echo  =================================================
echo.


:: Backend-Spring 프로젝트의 필요한 디렉토리들을 생성합니다.
:: "if not exist" 구문은 해당 디렉토리가 없을 경우에만 "mkdir"(디렉토리 생성) 명령을 실행합니다.
echo [Spring] Creating service layer directories...
if not exist "backend-spring\src\main\java\com\jober\final2teamdrhong\config" mkdir "backend-spring\src\main\java\com\jober\final2teamdrhong\config"
if not exist "backend-spring\src\main\java\com\jober\final2teamdrhong\controller" mkdir "backend-spring\src\main\java\com\jober\final2teamdrhong\controller"
if not exist "backend-spring\src\main\java\com\jober\final2teamdrhong\dto" mkdir "backend-spring\src\main\java\com\jober\final2teamdrhong\dto"
if not exist "backend-spring\src\main\java\com\jober\final2teamdrhong\entity" mkdir "backend-spring\src\main\java\com\jober\final2teamdrhong\entity"
if not exist "backend-spring\src\main\java\com\jober\final2teamdrhong\repository" mkdir "backend-spring\src\main\java\com\jober\final2teamdrhong\repository"
if not exist "backend-spring\src\main\java\com\jober\final2teamdrhong\service" mkdir "backend-spring\src\main\java\com\jober\final2teamdrhong\service"

echo.

:: 모든 작업이 완료되었음을 알립니다.
echo  Directory setup complete!

echo.