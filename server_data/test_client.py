import requests
import time
import json

# ==========================================
# [설정] 테스트할 서버 주소 (모두 로컬 기준)
# ==========================================
MAIN_API_URL = "http://(ip):8080/api/sensors"
LOCATION_API_URL = "http://(ip):8081/api/location"


# ==========================================
# [테스트 1] 전체 캠퍼스 환경 데이터 조회 (GET)
# ==========================================
def test_main_api():
    print("=" * 50)
    print("🚀 [TEST 1] 메인 환경 서버(8080) 전체 데이터 호출")
    print("=" * 50)
    try:
        response = requests.get(MAIN_API_URL, timeout=3)
        if response.status_code == 200:
            data = response.json()
            sensors = data.get('data', [])
            print(f"✅ 성공: 총 {len(sensors)}개의 센서 데이터를 수신했습니다.")
            
            # 첫 번째 데이터 살짝 엿보기
            if sensors:
                first_sensor = sensors[0]
                print(f"  📍 샘플 구역: {first_sensor.get('sensor')}")
                print(f"  🌡️ 기온: {first_sensor.get('temp')} / 😷 미세먼지: {first_sensor.get('aqi')}")
                print(f"  🚨 위험 레벨: {first_sensor.get('statusLevel')} ({first_sensor.get('mainRisk')})")
        else:
            print(f"❌ 실패 (상태 코드 {response.status_code}): {response.text}")
    except Exception as e:
        print(f"❌ 서버 통신 에러: {e}")
    print("\n")


# ==========================================
# [테스트 2] 시나리오별 위치 기반 맞춤형 데이터 호출 (POST)
# ==========================================
def test_location_api():
    print("=" * 50)
    print("🚀 [TEST 2] 위치(8081) 서버 하이브리드 측위 연산 테스트")
    print("=" * 50)

    # 시나리오 A: 실내 환경 (BLE 신호가 압도적으로 강할 때)
    # 기댓값: GPS를 무시하고 즉시 "opensrc_team2" 구역 데이터 반환
    scenario_indoor = {
        "user_id": "tester_indoor",
        "gps": {"lat": 36.626910, "lon": 127.457725},  # 무의미한 GPS
        "ble_signals": [
            {"mac": "D8:3A:DD:79:8E:BF", "rssi": -55}, # 매우 강력한 신호 (우선순위)
            {"mac": "00:11:22:33:44:55", "rssi": -90}
        ]
    }

    # 시나리오 B: 실외 환경 (BLE 신호가 약하거나 없고, GPS만 존재할 때)
    # 기댓값: 하버사인 공식이 가동되어 근처 공용 센서 중 하나를 매핑해 반환
    scenario_outdoor = {
        "user_id": "tester_outdoor",
        "gps": {"lat": 36.6280, "lon": 127.4560}, # 솔못이나 도서관 근처 GPS 가정
        "ble_signals": [
            {"mac": "D8:3A:DD:79:8E:BF", "rssi": -85} # 거리가 너무 먼 BLE 신호 (무시됨)
        ]
    }

    scenarios = [
        ("시나리오 A: 실내 (BLE 우선 탐색 테스트)", scenario_indoor),
        ("시나리오 B: 실외 (GPS 하버사인 연산 테스트)", scenario_outdoor)
    ]

    for title, payload in scenarios:
        print(f"\n▶️ {title}")
        try:
            res = requests.post(LOCATION_API_URL, json=payload, timeout=3)
            if res.status_code == 200:
                result = res.json()
                print(f"  📡 연산된 현재 구역: '{result['current_zone']}'")
                print(f"  💬 매핑된 행동 지침: {result['environment']['solution']}")
            else:
                 print(f"  ❌ 에러 반환 ({res.status_code}): {res.json()}")
        except Exception as e:
            print(f"  ❌ 서버 통신 에러: {e}")
        
        time.sleep(1) # 시나리오 간 짧은 대기


if __name__ == "__main__":
    test_main_api()
    test_location_api()