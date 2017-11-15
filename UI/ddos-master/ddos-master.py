import json
import pymysql
import requests

from flask import Flask, render_template
from flask import request
from flask import Flask, request,make_response,render_template, redirect, url_for
from sklearn.externals import joblib
from werkzeug.utils import secure_filename # 使用这个是为了确保filename是安全的
from os import path

dbuser = "root"
dbpassword = "root"
dbschema = "ddos"
dbhost = "localhost"
dbindex = 550
dbindexInDetect = 2080
protocalMap={"1.0":"ICMP","2.0":"IGMP","3.0":"GGP","6.0":"TCP","8.0":"EGP","17.0":"UDP","35.0":"IDPR","45.0":"IDRP"}
app = Flask(__name__)

def detect(filePath="./rf.model",data=[]):
    #data=[1,1,1]
    rf=joblib.load(filePath)
    result=rf.predict(data)[0]
    print(result)
    return result

#跳转至登录页
@app.route('/')
def hello_world():
    return render_template("login.html",loginStatus=0)

#跳转至用户状态页
@app.route('/profile')
def profile():
    return render_template("userProfit.html")

#跳转至状态界面
@app.route('/status')
def status():
    db = pymysql.connect(dbhost, dbuser, dbpassword, dbschema)
    cursor = db.cursor()
    sql = "select * from detect where id ='%s'"%(dbindexInDetect)
    n = cursor.execute(sql)
    inf = cursor.fetchone()
    cursor.close()
    db.close()
    aStatus = '未受攻击'
    #使用函数完成是否被攻击的检测
    base_path = path.abspath(path.dirname(__file__))
    upload_path = path.join(base_path, 'static/uploads/')
    file_name = upload_path + "rf.model"
    #ret = detect(r"C:\Users\whatseven\Desktop\project\DDOS\ddos-master\rf.model",[inf[1],inf[2],inf[3]])
    ret = detect(file_name,[inf[1],inf[2],inf[3]])
    if ret>0.5:
        aStatus = '受到攻击'
    return render_template("status.html",attackStatus = aStatus)

#跳转至外源DDOS界面
@app.route('/ddos')
def ddos():
    db = pymysql.connect(dbhost, dbuser, dbpassword, dbschema)
    cursor = db.cursor()
    sql = "select * from statics where id ='%s'"%(dbindex)
    n = cursor.execute(sql)
    inf = cursor.fetchone()
    cursor.close()
    db.close()
    temp=list(inf)
    temp[3]=protocalMap.get(temp[3])
    temp[5]=protocalMap.get(temp[5])
    temp[7]=protocalMap.get(temp[7])
    inf=tuple(temp)
    return render_template("analysis.html",information=inf)

#跳转至注册界面
@app.route('/toRegister')
def toRegister():
    return render_template("register.html",registerStatus = 0)

#跳转至登录界面
@app.route('/toLogin')
def toLogin():
    return render_template("login.html",loginStatus = 0)

#获取总览页的流量总和
@app.route('/thoughtput',methods=['GET'])
def thoughtput():
    db = pymysql.connect(dbhost, dbuser, dbpassword, dbschema)
    cursor = db.cursor()
    sql = "select sizeSum from statics where id ='%s'"%(dbindex)
    n = cursor.execute(sql)
    size = cursor.fetchone()
    cursor.close()
    db.close()
    return json.dumps({"size":size})

#获取总览页的包总和
@app.route('/numSum',methods=['GET'])
def numSum():
    db = pymysql.connect(dbhost, dbuser, dbpassword, dbschema)
    cursor = db.cursor()
    sql = "select numSum from statics where id ='%s'"%(dbindex)
    n = cursor.execute(sql)
    size = cursor.fetchone()
    cursor.close()
    db.close()
    return json.dumps({"size":size})

#获取状态页的dpdi
@app.route('/dpdi',methods=['GET'])
def dpdi():
    db = pymysql.connect(dbhost, dbuser, dbpassword, dbschema)
    cursor = db.cursor()
    sql = "select dpdi from detect where id ='%s'"%(dbindexInDetect)
    n = cursor.execute(sql)
    size = cursor.fetchone()
    cursor.close()
    db.close()
    return json.dumps({"size":size})

#获取状态页的sidi
@app.route('/sidi',methods=['GET'])
def sidi():
    db = pymysql.connect(dbhost, dbuser, dbpassword, dbschema)
    cursor = db.cursor()
    sql = "select sidi from detect where id ='%s'"%(dbindexInDetect)
    n = cursor.execute(sql)
    size = cursor.fetchone()
    cursor.close()
    db.close()
    return json.dumps({"size":size})

#获取状态页的sidp
@app.route('/sidp',methods=['GET'])
def sidp():
    db = pymysql.connect(dbhost, dbuser, dbpassword, dbschema)
    cursor = db.cursor()
    sql = "select sidp from detect where id ='%s'"%(dbindexInDetect)
    n = cursor.execute(sql)
    size = cursor.fetchone()
    cursor.close()
    db.close()
    return json.dumps({"size":size})

#登录
@app.route('/login',methods=['POST'])
def login():
    username = request.form['username']
    password = request.form['password']
    db = pymysql.connect(dbhost, dbuser, dbpassword, dbschema)
    cursor = db.cursor()
    sql = "select * from admin where username ='%s' AND password = '%s'"%(username,password)
    n = cursor.execute(sql)
    cursor.close()
    db.close()
    if n==0:
        return render_template("login.html", loginStatus=1)
    else:
        return render_template("thruput.html")

#注册
@app.route('/register',methods=['POST'])
def register():
    username = request.form['username']
    password = request.form['password']
    db = pymysql.connect(dbhost, dbuser, dbpassword, dbschema)
    cursor = db.cursor()
    sql = "select * from admin where username ='%s'"%(username)
    n = cursor.execute(sql)
    cursor.close()
    db.close()
    if n==1:
        return render_template("register.html", registerStatus=1)
    else:
        db2 = pymysql.connect(dbhost, dbuser, dbpassword, dbschema)
        cursor2 = db2.cursor()
        sql2 = """insert into admin(username,password) values('%s','%s')""" % (username, password)
        n2 = cursor2.execute(sql2)
        db2.commit()
        cursor2.close()
        db2.close()
        return render_template("thruput.html")

#接收文件
@app.route('/recvfile',methods=['POST'])
def recvfile():
    f=request.files['file']
    if f:
        base_path = path.abspath(path.dirname(__file__))
        upload_path = path.join(base_path, 'static/uploads/')
        file_name = upload_path + secure_filename(f.filename)
        f.save(file_name)
        return render_template("userProfit.html", upStatus=1)
    else:
        return render_template("userProfit.html", upStatus=0)

if __name__ == '__main__':
    app.run()
