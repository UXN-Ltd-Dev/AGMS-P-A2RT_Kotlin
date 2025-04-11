# glucose convert python script3
# 염기원, 24-06-27 : 신규 알고리즘 코드 반영

import os
import numpy as np
import pandas as pd
from os.path import dirname, join


def check_data_wrapper2(timestamp, W1, W2):
    return DataProcessor.check_data2(timestamp, W1, W2)

def onepoint_calibration_wrapper(timestamp, bloodglucose):
    return DataProcessor.onepoint_calibration(timestamp, bloodglucose)

def input_slope(slope) :
    Parameters.slope=np.array(slope)

def input_intercept(intercept) :
    Parameters.intercept=np.array(intercept)

def input_offset(offset) :
    Parameters.offset=np.array(offset)

def input_scale(scale) :
    Parameters.scale=np.array(scale)

class Parameters:
    scale=np.zeros(20)
    slope=np.zeros(20)
    intercept=np.zeros(20)
    offset=np.zeros(20)

class DataProcessor:
    # slope=np.zeros(20)
    # intercept=np.zeros(20)
    # offset=np.zeros(20)
    # SCALE = 40

    calValue = 0
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

    calibrating=0

    def get_cbuffer(nparray):
        if DataProcessor.startidx <= DataProcessor.currentidx :
            return nparray [DataProcessor.startidx : DataProcessor.currentidx+1]
        else :
            return np.concatenate((nparray [DataProcessor.startidx:200], nparray[0:DataProcessor.currentidx+1]))

    def get_cbuffer_length () :
        if DataProcessor.startidx <= DataProcessor.currentidx :
            return DataProcessor.currentidx - DataProcessor.startidx + 1
        else :
            return (200 - DataProcessor.startidx) + (DataProcessor.currentidx + 1)

    def insert_into_cbuffer(timestamp, wraw, nperm, lengthinmin) :
        
        DataProcessor.currentidx = (DataProcessor.currentidx+1) % 200
        DataProcessor.tstampcbuffer[DataProcessor.currentidx] = timestamp
        DataProcessor.rdatacbuffer[DataProcessor.currentidx] = wraw
        
        if DataProcessor.get_cbuffer_length() > lengthinmin * nperm :
            DataProcessor.startidx = (DataProcessor.startidx + 1) % 200
            
        return

    def insert_into_cbuffer2_3(timestamp, wtemp2, nperm, lengthinmin) :
        DataProcessor.tstampcbuffer2[DataProcessor.currentidx2] = timestamp - 150
        DataProcessor.rdatacbuffer2[DataProcessor.currentidx2] = wtemp2

        if DataProcessor.currentidx2 == 0 or DataProcessor.calibrating==1 :
            DataProcessor.rdatacbuffer3[DataProcessor.currentidx2] = wtemp2
            DataProcessor.calibrating=0
        else :
            tempdiff = DataProcessor.rdatacbuffer2[DataProcessor.currentidx2] - DataProcessor.rdatacbuffer2[DataProcessor.currentidx2-1]
            tempinterval = DataProcessor.tstampcbuffer2[DataProcessor.currentidx2] - DataProcessor.tstampcbuffer2[DataProcessor.currentidx2-1]

            # 상승율 제한
            if tempdiff / tempinterval >=  (3 /60) :
                tempdiff = (3/60) * tempinterval
            elif tempdiff / tempinterval <=  -3 /60 :
                tempdiff = (-3/60) * tempinterval

            DataProcessor.rdatacbuffer3[DataProcessor.currentidx2] = DataProcessor.rdatacbuffer3[DataProcessor.currentidx2-1] + tempdiff

        # return_tstamp = DataProcessor.tstampcbuffer2[DataProcessor.currentidx2]
        return_glucose = DataProcessor.rdatacbuffer3[DataProcessor.currentidx2]

        DataProcessor.currentidx2 = (DataProcessor.currentidx2+1)
        if DataProcessor.currentidx2 > lengthinmin * nperm :#2시간마다 초기화 : 데이터 상승/하강 방향 컨트롤
            DataProcessor.currentidx2 = 0

        # return [return_tstamp, return_glucose]
        return return_glucose

    # 표준 편차를 반영한 평균 값 계산
    def smooth_data(timestamp, wraw, nperm, lengthinmin):
        # print('python >> ts' + str(timestamp))

        #insert into circular buffer
        DataProcessor.insert_into_cbuffer(timestamp, wraw, nperm, lengthinmin)

        #get mean and std from circular buffer from the elements within time range
        temptstamps = DataProcessor.get_cbuffer(DataProcessor.tstampcbuffer)
        temprdata = DataProcessor.get_cbuffer(DataProcessor.rdatacbuffer)

        #lengthinmin(5) 분 이내의 데이터만 반영 : 최소 현재 데이터 1개가 반영됨
        tempidxs = timestamp - temptstamps <= lengthinmin * 60

        # temptstamps = temptstamps[tempidxs]
        temprdata = temprdata[tempidxs]


        #std - mean
        # tempmean = np.mean(temprdata)
        # tempstd = np.std(temprdata)
        #
        # # print('python >> mean1' + str(tempmean))
        #
        # #get mean from the elements within time range and in the range of mean +-std
        # temprdata = temprdata[(temprdata <= tempmean + tempstd) & (temprdata >= tempmean - tempstd)]

        #mean
        # result=np.mean(temprdata)

        #median
        result=np.median(temprdata)

        # print('python >> mean2 : ' + str(result))

        return result

    def check_data2(timestamp, W1raw, W2raw):
        #W1 - coeff*W2
        Wtemp = W2raw

        #시작 시간 저장
        if DataProcessor.starttime == -1 :
            DataProcessor.starttime = timestamp

            #smoothing - 노이즈제거
        #6, 5 : 1분당 6개(10초), 5분에 표준 편차를 반영한 평균 값 계산
        Wtemp2 = DataProcessor.smooth_data(timestamp, Wtemp, 6, 5)

        # print('python2 >> Wtemp2 : ' + str(Wtemp2))

        #1분에 1개의 데이터만 반환
        DataProcessor.global_counter = DataProcessor.global_counter + 1

        if DataProcessor.global_counter % 6 == 0 :
            #시작일로부터 day index : 0=0일차, 1=1일차...
            dayidx = (timestamp-DataProcessor.starttime) // 86400

            # print('python2 >> timestamp : ' + str(timestamp - DataProcessor.starttime) + ' sec')

            #baseline
            baseline = Parameters.slope[dayidx] * (timestamp - DataProcessor.starttime) + Parameters.intercept[dayidx]

            # print('python2 >> Wtemp2 : ' + str(Wtemp2))
            # print('python2 >> calValue : ' + str(DataProcessor.calValue))
            # print('python >> baseline : ' + str(baseline))

            #scale, offset, cal_value
            Wtemp2 = (Wtemp2 - baseline) * Parameters.scale[dayidx] + Parameters.offset[dayidx] + DataProcessor.calValue

            # insert into 1min interval buffer
            # process by rate limit filter
            # [tstamp, DataProcessor.glucose] = DataProcessor.insert_into_cbuffer2_3(timestamp, Wtemp2, 1, 120)
            DataProcessor.glucose = DataProcessor.insert_into_cbuffer2_3(timestamp, Wtemp2, 1, 120)

            if DataProcessor.glucose <= 40 and DataProcessor.global_counter != 0 :
                DataProcessor.glucose = 40

                # return [tstamp, DataProcessor.glucose]
            # print('python >> g1 : ' + str(DataProcessor.glucose))
            return DataProcessor.glucose

        else :
            # print('python >> g2 : ' + str(DataProcessor.glucose))
            return DataProcessor.glucose
            # return [-200, -1]

    def onepoint_calibration_check(y, yp):
        if ( 180 >= y >= 70 ):
            return 1
        else :
            return 0

    def onepoint_calibration(blood_glucose):
        # if DataProcessor.glucose == 0 :
        #     return 0
        # else:


        input_valid = DataProcessor.onepoint_calibration_check(blood_glucose, DataProcessor.glucose)

        if input_valid == 0 :
            return 0
        else :
            DataProcessor.calValue = DataProcessor.calValue + bloodglucose - DataProcessor.glucose
            DataProcessor.calibrating=1

            print('python2 >> onepoint_calibration : ' + str(DataProcessor.calValue) + '/' + str(blood_glucose))

        return 1


#Parameter 파일 읽기

# #현재 파일의 폴더 경로; 작업 파일 기준
path = os.path.dirname(os.path.realpath(__file__))

# print('******* glucose python path : ' + path)
paramFilePath = join(path, "parameters3.csv")

# print('python >> glucose conversion parameter path : ' + paramFilePath)
df = pd.read_csv(paramFilePath)
# print("python >> param len: " + str(len(df)))


#csv 파일에서 읽은 파라미터 파일을 변수에 저장
dScale = df["scale"]
dSlope = df["slope"]
dIntercept = df["intercept"]
dOffset = df["offset"]

input_scale(dScale)
input_slope(dSlope)
input_intercept(dIntercept)
input_offset(dOffset)

# print("python >> slope: " + str(Parameters.slope[0]))
# print("python >> intercept: " + str(Parameters.intercept[0]))
# print("python >> offset: " + str(Parameters.offset[0]))
# print("python >> scale: " + str(Parameters.SCALE))
# print("python >> coefficient: " + str(Parameters.COEFFICIENT))

#convert glucose
def getGlucoseValue(timestamp, W1, W2):

    # print('python >> getGlucoseValue called')

    # print('python >> input cal :' + str(calValue))


    # 1번만 적용 필요
    # DataProcessor.onepoint_calibration(calValue)

    print('python2 >> ts: ' + str(timestamp) + ', W1: ' + str(W1) + ', W2: ' + str(W2))

    # glucose = W1 + W2 + 100 #test
    glucose = check_data_wrapper2(timestamp, W1, W2)

    print('python2 >> result : ' + str(glucose))

    return glucose

    # if(arrayLength<= 0):
    #    print('python >> getGlucoseValue length error')
    #    return -1
    # result = DataProcessor.check_data_wrapper2(timestamp, W1, W2)
    #
    # print('python >> glucose conversion result : ' + str(result))
    #
    # return result

#print('******* glucose conversion python script finish')

def getCurrentCalValue():
    return DataProcessor.calValue

#calibration
def setOnepointCalibration(timestamp, calValue):
    print('python2 >> onepoint calibration value : ' + str(calValue))

    result = DataProcessor.onepoint_calibration(calValue)

    return result