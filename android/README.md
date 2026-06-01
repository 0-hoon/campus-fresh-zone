# Android

## 개요

Campus Fresh Zone Android 애플리케이션은 충북대학교 캠퍼스 내 환경 센서 데이터를 시각적으로 제공하고, 사용자의 현재 위치와 주변 환경 상태를 확인할 수 있는 모바일 애플리케이션이다.

백엔드 서버와 REST API를 통해 통신하며, Google Maps를 이용하여 센서 위치를 지도에 표시한다.

---

## 구현 기능

### 1. 현재 위치 표시

* GPS 기반 현재 위치 확인
* 위치 권한 요청 및 처리
* 실시간 위치 업데이트
* 지도 중심 자동 이동

### 2. 환경 센서 데이터 조회

* REST API 연동
* Retrofit2 기반 서버 통신
* JSON 데이터 파싱
* 서버 센서 데이터 수신

### 3. 센서 위치 시각화

* Google Maps SDK 적용
* 센서 위치를 지도 마커로 표시
* 총 42개 센서 데이터 시각화
* 현재 위치 마커 별도 표시

### 4. 센서 정보 확인

센서 마커 선택 시 다음 정보를 확인할 수 있다.

* 센서명
* 온도
* 위험도

### 5. 현재 상태 카드

현재 수신된 환경 데이터를 기반으로 다음 정보를 표시한다.

* 온도
* 습도
* 위험도

예시

```text
현재 상태

온도 : 26.2℃
습도 : 58.4%
위험도 : NORMAL
```

### 6. 행동 가이드

행동 가이드 버튼 선택 시 서버에서 전달한 솔루션 메시지를 표시한다.

예시

```text
모든 환경 수치가 쾌적합니다.
```

또는

```text
야외활동 자제 및 마스크 착용을 권장합니다.
```

---

## 사용 기술

### Language

* Kotlin

### UI

* Jetpack Compose
* Material3

### Map

* Google Maps SDK
* Maps Compose

### Network

* Retrofit2
* Gson Converter

### Async

* Kotlin Coroutines

### Location

* Google Play Services Location

---

## 프로젝트 구조

```text
screen/
 └ MainScreen.kt

model/
 ├ SensorData.kt
 └ SensorResponse.kt

network/
 ├ SensorApi.kt
 ├ RetrofitClient.kt
 └ SensorRepository.kt
```

---

## API 연동

### 센서 데이터 조회

```http
GET /api/sensors
```

서버에서 제공하는 환경 데이터를 수신하여 지도와 UI에 표시한다.

---

## 향후 개선 예정

* Fresh Zone 추천 기능
* GPS 기반 가장 가까운 센서 자동 선택
* 위험도별 마커 색상 구분
* UI/UX 개선
* 예외 처리 강화
* 실시간 데이터 갱신 기능
