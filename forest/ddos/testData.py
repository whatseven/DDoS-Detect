import random
import xml.etree.cElementTree as ET
from datetime import datetime
from sklearn.tree import DecisionTreeRegressor
from sklearn.ensemble import RandomForestRegressor
import numpy as np

def addToDict(dict,item):
    if dict.get(item) is None:
        dict[item]=1
    else:
        dict[item]+=1

def getNegativeSample():

    tree=ET.parse("mid-level-phase-5-inside.xml")
    root=tree.getroot()
    sIp={}
    dIp={}
    sPort={}
    dPort={}
    nowTime=datetime(2017,10,25,11,26,15)
    nowTime=nowTime.timestamp()
    res=[]

    for idmsf in root:
        myAlert=idmsf[0]

        myTime=myAlert[0]
        myAnalyze=myAlert[1]
        mySource=myAlert[2]
        myTarget=myAlert[3]


        #solve the interval of time
        itemTime=myTime[1].text
        itemTime=datetime.strptime('2017-10-25 '+itemTime, '%Y-%m-%d %H:%M:%S')
        itemTime=itemTime.timestamp()
        dictWithInterval = {}
        if itemTime-nowTime>=2:
            dictWithInterval['sidi']=len(sIp)/len(dIp)
            dictWithInterval['sidp']=len(sIp)/len(dPort)
            dictWithInterval['dpdi']=len(dPort)/len(dIp)
            dictWithInterval['time']=nowTime
            res.append(dictWithInterval)

            nowTime=nowTime+2
            sIp = {}
            dIp = {}
            sPort = {}
            dPort = {}
        addToDict(sIp, mySource[0][0][0].text)
        addToDict(dIp, myTarget[0][0][0].text)
        addToDict(sPort, myTarget[1][1].text)
        addToDict(dPort, myTarget[1][2].text)

    return res



temp=getNegativeSample()

originNegativeData=temp[-2:]
negativeData=[]
negativeTarget=[]
for item in originNegativeData:
    negativeData.append([item['sidi'],item['sidp'],item['dpdi']])
    negativeTarget.append(1)

# scale
for i in range(50):

    rsidi=random.uniform(negativeData[0][0],negativeData[1][0])
    rsidp=random.uniform(negativeData[0][1],negativeData[1][1])
    rdpdi=random.uniform(negativeData[0][2],negativeData[1][2])
    negativeData.append([rsidi,rsidp,rdpdi])
    negativeTarget.append(1)

positiveData=[]
positiveTarget=[]
for i in range(50):

    rsidi=random.uniform(0,100)
    rsidp=random.uniform(0,100)
    rdpdi=random.uniform(0,100)
    positiveData.append([rsidi,rsidp,rdpdi])
    positiveTarget.append(0)

totalData=np.concatenate((np.array(positiveData),np.array(negativeData)))
totalTarget=np.concatenate((np.array(positiveTarget),np.array(negativeTarget)))

rf = RandomForestRegressor()  # 这里使用了默认的参数设置
rf.fit(totalData[:40], totalTarget[:40])  # 进行模型的训练
#
# 随机挑选两个预测不相同的样本
print('instance 41 prediction；', rf.predict(totalData[41]))
print('instance 42 prediction；', rf.predict(totalData[42]))
print(totalData[41],totalData[42])
