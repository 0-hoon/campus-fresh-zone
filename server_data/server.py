from flask import Flask, jsonify
import json
import os

app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False

@app.route('/api/sensors', methods=['GET'])
def get_sensors():
    # 1. 파일이 존재하는지 확인
    if not os.path.exists('latest_data.json'):
        return jsonify({
            "status": "ERROR",
            "message": "아직 수집된 데이터가 없습니다. 수집기를 먼저 실행해주세요."
        }), 404

    # 2. collector.py가 만들어둔 파일을 읽어서 바로 앱으로 반환
    try:
        with open('latest_data.json', 'r', encoding='utf-8') as f:
            data = json.load(f)
        return jsonify(data)
        
    except Exception as e:
        return jsonify({
            "status": "ERROR",
            "message": f"데이터 읽기 실패: {e}"
        }), 500

if __name__ == '__main__':
    print("[웹 서버] 앱 통신용 API 서버가 시작되었습니다. (포트: 8080)")
    app.run(host='0.0.0.0', port=8080)