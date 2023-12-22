//[Dashboard Javascript]

//Project:	SoftMaterial admin - Responsive Admin Template
//Primary use:   Used only for the main dashboard (index.html)

$(function () {

  'use strict';

  // Make the dashboard widgets sortable Using jquery UI
  $('.connectedSortable').sortable({
    placeholder         : 'sort-highlight',
    connectWith         : '.connectedSortable',
    handle              : '.box-header, .nav-tabs',
    forcePlaceholderSize: true,
    zIndex              : 999999
  });
  $('.connectedSortable .box-header, .connectedSortable .nav-tabs-custom').css('cursor', 'move');

// jQuery UI sortable for the todo list
  $('.todo-list').sortable({
    placeholder         : 'sort-highlight',
    handle              : '.handle',
    forcePlaceholderSize: true,
    zIndex              : 999999
  });	
	
/* The todo list plugin */
  $('.todo-list').todoList({
    onCheck  : function () {
      window.console.log($(this), 'The element has been checked');
    },
    onUnCheck: function () {
      window.console.log($(this), 'The element has been unchecked');
    }
  });	
	
// donut chart
  $('.donut').peity('donut');
		
// bar chart
  $(".bar").peity("bar");
	
	
// sparkline chart
	$("#linechart").sparkline([3,4,7,5,4,6,8,7,4,3,5,7,10], {
		type: 'line',
		width: '200',
		height: '38',
		lineColor: '#28D094',
		fillColor: '#ffffff',
		lineWidth: 5,
		minSpotColor: '#28D094',
		maxSpotColor: '#28D094',
	});
	$("#linechart2").sparkline([7,5,6,8,9,7,5,3,1,2,4,6,8], {
		type: 'line',
		width: '200',
		height: '38',
		lineColor: '#FF4961',
		fillColor: '#ffffff',
		lineWidth: 5,
		minSpotColor: '#FF4961',
		maxSpotColor: '#FF4961',
	});
	


$('#usa').vectorMap({
	map : 'us_aea_en',
	backgroundColor : 'transparent',
	regionStyle : {
		initial : {
			fill : '#1e88e5'
		}
	}
});


// AREA CHART
    if($('#morris_extra_line_chart').length > 0)
		Morris.Line({
        element: 'morris_extra_line_chart',
        data: [{
            period: '2010',
            direct: 50,
            referrals: 80,
            search: 90,
            social: 20
        }, {
            period: '2011',
            direct: 130,
            referrals: 100,
            search: 190,
            social: 80
        }, {
            period: '2012',
            direct: 80,
            referrals: 60,
            search: 90,
            social: 70
        }, {
            period: '2013',
            direct: 70,
            referrals: 200,
            search: 60,
            social: 140
        }, {
            period: '2014',
            direct: 180,
            referrals: 150,
            search: 80,
            social: 140
        }, {
            period: '2015',
            direct: 105,
            referrals: 100,
            search: 110,
            social: 80
        },
         {
            period: '2016',
            direct: 250,
            referrals: 150,
            search: 80,
            social: 200
        }],
        xkey: 'period',
        ykeys: ['direct', 'referrals', 'search', 'social'],
        labels: ['Direct', 'Referrals', 'Search', 'Social'],
        pointSize: 2,
        fillOpacity: 0,
		lineWidth:2,
		pointStrokeColors:['#666EE8', '#1E9FF2', '#FF4961', '#FF9149'],
		behaveLikeLine: true,
		grid: false,
		hideHover: 'auto',
		lineColors: ['#666EE8', '#1E9FF2', '#FF4961', '#FF9149'],
		resize: true,
		gridTextColor:'#878787',
		gridTextFamily:"Open Sans"
        
    });
	
//e CHART	
	if( $('#e_chart_1').length > 0 ){
		var eChart_1 = echarts.init(document.getElementById('e_chart_1'));
		function detectionData(str) {
		var color = '#666EE8';
		if (str >= 30 && str <= 60) {
			color = '#666EE8';
		} else if (str > 60) {
			color = '#666EE8';
		}
		return color;
		}
		var option4 = {
			"tooltip": {
				"formatter": "{a} <br/>{b} : {c}%"
			},
			"series": [{
				"name": "traffic",
				"type": "gauge",
				"splitNumber": 5,
				"axisLine": {
					"lineStyle": {
						"color": [
							[0.31, "#f4f4f4"],
							[1, "#f4f4f4"]
						],
						"width": 5
					}
				},
				"axisTick": {
					"show":false
				},
				"axisLabel": {
					"distance": -80,
					"textStyle": {
						"color": "#878787"
					}
				},
				"splitLine": {
					"show": false
				},
				"itemStyle": {
					"normal": {
						"color": "#666EE8"
					}
				},
				"detail": {
					"formatter": "{value}%",
					"offsetCenter": [0, "60%"],
					"textStyle": {
						"fontSize": 12,
						"color": "#878787"
					}
				},
				"title": {
					"offsetCenter": [0, "100%"]
				},
				"data": [{
					"name": "",
					"value": 31
					
				}]
			}]
		}
		var app = [];
		app.timeTicket = setInterval(function() {
			var value = (Math.random() * 100).toFixed(2) - 0;
			option4.series[0].data[0].value = value;
			option4.series[0].axisLine.lineStyle.color[0][0] = value / 100;
			option4.series[0].axisLine.lineStyle.color[0][1] = detectionData(value);
			eChart_1.setOption(option4, true);
		}, 500);

		eChart_1.setOption(option4);
		eChart_1.resize();
	}		
	
	
}); // End of use strict

