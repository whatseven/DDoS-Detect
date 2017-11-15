/**
 * Created by Menfer on 2017/11/14.
 */
(function(){
	// 基于准备好的dom，初始化echarts实例
	var myChart = echarts.init(document.getElementById('sidiChart'));
	var base = +new Date(2014, 9, 3);
	var oneDay = 24 * 3600 * 1000;
	var date = [];

	var data = [Math.random() * 150];
	var now = new Date(base);
	var pn;
	var i=0;

	function addData(shift,item) {
		//pn =[now.getMonth() + 1, now.getDate()].join(':');
		var d = new Date();
		pn = [d.getHours(),d.getMinutes(),d.getSeconds()].join(':');
		now = [now.getFullYear(), now.getMonth() + 1, now.getDate()].join('/');

		date.push(pn);
		//date.push(now);
		data.push(item);

		if (shift) {
			date.shift();
			data.shift();
		}

		now = new Date(+new Date(now) + oneDay);
	}

	for (var i = 1; i < 30; i++) {
		addData();
	}

	option = {
		xAxis: {
			type: 'category',
			boundaryGap: false,
			data: date
		},
		yAxis: {
			boundaryGap: [0, '50%'],
			type: 'value'
		},
		series: [
			{
				name:'成交',
				type:'line',
				smooth:true,
				symbol: 'none',
				stack: 'a',
				areaStyle: {
					normal: {}
				},
				data: data
			}
		]
	};
	setInterval(function () {
		var xmlhttp=new XMLHttpRequest();
		xmlhttp.onreadystatechange=function()
		{
			if (xmlhttp.readyState==4 && xmlhttp.status==200)
			{
				var size=JSON.parse(xmlhttp.responseText).size;
				size=parseFloat(size);
				//Todo
				addData(true,size);
				myChart.setOption({
					xAxis: {
						data: date
					},
					series: [{
						name:'成交',
						data: data
					}]
				});
			}
		}
		xmlhttp.open("GET","/sidi",true);
		xmlhttp.send();
	}, 2000);


	// 使用刚指定的配置项和数据显示图表。
	myChart.setOption(option);

})();

