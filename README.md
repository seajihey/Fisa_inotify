# [ Inotify 기반 자동 배포 ]

## 📖 Overview

<br>

Linux의 inotify를 활용하여 파일 변경을 감지하고 자동으로 애플리케이션을 재배포하는 구조를 구현한 실습입니다.<br>

CI/CD 도구(Jenkins 등)를 사용하기 전 단계에서, 파일 시스템 이벤트 기반 자동화 흐름을 이해하는 것을 목표로 합니다.

<br>

---

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

> (목적 작성 자리)

---

### 🔄 전체 흐름

```text
(실습 전체 흐름)
```

---

### ⚙️ 구성 요소

* watch.sh
* deploy.sh
* (기타)

---

### 🚀 실행 방법

```bash
# 실행 절차
```

---

### 🔍 확인 방법

*
*
*

-
---

## 📚 목차

1. [inotify 개요](#1-inotify-개요)
2. [inotify 정리](#2-inotify-정리)
3. [실습 정리](#3-실습-정리)

---

# 1. inotify 개요

### 🔍 개념

> (설명 들어갈 자리)

---

### 🎯 사용 목적

*
*
*

---

### ⚙️ 동작 흐름

```text
(흐름 작성 자리)
```

---

# 2. inotify 정리

### 📌 핵심 개념

*
*
*

---

### 🧩 주요 이벤트

| Event | 설명 |
| ----- | -- |
|       |    |
|       |    |
|       |    |

---

### 🛠️ 사용 방식

```bash
# 명령어 예시 자리
```

---

### ⚠️ 주의사항

*
*
*

---

# 3. 실습 정리

### 💻 실습 목적

> (목적 작성 자리)

---

### 🔄 전체 흐름

```text
(실습 전체 흐름)
```

---

### ⚙️ 구성 요소

* watch.sh
* deploy.sh
* (기타)

---

### 🚀 실행 방법

```bash
# 실행 절차
```

---

### 🔍 확인 방법

*
*
*

---

이 정도 구조면 지금 하신 **inotify + 자동배포 실습** 깔끔하게 정리하기에 충분합니다.
내용 채우실 때 막히는 부분 있으면 그 파트만 따로 요청하시면 됩니다.
