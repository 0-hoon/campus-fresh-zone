from flask import Flask, request, jsonify
import requests

# ⭐️ 방금 분리한 계산 엔진 파일(positioning_engine.py)에서 함수를 수입(import)해 옵니다.
from positioning_engine import find_best_location

app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False

MAIN_SERVER_URL = "http://(ip or url):8080/api/sensors"

@app.route('/api/location', methods=['POST'])
def process_user_location():
    # 1. 클라이언트(앱)로부터 데이터 수신 및 파싱
    incoming_data = request.json
    user_gps = incoming_data.get('gps', {})
    ble_signals = incoming_data.get('ble_signals', [])

    if not user_gps and not ble_signals:
         return jsonify({"status": "ERROR", "message": "위치 데이터가 누락되었습니다."}), 400

    try:
        # 2. 메인 서버(8080)에서 전체 환경 데이터 가져오기
        try:
            response = requests.get(MAIN_SERVER_URL, timeout=3)
            response.raise_for_status() 
        except requests.exceptions.RequestException as req_err:
             print(f"[에러] 메인 환경 서버(8080)와 통신 실패: {req_err}")
             return jsonify({"status": "ERROR", "message": "메인 환경 서버 통신 에러"}), 500
             
        env_data = response.json()
        all_sensors = env_data.get("data", [])

        # 3. ⭐️ 수입해온 엔진을 가동하여 현재 구역 계산
        current_zone = find_best_location(user_gps, ble_signals, all_sensors)

        # 4. 결과 매핑 및 응답 반환
        matched_sensor = next((s for s in all_sensors if s.get("sensor") == current_zone), None)
        
        if matched_sensor:
            return jsonify({
                "status": "SUCCESS",
                "current_zone": current_zone,
                "distance_info": "측위 완료",
                "environment": matched_sensor
            })
        else:
            return jsonify({"status": "ERROR", "message": "해당 구역의 데이터를 찾을 수 없습니다."}), 404

    except Exception as e:
        print(f"[치명적 에러] 백엔드 연산 실패: {e}")
        return jsonify({"status": "ERROR", "message": f"서버 내부 에러: {e}"}), 500

if __name__ == '__main__':
    print("[위치 전용 서버] 하이브리드(GPS+BLE) 통신 API 가동 중 (포트: 8081)")
    app.run(host='0.0.0.0', port=8081)