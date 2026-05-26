import math

# ---------------------------------------------------------
# BLE 비콘 MAC 주소 <-> 캠퍼스 구역 매핑 테이블 (데이터베이스 역할)
# ---------------------------------------------------------
MAC_TO_SENSOR = {
    "D8:3A:DD:79:8E:BF": "opensrc_team2",
    "00:11:22:33:44:55": "sensor 01",
    "AA:BB:CC:DD:EE:FF": "sensor 02"
}

def calculate_distance_meters(lat1, lon1, lat2, lon2):
    """하버사인 공식을 이용한 두 좌표 간 실제 거리(m) 연산"""
    if None in (lat1, lon1, lat2, lon2):
        return float('inf')

    R = 6371000 
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    delta_phi = math.radians(lat2 - lat1)
    delta_lambda = math.radians(lon2 - lon1)

    a = math.sin(delta_phi / 2.0) ** 2 + \
        math.cos(phi1) * math.cos(phi2) * \
        math.sin(delta_lambda / 2.0) ** 2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

    return R * c 

def find_best_location(user_gps, ble_signals, all_sensors):
    """GPS와 BLE 데이터를 종합하여 최적의 구역 ID를 반환하는 하이브리드 측위 엔진"""
    print(f"\n[측위 엔진] 하이브리드 연산 시작")
    
    # 1. BLE 신호 우선 검사 (Short-Circuit)
    if ble_signals:
        strongest_ble = max(ble_signals, key=lambda x: x['rssi'])
        if strongest_ble['rssi'] >= -65:
            mac_address = strongest_ble['mac']
            print(f" -> 💡 [BLE 우선] 초근접 비콘 감지! (MAC: {mac_address}, RSSI: {strongest_ble['rssi']})")
            
            if mac_address in MAC_TO_SENSOR:
                return MAC_TO_SENSOR[mac_address]
            else:
                print(f"    ⚠️ 등록되지 않은 비콘입니다. GPS 연산으로 넘어갑니다.")

    # 2. GPS 기반 최단 거리 탐색
    if user_gps and 'lat' in user_gps and 'lon' in user_gps:
        print(f" -> 🌐 [GPS 탐색] 사용자 위치 기준 스캔 중...")
        
        nearest_sensor = None
        min_distance = float('inf')

        for sensor in all_sensors:
            s_lat = sensor.get('latitude')
            s_lon = sensor.get('longitude')
            
            dist = calculate_distance_meters(user_gps['lat'], user_gps['lon'], s_lat, s_lon)
            
            if dist < min_distance:
                min_distance = dist
                nearest_sensor = sensor.get('sensor')

        if nearest_sensor:
            print(f" 🎉 [결과] 가장 가까운 센서: '{nearest_sensor}' (거리: {min_distance:.1f}m)")
            return nearest_sensor

    print(" ⚠️ [경고] 유효한 위치 정보가 부족하여 기본 구역으로 매핑합니다.")
    return "opensrc_team2"