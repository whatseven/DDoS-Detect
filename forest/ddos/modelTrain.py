import json

from sklearn.ensemble import RandomForestRegressor
from sklearn.externals import joblib


def loadData(file):
    with open(file,encoding="utf-8") as json_file:
        data=json.load(json_file)
        return data


def myCleanForPositiveData(prePositiveData):
    result=[]
    for item in prePositiveData:
        zeroNum=1
        for key in item:
            if item.get(key)=="0.0":
                zeroNum-=1
        if zeroNum>0:
            result.append([float(item.get("sidi")),float(item.get("sidp")),float(item.get("dpdi"))])
    return result


def myCleanForNegativeData(preNegativeData):
    result = []
    for item in preNegativeData:
        zeroNum = 2
        for key in item:
            if item.get(key) == "0.0":
                zeroNum -= 1
        if zeroNum>0:
            result.append([float(item.get("sidi")),float(item.get("sidp")),float(item.get("dpdi"))])
    return result

def detect(filePath="./rf.model",data=[]):
    #data=[1,1,1]
    rf=joblib.load(filePath)
    return rf.predict(data)[0]

if __name__=="__main__":
    print(detect("rf.model",[-1,-1,-1]))
    prePositiveData=loadData("positive.json")
    preNegativeData=loadData("negative.json")

    positiveData=myCleanForPositiveData(prePositiveData)
    #negativeData=myCleanForNegativeData(preNegativeData)
    negativeData=[
     [-10.738825146455268, -0.08824161280176258, -11.5028019968459],
     [-11.149560359021851, -0.10247022121333445, -11.691472160280279],
     [-11.511230536596868, -0.12367279764103589, -11.891024150622098],
     [-11.582730841977178, -0.11508935967391114, -11.756747259143298],
     [-12.764286663141544, -0.28842356794865526, -13.000088387132124],
     [-12.384944605989974, -0.17820560113778453, -12.250848741269667],
     [-13.0775328414269,-0.29476860652400283,-13.032405554845445],
     [-12.945518344707114, -0.24569346200492795, -12.724374551565585],
     [-13.184365982844586, -0.2897534149990168,-13.027084014637504],
     [-10.59715073753022,- 0.08154142319375215 ,-10.507115841697452]
    ]
    targetData=[]
    for i in range(0,len(positiveData)):
        targetData.append(0)
    for i in range(0,len(negativeData)):
        targetData.append(1)

    totalAttr=positiveData+negativeData

    rf = RandomForestRegressor()  # 这里使用了默认的参数设置
    rf.fit(totalAttr, targetData)  # 进行模型的训练

    joblib.dump(rf,'rf.model')

    print('instance 21 prediction；', rf.predict(positiveData[21]))
    print('instance 2 prediction；', rf.predict(negativeData[2]))

    print("done")

