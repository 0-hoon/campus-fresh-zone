import requests
import time
import json

# --- 위험도 판단 엔진 (기존과 동일) ---
def evaluate_environment(temp, aqi, co2, fresh):
    status_levels, status_messages, solution_messages = [1], [], []
    
    if fresh is False:
        return 2, "데이터 지연", "현재 센서 데이터가 최신이 아닙니다. 참고용으로만 확인하세요."

    if temp is not None:
        if temp >= 33:
            status_levels.append(3); status_messages.append("폭염 경고"); solution_messages.append("야외활동 강력 자제")
        elif temp >= 27:
            status_levels.append(2); status_messages.append("더위 주의"); solution_messages.append("충분한 수분 섭취")

    if aqi is not None:
        if aqi >= 101:
            status_levels.append(3); status_messages.append("미세먼지 나쁨"); solution_messages.append("보건용 마스크 착용 필수")
        elif aqi >= 51:
            status_levels.append(2); status_messages.append("미세먼지 보통"); solution_messages.append("호흡기 민감군 주의")

    if co2 is not None:
        if co2 >= 1500:
            status_levels.append(3); status_messages.append("환기 불량"); solution_messages.append("밀폐 구역 이용 자제")
        elif co2 >= 1000:
            status_levels.append(2); status_messages.append("이산화탄소 높음"); solution_messages.append("실내 주기적 환기")

    final_level = max(status_levels)
    if final_level == 1:
        return 1, "NORMAL", "모든 환경 수치가 쾌적합니다. (Fresh-Zone)"
    else:
        return final_level, " 및 ".join(status_messages), f"현재 {' 및 '.join(status_messages)} 상태입니다. {', '.join(solution_messages)}를 권장합니다."


# 신규 추가: 1등 구역에 라벨링하는 함수 ---
def mark_best_zone(sensor_list):
    IDEAL_TEMP = 24.0
    
    # 1. 일단 모든 센서의 isBestZone 라벨을 False로 초기화합니다.
    for sensor in sensor_list:
        sensor["isBestZone"] = False
        
    # 2. 최신 데이터(fresh=True)이면서 위험도가 1(정상)인 곳만 필터링
    safe_zones = [
        s for s in sensor_list 
        if s.get("statusLevel") == 1 and s.get("fresh") is True
    ]
    
    if not safe_zones:
        return # 쾌적한 곳이 하나도 없으면 아무것도 안 하고 종료
        
    # 3. 1순위: CO2 낮은 순 / 2순위: 24도에 가까운 순으로 1등 찾기
    def calculate_score(sensor):
        co2_val = sensor.get("co2") if sensor.get("co2") is not None else 9999
        temp_val = sensor.get("temp") if sensor.get("temp") is not None else 9999
        return (co2_val, abs(temp_val - IDEAL_TEMP))
        
    safe_zones.sort(key=calculate_score)
    
    # 4. 1등 구역(index 0)의 센서 이름을 가져와서 원본 리스트에서 라벨을 True로 바꿔줍니다.
    best_sensor_name = safe_zones[0].get("sensor")
    
    for sensor in sensor_list:
        if sensor.get("sensor") == best_sensor_name:
            sensor["isBestZone"] = True
            # 앱 화면에서 더 돋보이게 solution 문구 앞부분을 살짝 꾸며줍니다.
            sensor["solution"] = "[추천] " + sensor["solution"]
            break


# --- 공식 명세서를 반영한 무한 반복 수집기 ---
def run_collector():
    print("[수집기] 공식 명세서 기반 데이터 수집 파이프라인 가동...")
    
    COMMON_API_URL = "http://203.255.81.72:10021/sensor/api/map"
    OPENSRC_API_URL = "http://203.255.81.72:10021/sensor/api/opensrc/"
    
    while True:
        try:
            print(f"\n[{time.strftime('%H:%M:%S')}] 외부 API 데이터 수집 중...")
            unified_data_list = []
            
            # 1. 공용 센서 데이터 수집
            try:
                res_common = requests.get(COMMON_API_URL, timeout=3)
                if res_common.status_code == 200:
                    for s in res_common.json():
                        t_val = round(float(s.get("temperature")), 2) if s.get("temperature") is not None else None
                        co2_val = round(s.get("co2"), 2) if s.get("co2") is not None else None
                        fresh_val = s.get("fresh", True)
                        
                        lvl, risk, sol = evaluate_environment(t_val, None, co2_val, fresh_val)
                        
                        unified_data_list.append({
                            "sensor": s.get("sensor"),
                            "latitude": s.get("latitude"),
                            "longitude": s.get("longitude"),
                            "temp": t_val,
                            "humidity": None,
                            "aqi": None,
                            "co2": co2_val,
                            "fresh": fresh_val,
                            "statusLevel": lvl,
                            "mainRisk": risk,
                            "solution": sol
                        })
            except Exception as e:
                pass

            # 2. 오픈소스 센서 데이터 수집
            try:
                res_opensrc = requests.get(OPENSRC_API_URL, timeout=3)
                if res_opensrc.status_code == 200:
                    for s in res_opensrc.json():
                        t_val = round(s.get("temp"), 2) if s.get("temp") is not None else None
                        h_val = round(s.get("humidity"), 2) if s.get("humidity") is not None else None
                        aqi_val = round(s.get("aqi"), 2) if s.get("aqi") is not None else None
                        co2_val = round(s.get("eco2"), 2) if s.get("eco2") is not None else None
                        
                        lvl, risk, sol = evaluate_environment(t_val, aqi_val, co2_val, True)
                        
                        unified_data_list.append({
                            "sensor": s.get("sensor"),
                            "latitude": s.get("lat"),
                            "longitude": s.get("lon"),
                            "temp": t_val,
                            "humidity": h_val,
                            "aqi": aqi_val,
                            "co2": co2_val,
                            "fresh": True,
                            "statusLevel": lvl,
                            "mainRisk": risk,
                            "solution": sol
                        })
            except Exception as e:
                pass

            # 3. ⭐️ 데이터 저장을 하기 직전에, 1등 구역을 찾아서 True 라벨을 찍어줍니다!
            if unified_data_list:
                mark_best_zone(unified_data_list)

            # 4. 기존 포맷을 완벽하게 유지하며 파일로 저장
            if unified_data_list:
                output_data = {
                    "status": "SUCCESS", 
                    "data": unified_data_list  # 외부 프레임 변화 없음!
                }
                
                with open('latest_data.json', 'w', encoding='utf-8') as f:
                    json.dump(output_data, f, ensure_ascii=False, indent=4)
                print(f"-> 총 {len(unified_data_list)}개의 센서 데이터 갱신 완료!")
            
        except Exception as e:
            print(f"-> 전체 루프 에러 발생: {e}")
            
        time.sleep(60)

if __name__ == '__main__':
    run_collector()
