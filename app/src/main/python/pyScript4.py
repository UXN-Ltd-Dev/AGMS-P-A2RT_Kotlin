import numpy as np

from datetime import datetime, timedelta
from dateutil import tz
from pytz import timezone

import matplotlib.pyplot as plt
import pandas as pd

import math

# import numpy as np

def check_data_wrapper2(timestamp, W1, W2):
    return DP.check_data2(timestamp, W1, W2)

def onepoint_calibration_wrapper(timestamp, bloodglucose):
    return DP.onepoint_calibration(timestamp, bloodglucose)

def input_scale(alist) :
    DP.scale=np.array(alist)

def input_a(alist) :
    DP.a=np.array(alist)

def input_b(blist) :
    DP.b=np.array(blist)

def input_offset(offsetlist) :
    DP.offset=np.array(offsetlist)

class DP:
    scale=np.zeros(20)
    a=np.zeros(20)
    b=np.zeros(20)
    offset=np.zeros(20)

    calvalue=0

    glucose = 0

    starttime = -1
    tstampcbuffer = np.zeros(200)
    rdatacbuffer = np.zeros(200)
    startidx = 0
    currentidx = -1
    W = -200

    #
    global_counter = -1
    tstampcbuffer2 = np.zeros(200)
    rdatacbuffer2 = np.zeros(200)
    rdatacbuffer3 = np.zeros(200)
    startidx2 = 0
    currentidx2 = 0

    # to get cal point
    tstampcbuffer4 = np.zeros(200)
    rdatacbuffer4 = np.zeros(200)
    startidx4 = 0
    currentidx4 = 0

    calibrating=0
    calforminus=0

    # kalman filter matrices

    #Define the initial state (position and velocity)
    x = np.array([[100], [100], [0]])  # Initial values
    # Define the state transition matrix
    F = np.array([[0.92, 0.08, 0], [0, 1, 1], [0, 0, 1] ])
    # Define the observation matrix
    H = np.array([[1, 0, 0]])
    # Define the process noise covariance matrix
    Q = np.array([[1, 0, 0], [0, 1, 0], [0, 0, 1] ])
    # Define the measurement noise covariance matrix
    R = np.array([[10000]])
    # Define the initial covariance matrix
    P = np.array([[3, 0, 0],[0, 3, 0], [0, 0, 3] ])


    def kalman_filter(z):

        # Prediction step
        DP.x = np.dot(DP.F, DP.x)
        DP.P = np.dot(DP.F, np.dot(DP.P, DP.F.T)) + DP.Q

        # Update step
        y = z - np.dot(DP.H, DP.x)  # Innovation
        S = np.dot(DP.H, np.dot(DP.P, DP.H.T)) + DP.R  # Innovation covariance
        K = np.dot(DP.P, np.dot(DP.H.T, np.linalg.inv(S)))  # Kalman gain
        DP.x = DP.x + np.dot(K, y)  # Updated state estimate
        DP.P = DP.P - np.dot(K, np.dot(DP.H, DP.P))  # Updated estimate covariance
        return DP.x[0,0]

    def get_cbuffer(nparray):
        if DP.startidx <= DP.currentidx :
            return nparray [DP.startidx : DP.currentidx+1]
        else :
            return np.concatenate((nparray [DP.startidx:200], nparray[0:DP.currentidx+1]))

    def get_cbuffer_length () :
        if DP.startidx <= DP.currentidx :
            return DP.currentidx - DP.startidx + 1
        else :
            return (200 - DP.startidx) + (DP.currentidx + 1)

    def insert_into_cbuffer(timestamp, wraw, nperm, lengthinmin) :
        DP.currentidx = (DP.currentidx+1) % 200
        DP.tstampcbuffer[DP.currentidx] = timestamp
        DP.rdatacbuffer[DP.currentidx] = wraw
        if DP.get_cbuffer_length() > lengthinmin * nperm :
            DP.startidx = (DP.startidx + 1) % 200
        return

    def get_cbuffer4(nparray):
        if DP.startidx4 <= DP.currentidx4 :
            return nparray [DP.startidx4 : DP.currentidx4+1]
        else :
            return np.concatenate((nparray [DP.startidx4:200], nparray[0:DP.currentidx4+1]))

    def get_cbuffer4_length () :
        if DP.startidx4 <= DP.currentidx4 :
            return DP.currentidx4 - DP.startidx4 + 1
        else :
            return (200 - DP.startidx4) + (DP.currentidx4 + 1)

    def insert_into_cbuffer4(timestamp, wraw, nperm, lengthinmin) :
        DP.currentidx4 = (DP.currentidx4+1) % 200
        DP.tstampcbuffer4[DP.currentidx4] = timestamp
        DP.rdatacbuffer4[DP.currentidx4] = wraw
        if DP.get_cbuffer4_length() > lengthinmin * nperm :
            DP.startidx4 = (DP.startidx4 + 1) % 200
        return

    def get_cal_point(timestamp, tempglucose):
        temptstamps = DP.get_cbuffer4(DP.tstampcbuffer4)
        temprdata = DP.get_cbuffer4(DP.rdatacbuffer4)
        tempidxs = timestamp - temptstamps <= 60 * 60
        temptstamps = temptstamps[tempidxs]
        temprdata = temprdata[tempidxs]

        max_temprdata = np.max(temprdata)
        min_temprdata = np.min(temprdata)

        if max_temprdata - min_temprdata < 30 :
            W = ( max_temprdata + min_temprdata ) / 2
        else :
            W = tempglucose

        return W


    def insert_into_cbuffer2_3(timestamp, wtemp2, nperm, lengthinmin) :
        DP.tstampcbuffer2[DP.currentidx2] = timestamp - 150
        DP.rdatacbuffer2[DP.currentidx2] = wtemp2

        if DP.currentidx2 == 0 or DP.calibrating==1 :
            DP.rdatacbuffer3[DP.currentidx2] = wtemp2
            DP.calibrating=0
        else :
            tempdiff = DP.rdatacbuffer2[DP.currentidx2] - DP.rdatacbuffer2[DP.currentidx2-1]
            tempinterval = DP.tstampcbuffer2[DP.currentidx2] - DP.tstampcbuffer2[DP.currentidx2-1]

            if tempdiff / tempinterval >=  (3 /60) :
                tempdiff = (3/60) * tempinterval
            elif tempdiff / tempinterval <=  -3 /60 :
                tempdiff = (-3/60) * tempinterval
            DP.rdatacbuffer3[DP.currentidx2] = DP.rdatacbuffer3[DP.currentidx2-1] + tempdiff

        return_tstamp = DP.tstampcbuffer2[DP.currentidx2]
        return_glucose = DP.rdatacbuffer3[DP.currentidx2]

        DP.currentidx2 = (DP.currentidx2+1)
        if DP.currentidx2 > lengthinmin * nperm :
            DP.currentidx2 = 0

        return [return_tstamp, return_glucose]

    def smoothe_data(timestamp, wraw, nperm, lengthinmin):
        #insert into circular buffer
        DP.insert_into_cbuffer(timestamp, wraw, nperm, lengthinmin)

        #get mean and std from circular buffer from the elements within time range
        temptstamps = DP.get_cbuffer(DP.tstampcbuffer)
        temprdata = DP.get_cbuffer(DP.rdatacbuffer)
        tempidxs = timestamp - temptstamps <= lengthinmin *60
        temptstamps = temptstamps[tempidxs]
        temprdata = temprdata[tempidxs]

        W=np.median(temprdata)

        return W

    def check_data2(timestamp, W1raw, W2raw):

        #Wtemp = W1raw - 3 * W2raw
        Wtemp = W2raw
        if DP.starttime == -1 :
            DP.starttime = timestamp
        Wtemp2 = DP.smoothe_data(timestamp, Wtemp, 6, 5)

        DP.global_counter = DP.global_counter + 1
        if DP.global_counter % 6 == 0 :
            dayidx = (timestamp-DP.starttime) // 86400


            baseline = DP.a[dayidx] * (timestamp-DP.starttime) + DP.b[dayidx]
            Wtemp2 = (Wtemp2 - baseline) * DP.scale[dayidx] + DP.offset[dayidx] + DP.calvalue

            # call kalman filter
            Wtemp2 = DP.kalman_filter(Wtemp2)
            # insert into 1min interval buffer
            # process by rate limit fiter

            [tstamp, DP.glucose] = DP.insert_into_cbuffer2_3(timestamp, Wtemp2, 1, 120)

            # for calibration point calc
            DP.insert_into_cbuffer4(tstamp, DP.glucose, 1, 60)

            if DP.glucose <= 40 and DP.global_counter != 0 :
                return [tstamp, 40]

            return [tstamp, DP.glucose]
        else :
            return [-200, -1]


    def onepoint_calibration_check(y, yp):
        if ( 180 > y > 40 ):
            return 1
        else :
            return 0

    def onepoint_calibration(timestamp, bloodglucose):
        tempresult = DP.onepoint_calibration_check(bloodglucose, DP.glucose)

        if tempresult==0 :
            return 0
        else :
            calpoint = DP.get_cal_point(timestamp, DP.glucose)
            DP.calvalue = DP.calvalue + bloodglucose - calpoint

            DP.calibrating=1
            return 1


# GET /api/hello

def test_test(timestamp, W1, W2, event_time, event_value):
    print("\n --------------- Glucose Conversion Python Start -------------------")



    DP.scale=np.zeros(20)
    DP.a=np.zeros(20)
    DP.b=np.zeros(20)
    DP.offset=np.zeros(20)

    DP.calvalue=0

    DP.glucose = 0

    DP.starttime = -1
    DP.tstampcbuffer = np.zeros(200)
    DP.rdatacbuffer = np.zeros(200)
    DP.startidx = 0
    DP.currentidx = -1
    DP.W = -200

    #
    DP.global_counter = -1
    DP.tstampcbuffer2 = np.zeros(200)
    DP.rdatacbuffer2 = np.zeros(200)
    DP.rdatacbuffer3 = np.zeros(200)
    DP.startidx2 = 0
    DP.currentidx2 = 0

    # to get cal point
    DP.tstampcbuffer4 = np.zeros(200)
    DP.rdatacbuffer4 = np.zeros(200)
    DP.startidx4 = 0
    DP.currentidx4 = 0

    DP.calibrating=0
    DP.calforminus=0


    #Define the initial state (position and velocity)
    DP.x = np.array([[100], [100], [0]])  # Initial values
    # Define the state transition matrix
    DP.F = np.array([[0.92, 0.08, 0], [0, 1, 1], [0, 0, 1] ])
    # Define the observation matrix
    DP.H = np.array([[1, 0, 0]])
    # Define the process noise covariance matrix
    DP.Q = np.array([[1, 0, 0], [0, 1, 0], [0, 0, 1] ])
    # Define the measurement noise covariance matrix
    DP.R = np.array([[10000]])
    # Define the initial covariance matrix
    DP.P = np.array([[3, 0, 0],[0, 3, 0], [0, 0, 3] ])

    parameterData = {
        'day' : [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15],
        'scale' : [120, 120, 120, 120, 120, 120,120, 120, 120, 120, 120, 120, 120, 120, 120,120],
        'slope' : [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
        'intercept' : [3.899827,3.899827,3.899827,3.899827,3.899827,3.899827,3.899827,3.899827,3.899827,3.899827,3.899827,3.899827,3.899827,3.899827,3.899827,3.899827],
        'offset' : [90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90]
    }

    parameterDf = pd.DataFrame(parameterData)
    dScale = parameterDf["scale"]
    dSlope = parameterDf["slope"]
    dIntercept = parameterDf["intercept"]
    dOffset = parameterDf["offset"]


    #parameter init
    input_scale(dScale)
    input_a(dSlope)
    input_b(dIntercept)
    input_offset(dOffset)

    #[['experiment_date',item["experiment_date"]]]

    glucoseData = {
        "experiment_date": data["experiment_date"],
        "value_current": data["value_current"],
        "value_ae": data["value_ae"]
    }

    df = pd.DataFrame(glucoseData)
    dt = pd.to_datetime(df["experiment_date"])
    df['ts'] = (dt.values.astype('float')/10**9).astype('int')

    eventData = {
        "event_time" : data["event_time"],
        "event_value" : data["event_value"]
    }

    eventDF = pd.DataFrame(eventData)
    eventDT = pd.to_datetime(eventDF["event_time"])
    eventDF['ts'] = (eventDT.values.astype('float')/10**9).astype('int')

    print(eventDF)
    g_data = []

    print('Start calculating. Please wait a moment...')
    # ts(time stamp)와 value_current를 하나씩 꺼내와 알고리즘으로 변환을 시도

    base_second = df.loc[0,'ts']

    event_base_count = 0

    print(df)
    print(eventDF)
    for idx, row in df.iterrows():
        W1 = row["value_current"]
        W2 = row["value_ae"]

        #Calibration
        if idx + 1 < len(df):
            next_row_ts = df.iloc[idx + 1]['ts']
            #만약 이벤트 리스트의 event_base_count번째 ts가 row들 사이라면 칼리브레이션 적용
            if event_base_count < len(eventDF) :
                if row['ts'] <= eventDF.iloc[event_base_count]['ts'] and eventDF.iloc[event_base_count]['ts'] <= next_row_ts :
                    print('ok')
                    print(event_base_count)
                    print(eventDF.iloc[event_base_count]['ts'])
                    print(eventDF.iloc[event_base_count]['event_value'])
                    onepoint_calibration_wrapper(eventDF.iloc[event_base_count]['ts'] - base_second, eventDF.iloc[event_base_count]['event_value'])
                    event_base_count = event_base_count + 1

        # # # data 변환
        glucose = check_data_wrapper2(row['ts'] - base_second, W1, W2)

        #print(glucose)
        ts = row['ts']
        dtc = datetime.fromtimestamp(row['ts'], tz=timezone('Asia/Seoul'))

        if glucose[1] == -1:
            glucose = ''
        else:
            #g_data.append([glucose])
            #g_data.append([ dtc.strftime('%Y-%m-%d %H:%M:%S'), W2,  math.trunc(glucose[1])])

            g_data.append([row["experiment_date"], W1, W2,  math.trunc(glucose[1])])


        result = pd.DataFrame(g_data, columns=['created_at','w1_current','w2_current','glucose'])
        #result = pd.DataFrame(g_data, columns=['glucose'])
        jsonjson = result.to_json(orient='records')

    return jsonjson, 200

















































