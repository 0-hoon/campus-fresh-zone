# API Specification

## 1. API 개요

본 문서는 Campus Fresh Zone 서비스에서 Android App과 중간 서버가 통신하기 위한 API 명세서이다.

Android App은 중간 서버 API를 호출하여 캠퍼스 내 센서별 환경 데이터, 위험도 판단 결과, 대응 솔루션 메시지를 조회한다.

본 API 명세서는 서버와 앱의 병렬 개발을 위해 작성한다. 서버 구현이 완료되기 전에도 앱 개발자는 명세된 Response Example을 Mock Data로 활용하여 화면을 구현할 수 있다.

앱과 서버는 본 문서에 정의된 Endpoint, Request, Response Field를 기준으로 개발한다.

중간 서버는 제공된 센서 API의 데이터 구조를 기준으로 앱에서 사용하기 쉬운 공통 응답 형식으로 변환한다. 공용 센서 데이터는 `sensor`, `latitude`, `longitude`, `temperature`, `co2`, `time`, `fresh` 필드를 제공한다. 공용 센서 API는 온도와 CO2만 제공하며, 습도, 공기질, 가스농도는 제공하지 않는다.

오픈소스 센서 데이터는 `id`, `team`, `sensor`, `mac`, `temp`, `humidity`, `aqi`, `tvoc`, `eco2`, `timestamp`, `created_at`, `lat`, `lon`, `rssi`, `sender` 필드를 제공한다. 오픈소스 센서 데이터는 온도, 습도, AQI, TVOC, eCO2 정보를 포함하므로 일반 공용 센서보다 더 상세한 환경 상태 판단에 활용할 수 있다.

따라서 중간 서버는 서로 다른 센서 데이터 형식을 앱에서 일관되게 사용할 수 있도록 필드명을 통일한다. 공용 센서에서 제공되지 않는 `humidity`, `aqi`, `tvoc`, `mac`, `rssi`, `sender` 값은 `null`로 처리할 수 있다.

하지만 해당 내용은 개발 상황이나 편의에 따라 수정 및 변경이 얼마든지 가능하다.

```text
Android App
    ↓
중간 서버 API
    ↓
센서 데이터 조회 / 위험도 판단 / 솔루션 제공
```

### 필드 변환 규칙

중간 서버는 센서 API별로 다른 필드명을 앱에서 사용하기 쉬운 공통 필드명으로 변환한다.

| 원본 필드 | 중간 서버 응답 필드 | 설명 |
|---|---|---|
| sensor | sensor | 센서 이름 |
| latitude | latitude | 위도 |
| longitude | longitude | 경도 |
| temperature | temperature | 공용 센서 온도 |
| co2 | co2 | 공용 센서 이산화탄소 농도 |
| time | time | 공용 센서 데이터 시간 |
| fresh | fresh | 공용 센서 데이터 갱신 여부 |
| temp | temperature | 오픈소스 센서 온도 |
| eco2 | co2 | 오픈소스 센서 이산화탄소 농도 |
| lat | latitude | 오픈소스 센서 위도 |
| lon | longitude | 오픈소스 센서 경도 |
| created_at | time | 오픈소스 센서 DB 저장 시간 |

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
| GET | 서버에서 데이터를 조회할 때 사용 | 센서별 환경 상태 조회 |
| POST | 서버에 데이터를 새로 등록하거나 전송할 때 사용 | 테스트 데이터 등록, 자체 데이터 저장 |

앱이 화면에 표시할 데이터를 받아오는 기능은 GET을 사용한다.  
센서 데이터나 사용자 입력값을 서버에 저장해야 하는 경우에는 POST를 사용한다.

---

## 4. Base URL

```text
http://서버주소/api
```

개발 단계에서는 로컬 서버 또는 테스트 서버 주소를 사용한다.

예시:

```text
http://localhost:8080/api
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
| 전체 센서 상태 조회 | GET | `/api/sensors` | 없음 | 센서별 환경 데이터 배열 | 메인 화면 또는 지도 화면에서 사용 |
| 특정 센서 상세 조회 | GET | `/api/sensors/{sensor}` | Path Parameter: `sensor` | 특정 센서 상세 데이터 | 센서 카드 또는 지도 마커 클릭 시 사용 |
| 오픈소스 센서 상태 조회 | GET | `/api/sensors/opensrc` | 없음 | 오픈소스 센서 데이터 배열 | 습도, AQI, TVOC 포함 데이터 조회 |
| 센서 데이터 등록 | POST | `/api/sensor-data` | 센서 데이터 JSON | 등록 결과 | 선택 구현, 테스트 데이터 등록 또는 자체 저장 필요 시 사용 |

초기 MVP에서는 `GET /api/sensors`와 `GET /api/sensors/opensrc`를 우선 구현한다.  
`GET /api/sensors/{sensor}`는 특정 센서 상세 화면이 필요한 경우 구현한다.  
`POST /api/sensor-data`는 테스트 데이터 등록 또는 자체 데이터 저장이 필요한 경우 추가로 구현한다.

---

## 7. 전체 센서 상태 조회

```http
GET /api/sensors
```

### 기능 설명

캠퍼스 내 전체 센서의 최신 환경 데이터와 서버에서 판단한 상태, 솔루션 메시지를 조회한다.

앱의 메인 화면, 목록 화면, 지도 화면에서 사용한다.

중간 서버는 공용 센서 데이터 형식을 기준으로 앱에서 사용할 수 있는 응답 데이터를 생성한다. 공용 센서 데이터는 온도와 CO2 중심으로 제공되며, 습도, AQI, TVOC 값은 제공되지 않을 수 있다.

### Request

요청 데이터는 없다.

### Response

센서별 환경 데이터 배열을 반환한다.

### Response Example

```json
{
  "status": "OK",
  "message": "전체 센서 데이터 조회 성공",
  "data": [
    {
      "sensor": "sensor 01",
      "latitude": 36.626906,
      "longitude": 127.457722,
      "temperature": 28,
      "humidity": null,
      "aqi": null,
      "tvoc": null,
      "co2": 400,
      "time": "Thu Apr 16 10:48:48 2026",
      "fresh": false,
      "statusText": "정상",
      "statusLevel": 1,
      "mainRisk": "DATA_STALE",
      "solution": "최신 데이터가 아닐 수 있으므로 참고용으로 확인하는 것이 좋습니다."
    },
    {
      "sensor": "sensor 02",
      "latitude": 36.627100,
      "longitude": 127.457900,
      "temperature": 33,
      "humidity": null,
      "aqi": null,
      "tvoc": null,
      "co2": 1200,
      "time": "Thu Apr 16 11:20:48 2026",
      "fresh": true,
      "statusText": "주의",
      "statusLevel": 2,
      "mainRisk": "TEMP",
      "solution": "온도가 높으므로 장시간 야외활동 시 주의가 필요합니다."
    }
  ]
}
```

### Response Field

| 필드 | 타입 | 설명 |
|---|---|---|
| sensor | String | 센서 이름 |
| latitude | Double | 센서 설치 위도 |
| longitude | Double | 센서 설치 경도 |
| temperature | Double | 센서 측정 기온 |
| humidity | Double / null | 습도, 공용 센서에서는 제공되지 않음 |
| aqi | Int / null | 공기질 지수, 공용 센서에서는 제공되지 않음 |
| tvoc | Int / null | 총휘발성유기화합물, 공용 센서에서는 제공되지 않음 |
| co2 | Int | 센서 측정 이산화탄소 농도 |
| time | String | 데이터 저장 또는 측정 시간 |
| fresh | Boolean | 데이터 갱신 여부 |
| statusText | String | 환경 상태 텍스트 |
| statusLevel | Int | 상태 단계 |
| mainRisk | String | 주요 위험 요인 |
| solution | String | 대응 솔루션 메시지 |

---

## 8. 특정 센서 상세 조회

```http
GET /api/sensors/{sensor}
```

### 기능 설명

특정 센서의 상세 환경 데이터와 판단 결과를 조회한다.

앱에서 특정 센서 카드 또는 지도 마커를 선택했을 때 상세 화면에 표시하기 위해 사용한다.

### Request

Path Parameter를 사용한다.

| 이름 | 타입 | 필수 여부 | 설명 |
|---|---|---|---|
| sensor | String | 필수 | 조회할 센서 이름 |

### Request Example

```http
GET /api/sensors/sensor%2001
```

센서 이름에 공백이 포함될 수 있으므로 URL Encoding을 적용한다.

### Response

특정 센서의 상세 환경 데이터와 솔루션 정보를 반환한다.

### Response Example

```json
{
  "status": "OK",
  "message": "센서 상세 데이터 조회 성공",
  "data": {
    "sensor": "sensor 01",
    "latitude": 36.626906,
    "longitude": 127.457722,
    "temperature": 28,
    "humidity": null,
    "aqi": null,
    "tvoc": null,
    "co2": 400,
    "time": "Thu Apr 16 10:48:48 2026",
    "fresh": false,
    "statusText": "정상",
    "statusLevel": 1,
    "mainRisk": "DATA_STALE",
    "solution": "최신 데이터가 아닐 수 있으므로 참고용으로 확인하는 것이 좋습니다.",
    "description": "해당 센서 데이터는 1시간 이내에 갱신되지 않았을 수 있으므로 현재 상태 판단 시 주의가 필요합니다."
  }
}
```

### Response Field

| 필드 | 타입 | 설명 |
|---|---|---|
| sensor | String | 센서 이름 |
| latitude | Double | 센서 설치 위도 |
| longitude | Double | 센서 설치 경도 |
| temperature | Double | 센서 측정 기온 |
| humidity | Double / null | 습도, 공용 센서에서는 제공되지 않음 |
| aqi | Int / null | 공기질 지수, 공용 센서에서는 제공되지 않음 |
| tvoc | Int / null | 총휘발성유기화합물, 공용 센서에서는 제공되지 않음 |
| co2 | Int | 센서 측정 이산화탄소 농도 |
| time | String | 데이터 저장 또는 측정 시간 |
| fresh | Boolean | 데이터 갱신 여부 |
| statusText | String | 환경 상태 텍스트 |
| statusLevel | Int | 상태 단계 |
| mainRisk | String | 주요 위험 요인 |
| solution | String | 대응 솔루션 메시지 |
| description | String | 상세 설명 |

---

## 9. 오픈소스 센서 상태 조회

```http
GET /api/sensors/opensrc
```

### 기능 설명

오픈소스 센서의 최신 환경 데이터와 서버에서 판단한 상태, 솔루션 메시지를 조회한다.

오픈소스 센서 데이터는 온도, 습도, AQI, TVOC, eCO2 정보를 포함할 수 있으므로 일반 공용 센서보다 더 상세한 환경 상태 판단에 활용한다.

오픈소스 센서 데이터의 `fresh` 값은 원본 API에서 직접 제공되는 값이 아니라, `created_at` 또는 `timestamp`를 기준으로 중간 서버에서 계산한 값이다.

### Request

요청 데이터는 없다.

### Response

오픈소스 센서별 환경 데이터 배열을 반환한다.

### Response Example

```json
{
  "status": "OK",
  "message": "오픈소스 센서 데이터 조회 성공",
  "data": [
    {
      "id": 7974,
      "team": "team 3",
      "sensor": "opensrc_team1",
      "mac": "D8:3A:DD:79:8E:BF",
      "latitude": 36.6289417,
      "longitude": 127.4570621,
      "temperature": 16.58,
      "humidity": 44.48,
      "aqi": 0,
      "tvoc": 791,
      "co2": 949,
      "timestamp": 1777366982,
      "time": "2026-04-28T09:03:02.692Z",
      "rssi": -88,
      "sender": "0242a83949ec18ef",
      "fresh": true,
      "statusText": "주의",
      "statusLevel": 2,
      "mainRisk": "TVOC",
      "solution": "TVOC 수치가 높아 장시간 체류 시 주의가 필요합니다."
    }
  ]
}
```

### Response Field

| 필드 | 타입 | 설명 |
|---|---|---|
| id | Long | 데이터 식별자 |
| team | String | 데이터 전송 팀 |
| sensor | String | 센서 이름 |
| mac | String | 센서 MAC 주소 |
| latitude | Double | 센서 설치 위도 |
| longitude | Double | 센서 설치 경도 |
| temperature | Double | 센서 측정 기온 |
| humidity | Double | 센서 측정 습도 |
| aqi | Int | 센서 측정 공기질 |
| tvoc | Int | 가스 농도 |
| co2 | Int | 센서 측정 이산화탄소 농도 |
| timestamp | Long | 데이터 수집 시간 |
| time | String | DB 저장 시간 |
| rssi | Int | 수신 신호 세기 |
| sender | String | 데이터 전송 기기 |
| fresh | Boolean | 데이터 갱신 여부 |
| statusText | String | 환경 상태 텍스트 |
| statusLevel | Int | 상태 단계 |
| mainRisk | String | 주요 위험 요인 |
| solution | String | 대응 솔루션 메시지 |

---

## 10. 센서 데이터 등록

```http
POST /api/sensor-data
```

### 기능 설명

서버 테스트 또는 자체 테스트 데이터 저장이 필요한 경우 사용하는 선택 구현 API이다.

초기 MVP에서는 필수 구현 대상이 아니며, 필요 시 추가 구현한다.

본 API는 앱과 중간 서버 사이에서 테스트 데이터 등록이나 자체 데이터 저장 확장이 필요한 경우를 고려하여 설계한 API이다.

### Request

센서 데이터 JSON을 Request Body로 전송한다.

### Request Example

```json
{
  "sensor": "opensrc_team1",
  "mac": "D8:3A:DD:79:8E:BF",
  "latitude": 36.6289417,
  "longitude": 127.4570621,
  "temperature": 16.58,
  "humidity": 44.48,
  "aqi": 0,
  "tvoc": 791,
  "co2": 949,
  "timestamp": 1777366982,
  "time": "2026-04-28T09:03:02.692Z",
  "rssi": -88,
  "sender": "0242a83949ec18ef"
}
```

### Request Field

| 필드 | 타입 | 필수 여부 | 설명 |
|---|---|---|---|
| sensor | String | 필수 | 센서 이름 |
| mac | String | 선택 | 센서 MAC 주소 |
| latitude | Double | 선택 | 센서 설치 위도 |
| longitude | Double | 선택 | 센서 설치 경도 |
| temperature | Double | 필수 | 온도 |
| humidity | Double / null | 선택 | 습도 |
| aqi | Int / null | 선택 | 공기질 지수 |
| tvoc | Int / null | 선택 | 총휘발성유기화합물 |
| co2 | Int | 필수 | 이산화탄소 농도 |
| timestamp | Long | 선택 | 데이터 수집 시간 |
| time | String | 선택 | 데이터 저장 시간 |
| rssi | Int | 선택 | 수신 신호 세기 |
| sender | String | 선택 | 데이터 전송 기기 |

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

## 11. 상태값 정의

| statusLevel | statusText | 설명 |
|---|---|---|
| 1 | 정상 | 야외활동 가능 |
| 2 | 주의 | 장시간 야외활동 주의 |
| 3 | 위험 | 야외활동 자제 권장 |

---

## 12. 주요 위험 요인 정의

| mainRisk | 설명 |
|---|---|
| NONE | 위험 요인 없음 |
| AQI | 공기질 악화 |
| CO2 | 이산화탄소 증가 |
| TVOC | 휘발성 유기화합물 증가 |
| TEMP | 고온 |
| HUMIDITY | 습도 문제 |
| COMPLEX | 복합 위험 |
| DATA_STALE | 센서 데이터가 최신 상태가 아님 |

---

## 13. 상태 판단 기준

서버는 환경 데이터를 아래 기준에 따라 정상 / 주의 / 위험 단계로 분류한다.

| 항목 | 정상 | 주의 | 위험 |
|---|---|---|---|
| AQI / CAI | 0~50 | 51~100 | 101 이상 |
| CO2 | 1000ppm 이하 | 1001~1500ppm | 1501ppm 이상 |
| TVOC | 400㎍/㎥ 이하 | 401~500㎍/㎥ | 501㎍/㎥ 이상 |
| 온도 | 24~26℃ | 27~32℃ | 33℃ 이상 |
| 습도 | 적정 범위 | 고습 / 저습 | 고온과 결합 시 위험 가중 |

`humidity`, `aqi`, `tvoc` 값이 `null`인 경우 해당 항목은 상태 판단에서 제외하고, 제공된 `temperature`, `co2`, `fresh` 값을 중심으로 상태를 판단한다.

센서 데이터의 `fresh` 값이 `false`인 경우 해당 데이터는 최신 상태가 아닐 수 있으므로, 앱에서는 참고용 데이터임을 표시할 수 있다. 이 경우 상태 판단 시 `DATA_STALE` 위험 요인으로 분류할 수 있다.

본 서비스는 공식적인 법적 측정·판정 시스템이 아니라, 공공기관 기준을 참고하여 위험 가능성을 사용자에게 안내하는 모니터링 및 의사결정 보조 서비스이다.

---

## 14. 솔루션 매핑 기준

| 조건 | 판단 | 제공 솔루션 |
|---|---|---|
| AQI / CAI 101 이상 | 공기질 나쁨 | 야외활동 자제 및 마스크 착용 권장 |
| CO2 1000ppm 초과 | 환기 부족 가능성 | 장시간 체류 주의 |
| CO2 1500ppm 초과 | 환기 위험 수준 | 해당 구역 이용 자제 권장 |
| TVOC 500㎍/㎥ 초과 | 유기화합물 농도 높음 | 오염원 확인 및 접근 주의 |
| 온도 33℃ 이상 | 온열질환 위험 | 야외활동 자제, 수분 섭취, 휴식 권장 |
| 고온 + 고습 | 체감온도 상승 가능 | 장시간 야외활동 자제 권장 |
| fresh 값이 false | 데이터 갱신 지연 | 최신 데이터가 아닐 수 있으므로 참고용으로 확인 권장 |

---

## 15. 에러 응답

요청 실패 시 아래 형식으로 응답한다.

```json
{
  "status": "NOT_FOUND",
  "message": "존재하지 않는 센서입니다.",
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
| GET /api/sensors | 서버에서 센서 데이터를 불러오지 못한 경우 | 500 | 전체 센서 데이터 조회 중 오류가 발생했습니다. |
| GET /api/sensors | 일부 센서 데이터가 최신 상태가 아닌 경우 | 200 | 일부 센서 데이터가 최신 상태가 아닙니다. |
| GET /api/sensors/{sensor} | sensor 값이 비어 있는 경우 | 400 | 센서 이름이 입력되지 않았습니다. |
| GET /api/sensors/{sensor} | 해당 sensor의 데이터가 존재하지 않는 경우 | 404 | 존재하지 않는 센서입니다. |
| GET /api/sensors/opensrc | 오픈소스 센서 데이터를 불러오지 못한 경우 | 500 | 오픈소스 센서 데이터 조회 중 오류가 발생했습니다. |
| POST /api/sensor-data | 필수 센서 데이터가 누락된 경우 | 400 | 필수 센서 데이터가 누락되었습니다. |
| POST /api/sensor-data | 센서 데이터 저장 중 오류가 발생한 경우 | 500 | 센서 데이터 등록 중 오류가 발생했습니다. |

---