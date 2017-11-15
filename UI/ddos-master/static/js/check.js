$(document).ready(function(){
  var Chart1 = echarts.init(document.getElementById('largeFlowSource'));
  var option1 = {
              title: {
                  text: '流量来源'
              },
              tooltip: {},
              legend: {
                  data:['销量']
              },

              series: [
                  {
                      itemStyle:{
                          normal:{
                              label:{
                                  show: true,
                                  formatter: '{b} : {d}%'
                              },
                              labelLine :{show:true}
                          }
                      },
                      name: '访问来源',
                      type: 'pie',
                      radius: '70%',
                      roseType: 'angle',
                      data:[
                          {value:'45', name:'北京'},
                          {value:'80', name:'上海'},
                          {value:'75', name:'广州'},
                          {value:'68', name:'成都'},
                          {value:'54', name:'重庆'}
                      ]
                  }
              ]
          };
        Chart1.setOption(option1);
  /*Chart1.setOption({
    series :
  });*/
  var Chart2 = echarts.init(document.getElementById('protocol'));
  var option2 = {
              title: {
                  text: '协议'
              },
              tooltip: {},
              series: [
                  {
                      itemStyle:{
                          normal:{
                              label:{
                                  show: true,
                                  formatter: '{b} : {d}%'
                              },
                              labelLine :{show:true}
                          }
                      },
                      name: '访问来源',
                      type: 'pie',
                      radius: '70%',
                      roseType: 'angle',
                      data:[
                          {value:190, name:'FTP'},
                          {value:55, name:'TCP'},
                          {value:88, name:'UDP'}
                      ]
                  }
              ]
          };
  Chart2.setOption(option2);

  var Chart3 = echarts.init(document.getElementById('target'));
  var option3 = {
              title: {
                  text: '目标'
              },
              tooltip: {},
              series: [
                  {
                      itemStyle:{
                          normal:{
                              label:{
                                  show: true,
                                  formatter: '{b} : {d}%'
                              },
                              labelLine :{show:true}
                          }
                      },
                      name: '访问来源',
                      type: 'pie',
                      radius: '70%',
                      roseType: 'angle',
                      data:[
                          {value:85, name:'主机1'},
                          {value:190, name:'主机2'}
                      ]
                  }
              ]
          };
  Chart3.setOption(option3);



})
