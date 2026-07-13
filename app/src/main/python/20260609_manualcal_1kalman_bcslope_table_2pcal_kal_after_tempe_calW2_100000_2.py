# DP.check_data3(timestamp_list, W1_list, W2_list, temperature_list, k, timestamp2_list, BG_list)
# 에는 
# 1) 1분간격의 timestamp_list, W1_list, W2_list, temperature_list가 세트로 입력되어야 하고, 
# 2) temperature_list에 0 또는 결측치가 있으면 대응되는 list 값도 제거 해야 함 
# 3) k는 앱 또는 웹에서 입력 가능하도록 해야 함
# 4) timestamp2_list, BG_list는 채혈혈당 세트임 
# 5) 아래의 최종 결과는
#    return allBGtstamp, allBG, timestamps.tolist(), (np.array(tempresult5) * np.array(slopeline) + np.array(interceptline)).tolist() 
#    allBGtstamp, allBG는 캘리브레이션에 사용된 최종 채혈혈당값
#    timestamps.tolist()와 (np.array(tempresult5) * np.array(slopeline) + np.array(interceptline)).tolist() 는 raw 전류신호가 최종 혈당을 변한된 값임

import sys
import io
import traceback
import json

import numpy as np
from collections import deque
from datetime import datetime

from scipy.optimize import curve_fit

class RollingMedianMinutes:
    def __init__(self, window_minutes=5, ignore_nan=True, min_points=1):
        self.window_seconds = float(window_minutes) * 60.0
        self.ignore_nan = ignore_nan
        self.min_points = int(min_points)
        self.buffer = deque()  # each item: (t_seconds, value)

    def reset(self):
        self.buffer = deque()

    def _to_seconds(self, ts):
        # Accept datetime or numeric (seconds)
        if isinstance(ts, datetime):
            return ts.timestamp()
        else:
            return float(ts)

    def _purge_old(self, t_now):
        # Remove items older than (t_now - window_seconds)
        t_min = t_now - self.window_seconds
        while self.buffer and self.buffer[0][0] < t_min:
            self.buffer.popleft()

    def update(self, ts, value):
        """
        Add (timestamp, value), then return rolling median over last N minutes.
        If there are fewer than min_points, returns np.nan.
        """
        t = self._to_seconds(ts)

        # Optionally ignore NaN / None
        if value is None:
            if self.ignore_nan:
                self._purge_old(t)
                return self.median(ts)
            value = np.nan

        v = float(value)
        if self.ignore_nan and np.isnan(v):
            self._purge_old(t)
            return self.median(ts)

        self.buffer.append((t, v))
        self._purge_old(t)
        return self.median(ts)

    def median(self, ts=None):
        """
        Get median of current window.
        If ts is given, first purge old values based on ts.
        """
        if ts is not None:
            t = self._to_seconds(ts)
            self._purge_old(t)

        if len(self.buffer) < self.min_points:
            return np.nan

        values = [v for (_, v) in self.buffer]
        return float(np.median(values))

    def count(self):
        return len(self.buffer)

    def filter(self, timestamps, values):
        """
        Batch processing: return medians for each (ts, value).
        """
        out = []
        for ts, v in zip(timestamps, values):
            out.append(self.update(ts, v))
        return np.array(out, dtype=float)

class SimpleKalman:
    """
    Very simple linear Kalman Filter.

    Model:
        x = F x
        z = H x

    Required shapes (your case):
        x: (n,1)  -> n=3
        F: (n,n)
        H: (m,n)  -> m=1
        Q: (n,n)
        R: (m,m)
        P: (n,n)
    """

    def __init__(self, x, F, H, Q, R, P):
        self.x = np.array(x, dtype=float)
        self.F = np.array(F, dtype=float)
        self.H = np.array(H, dtype=float)
        self.Q = np.array(Q, dtype=float)
        self.R = np.array(R, dtype=float)
        self.P = np.array(P, dtype=float)

        self._check_shapes()

    def _check_shapes(self):
        # x must be (n,1)
        if self.x.ndim != 2 or self.x.shape[1] != 1:
            raise ValueError("x must be shape (n,1)")

        n = self.x.shape[0]

        if self.F.shape != (n, n):
            raise ValueError("F must be shape (n,n)")
        if self.Q.shape != (n, n):
            raise ValueError("Q must be shape (n,n)")
        if self.P.shape != (n, n):
            raise ValueError("P must be shape (n,n)")

        # H is (m,n), so R must be (m,m)
        if self.H.ndim != 2 or self.H.shape[1] != n:
            raise ValueError("H must be shape (m,n)")
        m = self.H.shape[0]
        if self.R.shape != (m, m):
            raise ValueError("R must be shape (m,m)")

    def predict(self):
        # x = F x
        self.x = self.F.dot(self.x)

        # P = F P F^T + Q
        self.P = self.F.dot(self.P).dot(self.F.T) + self.Q

        return self.x.copy()

    def update(self, z):
        """
        z can be:
          - number (if m=1)
          - array shape (m,) or (m,1)
        """
        z = np.array(z, dtype=float)

        # make z shape (m,1)
        if z.ndim == 0:
            z = z.reshape(1, 1)
        elif z.ndim == 1:
            z = z.reshape(-1, 1)
        elif z.ndim == 2 and z.shape[1] != 1:
            raise ValueError("z must be scalar, (m,), or (m,1)")

        # y = z - Hx
        y = z - self.H.dot(self.x)

        # S = HPH^T + R
        S = self.H.dot(self.P).dot(self.H.T) + self.R

        # K = P H^T S^-1
        K = self.P.dot(self.H.T).dot(np.linalg.inv(S))

        # x = x + K y
        self.x = self.x + K.dot(y)

        # P = (I - K H) P
        I = np.eye(self.P.shape[0])
        self.P = (I - K.dot(self.H)).dot(self.P)

        return self.x.copy()

    def step(self, z):
        """One full Kalman step: predict then update."""
        self.predict()
        return self.update(z)





class MergedStream(io.StringIO):
    def __init__(self):
        super().__init__()
        self.buffer = []

    def write(self, text):
        self.buffer.append(text)

    def flush(self):
        pass  # required for compatibility

    def get_output(self):
        return ''.join(self.buffer)
        
        
def check_data_wrapper3(timestamp_list, W1_list, W2_list, temperature_list, alphas, timestamp2_list, BG_list, bcurrents, sensitivities, bcslopes, times):

    merged = MergedStream()

    # Redirect both stdout and stderr to merged stream
    sys.stdout = merged
    sys.stderr = merged

    try:
        if len(timestamp_list) < 10 :
            result = [0, 60], [100, 100], [0, 60, 120], [100, 100, 100] 
        else :
            result=DP.check_data3(timestamp_list, W1_list, W2_list, temperature_list, alphas, timestamp2_list, BG_list, bcurrents, sensitivities, bcslopes, times)
    except Exception:
        traceback.print_exc()  # also goes to merged
    finally:
        # Restore default stdout/stderr
        sys.stdout = sys.__stdout__
        sys.stderr = sys.__stderr__

    return merged.get_output(), *result

def _to_py_list(maybe_list):
    """
    Java ArrayList (with .size()/.get()) 또는 Python iterable(list/tuple/numpy) 모두 안전히 처리.
    """
    # 먼저 Java-style list 시도
    try:
        # 일부 Java 객체는 hasattr이 제대로 동작하지 않으니 예외 처리로 확인
        size = maybe_list.size()
        return [maybe_list.get(i) for i in range(size)]
    except Exception:
        pass

    # 그 다음 일반적인 파이썬 변환 시도
    try:
        return list(maybe_list)
    except Exception:
        pass

    # fallback: 빈 리스트
    return []

def custom_function(timestamp_list, W1_list, W2_list, temperature_list, alphas, timestamp2_list, BG_list, bCurrents, sensitivities, bcSlopes, times):
    print("\n --------------- Glucose Conversion Python Start -------------------")
    try:
        # 안전 변환: Java list 또는 Python list 모두 대응
        timestamp_list_py = _to_py_list(timestamp_list)
        W1_list_py = _to_py_list(W1_list)
        W2_list_py = _to_py_list(W2_list)
        temperature_list_py = _to_py_list(temperature_list)
        timestamp2_list_py = _to_py_list(timestamp2_list)
        BG_list_py = _to_py_list(BG_list)

        alphas_py = _to_py_list(alphas)
        bCurrents_py = _to_py_list(bCurrents)
        sensitivities_py = _to_py_list(sensitivities)
        bcSlopes_py = _to_py_list(bcSlopes)
        times_py = _to_py_list(times)

        print("timestamp_list :", timestamp_list_py[:10], "... (len={})".format(len(timestamp_list_py)))
        print("W1_list :", W1_list_py[:10], "... (len={})".format(len(W1_list_py)))
        print("W2_list :", W2_list_py[:10], "... (len={})".format(len(W2_list_py)))
        print("temperature_list :", temperature_list_py[:10], "... (len={})".format(len(temperature_list_py)))
        print("alphas :", alphas_py[:10], "... (len={})".format(len(alphas_py)))
        print("bCurrents :", bCurrents_py[:10], "... (len={})".format(len(bCurrents_py)))
        print("sensitivities :", sensitivities_py[:10], "... (len={})".format(len(sensitivities_py)))
        print("bcSlopes :", bcSlopes_py[:10], "... (len={})".format(len(bcSlopes_py)))
        print("times :", times_py[:10], "... (len={})".format(len(times_py)))
        print("bg_timestamp_list :", timestamp2_list_py[:10], "... (len={})".format(len(timestamp2_list_py)))
        print("bg_list :", BG_list_py[:10], "... (len={})".format(len(BG_list_py)))

        if len(BG_list_py) == 0:
              BG_list_py = []
              timestamp2_list_py = []
        result = check_data_wrapper3(timestamp_list_py, W1_list_py, W2_list_py, temperature_list_py, alphas_py, timestamp2_list_py, BG_list_py, bCurrents_py, sensitivities_py, bcSlopes_py, times_py)
        # result22[0]은 merged logs, 이후가 함수 반환값
        merged_logs = result[0]
        # 함수 반환값은 result22[1:] — None일 수 있음
        func_results = result[1:] if len(result) > 1 else ()
        # 로그/결과 출력 (Chaquopy 로그에 보일 것)
        print("----- DP internal logs START -----")
        print(merged_logs)
        print("----- DP internal logs END -----")

        print("----- DP function results START -----")
        print("func_results:", func_results)
        print("----- DP function results END -----")


        # 필요하면 결과를 어떤 형태로 Java 쪽에 리턴할지 결정 (예: 튜플/리스트)


        result_dict = {
            "bgTimestamps": func_results[0],
            "bgValues": func_results[1],
            "sensorTimestamps": func_results[2],
            "calculatedGlucose": func_results[3]
        }

        print("==== result_dcit====")
        print(result_dict)

        # 여기서는 아무 것도 리턴하지 않음 (Chaquopy에서 호출만 하는 경우)
#         return merged_logs, func_results
        return json.dumps(result_dict, indent=4)

    except Exception:
        # 변환 중 예외/기타 오류는 여기에서 캡처
        tb = traceback.format_exc()
        print("custom_function exception:\n", tb)
        return "exception", tb
    
# Define the power function with an offset
def power_law_with_offset(x, a, b, c):
    return a * np.power(x, b) + c

class DP:
    scale=np.zeros(20)
    a=np.zeros(20)
    b=np.zeros(20)
    offset=np.zeros(20)

    def filter_after_minutes_np(tstamps, vals, secs):
        t0 = tstamps[0]
        threshold = t0 + secs

        mask = tstamps >= threshold
        return tstamps[mask], vals[mask]
        

    def check_data3(timestamp_list, W1_list, W2_list, temperature_list, alphas, timestamp2_list, BG_list, bcurrents, sensitivities, bcslopes, times):    

        timestamp_list_clean, W2_list_clean, temperature_list_clean = zip(*[(a, b, c) for a, b, c in zip(timestamp_list, W2_list, temperature_list) if c > 0]) 
        
        allBGtstamp=[]
        allBG=[]
        timestamps = np.array(timestamp_list_clean)
        Wtemps = np.array(W2_list_clean)
        temperatures = np.array(temperature_list_clean)

        timestamps2 = np.array(timestamp2_list)
        BGs = np.array(BG_list)      
        
        timestamps = timestamps[::1]
        # Initialize an array to store results
        results1 = np.zeros_like(Wtemps, dtype=float)
        f1 = RollingMedianMinutes(window_minutes=5)
        # Process each pair and store the result
        for i, (timestamp, value) in enumerate(zip(timestamps, Wtemps)):
            #results[i] = DP.smoothe_data(timestamp, value, 1, 5)
            results1[i] = f1.update(timestamp, value)
        tempresult1 = results1[::1]

        tempresult5 = np.zeros_like(tempresult1, dtype=float)
        x = np.array([[tempresult1[0]], [tempresult1[0]], [0]])
        # State transition (3x3)
        F = np.array([[0.92, 0.08, 0],
                      [0,    1,    1],
                      [0,    0,    1]])
        # Observation (m=1, so 1x3)
        H = np.array([[1, 0, 0]])
        # Process noise (3x3)
        Q = np.array([[1, 0, 0],
                      [0, 1, 0],
                      [0, 0, 1]])
        # Measurement noise (1x1)
        R = np.array([[100000]])
        # Initial covariance (3x3)
        P = np.array([[3, 0, 0],
                      [0, 3, 0],
                      [0, 0, 3]])
        kf1 = SimpleKalman(x, F, H, Q, R, P)
        #DP.kalman_filter_reset1 (tempresult[0])
        for i, val in enumerate(tempresult1):
            #tempresult5[i] = DP.kalman_filter1(tempresult[i])
            x_est = kf1.step(tempresult1[i])
            tempresult5[i] = x_est.ravel()[1]
            
            ### bypass kalman
            tempresult5[i] = tempresult1[i]

        
        # Initialize an array to store results
        results2 = np.zeros_like(Wtemps, dtype=float)
        f2 = RollingMedianMinutes(window_minutes=5)
        # Process each pair and store the result
        for i, (timestamp, value) in enumerate(zip(timestamps, temperatures)):
            results2[i] = f2.update(timestamp, value)
        tempresult2 = results2[::1]

        tempresult6 = np.zeros_like(tempresult2, dtype=float)
        x = np.array([[tempresult2[0]], [tempresult2[0]], [0]])
        # State transition (3x3)
        # F = np.array([[0.92, 0.08, 0],
                      # [0,    1,    1],
                      # [0,    0,    1]])
        # # Observation (m=1, so 1x3)
        # H = np.array([[1, 0, 0]])
        # # Process noise (3x3)
        # Q = np.array([[1, 0, 0],
                      # [0, 1, 0],
                      # [0, 0, 1]])
        # # Measurement noise (1x1)
        # R = np.array([[100000]])
        # # Initial covariance (3x3)
        # P = np.array([[3, 0, 0],
                      # [0, 3, 0],
                      # [0, 0, 3]])
        #print(P)
        kf2 = SimpleKalman(x, F, H, Q, R, P)
        #DP.kalman_filter_reset1 (tempresult[0])
        for i, val in enumerate(tempresult2):
            #tempresult5[i] = DP.kalman_filter1(tempresult[i])
            x_est = kf2.step(tempresult2[i])
            tempresult6[i] = x_est.ravel()[1]
            
            ### bypass kalman
            tempresult6[i] = tempresult2[i]

        #alphas, bcurrents, sensitivities, bcslopes, times)
        
        alpha = alphas[0]                
        time = np.array(times)[0]
        sensitivity = np.zeros_like(timestamps, dtype=float)
        basecurrent = np.zeros_like(timestamps, dtype=float)
        bcslope = np.zeros_like(timestamps, dtype=float)
        bcline = np.zeros_like(timestamps, dtype=float)
        # bcline = -(timestamps - timestamps[-1]) * bcslope / 86400 + basecurrent - bcslope /2 
        
        for i, val in enumerate(timestamps):            
            #idx = int((timestamps[i]-timestamps[0]) / (time *3600))
            #temppercent = int((timestamps[i]-timestamps[0]) % (time *3600)) 
            idx = int((timestamps[i]-timestamps[0]) / ( (time) *3600))
            temppercent = int((timestamps[i]-timestamps[0]) % ((time) *3600)) 

            #idx = idx + 1
            if idx < 0 :
                idx =0 
            sensitivity[i] = sensitivities[idx]
            basecurrent[i] = bcurrents[idx]
            bcslope[i] = bcslopes[idx]
            #bcline = -(timestamps - timestamps[-1]) * bcslope / 86400 + basecurrent - bcslope /2
            bcline[i] = basecurrent[i] - bcslope[i] * temppercent / (time *3600) + bcslope[i]/2
            #bcline[i] = basecurrent[i] 
            

        # timestamps2 = np.array(timestamp_list)
        # BGs = np.array(BG_list)      
        deltas = np.zeros_like(timestamps, dtype=float)

        #tempresult = (tempresult5-bcline)*18/sensitivity/(1-(37-tempresult6)*alpha)
        
        tempresult = (tempresult5)/(1-(37-tempresult6)*alpha) -bcline 
        tempresult = tempresult*18/sensitivity
        
        
        x = np.array([[tempresult[0]], [tempresult[0]], [0]])
        kf3 = SimpleKalman(x, F, H, Q, R, P)
        #DP.kalman_filter_reset1 (tempresult[0])
        for i, val in enumerate(tempresult):
            #tempresult5[i] = DP.kalman_filter1(tempresult[i])
            x_est = kf3.step(tempresult[i])
            tempresult[i] = x_est.ravel()[1]

        
        if len(timestamps2) == 1 :
            idx = np.searchsorted(timestamps, timestamps2[0]) + 1            
            deltas[idx:] = BGs[0] - tempresult[idx]
            tempresult[idx:]= tempresult[idx:]+deltas[idx:]        
        if len(timestamps2) >=2 :            
            for i in range(len(timestamps2)-1):
                val1 = timestamps2[i]
                val2 = timestamps2[i+1]
                idx1 = np.searchsorted(timestamps, val1) + 1            
                idx2 = np.searchsorted(timestamps, val2)
                delta1 = BGs[i] - tempresult[idx1]
                delta2 = BGs[i+1] - tempresult[idx2]                
                for j in range(idx2-idx1+1):                    
                    deltas[idx1+j] = delta1 + j * (delta2-delta1)/(idx2-idx1)                                
            deltas[idx2:] = delta2                        
            tempresult= tempresult+deltas
            
             
        timestamps = timestamps - 720

        return allBGtstamp, allBG, timestamps.tolist(), np.array(tempresult).tolist()                           
        #return allBGtstamp, allBG, timestamps.tolist(), ( (np.array(tempresult) - bcurrent)*18/sensitivity).tolist() 
            
        

def main():
    # This is where your main application logic will go
    print("Hello, world!")    
    results = check_data_wrapper3(
        [1000, 2000, 3000, 10000, 10010, 10020, 10030, 10040, 10050, 11000, 12000, 13000, 21000, 25000, 26000, 27000, 28000, 40000], 
        [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18], 
        [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18], 
        [10000, 11000, 13000, 25000, 26000], 
        [3, 4, 5, 6, 7])
    print(results)


# This ensures the main function is called only if this script is run directly
if __name__ == "__main__":
    main()