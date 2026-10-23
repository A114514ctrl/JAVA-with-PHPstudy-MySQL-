@echo off
chcp 936 >nul
title JAVA学生信息管理系统
cd /d "%~dp0"

set "JAVA_EXE="

rem ===== 1. 优先使用系统 PATH 中的 java =====
where java >nul 2>nul
if not errorlevel 1 set "JAVA_EXE=java"

rem ===== 2. 使用 JAVA_HOME =====
if not defined JAVA_EXE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

rem ===== 3. IDEA 下载的 JDK（.jdks 目录）=====
if not defined JAVA_EXE (
  for /d %%D in ("%USERPROFILE%\.jdks\*") do (
    if exist "%%D\bin\java.exe" if not defined JAVA_EXE set "JAVA_EXE=%%D\bin\java.exe"
  )
)

rem ===== 4. Program Files\Java =====
if not defined JAVA_EXE (
  for /d %%D in ("C:/Program Files/Java/jdk*") do (
    if exist "%%D\bin\java.exe" if not defined JAVA_EXE set "JAVA_EXE=%%D\bin\java.exe"
  )
)
if not defined JAVA_EXE (
  for /d %%D in ("C:/Program Files/Java/jre*") do (
    if exist "%%D\bin\java.exe" if not defined JAVA_EXE set "JAVA_EXE=%%D\bin\java.exe"
  )
)
if not defined JAVA_EXE (
  for /d %%D in ("C:/Program Files (x86)/Java/jre*") do (
    if exist "%%D\bin\java.exe" if not defined JAVA_EXE set "JAVA_EXE=%%D\bin\java.exe"
  )
)

rem ===== 5. 其他常见发行版 =====
if not defined JAVA_EXE (
  for /d %%D in ("C:/Program Files/Eclipse Adoptium/jdk*") do (
    if exist "%%D\bin\java.exe" if not defined JAVA_EXE set "JAVA_EXE=%%D\bin\java.exe"
  )
)
if not defined JAVA_EXE (
  for /d %%D in ("C:/Program Files/Microsoft/jdk*") do (
    if exist "%%D\bin\java.exe" if not defined JAVA_EXE set "JAVA_EXE=%%D\bin\java.exe"
  )
)
if not defined JAVA_EXE (
  for /d %%D in ("C:/Program Files/Zulu/zulu*") do (
    if exist "%%D\bin\java.exe" if not defined JAVA_EXE set "JAVA_EXE=%%D\bin\java.exe"
  )
)
if not defined JAVA_EXE (
  for /d %%D in ("C:/Program Files/BellSoft/LibericaJDK*") do (
    if exist "%%D\bin\java.exe" if not defined JAVA_EXE set "JAVA_EXE=%%D\bin\java.exe"
  )
)

rem ===== 没找到 Java =====
if not defined JAVA_EXE (
  echo.
  echo  [错误] 没有检测到 Java 运行环境。
  echo.
  echo  解决办法（任选一种）：
  echo    1. 安装 JDK 8 或更高版本（安装时勾选 Add to PATH），再双击本文件；
  echo    2. 用 IntelliJ IDEA 打开本文件夹，直接运行 src\JAVA\Main.java。
  echo.
  pause
  exit /b 1
)

echo  正在启动 JAVA学生信息管理系统 ...
echo  使用 Java: %JAVA_EXE%
"%JAVA_EXE%" -jar "StudentManagement.jar"

if errorlevel 1 (
  echo.
  echo  程序异常退出，请把上面的报错信息截图反馈。
  pause
)
