# [ Inotify 기반 자동 배포 ]

## 📖 Overview

<br>

Linux의 inotify를 활용하여 파일 변경을 감지하고 자동으로 애플리케이션을 재배포하는 구조를 구현한 실습입니다.<br>

CI/CD 도구(Jenkins 등)를 사용하기 전 단계에서, 파일 시스템 이벤트 기반 자동화 흐름을 이해하는 것을 목표로 합니다.

<br>

---

## 👥 Contributors
|       배기영       |       서지혜        |
| :-----------------: | :-----------------: |
| [<img width="160px" src="https://github.com/bbky323.png">](https://github.com/bbky323) | [<img width="160px" src="https://github.com/seajihey.png">](https://github.com/seajihey) |
| [@bbky323](https://github.com/bbky323) | [@seajihey](https://github.com/seajihey) |

## 📚 목차

1. [inotify 개요](#1-inotify-개요)
2. [inotify 정리](#2-inotify-정리)
3. [실습 정리](#3-실습-정리)

---

# 1. inotify 개요

---

### 🔍 개념 및 활용 흐름

> Linux의 **inotify**는 파일 시스템의 변화를 감지하는 기능으로,
> 파일 생성·수정·삭제와 같은 이벤트가 발생했을 때 이를 실시간으로 감지하고,
> 해당 이벤트를 기반으로 자동화 작업을 수행할 수 있도록 해줍니다.

- 파일이 변경되면 커널에서 이벤트를 감지하고, 이를 사용자 영역(inotify-tools)으로 전달합니다.<br>
- 이후 스크립트에서 해당 이벤트를 받아 **배포, 로그 처리, 서비스 재시작**과 같은 작업을 수행하는 흐름으로 동작합니다.<Br>


---

### ⚙️ 동작 흐름

```text
파일 변경 발생
→ 커널(inotify)이 이벤트 감지
→ 사용자 영역(inotify-tools)으로 전달
→ 스크립트에서 이벤트 처리 (자동 배포 / 로그 / 재시작 등)
```


---

# 2. inotify 사용 명령어

### 🧩 주요 이벤트

| Event       | 설명             |
| ----------- | -------------- |
| modify      | 파일 내용이 변경됨     |
| create      | 파일/디렉터리 생성     |
| delete      | 파일/디렉터리 삭제     |
| close_write | 파일 수정 후 저장(닫힘) |
| open        | 파일 열림          |
| access      | 파일 읽기          |

---

### 🛠️ 명령어 

```bash
# 파일 수정 감지 (지속 모니터링)
inotifywait -m -e close_write file.jar

# 디렉토리 전체 감지
inotifywait -m -e close_write .

# 여러 이벤트 감지
inotifywait -m -e modify,create,delete /path

# 이벤트 통계 확인
inotifywatch -v -r /path
```

<hr>

#### 📌 1. 파일/디렉토리 변경 감지

```bash
inotifywait -m -e close_write file.jar
inotifywait -m -e close_write .
```

* `close_write` 이벤트를 기준으로 **파일이 저장되는 시점**을 감지
* 특정 파일(`file.jar`)만 감지하거나, 디렉토리(`.`) 전체로 확장 가능
* `-m` 옵션을 통해 지속적으로 이벤트를 모니터링
* 실제 실무에서는 이 이벤트를 기준으로 **자동 배포 트리거**로 활용


#### [ 실행 결과 ] <br><br>
<img width="816" height="232" alt="image" src="https://github.com/user-attachments/assets/8fa86a4d-0042-4480-bf29-c04a7c43cc21" />


---

#### 📌 2. 여러 이벤트 동시 감지

```bash id="qk7f2y"
inotifywait -m -e modify,create,delete /path
```

* 파일 시스템에서 발생하는 **여러 이벤트를 동시에 감지**
* 파일 생성 → 수정 → 삭제 흐름을 실시간으로 추적 가능

---

#### create (파일 생성)

* 새로운 파일이 생성될 때 발생

[ 실행 결과 ]<br><br> <img width="600" height="232" alt="image" src="https://github.com/user-attachments/assets/ad0c5743-d8ec-4ec4-8cfa-96c40b30d1f4" />

---

####  modify (파일 수정)

* 파일 내용이 변경될 때 발생

[ 실행 결과 ]<br><br> <img width="600" height="58" alt="image" src="https://github.com/user-attachments/assets/18ccf321-fc76-4c79-96a5-af5950198946" />

---

####  delete (파일 삭제)

* 파일이 삭제될 때 발생

[ 실행 결과 ]<br><br> <img width="600" height="21" alt="image" src="https://github.com/user-attachments/assets/65feaaf2-a0e9-47c0-97a5-13368bc131d3" />



---

#### 📌 3. 이벤트 통계 확인

```bash
inotifywatch -v -r /path
```

* 특정 디렉토리를 대상으로 이벤트 발생 횟수를 **통계 형태로 출력**

* `-r` : 하위 디렉토리까지 재귀적으로 감시

* `Ctrl + C`로 종료하면 결과 출력

* 단순 감지가 아니라 **어떤 이벤트가 얼마나 발생했는지 분석할 때 사용**

[ 실행 결과 ]<br><br>
<img width="812" height="195" alt="image" src="https://github.com/user-attachments/assets/0dda8239-9c59-4f92-b8d1-f6500253fae2" />

---
#### 📌 핵심 정리

1. `inotifywait`는 파일 시스템 이벤트를 실시간으로 감지하는 도구로, 특정 이벤트 발생 시 즉시 동작을 수행할 수 있기 때문에 자동 배포나 서비스 재시작과 같은 **트리거 역할**에 주로 사용됩니다. <br>

2. `inotifywatch`는 일정 시간 동안 발생한 이벤트를 집계하여 보여주는 도구로, 어떤 이벤트가 얼마나 발생했는지 확인하는 **분석 및 모니터링 목적**에 적합합니다. <br>

3.  파일이 완전히 저장된 시점을 의미하는 `close_write` 이벤트를 기준으로 스크립트를 실행하는 방식이 일반적이며, 이를 통해  **정상적으로 반영된 변경만을 기준으로하는 자동화 흐름**을 구성할 수 있습니다.
 <br>

---

### ⚠️ 주의사항

* monitor 모드(-m)는 지속 실행되므로 무한 루프 구조 주의
* 감지 대상 파일 변경 시, 스크립트가 다시 트리거되는 구조에서 루프 발생 가능
* swap 파일(.swp 등) 생성으로 예상치 못한 이벤트 발생 가능

---

# 3. 실습 정리

### 💻 실습 목적

Spring Boot 애플리케이션의 **jar** 파일을 배포 디렉터리에 업로드하면, **inotifywait**가 파일 변경을 감지하여 자동으로 배포 스크립트를 실행하도록 구성했다. 이를 통해 수동 실행 과정을 줄이고, 애플리케이션 재배포를 자동화했다.

---

### 🔄 전체 흐름


1. 로컬에서 Spring Boot 프로젝트를 빌드하여 **jar** 파일을 생성한다.
2. 생성한 **jar** 파일을 Ubuntu VM의 **deploy** 디렉터리로 업로드한다.
3. VM에서 실행 중인 **watch.sh**가 **deploy** 디렉터리의 파일 변경을 감지한다.
4. 변경이 감지되면 **deploy.sh**를 실행한다.
5. **deploy.sh**는 기존 애플리케이션 프로세스를 종료하고, 새 **jar** 파일을 실행한다.
6. 실행 결과는 **app.log**에 기록된다.

---

### ⚙️ 구성 요소

#### **deploy.sh**
- 배포 대상 **jar** 파일 존재 여부 확인
- 기존 포트 점유 프로세스 종료
- 새 애플리케이션 실행
- 로그 파일 저장
- 실행 성공 여부 확인
```bash
#!/bin/bash

APP_NAME="inotify_web-0.0.1-SNAPSHOT.jar"
DEPLOY_DIR="/home/ubuntu/mission0324/deploy"
LOG_DIR="/home/ubuntu/mission0324/logs"
LOG_FILE="$LOG_DIR/app.log"

echo "===== 배포 시작 ====="
date

# 1. jar 존재 확인
if [ ! -f "$DEPLOY_DIR/$APP_NAME" ]; then
  echo "JAR 파일이 없습니다: $DEPLOY_DIR/$APP_NAME"
  exit 1
fi

# 2. 기존 8080 포트 프로세스 종료
PID=$(lsof -t -i:8081)

if [ -n "$PID" ]; then
  echo "기존 프로세스 종료: $PID"
  kill "$PID"
  sleep 3

  PID2=$(lsof -t -i:8081)
  if [ -n "$PID2" ]; then
    echo "강제 종료: $PID2"
    kill -9 "$PID2"
  fi
fi

# 3. 새 앱 실행
echo "새 앱 실행"
nohup java -jar "$DEPLOY_DIR/$APP_NAME" > "$LOG_FILE" 2>&1 &

sleep 8

# 4. 실행 확인
NEW_PID=$(lsof -t -i:8081)
if [ -n "$NEW_PID" ]; then
  echo "새 앱 실행 성공: PID=$NEW_PID"
else
  echo "새 앱 실행 실패"
  exit 1
fi

echo "===== 배포 완료 ====="

```

#### **watch.sh**
- **deploy** 디렉터리를 지속적으로 감시
- **create**, **close_write**, **moved_to** 이벤트 발생 시 배포 스크립트 실행
- 새 **jar** 업로드 시 자동 재배포 수행
```bash
#!/bin/bash

WATCH_DIR="/home/ubuntu/mission0324/deploy"
TARGET_JAR="inotify_web-0.0.1-SNAPSHOT.jar"
DEPLOY_SCRIPT="/home/ubuntu/mission0324/deploy.sh"

echo "배포 폴더 감시 시작..."
echo "감시 대상: $WATCH_DIR/$TARGET_JAR"

while true
do
  inotifywait -e create -e close_write -e moved_to "$WATCH_DIR"

  if [ -f "$WATCH_DIR/$TARGET_JAR" ]; then
    echo "JAR 변경 감지: $TARGET_JAR"
    "$DEPLOY_SCRIPT"
  fi
done

```

---

### 🚀 실행 방법

#### 1) 감시 스크립트 실행

```bash
ssh watch.sh
```
- watch.sh의 inotifywait가 변경 감지
- 변경 감지 시 deploy.sh 실행

#### 2) jar 파일 업로드

```bash
cp inotify_web-0.0.1-SNAPSHOT.jar /home/ubuntu/mission0324/deploy/
```

- deploy를 디렉터리로 jar 파일을 복사하여 jar가 업데이트 된다고 가정!

---

#### 🔍 확인 방법


**1) watch.sh 실행 결과**

<img width="803" height="226" alt="image" src="https://github.com/user-attachments/assets/f6575aad-4f2f-4006-ad6e-f691fef67f62" />

**2) app.log 결과**

<img width="791" height="451" alt="image" src="https://github.com/user-attachments/assets/c7779e7a-6257-4243-a7f0-c1096589f416" />
 
**3) 재배포시 서버를 내렸을 때**
```bash
curl http://localhost:8081
```
<img width="798" height="66" alt="image" src="https://github.com/user-attachments/assets/9d11ed98-51d1-45da-90fc-f3323babf5a0" />

**4) 서버 다시 올라왔을 때**

<img width="767" height="358" alt="image" src="https://github.com/user-attachments/assets/b35e8a73-a72c-4cf5-9c25-65b8fc1a5753" />

---

### 🛠️ 트러블 슈팅
#### 로그 파일 경로 오류
- 상황:
    - log 파일을 못 찾는 오류 발생
- 원인:
    - 경로 앞에 '$' 가 붙여져 있음 확인
- 해결:
    - 해당 문자를 지워 정상적인 경로로 수정

#### 애플리케이션 실행 확인 실패
- 상황:
    - 위 이미지처럼 프로세스 종료 후 재실행 실패 오류 발생
- 원인:
    - sleep 3 이후 바로 포트를 확인하는 구조 때문에 실행 실패로 오판하는 문제 발생
    - Spring Boot 가동 시간이 4초 이상 걸리면서 발생한 문제였음! 
- 해결:
    - 대기 시간을 8초로 조정하여 문제 해결!
