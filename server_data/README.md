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