```markdown
#  Campus Fresh Zone - Backend

##  프로젝트 소개
충북대학교 캠퍼스 내 환경(기온, 미세먼지, 이산화탄소 등)을 실시간으로 모니터링하고, 사용자의 현재 위치(GPS + BLE)를 기반으로 맞춤형 행동 가이드를 제공하는 백엔드 시스템입니다. 시스템의 안정성을 위해 수집, 데이터 제공, 위치 연산 모듈이 독립된 마이크로서비스(MSA) 형태로 분리되어 작동합니다.

---

##  시스템 아키텍처 및 모듈 구성

본 백엔드는 총 4개의 독립된 모듈로 구성되어 있습니다.

1. **`collector.py` (수집 및 정제 엔진)**: 외부 API를 1분 주기로 호출하여 데이터를 정제하고 위험도를 연산한 뒤 `latest_data.json`으로 덮어씁니다.
2. **`server.py` (메인 환경 API - Port 8080)**: 정제된 전체 캠퍼스 환경 데이터를 앱으로 빠르게 제공합니다.
3. **`positioning_engine.py` (측위 연산 모듈)**: 하버사인 공식(GPS)과 신호 강도(BLE)를 결합하여 사용자의 구역을 수학적으로 판별합니다.
4. **`location_server.py` (위치 매핑 API - Port 8081)**: 앱으로부터 위치 데이터를 받아 연산 엔진에 넘기고, 해당하는 구역의 환경 데이터를 반환합니다.

---

##  API 명세서 (Data Specification)

### 1. 캠퍼스 전체 환경 데이터 조회
- **Method:** `GET`
- **URL:** `http://<서버IP>:8080/api/sensors`
- **Description:** `collector.py`가 가공한 최신 데이터를 반환합니다.
- **Response Example (`latest_data.json`):**
```json
{
  "status": "SUCCESS",
  "data": [
    {
      "sensor": "opensrc_team2",
      "latitude": 36.6296169,
      "longitude": 127.45700346,
      "temp": 26.93,
      "humidity": 17.71,
      "aqi": 1,
      "co2": 402,
      "fresh": true,
      "statusLevel": 1,
      "mainRisk": "NORMAL",
      "solution": "모든 환경 수치가 쾌적합니다. (Fresh-Zone)"
    }
  ]
}

```

### 2. 하이브리드 사용자 위치 기반 매핑

* **Method:** `POST`
* **URL:** `http://<서버IP>:8081/api/location`
* **Description:** 사용자의 GPS 좌표 및 BLE 신호를 기반으로 가장 적합한 센서 구역의 데이터를 반환합니다.
* **Request (앱 ➔ 백엔드):**

```json
{
  "user_id": "student_01",
  "gps": {
    "lat": 36.626910,
    "lon": 127.457725
  },
  "ble_signals": [
    {"mac": "D8:3A:DD:79:8E:BF", "rssi": -85}
  ]
}

```

* **Response (백엔드 ➔ 앱):**

```json
{
  "status": "SUCCESS",
  "current_zone": "opensrc_team2",
  "distance_info": "측위 완료",
  "environment": {
      "temp": 26.93,
      "statusLevel": 1,
      "mainRisk": "NORMAL",
      "solution": "모든 환경 수치가 쾌적합니다. (Fresh-Zone)"
  }
}

```

---

##  외부 API 연동 정보 (Collector)

* **공용 센서 통신망:** `http://203.255.81.72:10021/sensor/api/map`
* **오픈소스 팀 센서 통신망:** `http://10.255.81.72:10021/sensor/api/opensrc/` (교내망 접속 필수)

---

##  로컬 실행 방법 (How to Run)

모듈화된 아키텍처 특성상, 정상적인 구동을 위해 **터미널을 3개 열어 각각 실행**해야 합니다.

1. **필수 패키지 설치**

```bash
pip install flask requests

```

2. **데이터 수집기 가동 (Terminal 1)**

```bash
python collector.py

```

3. **메인 환경 서버 가동 (Terminal 2)**

```bash
python server.py
# 8080 포트 개방

```

4. **위치 매핑 서버 가동 (Terminal 3)**

```bash
python location_server.py
# 8081 포트 개방

```

```

```

백엔드 전체 구조 (모듈별 역할)

collector.py (데이터 수집 및 정제 엔진)
외부 환경 센서망(공용 API 및 오픈소스 센서 API)을 1분 주기로 호출하는 백그라운드 파이프라인입니다. 결측치 방어, 수치 정제, 위험도 연산, 그리고 최우수 쾌적 구역(isBestZone) 판별까지 수행한 뒤 그 결과를 latest_data.json 파일로 저장하여 서버 데이터를 최신 상태로 유지합니다.

server.py (메인 환경 데이터 API 서버)
collector.py가 갱신해 둔 latest_data.json 파일을 실시간으로 읽어 클라이언트(안드로이드 앱)에 제공하는 8080 포트 서버입니다. 무거운 연산 없이 파일만 읽어 즉시 반환하므로, 대규모 트래픽에도 매우 빠르고 안정적인 읽기 전용(Read-only) 역할을 합니다.

positioning_engine.py (하이브리드 측위 엔진)
서버 통신 기능 없이 순수 수학적 위치 연산만 전담하는 모듈입니다. 앱에서 넘어온 GPS 좌표(하버사인 공식 적용)와 BLE 비콘 신호 강도(RSSI)를 분석하여 사용자가 캠퍼스 내 어느 구역에 있는지 정확하게 판별합니다.

location_server.py (위치 매핑 API 서버)
사용자와 직접 통신하며 맞춤형 정보를 제공하는 8081 포트 라우터입니다. 앱으로부터 위치 데이터(GPS/BLE)를 수신하면 positioning_engine.py에 계산을 의뢰해 현재 구역을 찾아냅니다. 이후 server.py에서 해당 구역에 맞는 1:1 맞춤형 데이터만 뽑아 앱으로 반환합니다.

데이터 흐름

데이터 수집 파이프라인 흐름:
외부 센서 API (원시 데이터) -> collector.py (데이터 정제 및 추천 연산) -> latest_data.json (저장)

전체 환경 데이터 조회 흐름:
latest_data.json -> server.py -> Android 앱 (캠퍼스 전체 상태 및 1등 명당 UI 렌더링)

사용자 맞춤형 위치 기반 안내 흐름:
Android 앱 (GPS+BLE 데이터 전송) -> location_server.py (데이터 수신) -> positioning_engine.py (사용자 현재 구역 판별) -> server.py (데이터 연동 및 필터링) -> Android 앱 (최종 맞춤형 환경 가이드 반환)

API 구조

GET /api/sensors (포트: 8080)
목적: 캠퍼스 전체 환경 센서 데이터 및 명당 추천 결과 일괄 조회
요청: 파라미터 없음
반환: 36개 전체 센서의 배열 데이터 (기온, 습도, 미세먼지, CO2 수치, 위험도 레벨, 행동 지침 문자열, isBestZone 추천 라벨 포함)

POST /api/location (포트: 8081)
목적: 사용자 실시간 위치 기반 최적 센서 구역 매핑 및 맞춤형 환경 정보 안내
요청: 안드로이드 앱의 현재 GPS 좌표(lat, lon) 및 반경 내 수집된 BLE 비콘 신호 배열(mac, rssi)
반환: 엔진을 통해 측위 완료된 현재 구역 이름(current_zone) 및 해당 구역에 매핑된 단일 맞춤형 환경 데이터

아키텍처 흐름도 (도식화)

[ 외부 캠퍼스 센서 API망 ]
↓ (1분 주기 원시 데이터 수집)
[ collector.py ] (결측치 정제, 위험도 판별, 명당 연산 수행)
↓ (JSON 파일 덮어쓰기)
[ latest_data.json ]
↓ (초고속 Read-Only 데이터 연동)
[ server.py ] (GET /api/sensors 담당)
↓ (캠퍼스 전체 데이터 전송)
[ Android 앱 ] (클라이언트)
↓ (POST /api/location : GPS + BLE 배열 전송)
[ location_server.py ] (맞춤형 위치 매핑 API 담당)
↓ (위치 판별 의뢰 및 결과 수신)
[ positioning_engine.py ] (하버사인 공식 + RSSI 하이브리드 연산)

참고: location_server.py는 positioning_engine.py에서 판별된 위치를 바탕으로 server.py의 데이터를 가져와 Android 앱에 최종 결과를 반환하는 구조입니다.
