# API Specification

## 1. API 개요

본 문서는 Campus Fresh Zone 서비스에서 Android App과 중간 서버가 통신하기 위한 API 명세서이다.

Android App은 중간 서버 API를 호출하여 캠퍼스 구역별 환경 데이터, 위험도 판단 결과, 대응 솔루션 메시지를 조회한다.

본 API 명세서는 서버와 앱의 병렬 개발을 위해 작성한다. 서버 구현이 완료되기 전에도 앱 개발자는 명세된 Response Example을 Mock Data로 활용하여 화면을 구현할 수 있다.

```text
Android App
    ↓
중간 서버 API
    ↓
환경 데이터 조회 / 위험도 판단 / 솔루션 제공
```

---

## 2. API 명세 작성 기준

API 명세는 다음 항목을 기준으로 작성한다.

| 항목 | 설명 |
|---|---|
| Method | 어떤 동작을 수행하는지 나타내는 HTTP 메소드 |
| Endpoint / Path | 요청을 보내는 URL 주소 |
| Request | 클라이언트가 서버로 보내는 데이터 |
| Response | 서버가 클라이언트에게 반환하는 데이터 |

---

## 3. Method 설계 기준

본 프로젝트에서는 API의 목적에 따라 GET과 POST를 구분하여 사용한다.

| Method | 사용 목적 | 예시 |
|---|---|---|
| GET | 서버에서 데이터를 조회할 때 사용 | 구역별 환경 상태 조회 |
| POST | 서버에 데이터를 새로 등록하거나 전송할 때 사용 | 센서 데이터 저장, 테스트 데이터 등록 |

앱이 화면에 표시할 데이터를 받아오는 기능은 GET을 사용한다.  
센서 데이터나 사용자 입력값을 서버에 저장해야 하는 경우에는 POST를 사용한다.

---

## 4. Base URL

```text
http://서버주소/api...
```

개발 단계에서는 로컬 서버 또는 테스트 서버 주소를 사용한다.

예시:

```text
http://localhost:8080/api...
```

---

## 5. 공통 응답 형식

모든 API 응답은 아래 형식을 기본으로 한다.

```json
{
  "status": "OK",
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| status | String | 응답 상태 |
| message | String | 응답 메시지 |
| data | Object / Array | 실제 응답 데이터 |

### 응답 상태 코드

| HTTP Status | status | 설명 |
|---|---|---|
| 200 | OK | 요청 성공 |
| 400 | BAD_REQUEST | 잘못된 요청 |
| 404 | NOT_FOUND | 데이터를 찾을 수 없음 |
| 500 | INTERNAL_SERVER_ERROR | 서버 내부 오류 |

---

## 6. API 목록

| 기능 설명 | Method | URL | Request | Response | 기타 |
|---|---|---|---|---|---|
| 전체 구역 상태 조회 | GET | `/api/zones` | 없음 | 구역별 환경 데이터 배열 | 메인 화면 또는 목록 화면에서 사용 |
| 특정 구역 상세 조회 | GET | `/api/zones/{zoneId}` | Path Parameter: `zoneId` | 특정 구역 상세 데이터 | 구역 카드 클릭 시 사용 |
| 센서 데이터 등록 | POST | `/api/sensor-data` | 센서 데이터 JSON | 등록 결과 | 선택 구현, 테스트 데이터 등록 또는 저장 필요 시 사용 |

초기 MVP에서는 `GET /api/zones`와 `GET /api/zones/{zoneId}`를 우선 구현한다.  
`POST /api/sensor-data`는 테스트 데이터 등록 또는 데이터 저장이 필요한 경우 추가로 구현한다.

---

## 7. 전체 구역 상태 조회

```http
GET /api/zones
```

### 기능 설명

캠퍼스 내 전체 구역의 환경 데이터와 서버에서 판단한 상태, 솔루션 메시지를 조회한다.

앱의 메인 화면 또는 구역 목록 화면에서 사용한다.

### Request

요청 데이터는 없다.

### Response

구역별 환경 데이터 배열을 반환한다.

### Response Example

```json
{
  "status": "OK",
  "message": "구역별 환경 데이터 조회 성공",
  "data": [
    {
      "zoneId": 1,
      "zoneName": "도서관 입구",
      "sensorName": "opensrc_team1",
      "temperature": 28.5,
      "humidity": 62,
      "aqi": 85,
      "tvoc": 420,
      "eco2": 1100,
      "statusText": "주의",
      "statusLevel": 2,
      "mainRisk": "CO2",
      "solution": "CO2 수치가 높아 장시간 체류 시 주의가 필요합니다.",
      "measuredAt": "2026-05-13T15:30:00"
    },
    {
      "zoneId": 2,
      "zoneName": "정문 앞",
      "sensorName": "opensrc_team2",
      "temperature": 24.8,
      "humidity": 45,
      "aqi": 35,
      "tvoc": 250,
      "eco2": 700,
      "statusText": "정상",
      "statusLevel": 1,
      "mainRisk": "NONE",
      "solution": "야외활동에 적합한 상태입니다.",
      "measuredAt": "2026-05-13T15:30:00"
    }
  ]
}
```

### Response Field

| 필드 | 타입 | 설명 |
|---|---|---|
| zoneId | Long | 구역 ID |
| zoneName | String | 구역 이름 |
| sensorName | String | 센서 이름 |
| temperature | Double | 온도 |
| humidity | Double | 습도 |
| aqi | Int | 공기질 지수 |
| tvoc | Int | 총휘발성유기화합물 |
| eco2 | Int | 이산화탄소 추정값 |
| statusText | String | 환경 상태 텍스트 |
| statusLevel | Int | 상태 단계 |
| mainRisk | String | 주요 위험 요인 |
| solution | String | 대응 솔루션 메시지 |
| measuredAt | String | 측정 시간 |

---

## 8. 특정 구역 상세 조회

```http
GET /api/zones/{zoneId}
```

### 기능 설명

특정 구역의 상세 환경 데이터와 판단 결과를 조회한다.

앱에서 특정 구역 카드를 선택했을 때 상세 화면에 표시하기 위해 사용한다.

### Request

Path Parameter를 사용한다.

| 이름 | 타입 | 필수 여부 | 설명 |
|---|---|---|---|
| zoneId | Long | 필수 | 조회할 구역 ID |

### Response

특정 구역의 상세 환경 데이터와 솔루션 정보를 반환한다.

### Response Example

```json
{
  "status": "OK",
  "message": "구역 상세 데이터 조회 성공",
  "data": {
    "zoneId": 1,
    "zoneName": "도서관 입구",
    "sensorName": "opensrc_team1",
    "temperature": 28.5,
    "humidity": 62,
    "aqi": 85,
    "tvoc": 420,
    "eco2": 1100,
    "statusText": "주의",
    "statusLevel": 2,
    "mainRisk": "CO2",
    "solution": "CO2 수치가 높아 장시간 체류 시 주의가 필요합니다.",
    "description": "CO2 수치가 주의 단계에 해당하므로 장시간 체류를 피하는 것이 좋습니다.",
    "measuredAt": "2026-05-13T15:30:00"
  }
}
```

### Response Field

| 필드 | 타입 | 설명 |
|---|---|---|
| zoneId | Long | 구역 ID |
| zoneName | String | 구역 이름 |
| sensorName | String | 센서 이름 |
| temperature | Double | 온도 |
| humidity | Double | 습도 |
| aqi | Int | 공기질 지수 |
| tvoc | Int | 총휘발성유기화합물 |
| eco2 | Int | 이산화탄소 추정값 |
| statusText | String | 환경 상태 텍스트 |
| statusLevel | Int | 상태 단계 |
| mainRisk | String | 주요 위험 요인 |
| solution | String | 대응 솔루션 메시지 |
| description | String | 상세 설명 |
| measuredAt | String | 측정 시간 |

---

## 9. 센서 데이터 등록

```http
POST /api/sensor-data
```

### 기능 설명

서버 테스트 또는 센서 데이터 저장이 필요한 경우 사용하는 API이다.

초기 MVP에서는 필수 구현 대상이 아니며, 필요 시 추가 구현한다.

### Request

센서 데이터 JSON을 Request Body로 전송한다.

### Request Example

```json
{
  "sensorName": "opensrc_team1",
  "temperature": 28.5,
  "humidity": 62,
  "aqi": 85,
  "tvoc": 420,
  "eco2": 1100,
  "measuredAt": "2026-05-13T15:30:00"
}
```

### Request Field

| 필드 | 타입 | 필수 여부 | 설명 |
|---|---|---|---|
| sensorName | String | 필수 | 센서 이름 |
| temperature | Double | 필수 | 온도 |
| humidity | Double | 필수 | 습도 |
| aqi | Int | 필수 | 공기질 지수 |
| tvoc | Int | 필수 | 총휘발성유기화합물 |
| eco2 | Int | 필수 | 이산화탄소 추정값 |
| measuredAt | String | 선택 | 측정 시간 |

### Response Example

```json
{
  "status": "OK",
  "message": "센서 데이터 등록 성공",
  "data": {
    "sensorDataId": 1
  }
}
```

---

## 10. 상태값 정의

| statusLevel | statusText | 설명 |
|---|---|---|
| 1 | 정상 | 야외활동 가능 |
| 2 | 주의 | 장시간 야외활동 주의 |
| 3 | 위험 | 야외활동 자제 권장 |

---

## 11. 주요 위험 요인 정의

| mainRisk | 설명 |
|---|---|
| NONE | 위험 요인 없음 |
| AQI | 공기질 악화 |
| CO2 | 이산화탄소 증가 |
| TVOC | 휘발성 유기화합물 증가 |
| TEMP | 고온 |
| HUMIDITY | 습도 문제 |
| COMPLEX | 복합 위험 |

---

## 12. 상태 판단 기준

서버는 환경 데이터를 아래 기준에 따라 정상 / 주의 / 위험 단계로 분류한다.

| 항목 | 정상 | 주의 | 위험 |
|---|---|---|---|
| AQI / CAI | 0~50 | 51~100 | 101 이상 |
| eCO2 / CO2 | 1000ppm 이하 | 1001~1500ppm | 1501ppm 이상 |
| TVOC | 400㎍/㎥ 이하 | 401~500㎍/㎥ | 501㎍/㎥ 이상 |
| 온도 | 24~26℃ | 27~32℃ | 33℃ 이상 |
| 습도 | 적정 범위 | 고습 / 저습 | 고온과 결합 시 위험 가중 |

본 서비스는 공식적인 법적 측정·판정 시스템이 아니라, 공공기관 기준을 참고하여 위험 가능성을 사용자에게 안내하는 모니터링 및 의사결정 보조 서비스이다.

---

## 13. 솔루션 매핑 기준

| 조건 | 판단 | 제공 솔루션 |
|---|---|---|
| AQI / CAI 101 이상 | 공기질 나쁨 | 야외활동 자제 및 마스크 착용 권장 |
| CO2 1000ppm 초과 | 환기 부족 가능성 | 장시간 체류 주의 |
| CO2 1500ppm 초과 | 환기 위험 수준 | 해당 구역 이용 자제 권장 |
| TVOC 500㎍/㎥ 초과 | 유기화합물 농도 높음 | 오염원 확인 및 접근 주의 |
| 온도 33℃ 이상 | 온열질환 위험 | 야외활동 자제, 수분 섭취, 휴식 권장 |
| 고온 + 고습 | 체감온도 상승 가능 | 장시간 야외활동 자제 권장 |

---

## 14. 에러 응답

요청 실패 시 아래 형식으로 응답한다.

```json
{
  "status": "NOT_FOUND",
  "message": "존재하지 않는 구역입니다.",
  "data": null
}
```

### Error Code

| HTTP Status | status | 설명 |
|---|---|---|
| 200 | OK | 요청 성공 |
| 400 | BAD_REQUEST | 잘못된 요청 |
| 404 | NOT_FOUND | 데이터 없음 |
| 500 | INTERNAL_SERVER_ERROR | 서버 내부 오류 |

### API별 예외 상황

| API | 예외 상황 | HTTP Status | message 예시 |
|---|---|---|---|
| GET /api/zones | 서버에서 구역 데이터를 불러오지 못한 경우 | 500 | 구역별 환경 데이터 조회 중 오류가 발생했습니다. |
| GET /api/zones/{zoneId} | zoneId가 숫자 형식이 아닌 경우 | 400 | 잘못된 구역 ID 형식입니다. |
| GET /api/zones/{zoneId} | 해당 zoneId의 구역이 존재하지 않는 경우 | 404 | 존재하지 않는 구역입니다. |
| POST /api/sensor-data | 필수 센서 데이터가 누락된 경우 | 400 | 필수 센서 데이터가 누락되었습니다. |
| POST /api/sensor-data | 센서 데이터 저장 중 오류가 발생한 경우 | 500 | 센서 데이터 등록 중 오류가 발생했습니다. |

---

## 15. MVP 구현 우선순위

초기 MVP에서는 아래 API를 우선 구현한다.

| 우선순위 | API | 설명 |
|---|---|---|
| 1 | GET /api/zones | 전체 구역 환경 데이터 조회 |
| 2 | GET /api/zones/{zoneId} | 특정 구역 상세 조회 |

초기 구현에서는 앱이 중간 서버의 `/api/zones` API를 호출하여 구역별 환경 상태와 솔루션을 표시하는 것을 목표로 한다.