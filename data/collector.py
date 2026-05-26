import requests
import time
import json

# --- 위험도 판단 엔진 (기존과 동일하므로 생략 없이 사용) ---
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


# --- 공식 명세서를 반영한 무한 반복 수집기 ---
def run_collector():
    print("[수집기] 공식 명세서 기반 데이터 수집 파이프라인 가동...")
    
    # 명세서에 명시된 정확한 URL 적용 (IP 차이 주의)
    COMMON_API_URL = "http://203.255.81.72:10021/sensor/api/map"
    OPENSRC_API_URL = "http://203.255.81.72:10021/sensor/api/opensrc/"
    
    while True:
        try:
            print(f"\n[{time.strftime('%H:%M:%S')}] 외부 API 데이터 수집 중...")
            unified_data_list = []
            
            # 1. 공용 센서 데이터 수집 (API 1번)
            try:
                res_common = requests.get(COMMON_API_URL, timeout=3)
                if res_common.status_code == 200:
                    for s in res_common.json():
                        # 명세서: temperature(int), co2(int)
                        t_val = float(s.get("temperature")) if s.get("temperature") is not None else None
                        co2_val = s.get("co2")
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
                print(f"⚠️ 공용 센서 통신 에러: {e}")

            # 2. 오픈소스 센서 데이터 수집 (API 4번)
            try:
                res_opensrc = requests.get(OPENSRC_API_URL, timeout=3)
                if res_opensrc.status_code == 200:
                    for s in res_opensrc.json():
                        # 명세서: temp(double), eco2(int), aqi(int)
                        t_val = s.get("temp")
                        h_val = s.get("humidity")
                        aqi_val = s.get("aqi")
                        co2_val = s.get("eco2")
                        
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
                print(f"⚠️ 오픈소스 센서 통신 에러 (학교망인지 확인 요망): {e}")

            # 3. 수집된 데이터를 JSON 파일로 덮어쓰기
            if unified_data_list:
                output_data = {"status": "SUCCESS", "data": unified_data_list}
                with open('latest_data.json', 'w', encoding='utf-8') as f:
                    json.dump(output_data, f, ensure_ascii=False, indent=4)
                print(f"-> 총 {len(unified_data_list)}개의 센서 데이터 갱신 완료!")
            
        except Exception as e:
            print(f"-> 전체 루프 에러 발생: {e}")
            
        # 60초 대기 후 다시 수집
        time.sleep(60)

if __name__ == '__main__':
    run_collector()