# 센서 종료·서비스/BLE 정리·QR 재스캔 변경 기록

- 작성일: 2026-06-28
- 목적: 기존 코드 대비 변경점을 추적하고 향후 디버깅과 롤백에 사용한다.
- 검증 명령: sh gradlew :app:compileDebugKotlin
- 검증 결과: BUILD SUCCESSFUL

## 1. 변경 목적

기존 센서 종료 흐름에는 다음 문제가 있었다.

1. 센서 종료를 로그아웃처럼 처리했다.
   - 액세스 토큰, 리프레시 토큰, 사용자 ID와 이메일을 삭제했다.
   - 앱 프로세스를 강제로 종료했다.
   - 로그인 상태를 유지한 채 새 센서를 등록할 수 없었다.
2. 설정 화면의 STOP_SERVICE 호출이 주석 처리돼 있었다.
   - 앱 프로세스만 죽고 START_STICKY 서비스가 다시 생성될 가능성이 있었다.
3. 서비스 onDestroy()가 BluetoothGatt.disconnect()와 close()를 호출하지 않았다.
4. BleManager가 연결 해제 후 3초 뒤 재연결을 무조건 시도했다.
5. 센서 종료 후 QR 재등록 시 이전 isFindDevice == true가 남았다.
   - 새 스캔이 바로 중지될 수 있었다.
   - 20초 후에도 이미 찾은 것으로 판단해 ScanFailScreen으로 가지 않았다.

현재 목표 흐름:

~~~text
센서 종료 API 성공
→ 종료 상태 저장
→ BLE 재연결 금지
→ GATT disconnect + close
→ 서비스 자원 정리
→ stopSelf + START_NOT_STICKY
→ 로그인 유지
→ 홈 종료 카드 표시
→ 새 센서 등록 시 종료 상태 해제
→ 새 BLE 스캔은 isFindDevice=false부터 시작
~~~

## 2. 정상 상태별 전체 흐름

### 2.1 센서 사용 중

~~~text
is_sensor_ended = false
→ AlwaysA2RTService 실행
→ BLE 연결
→ 일시적인 연결 해제 시 3초 뒤 자동 재연결
→ 홈에서 기존 혈당 카드와 그래프 표시
~~~

### 2.2 설정에서 센서 종료

~~~text
SettingListScreen 센서 종료 확인
→ /api/device/end 호출
→ API success 확인
→ 현재 센서 Room 데이터 삭제
→ is_sensor_ended = true
→ MAC, user_device_id, serial, 센서 시간 삭제
→ STOP_SERVICE 이벤트
→ ACTION_STOP_SERVICE
→ AlwaysA2RTService.releaseResources()
→ BleManager.shutdown()
→ stopSelf()
→ MainScreen/0 이동
→ 홈 종료 카드 표시
~~~

### 2.3 종료 후 새 센서

~~~text
홈의 새로운 센서로 시작하기
→ RegisterDeviceQRScreen
→ QR serial로 서버에서 MAC 조회
→ ScanDeviceScreen
→ isFindDevice=false 초기화
→ BLE scan
→ 발견 시 scan 종료
→ StabilizationScreen
→ 서비스 시작 전에 is_sensor_ended=false
→ 새 BleManager와 새 GATT로 연결
~~~

## 3. 파일별 변경

### 3.1 DataStoreManager.kt

경로:

~~~text
app/src/main/java/kr/co/uxn/agms_p_a2rt/api/token/DataStoreManager.kt
~~~

추가 키:

~~~kotlin
private val IS_SENSOR_ENDED =
    booleanPreferencesKey("is_sensor_ended")
~~~

추가 조회:

~~~kotlin
fun getIsSensorEnded(): Flow<Boolean> {
    return dataStore.data.map { prefs ->
        prefs[IS_SENSOR_ENDED] ?: false
    }
}
~~~

추가 저장:

~~~kotlin
suspend fun saveIsSensorEnded(isSensorEnded: Boolean)
~~~

상태 의미:

| 값 | 의미 |
|---|---|
| false | 센서 사용 중 또는 새 센서 시작 가능 |
| true | 사용자가 센서를 명시적으로 종료함 |

키가 없는 기존 설치에서는 false가 기본값이다. 상태는 DataStore에 남으므로 앱 재실행과 휴대폰 재부팅 후에도 유지된다.

### 3.2 SettingListScreen.kt

경로:

~~~text
app/src/main/java/kr/co/uxn/agms_p_a2rt/ui/components/main/setting/SettingListScreen.kt
~~~

SettingListScreen과 내부 SensorInfoScreen에 다음 콜백을 추가했다.

~~~kotlin
onSensorEnded: () -> Unit
~~~

설정 화면은 자체 NavHostController를 사용하므로 앱 루트 홈으로 돌아가기 위한 콜백이다.

#### 기존 종료 코드 보존

기존 코드는 삭제하지 않고 아래 주석 안에 보존했다.

~~~kotlin
/*
 * 기존 센서 종료 로직. 필요 시 아래 블록을 복원할 수 있도록 보존한다.
 ...
 */
~~~

기존 로직이 수행하던 작업:

- tokenRetrofit.logout()
- UserValue, UserGlucose, UserCalibration 삭제
- isMain=false
- route=Splash
- 액세스 토큰 삭제
- 리프레시 토큰 삭제
- 사용자 ID 삭제
- MAC 삭제
- 센서 시작/종료 시간 삭제
- 목표 혈당 삭제
- 이메일 삭제
- 앱 프로세스 강제 종료

#### 현재 활성화된 종료 코드

삭제하는 항목:

- 현재 사용자의 UserValue
- 현재 사용자의 UserGlucose
- 현재 사용자의 UserCalibration
- deviceMac
- userDeviceId
- serialNumber
- startTime
- measurementTime
- endTime
- dailyCalibrationTime
- dailyCalibrationLastTime
- 고혈당/저혈당 알림 활성 상태
- landscapeMode

유지하는 항목:

- 액세스 토큰
- 리프레시 토큰
- 사용자 ID
- 이메일
- isMain 로그인 상태
- 사용자 프로필 캐시
- 목표 혈당값

핵심 종료 코드:

~~~kotlin
DataStoreManager.saveIsSensorEnded(true)

withContext(Dispatchers.Main) {
    bleViewModel.emit("STOP_SERVICE")
    onSensorEnded()
}
~~~

주의:

- 센서 종료 API가 성공한 경우에만 로컬 종료가 실행된다.
- API 실패 시 서비스와 BLE는 계속 유지된다.
- 현재 구현은 센서 종료 시 현재 센서의 Room 측정 데이터를 삭제한다.

### 3.3 MainScreen.kt

경로:

~~~text
app/src/main/java/kr/co/uxn/agms_p_a2rt/ui/components/main/MainScreen.kt
~~~

SettingListScreen에 루트 화면 이동 콜백을 전달한다.

~~~kotlin
onSensorEnded = {
    navController.navigate("MainScreen/0") {
        popUpTo("MainScreen/{startIndex}") {
            inclusive = true
        }
    }
}
~~~

센서 종료 후 앱이나 계정을 끝내지 않고 홈 탭으로 이동한다.

### 3.4 HomeScreen.kt

경로:

~~~text
app/src/main/java/kr/co/uxn/agms_p_a2rt/ui/components/main/home/HomeScreen.kt
~~~

종료 상태 구독:

~~~kotlin
val isSensorEnded by DataStoreManager
    .getIsSensorEnded()
    .collectAsState(initial = false)
~~~

화면 분기:

~~~kotlin
if (isSensorEnded) {
    // 센서가 종료되었습니다 카드
} else {
    // 기존 혈당 카드, 잔여 시간, 그래프
}
~~~

새 센서 버튼:

~~~kotlin
navController.navigate("RegisterDeviceQRScreen")
~~~

참고:

- 그래프 관련 LaunchedEffect 일부는 화면 분기보다 위에 있다.
- 종료 카드 표시 직후 그래프 계산 로그가 잠시 보일 수 있다.
- 이 로그는 서비스나 BLE가 재시작됐다는 의미가 아니다.

### 3.5 AlwaysA2RTService.kt

경로:

~~~text
app/src/main/java/kr/co/uxn/agms_p_a2rt/ble/AlwaysA2RTService.kt
~~~

#### releaseResources() 추가

서비스 자원 해제를 한 함수로 모았다.

정리 대상:

1. isServiceRunning=false
2. 포그라운드 알림 제거
3. 알림 TimerTask 취소
4. 알림 Timer 취소
5. 반복 서비스 Job 취소
6. 서비스 SupervisorJob 취소
7. BleManager.shutdown() 호출
8. Bluetooth 상태 BroadcastReceiver 해제

onDestroy 변경:

~~~kotlin
override fun onDestroy() {
    releaseResources()
    super.onDestroy()
}
~~~

#### 종료 명령 및 종료 상태 검사

onStartCommand()에서 아래를 검사한다.

~~~kotlin
intent?.action == "ACTION_STOP_SERVICE"
isSensorEnded == true
~~~

하나라도 참이면:

~~~kotlin
releaseResources()
stopForeground(true)
stopSelf()
return START_NOT_STICKY
~~~

효과:

- 명시적으로 종료한 서비스는 START_STICKY로 다시 살아나지 않는다.
- 다른 경로에서 서비스가 호출돼도 isSensorEnded=true면 즉시 종료한다.
- 시스템의 예외적인 서비스 재생성과 재부팅 경로를 이중 방어한다.

#### GATT 참조 저장

기존 일부 경로는 connectGatt() 반환값을 보관하지 않았다. 종료할 GATT를 알 수 있도록 다음 방식으로 변경했다.

~~~kotlin
BleManager.mGatt = bluetoothDevice.connectGatt(...)
~~~

적용 위치:

- 서비스 최초 BLE 연결
- Bluetooth OFF 후 다시 ON 됐을 때의 연결

### 3.6 BleManager.kt

경로:

~~~text
app/src/main/java/kr/co/uxn/agms_p_a2rt/ble/BleManager.kt
~~~

#### 재연결 전용 Job/Scope

~~~kotlin
private val reconnectJob = SupervisorJob()
private val reconnectScope =
    CoroutineScope(Dispatchers.IO + reconnectJob)
~~~

기존에는 독립 CoroutineScope를 사용해 서비스 종료 시 재연결 작업을 취소하기 어려웠다.

#### 연결 성공 시 GATT 저장

~~~kotlin
mGatt = gatt
~~~

STATE_CONNECTED 콜백에서 현재 연결을 저장한다.

#### 종료 중 연결 해제 처리

~~~kotlin
if (!isReconnect) {
    gatt?.close()
    mGatt = null
    BleBridge.updateState(DISCONNECTED)
    return
}
~~~

사용자 종료로 끊긴 경우:

- 연결 끊김 알림을 만들지 않는다.
- CONNECTING 상태로 바꾸지 않는다.
- 3초 뒤 재연결하지 않는다.

#### 재연결 이중 방어

3초 대기 후:

~~~kotlin
delay(3000L)
if (!isReconnect) return@launch
~~~

reconnect() 진입 시:

~~~kotlin
if (!isReconnect) return
~~~

종료와 재연결 타이밍이 겹쳐도 새 GATT를 만들지 않는다.

#### 재연결 GATT 저장

~~~kotlin
mGatt = bluetoothDevice.connectGatt(...)
~~~

재연결로 생성한 GATT도 이후 shutdown()에서 닫을 수 있다.

#### shutdown() 추가

종료 순서:

~~~text
isReconnect=false
→ reconnectJob.cancel()
→ reconnect/disconnect Handler callback 제거
→ BLE Timer/TimerTask 취소
→ mGatt=null 처리
→ gatt.disconnect()
→ gatt.close()
→ BleBridge DISCONNECTED
→ BleManager INSTANCE=null
~~~

INSTANCE=null이 필요한 이유:

- 종료된 BleManager는 reconnectJob이 취소된 상태다.
- 이를 새 센서에서 재사용하면 재연결이 동작하지 않을 수 있다.
- singleton을 제거하면 새 센서에서 isReconnect=true인 새 인스턴스와 새 Job이 생성된다.

### 3.7 StabilizationScreen.kt

경로:

~~~text
app/src/main/java/kr/co/uxn/agms_p_a2rt/ui/components/ready/StabilizationScreen.kt
~~~

서비스 시작 이벤트 직전에 추가:

~~~kotlin
DataStoreManager.saveIsSensorEnded(false)
bleViewModel.emit("START_SERVICE")
~~~

필요한 이유:

- 이전 센서 종료 후 isSensorEnded는 true다.
- 서비스는 true이면 즉시 stopSelf()한다.
- 새 센서 서비스를 시작하기 전에 반드시 false로 바꿔야 한다.

센서 시작 시간을 저장하는 위치에서도 false를 다시 저장한다. 시작 이벤트 직전 코드가 주 방어이고 시작 시간 저장부는 보조 방어다.

### 3.8 MainActivity.kt의 STOP_SERVICE 전달 경로

경로:

~~~text
app/src/main/java/kr/co/uxn/agms_p_a2rt/MainActivity.kt
~~~

이 이벤트 수집 코드는 이번 작업에서 새로 추가하지 않았지만, 새 센서 종료 흐름이 실제 서비스까지 전달되기 위해 사용하는 기존 코드다.

~~~text
bleViewModel.emit("STOP_SERVICE")
→ MainActivity의 events collector
→ ACTION_STOP_SERVICE가 지정된 Intent 생성
→ AlwaysA2RTService.onStartCommand()
~~~

따라서 종료 디버깅 시 다음 두 지점을 함께 확인해야 한다.

1. SettingListScreen에서 STOP_SERVICE가 emit됐는가
2. 서비스 로그에 Received ACTION_STOP_SERVICE가 출력됐는가

MainActivity가 STARTED 상태가 아니어서 SharedFlow collector가 중지된 상황에서는 이벤트가 전달되지 않을 수 있다. 현재 설정 화면은 MainActivity가 STARTED인 상태에서 동작하므로 정상적인 사용자 종료 경로에서는 수집된다.

### 3.9 AlwaysA2RTBroadcastReceiver.kt와 재부팅

경로:

~~~text
app/src/main/java/kr/co/uxn/agms_p_a2rt/ble/AlwaysA2RTBroadcastReceiver.kt
~~~

이 파일은 이번 작업에서 직접 변경하지 않았다.

기존 부팅 흐름:

~~~text
BOOT_COMPLETED
→ DataStore deviceMac 확인
→ MAC이 있을 때만 서비스 시작
~~~

센서 종료 시 새 로직이 deviceMac을 삭제하므로 부팅 리시버가 서비스를 시작하지 않는다.

혹시 다른 경로에서 서비스가 호출돼도 서비스가 isSensorEnded=true를 확인하고 START_NOT_STICKY로 종료한다.

## 4. QR 연결과 BLE 검색 화면 변경

### 4.1 RegisterDeviceScreenQR.kt

경로:

~~~text
app/src/main/java/kr/co/uxn/agms_p_a2rt/ui/components/ready/RegisterDeviceScreenQR.kt
~~~

QR 인식과 서버 MAC 조회 로직은 이번 재스캔 수정에서 변경하지 않았다.

기존 흐름:

~~~text
QR serialNumber
→ getDeviceMac(serialNumber)
→ 서버의 deviceMac
→ ScanDeviceScreen/{deviceMac}/{serialNumber}
~~~

### 4.2 ScanDeviceScreen.kt

경로:

~~~text
app/src/main/java/kr/co/uxn/agms_p_a2rt/ui/components/ready/ScanDeviceScreen.kt
~~~

#### 기존 문제

BleViewModel은 화면 이동 후에도 유지된다. 최초 등록에서 기기를 찾으면 isFindDevice=true가 된 뒤 초기화되지 않았다.

센서 종료 후 재등록 시:

1. 새 화면에서도 isFindDevice=true
2. 기존 collector가 즉시 scan 중지
3. 20초 타임아웃도 이미 발견했다고 판단
4. 기기를 못 찾았어도 ScanFailScreen으로 이동하지 않음

#### isFindDevice는 제거하지 않음

제거한 것은 사용하지 않던 Compose 변수뿐이다.

~~~kotlin
val data by bleViewModel.isFindDevice.collectAsState()
~~~

상태 자체는 계속 사용한다.

- 기기를 발견했는지 기록
- 중복 onScanResult 방지
- 20초 타임아웃 성공/실패 판단

#### 매 검색마다 false 초기화

~~~kotlin
LaunchedEffect(mac, serialNumber) {
    bleViewModel.updateIsFindDevice(false)
    ...
}
~~~

적용되는 경우:

- 최초 센서 등록
- 센서 종료 후 재등록
- ScanFail 후 재시도
- 다른 MAC/serial로 재진입

#### 중복 결과 방지

~~~kotlin
if (bleViewModel.isFindDevice.value) return
~~~

같은 기기의 광고 패킷이 여러 번 와도 첫 결과만 처리한다.

#### 발견 즉시 scan 종료

~~~kotlin
bleViewModel.updateIsFindDevice(true)
bluetoothAdapter.bluetoothLeScanner?.stopScan(this)
~~~

#### 실패 처리 추가

다음 경우 즉시 ScanFailScreen으로 이동한다.

- BLUETOOTH_SCAN 권한 없음
- BluetoothLeScanner가 null
- Android BLE API의 onScanFailed() 호출

#### 20초 타임아웃 통합

기존 별도 teraRups()와 StateFlow collector를 제거했다.

~~~kotlin
scanner.startScan(...)
delay(20_000L)

if (!bleViewModel.isFindDevice.value) {
    navController.navigate("ScanFailScreen")
}
~~~

#### 모든 경로에서 scan 종료

~~~kotlin
try {
    scanner.startScan(...)
    ...
} finally {
    scanner.stopScan(scanCallback)
}
~~~

다음 경우 모두 scan을 정리한다.

- 발견 성공
- 20초 타임아웃
- API scan 실패
- 안정화 화면 이동
- 실패 화면 이동
- 사용자 뒤로 이동
- Composable 취소

추가 로그:

~~~text
BLE 스캔 시작: <MAC>
mac : <발견 MAC>, id : <이름>, rssi : <RSSI>
onScanFailed.. errorCode : <코드>
20초 동안 기기를 찾지 못해 실패 화면으로 이동합니다.
BLE 스캔 종료: <MAC>
~~~

## 5. 최초 연결과 자동 재연결 영향

### 최초 연결

~~~text
isSensorEnded 기본 false
→ ScanDeviceScreen isFindDevice=false
→ 기기 발견
→ StabilizationScreen isSensorEnded=false
→ 서비스 시작
~~~

기존 최초 연결을 막는 변경은 없다.

### 서비스 실행 중 일시적인 BLE 끊김

isReconnect 기본값은 계속 true다.

~~~text
STATE_DISCONNECTED
→ isReconnect=true
→ 3초 대기
→ isReconnect 재검사
→ reconnect()
→ 새 GATT 저장
~~~

기존 자동 재연결은 유지된다.

### 사용자 센서 종료

~~~text
shutdown()
→ isReconnect=false
→ 재연결 Job 취소
→ GATT close
→ BleManager singleton 제거
~~~

사용자 종료에서만 자동 재연결을 막는다.

### 종료 후 새 센서

새 BleManager가 만들어진다.

~~~text
새 BleManager
→ isReconnect=true
→ 새 reconnectJob
→ 새 GATT
~~~

## 6. 정상 종료 로그와 판정

정상 예시:

~~~text
Received ACTION_STOP_SERVICE, stopping service.
서비스 코루틴 에러 발생 : StandaloneCoroutine was cancelled
BluetoothGatt cancelOpen()
BluetoothGatt close()
BluetoothGatt unregisterApp()
bleState : DISCONNECTED
Service onDestroy() call!
~~~

의미:

- ACTION_STOP_SERVICE: 종료 명령 도착
- StandaloneCoroutine cancelled: 서비스 Job 취소에 따른 예상 로그
- close/unregisterApp: GATT 자원 해제
- DISCONNECTED: UI 연결 상태 반영
- onDestroy: 서비스 파괴 완료

releaseResources()가 종료 명령과 onDestroy에서 모두 호출될 수 있어 DISCONNECTED 로그가 두 번 보일 수 있다. 함수는 반복 호출 가능하게 구성돼 있어 기능상 문제는 없다.

종료 후 아래 로그가 나오면 재연결 또는 재시작 여부를 조사해야 한다.

~~~text
======BLE reconnect() 진입 ======
gatt connected!
Service onCreate() call!
~~~

새 센서를 명시적으로 등록한 뒤라면 위 로그는 정상이다.

## 7. 재스캔 디버깅 체크리스트

화면 진입:

~~~text
BleViewModel: _isFindDevice 값 : false
SCAN: BLE 스캔 시작: <MAC>
~~~

성공:

~~~text
SCAN: mac : <MAC>, id : <name>, rssi : <value>
BleViewModel: _isFindDevice 값 : true
SCAN: BLE 스캔 종료: <MAC>
~~~

실패:

~~~text
20초 동안 기기를 찾지 못해 실패 화면으로 이동합니다.
BLE 스캔 종료: <MAC>
~~~

시작 로그가 없다면 확인:

- Bluetooth가 켜져 있는가
- BLUETOOTH_SCAN 권한이 있는가
- QR API MAC 형식이 올바른가
- BluetoothLeScanner가 null인가

## 8. 롤백 지점

### 기존 로그아웃 방식으로 센서 종료

SettingListScreen.kt의 기존 센서 종료 로직 주석 블록을 복원하고 바로 아래 새 종료 로직을 비활성화한다.

주의:

- 토큰과 사용자 정보를 삭제한다.
- 앱을 강제 종료한다.
- 홈 종료 카드로 이동하지 않는다.

### 서비스/BLE 정리만 롤백

AlwaysA2RTService.kt:

- releaseResources()
- getIsSensorEnded() 검사
- START_NOT_STICKY 종료 분기
- BleManager.mGatt에 connectGatt() 반환값 저장

BleManager.kt:

- reconnectJob/reconnectScope
- shutdown()
- isReconnect 조기 반환
- mGatt 저장

이 부분을 되돌리면 종료 후 GATT나 재연결 작업이 남을 수 있다.

### QR 재스캔만 롤백

ScanDeviceScreen.kt:

- 진입 시 updateIsFindDevice(false)
- 중복 결과 방지
- 성공 즉시 stopScan()
- 권한/scanner null/onScanFailed 처리
- try/finally 20초 타임아웃

되돌리면 이전 true 상태가 남아 새 스캔이 즉시 중지되는 문제가 다시 발생할 수 있다.

## 9. 현재 주의점

1. 센서 종료 API 실패 시 로컬 서비스 종료를 진행하지 않는다.
2. 센서 종료 시 현재 센서 Room 데이터가 삭제된다.
3. Job 취소가 StandaloneCoroutine was cancelled라는 에러 로그로 표시되지만 예상 가능한 취소다.
4. 휴대폰 Bluetooth 자체는 끄지 않는다. 앱의 GATT 연결만 종료한다.
5. 종료 카드 중 홈 그래프 계산 로그가 잠시 출력될 수 있다.
6. Android 제조사별 BLE 스택 차이가 있으므로 실기기 회귀 테스트가 필요하다.

## 10. 권장 실기기 회귀 테스트

1. 앱 최초 QR 등록 성공
2. 최초 등록에서 기기를 못 찾으면 20초 뒤 ScanFail
3. ScanFail 재시도 후 성공
4. 서비스 실행 중 센서 신호 단절 후 자동 재연결
5. 설정 센서 종료 후 GATT close와 Service onDestroy
6. 종료 후 10초 이상 재연결 로그 없음
7. 앱 재실행 후 종료 카드 유지
8. 휴대폰 재부팅 후 서비스 미실행
9. 종료 카드에서 새 센서 QR 등록 성공
10. 새 센서 서비스 시작과 자동 재연결 확인
