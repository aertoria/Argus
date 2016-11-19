angular.module('argus.services.dashboard', [])
.service('DashboardService', ['$filter', '$compile', '$resource', 'CONFIG', 'VIEWELEMENT', 'Metrics', '$sce', '$http', 'Annotations', 'growl',
    function ($filter, $compile, $resource, CONFIG, VIEWELEMENT, Metrics, $sce, $http,Annotations,growl) {

        this.getDashboardById = function(dashboardId){
            return $http.get(CONFIG.wsUrl + 'dashboards/' + dashboardId);
        };

        this.populateView = function(metricList, annotationExpressionList, optionList, divId, attributes, elementType, scope){
            if(metricList && metricList.length>0 && divId) {
                if(elementType === VIEWELEMENT.avatreemap){
                    //OK, ONLY I USE THIS CONTROLS
                    growl.info('Routing to Heimdall Argus Core and  Running as fast as we can...');
                    var expressionList = getMetricExpressionList(metricList);
                    $('#' + divId).show();
                    updateAva({}, expressionList, divId, attributes, scope.controls);
                    return;//STOP RIGHT HERE
                }
                if (metricList && metricList.length > 0) {
                    if(elementType === VIEWELEMENT.chart){
                        populateChart(metricList, annotationExpressionList, optionList, divId, attributes, elementType, scope);
                    }else{
                        var metricExpressionList = getMetricExpressionList(metricList);
                        $http({
                            method: 'GET',
                            url: CONFIG.wsUrl + 'metrics',
                            params: {'expression': metricExpressionList}
                        }).success(function(data, status, headers, config){
                            if(data && data.length>0) {
                                //$('#' + divId).show();
                                //if(elementType === VIEWELEMENT.heatmap)
                                //    updateHeatmap({}, data, divId, optionList, attributes);
                                //else if(elementType === VIEWELEMENT.table)
                                //    updateTable(data, scope, divId, optionList);
                                $('#' + divId).show();
                                //.log("metricExpressionList:"+metricExpressionList.toSource()+"    annotationExpressionList:"+annotationExpressionList.toSource()+"    optionList:"+optionList.toSource()+"    divId:"+divId.toSource()+"     attributes:"+attributes.toSource()+"    elementType:"+elementType.toSource());
                                if(elementType === VIEWELEMENT.chart)
                                    updateChart({}, data, divId, annotationExpressionList, optionList, attributes);
                                else if(elementType === VIEWELEMENT.heatmap)
                                    updateHeatmap({}, data, divId, optionList, attributes);
                                //TOsplit
                                else if(elementType === VIEWELEMENT.treemap)
                                    updateTreemap({}, data, divId, optionList, attributes);
                                //updateAvaTreemap({}, data, divId, optionList, attributes);
                                else if(elementType === VIEWELEMENT.table)
                                    updateTable(data, scope, divId, optionList);
                            } else {
                                updateChart({}, data, divId, annotationExpressionList, optionList, attributes);
                                growl.info('No data found for the metric expressions: ' + JSON.stringify(metricExpressionList));

                            }
                        }).error(function(data, status, headers, config) {
                            growl.error(data.message);
                            $('#' + divId).hide();
                        });
                    }
                } else {
                    growl.error('Valid metric expressions are required to display the chart/table.');
                    $('#' + divId).hide();
                }
            }
        };
        ////LEGACY CODESTART NOW


        //GRAPHITE DATA SERVICE
        //Construct all parameter part
        var doControlParser=function(controlJSON){
            paraJSON={};
            for(var controlIndex in controlJSON) {
                var controlName = controlJSON[controlIndex].name;
                var controlValue = controlJSON[controlIndex].value;
                paraJSON[controlName] = controlValue;
            }
            return paraJSON;
        };

        //JSONProcessor will convert json to highchart format. Only used by total availablity
        var JSONProcessor=function(JSON){
            X_cat=extractCategory("Timemark",JSON);
            Y_cat=extractCategory("Racnode",JSON);
            returnJSON=[];
            for (var index in JSON){
                returnItem={};
                returnItem.value=parseInt(JSON[index]["All_RATE_BK"]);
                returnItem.valueLabel=returnItem.value+"%";
                if (returnItem.value<0){
                    returnItem.valueLabel="NaN";
                }
                returnItem.x=X_cat.indexOf(JSON[index]["Timemark"]);
                returnItem.y=Y_cat.indexOf(JSON[index]["Racnode"]);
                returnItem.AvgAPT=parseFloat(JSON[index]["AvgAPT"]);
                returnItem.AvgActSession=parseFloat(JSON[index]["AvgActSession"]);
                returnItem.ImpMin=parseFloat(JSON[index]["ImpMin"]);
                returnItem.CollectedMin=parseFloat(JSON[index]["CollectedMin"]);
                returnJSON.push(returnItem);
            }
            //console.log(returnJSON.toSource());
            return returnJSON;
        };

        //JSONProcessor will convert json to highchart format.
        var JSONProcessorGeneric=function(JSON,valuetag){
            X_cat=extractCategory("Timemark",JSON);
            Y_cat=extractCategory("Racnode",JSON);
            returnJSON=[];
            for (var index in JSON){
                returnItem={};
                returnItem.value=parseInt(JSON[index][valuetag]);
                returnItem.x=X_cat.indexOf(JSON[index]["Timemark"]);
                returnItem.y=Y_cat.indexOf(JSON[index]["Racnode"]);
                returnItem.AvgAPT=parseFloat(JSON[index]["AvgAPT"]);
                returnItem.AvgActSession=parseFloat(JSON[index]["AvgActSession"]);
                returnItem.ImpMin=parseFloat(JSON[index]["ImpMin"]);
                returnItem.CollectedMin=parseFloat(JSON[index]["CollectedMin"]);
                returnItem.Spare=parseFloat(JSON[index]["Spare"]);
                returnItem.TrafficValue=parseFloat(JSON[index]["TrafficValue"]);
                returnItem.CPUValue=parseFloat(JSON[index]["CPUValue"]);
                returnJSON.push(returnItem);
            }
            //console.log(returnJSON.toSource());
            return returnJSON;
        };

        //extractX extracts the x categories from JSON.
        var extractCategory=function(mark,JSON){
            var categories=[];
            for(var index in JSON){
                var category=JSON[index][mark];
                if(!categories.includes(category)){
                    categories.push(category)
                }
            }
            return categories;
        };

        //parse date format from 2012-10-23.2:4 to 10-23 02:04
        var doFormatDate=function(X_CAT){
            function padLeft(nr, n, str){
                return Array(n-String(nr).length+1).join(str||'0')+nr;
            }
            var X_CAT_OUTPUT=[];
            for(var index in X_CAT){
                var dateItem=X_CAT[index];
                var MONTH=dateItem.split("-")[1];
                var DAY=dateItem.split("-")[2].split(".")[0];

                if (dateItem.split("-")[2].split(".")[1].split(":").length==2){
                    var HOUR=dateItem.split("-")[2].split(".")[1].split(":")[0];
                    var MINUTE=dateItem.split("-")[2].split(".")[1].split(":")[1];
                    HOUR=padLeft(HOUR,2);
                    MINUTE=padLeft(MINUTE,2);
                    var axis=MONTH+'-'+DAY+' '+HOUR+':'+MINUTE;
                    X_CAT_OUTPUT.push(axis);
                }else if(dateItem.split("-")[2].split(".")[1].split(":").length==1){
                    var HOUR=dateItem.split("-")[2].split(".")[1];
                    HOUR=padLeft(HOUR,2);
                    var axis=MONTH+'-'+DAY+' '+HOUR;
                    X_CAT_OUTPUT.push(axis);
                }
            }
            return X_CAT_OUTPUT;
        };

        //parse date format from 2012-10-23.2:4 to 10-23 02:04
        var doFormatDateTimeFromUNIX=function(X_CAT){
            ////convert unix time to date.time format such as 2013-12-03.13
            var convertEpochToMin=function(utcSeconds){
                var d = new Date(0); // The 0 there is the key, which sets the date to the epoch
                d.setUTCSeconds(utcSeconds/1000);
                return (d.toISOString().slice(0,10)+"."+ d.getUTCHours()+":"+d.getUTCMinutes());
            };

            function padLeft(nr, n, str){
                return Array(n-String(nr).length+1).join(str||'0')+nr;
            }
            var X_CAT_OUTPUT=[];
            for(var index in X_CAT){
                var dateItem=convertEpochToMin(X_CAT[index]);
                var MONTH=dateItem.split("-")[1];
                var DAY=dateItem.split("-")[2].split(".")[0];
                if (dateItem.split("-")[2].split(".")[1].split(":").length==2){
                    var HOUR=dateItem.split("-")[2].split(".")[1].split(":")[0];
                    var MINUTE=dateItem.split("-")[2].split(".")[1].split(":")[1];
                    HOUR=padLeft(HOUR,2);
                    MINUTE=padLeft(MINUTE,2);
                    var axis=MONTH+'-'+DAY+' '+HOUR+':'+MINUTE;
                    X_CAT_OUTPUT.push(axis);
                }else if(dateItem.split("-")[2].split(".")[1].split(":").length==1){
                    var HOUR=dateItem.split("-")[2].split(".")[1];
                    HOUR=padLeft(HOUR,2);
                    var axis=MONTH+'-'+DAY+' '+HOUR;
                    X_CAT_OUTPUT.push(axis);
                }
            }
            return X_CAT_OUTPUT;
        };

        //parse date format from 2012-10-23.2:4 to 10-23 02:04
        var doFormatDateFromUNIX=function(X_CAT){
            ////convert unix time to date.time format such as 2013-12-03.13
            var convertEpochToMin=function(utcSeconds){
                var d = new Date(0); // The 0 there is the key, which sets the date to the epoch
                d.setUTCSeconds(utcSeconds/1000);
                return (d.toISOString().slice(0,10)+"."+ d.getUTCHours());
            };

            function padLeft(nr, n, str){
                return Array(n-String(nr).length+1).join(str||'0')+nr;
            }
            var X_CAT_OUTPUT=[];
            for(var index in X_CAT){
                var dateItem=convertEpochToMin(X_CAT[index]);
                var MONTH=dateItem.split("-")[1];
                var DAY=dateItem.split("-")[2].split(".")[0];
                if (dateItem.split("-")[2].split(".")[1].split(":").length==2){
                    var HOUR=dateItem.split("-")[2].split(".")[1].split(":")[0];
                    var MINUTE=dateItem.split("-")[2].split(".")[1].split(":")[1];
                    HOUR=padLeft(HOUR,2);
                    MINUTE=padLeft(MINUTE,2);
                    var axis=MONTH+'-'+DAY+' '+HOUR+':'+MINUTE;
                    X_CAT_OUTPUT.push(axis);
                }else if(dateItem.split("-")[2].split(".")[1].split(":").length==1){
                    var HOUR=dateItem.split("-")[2].split(".")[1];
                    HOUR=padLeft(HOUR,2);
                    var axis=MONTH+'-'+DAY+' '+HOUR;
                    X_CAT_OUTPUT.push(axis);
                }
            }
            return X_CAT_OUTPUT;
        };

        //parse the expression from # to %23
        var parseExpression=function(expression){
            //return expression.replace("#","%23");
            return expression.split("#").join("%23");
        };

        //Dataguard
        /*
         * aruikar@salesforce.com
         * @param: integer
         * format date for timeseries Highcharts
         * @returns: formatted date/time string
         */
        var formateDate = function (value) {
            if(value < 10)
            {
                return '0' + value;
            }
            else {
                return value;
            }
        };

        /*
         * aruikar@salesforce.com
         * @param: divId from angular, container for HighCharts, chart Data array of [Timestamp, value], title for chart
         * helper function to create Time series charts for all 4 types of charts
         * @returns: none
         */
        var drawPodDGlagTimeseries = function(AngularDiv, contianerDiv, chartData, titleStr, thrLine) {
            // add container div, then apply Highcharts
            $('#'+AngularDiv).append('<div id="'+contianerDiv+'"></div>');

            // render timeseries chart
            $('#' + contianerDiv).highcharts({
                chart: {
                    zoomType: 'x'
                },
                title: {
                    text: 'Data-guard lag over time ' + titleStr
                },
                subtitle: {
                    text: document.ontouchstart === undefined ?
                        'Click and drag in the plot area to zoom in' : 'Pinch the chart to zoom in'
                },
                xAxis: {
                    type: 'datetime'
                },
                yAxis: {
                    title: {
                        text: 'data-guard lag(sec)'
                    },
                    plotLines: [{
                        color: 'red',
                        value: thrLine,
                        width: 2
                    }]
                },
                legend: {
                    enabled: false
                },
                plotOptions: {
                    area: {
                        fillColor: {
                            linearGradient: {
                                x1: 0,
                                y1: 0,
                                x2: 0,
                                y2: 1
                            },
                            stops: [
                                [0, '#FF0000'],
                                [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                            ]
                        },
                        marker: {
                            radius: 2
                        },
                        lineWidth: 1,
                        states: {
                            hover: {
                                lineWidth: 1
                            }
                        },
                        threshold: null
                    },

                },
                tooltip:{
                    formatter: function () {

                        var t = new Date(this.x);
                        return formateDate(t.getUTCMonth()+1)+"/"+ formateDate(t.getUTCDate())+"/"+ t.getFullYear()+", " +
                            ""+formateDate(t.getUTCHours())+":"+ formateDate(t.getUTCMinutes())+"<br/>"+titleStr+"<b> "+ this.y + "</b><i>sec</i>";
                    }
                },
                series: [{
                    type: 'area',
                    name: 'dg lag',
                    data: chartData
                }]
            });


        };

        //sort the data-points for max lag
        var sortMaxLagDataPoints = function (podArray) {

            podArray.sort(function(a, b) {
                return parseInt(a.lagValue) - parseInt(b.lagValue);
            });
            return  podArray;

        };

        /*
         * aruikar@salesforce.com
         * @param: json data, pod Category(na, cs), pods to exclude from calculations(IGL)
         * helper function to create string of missing pods information.
         * @returns: JSON objects - podName
         */
        var getMissingInfo = function(missingInfoList, podCat, IGLpodList){

            var missingInfo = [];

            for(var pod in missingInfoList){

                if(IGLpodList.indexOf(missingInfoList[pod])  != -1 ){
                    continue;
                }
                if(missingInfoList[pod].indexOf(podCat) == -1){
                    continue;
                }

                missingInfo.push(missingInfoList[pod]);

            }

            return  missingInfo;
        };

        /*
         * aruikar@salesforce.com
         * @param: json data, pod Category(na, cs), pods to exclude from calculations(IGL)
         * helper function to create table entry for DGoWAN pods(remote Tr, remote apply)
         * DGoWAN pods - pods which have a DR site(leading edge pods)
         * @returns: JSON for Compliance percentage, pods not met and missing information
         */
        var DGoWANSLA = function (data, podCat,IGLpodList, SLApc) {

            var totalCount = 0;
            var complyCount = 0;
            var confidence = 0;
            var defaulterInfo = [];
            var podList = data['data'];

            for(var pod in podList){

                if(IGLpodList.indexOf(podList[pod]['podName'])  != -1 ){
                    continue;
                }
                if(podList[pod]['podName'].indexOf(podCat) == -1){
                    continue;
                }

                totalCount+=1;
                confidence += podList[pod]['conf'];
                if(podList[pod]['compliance'] >= SLApc){
                    complyCount += 1;
                }
                else{
                    defaulterInfo.push({'podName': podList[pod]['podName'], 'pctCompliance':podList[pod]['compliance']});
                }
            }

            var processedJson = {};
            processedJson['count'] = totalCount;
            processedJson['complyCount'] = complyCount;
            processedJson['conf'] = confidence;
            processedJson['defaulterInfo'] = defaulterInfo;
            processedJson['missingInfo'] = getMissingInfo(data['missingInfo'], podCat, IGLpodList);
            return processedJson;

        };

        var lagSpecificList = function(data, lagType){

            var dgObj = {};
            var objectsIn = {};

            for(var metric in data){

                if(lagType === data[metric]['metric'].split(".")[1]){

                    var pod = data[metric]['metric'].split(".")[0];
                    var podType = pod.substring(0,2);
                    var obj = {};

                    obj['pod'] = pod;
                    obj['compliance'] = data[metric]['datapoints']['0'];
                    obj['conf'] = data[metric]['datapoints']['1'];

                    if(dgObj[podType]){

                        if(objectsIn[pod] == 1){
                            //already filled
                        }
                        else{
                            dgObj[podType].push(obj);
                            objectsIn[pod] = 1;
                        }
                    }
                    else{
                        var podarr = [];
                        podarr.push(obj);
                        objectsIn[pod] = 1;
                        dgObj[podType] = podarr;

                    }
                }

            }

            return  dgObj;

        };

        var countPods = function(data, SLApc){
            var podsComplied = 0;
            var totalPods = 0;


            for(var metric in data){

                if(data[metric]['conf'] == 0.00){
                    continue;
                }
                if(data[metric]['compliance'] >= SLApc){
                    podsComplied++;
                }
                totalPods++;
            }


            return  {'podsComplied': podsComplied, 'totalPods':totalPods};

        };

        /**
         * @Author ethan.wang@salesforce.com
         * THis is the starting point of ever update AVA directives
         * @param config
         * @param metricList
         * @param divId
         * @param attributes
         * @param para
         */
        function updateAva(config, metricList, divId, attributes, para){
            $('#'+divId).html('<img src="img/spin.gif" />');
            //url(../img/spin.gif)
            var expression=parseExpression(metricList[0]);
            var type = attributes["type"];

            //obsolete
            var paraJSON=doControlParser(para);
            var start=paraJSON["start"];
            var end=paraJSON["end"];
            var pod=paraJSON["pod"];

            var dgLagThreshold=paraJSON["threshold"];
            var IGLstr = paraJSON['IGLlist'];
            var SLApc = paraJSON['SLAtime'];
            var prodThreshold = paraJSON['prodThr'];
            var csThreshold = paraJSON['csThr'];
            //var thresholdLine = paraJSON['thrLine'];
            //var podDRstr = paraJSON['DRpods'];
            //var CSDRstr = paraJSON['CSpods'];
            //if (paraJSON["threasholdConfig"]==-1)
            //    {var Cfg=300100;}
            //    else if(paraJSON["threasholdConfig"]==1)
            //    {var Cfg=500150;}
            if(type == "ava"){
                //drawHeatmap();
            }else if(type == "ava-high-resolution"){
                //drawHeatmapHighResolutionAPT();
            }else if(type == "ava-high-resolution-ACT"){
                //drawHeatmapHighResolutionACT();
            }else if(type == "report"){
                //drawReport();
            }else if(type == "ava-HD"){
                //drawHeatmapHDAPT();
            }else if(type == "ava-HD-ACT"){
                //drawHeatmapHDACT();
            }else if(type == "ava-TTM"){
                //drawTTM();
            }else if(type == 'SLAtable'){
                drawDGoWANSLAtable();
            }else if(type == 'ava-dg-Lag-pod'){
                drawPodTimeseries();
            }else if(type == 'max-lag'){
                drawMaxLagReport();
            }else if(type == 'argus+POD'){
                drawArgusPlusPOD();
            }else if(type == 'argus+DETAIL'){
                drawArgusPlusHDDETAIL();
            }else if(type == 'argus+AVA'){
                drawArgusPlusHDAVA();
            }else if(type == 'argus+REPORT'){
                drawArgusPlusReport();
            }else if(type == 'argus+DGLag'){
                drawDGLagSLA();
            }else if(type == 'argus+SFDC'){
                drawArgusPlusRollUpTreemap();
            }else{
                growl.error("This heimdall component type is not defined");
            }


            /**
             *  //Used by rollup map
             *
             */
            function drawArgusPlusRollUpTreemap(){
                /**
                 * conver from 1477094400:1477180800:REDUCEDTEST.core.CHI.*:IMPACTPOD:avg
                 * to JOIN(1477094400:1477180800:REDUCEDTEST.core.CHI.A:IMPACTPOD:avg,1477094400:1477180800:REDUCEDTEST.core.CHI.B:IMPACTPOD:avg)
                 * @param template
                 */
                var localExpressionMapper = function(template){
                    var expressions=[]
                    var listOfDC=['CHI','WAS','PHX','DFW','FRF','LON','PAR','TYO'];
                    for(var idx in listOfDC){
                        dc = listOfDC[idx];
                        expressions.push(template.replace('*',dc+'.*'));
                    }
                    var joinExpression='JOIN('+expressions.join(',')+')';
                    return joinExpression;
                };

                /**
                 *
                 * @param inputmap
                 * @returns {*}
                 */
                var getOnlyValueFromHashMap=function(inputmap){
                    var firstValue;
                    for (var key in inputmap){firstValue=inputmap[key];}
                    return firstValue;
                };

                /**
                 * helper for align up two list of metrics
                 * @param input1
                 * @param input2
                 * @returns {Object}
                 */
                var alignUpByScope=function(input1, input2, input3){
                    var resultMap=new Object();//Map of list of three numbers: impacted Min, traffic, collectedMin, ava
                    for(var idx in input1){
                        key = input1[idx]['scope'];
                        impactMin = getOnlyValueFromHashMap(input1[idx]['datapoints']);
                        resultMap[key]=[impactMin];
                    }

                    for(var idx in input3){
                        key = input3[idx]['scope'];
                        traffic = getOnlyValueFromHashMap(input3[idx]['datapoints']);
                        if(key in resultMap){
                            resultMap[key].push(traffic);
                        }
                    }
                    for(var idx in input2){
                        key = input2[idx]['scope'];
                        collectedMin = getOnlyValueFromHashMap(input2[idx]['datapoints']);
                        resultMap[key].push(collectedMin);
                        if (collectedMin&&collectedMin>0){
                            ava=1 - (parseFloat(resultMap[key][0]) / parseFloat(collectedMin));
                            resultMap[key].push(ava);
                        }
                    }
                    return resultMap;//FORMAT   K:V   CHI.SP2.cs15:  [impactmin, traffic, collectedMin, ava]
                };


                var expression=parseExpression(metricList[0]);
                //assert that input two metricList for this transform
                var expression2=parseExpression(metricList[1]);
                var expression3=parseExpression(metricList[2]);

                var URL1=CONFIG.wsUrl+"metrics?expression="+localExpressionMapper(expression);
                var URL2=CONFIG.wsUrl+"metrics?expression="+localExpressionMapper(expression2);
                var URL3=CONFIG.wsUrl+"metrics?expression="+localExpressionMapper(expression3);
                $.when($.getJSON(URL1), $.getJSON(URL2), $.getJSON(URL3))
                    .done(function(raw1,raw2, raw3){
                        avaMap=alignUpByScope(raw1[0],raw2[0],raw3[0]);
                        renderChart(avaMap);
                    });



                Highcharts.seriesTypes.treemap.prototype.getExtremesFromAll = true;
                /**
                 * Render a roll up tree map from rawdata
                 * @param rawdata
                 */
                var renderChart=function(map){
                    datainput=[];
                    for(var key in map){
                        var scope=key;
                        if(map[key].length<3){continue;}

                        var impactValue=parseFloat(map[key][0]);
                        var trafficValue=parseFloat(map[key][1]);
                        var collectedValue=parseFloat(map[key][2]);
                        var ava=parseFloat(map[key][3]);

                        var avaText=(ava*100).toFixed(2)+"%";
                        var impactMinText=impactValue+'mins';

                        var avaValue=ava*100;
                        //scope example:  REDUCED.db.SANDBOX.WAS.SP2.na12
                        var podname=scope.split('.')[3]+'.'+scope.split('.')[4]+'.'+scope.split('.')[5];
                        var currentPod={
                            name: '<h2>'+podname+'<br>'+avaText + '</h2>',
                            value: trafficValue,//for size
                            colorValue: avaValue,//for color
                            podAddress: podname,
                            impactedValue: impactValue,
                            trafficValue: trafficValue,
                            collectedValue: collectedValue,
                        };
                        datainput.push(currentPod);
                    }
                    //goal:
                    //every item{
                    //colorValue:157
                    //name:"CHI.SP3cs25<br> IMPACT TIME:157.0"
                    //value:157}


                    //Caculate overall:
                    impactedMin=0;
                    collectedMin=0;
                    traffic=0;
                    for(var idx in datainput){
                        var currentPod = datainput[idx];
                        impactedMin+=currentPod['impactedValue'];
                        collectedMin+=currentPod['collectedValue'];
                    }
                    var totalAva=100*(1-impactedMin/collectedMin);
                    var totalImpacted=impactedMin;
                    var totalCollected=collectedMin;

                    $('#'+divId).highcharts({
                        chart: {
                            height: 1100
                        },
                        xAxis: {
                            events: {
                                setExtremes: function (e) {
                                    if(e.max == 100 && e.min == 0){
                                        this.series[0].levelMap[2].dataLabels.format = "{point.name}<br/><span style='font-size: 10px'><span style='font-size: 17px'>{point.colorValue}s</span></span>";
                                    }
                                },
                            }
                        },
                        colorAxis: {
                            dataClassColor: 'category',
                            dataClasses: [{
                                to: 89,
                                from: 0,
                                color:'#B90009'
                            },{
                                to: 90,
                                from: 89,
                                color:'#C5221A'
                            },{
                                to:91,
                                from: 90,
                                color:'#D23B2B'
                            },{
                                to: 92,
                                from: 91,
                                color:'#D23B2B'
                            },{
                                to:93,
                                from:92,
                                color:'#F98375'
                            },{
                                to:94,
                                from:93,
                                color:'#F3B3A2'
                            },{
                                to:95,
                                from:94,
                                color:'#9FD2A1'
                            },{
                                to:96,
                                from:95,
                                color:'#9DD56F'
                            },{
                                to:97,
                                from:96,
                                color:'#85C462'
                            },{
                                to:98,
                                from:97,
                                color:'#74B35A'
                            },{
                                to:99,
                                from:98,
                                color:'#62A247'
                            },{
                                to:100,
                                from:99,
                                color:'#4E8E1C'
                            },{
                                to:-1,
                                from:-10,
                                color:'#6E6E6E'
                            }]
                        },
                        tooltip: {
                            backgroundColor: 'yellow',
                            formatter: function () {
                                return '<h2>'+this.point.podAddress+'<br>Total DB Availablity:'+avaText + '</h2>' +
                                    '<br>Impacted Min: ' + this.point.impactedValue + 'mins<br>' +
                                    '<br>(Out of)Collected Min: ' + this.point.collectedValue + 'mins<br>' +
                                    '<br>Traffic: '+(this.point.trafficValue).toFixed(0)+'<br> click to see detail';
                            }
                        },
                        series: [{
                            type: "treemap",
                            drillUpButton:{
                                relativeTo: 'spacingBox',
                                position: {
                                    y: 0,
                                    x: 0
                                },
                                theme: {
                                    color: 'white',
                                    fill: 'rgb(66, 180, 240)',
                                    'stroke-width': 1,
                                    stroke: 'black',
                                    r: 3,
                                    states: {
                                        hover: {
                                            fill: 'rgb(66, 139, 202)'
                                        },
                                        select: {
                                            stroke: '#039',
                                            fill: 'rgb(66, 139, 220)'
                                        }
                                    }
                                }

                            },
                            levels: [{
                                level: 1,
                                layoutAlgorithm: 'squarified',
                                borderRadius: 100,
                                borderColor: 'black',
                                borderWidth: 3,

                            }, {
                                level: 2,
                                layoutAlgorithm: 'squarified',
                                borderRadius: 100,
                                borderColor: '#e0e0e0',
                                borderWidth: 2,

                                dataLabels: {
                                    align: 'left',
                                    verticalAlign: 'top',
                                    rotation: 0,
                                    padding:  5,
                                    useHTML: true,
                                    enabled: true,
                                    format: "{point.name}<br/><span style='font-size: 10px'><span style='font-size: 17px'>{point.colorValue}s</span></span>",
                                    style: {
                                        fontSize: "14px",
                                        color: 'contrast',
                                        textShadow: 'underline',
                                    }
                                }
                            }],
                            inside: true,
                            events:{
                                click: regionClick
                            },
                            allowDrillToNode: true,
                            data: datainput,
                        }],
                        title: {
                            text: '<br><h1><span id="helpBlock" class="help-block">Heimdall Rollup Dashboard Beta ' +
                            '<b>DBCloud availblity: '+(totalAva).toFixed(4)+'% </b>' +
                            '   Total impacted: '+ (totalImpacted).toFixed(0)+' mins, out of '+ (totalCollected).toFixed(0)+' mins<br>' +
                            '<br>For each pod: Color represents availablity. Size represents load.',
                        }
                    });
                };


            }//end dg-lag treemap

            //test
            var regionClick = function(event){
                $('#'+divId).html('<img src="img/spin.gif" />');
                expression='HEIMDALL(-4d:core.'+event.point.podAddress+':SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, -4d:core.'+event.point.podAddress+':SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg,%23RACHOUR%23)';

                var URL=CONFIG.wsUrl+"metrics?expression="+expression;

                console.log('start'+start);
                console.log('end'+end);
                console.log(URL);
                $.getJSON(URL, function(rawdata) {
                    readydata=adaptArgusPlusRenderRACLevelHour(rawdata);
                    X_cat=extractCategory("ts",readydata);
                    Y_cat=extractCategory("Racnode",readydata);
                    returnJSON=[];
                    for (var index in readydata){
                        returnItem={};
                        returnItem.value=parseInt(readydata[index]["value"]);
                        returnItem.x=X_cat.indexOf(readydata[index]["ts"]);
                        returnItem.y=Y_cat.indexOf(readydata[index]["Racnode"]);
                        returnItem.APT=parseInt(readydata[index]["APT"]);
                        returnItem.ImpactedMin=parseInt(readydata[index]["ImpactedMin"]);
                        returnItem.CollectedMin=parseInt(readydata[index]["CollectedMin"]);
                        returnItem.ACT=parseInt(readydata[index]["ACT"]);
                        returnItem.CPU=parseInt(readydata[index]["CPU"]);
                        returnItem.Traffic=parseInt(readydata[index]["Traffic"]);
                        returnItem.valueLabel=returnItem.value+"%";
                        returnJSON.push(returnItem);
                    }

                    $('#' + divId).highcharts({
                        chart: {
                            type: 'heatmap',
                            marginTop: 40,
                            marginBottom: 80,
                            plotBorderWidth: 1
                        },

                        labels:{
                            style: {
                                fontFamily: 'monospace',
                                color: "#f00"
                            }
                        },

                        plotOptions: {
                            series: {
                                borderWidth: 1,
                                borderColor: 'white',
                                turboThreshold: 1000000,
                                animation: {
                                    duration: 200
                                },
                                point: {
                                    events: {
                                        click: function(e){//console.log(e);
                                        }
                                    }
                                }
                            },
                        },

                        title: {
                            text: '',
                        },

                        subtitle: {
                            text: '<span id="helpBlock" class="help-block">Heimdall DB Availblity: Percentage of availblity for each node over hourly resolution' +
                            ' (available is caculated based on weighted apt and act Green=highly available)</span>',
                            useHTML: true
                        },

                        xAxis: {
                            categories: doFormatDateFromUNIX(X_cat),
                            max: Math.min(20,X_cat.length-1)
                        },

                        scrollbar: {
                            enabled: true
                        },

                        yAxis: {
                            categories: Y_cat,
                            title: null,
                            reversed: true
                        },

                        colorAxis: {
                            dataClassColor: 'category',
                            dataClasses: [{
                                from: 0,
                                to: 10,
                                color:'#B90009'
                            },{
                                from: 10,
                                to: 20,
                                color:'#C5221A'
                            },{
                                from: 20,
                                to: 30,
                                color:'#D23B2B'
                            },{
                                from: 30,
                                to: 40,
                                color:'#D23B2B'
                            },{
                                from:40,
                                to:50,
                                color:'#F98375'
                            },{
                                from:50,
                                to:60,
                                color:'#F3B3A2'
                            },{
                                from:60,
                                to:65,
                                color:'#9FD2A1'
                            },{
                                from:65,
                                to:75,
                                color:'#9DD56F'
                            },{
                                from:75,
                                to:85,
                                color:'#85C462'
                            },{
                                from:85,
                                to:90,
                                color:'#74B35A'
                            },{
                                from:90,
                                to:95,
                                color:'#62A247'
                            },{
                                from:95,
                                color:'#4E8E1C'
                            },{
                                to:-0.01,
                                from:-999999,
                                color:'#6E6E6E'
                            }]
                        },

                        legend: {
                            enabled: false,
                            align: 'right',
                            layout: 'vertical',
                            margin: 0,
                            verticalAlign: 'top',
                            y: 24,
                            //symbolHeight: 280
                        },

                        tooltip: {
                            formatter: function () {
                                return 'Time: ' + this.series.xAxis.categories[this.point.x] +
                                    '<br>RacNode: ' + this.series.yAxis.categories[this.point.y] +
                                    '<br><br>DB Availablity:  <b>' + this.point.value + '%' +
                                    '<br>ImpactedMin:  <b>' + this.point.ImpactedMin + ' min' +
                                    '<br>(out-of)MonitoredMin:  <b>' + this.point.CollectedMin + ' min' +
                                    '<br>weighted APT:  <b>' + this.point.APT + 'ms' +
                                    '<br>weighted ACT:  <b>' + this.point.ACT + '' +
                                    '<br>weighted CPU:  <b>' + this.point.CPU + '' +
                                    '<br>Total Traffic: <b>' + this.point.Traffic + '' +
                                    '';
                            }
                        },

                        series: [{
                            //threshold: 1,
                            //borderWidth: 1,
                            dataLabels: {
                                enabled: true,
                                color: '#ffffff',
                                format: '{point.valueLabel}',
                                shadow: false,
                                style: {
                                    fontWeight: 'bold',
                                    fontSize: "10px",
                                    "textShadow": "0 0 0px contrast, 0 0 0px contrast"
                                }
                            },
                            data: returnJSON,
                        }],

                    });
                }).error(function(jqXHR, textStatus, errorThrown) {
                    errorHandle(jqXHR, textStatus, errorThrown);
                });
                //this.levelMap[2].dataLabels.format = "<span style='font-size: 18px'>{point.nameChars}{point.nameNumbers}<br/>" +
                //    "<span style='font-size: 12px'><i>local apply dg</i>lag:</span>" +
                //    "<span style='font-size: 40px'>{point.colorValue}</span>" +
                //    "<span style='font-size: 10px'><i>s</i></span></span>";
                //drawDgLagTimeseries(x.point.name);
            };




            //Used by adaptArgusPlusRenderPOD
            var getMetricFromMetrics=function(rawdata,metricName){
                for (var idx in rawdata){
                    current=rawdata[idx];
                    if (current['metric']==metricName){
                        return current;
                    }
                }
                throw "This metricName you provided has not been found yet"+metricName;
            };

            //Used by adaptArgusPlusRenderPOD
            var getDatapointsFromScope=function(rawdata,scopeName){
                for (var idx in rawdata){
                    current=rawdata[idx];
                    if (current['scope']==scopeName){
                        return current["datapoints"];
                    }
                }
                //throw "This metricName you provided has not been found yet "+scopeName;
            };

            //Used by adaptArgusPlusRenderPOD
            var getMetricsFromRac=function(rawdata,racName){
                var listOfMetric=[];
                for (var idx in rawdata){
                    current=rawdata[idx];
                    if (current['metric']==racName){
                        listOfMetric.push(current);
                    }
                }
                return listOfMetric;
            };

            //Used by adaptArgusPlusRenderPOD
            var getFirstItemInMetrics=function(hashmap){
                for(var idx in hashmap){
                    return hashmap[idx];
                }
                throw "this metrics is emtpy";
            };

            //used for process time to GMT format
            function processGMTTime(timeString){
                timeStringGMT=timeString+' GMT';
                dateGMT = Date.parse(timeStringGMT);
                if(isNaN(dateGMT)){
                    return timeString;
                }
                return dateGMT;
            };

            //Adaptor used for Pod Level
            var adaptArgusPlusRenderPOD=function(rawdata){
                var returnData=[];
                var PodLevelAPT=getMetricFromMetrics(rawdata,'PodLevelAPT');
                var ImpactedMin=getMetricFromMetrics(rawdata,'ImpactedMin');
                var Availability=getMetricFromMetrics(rawdata,'Availability');
                var TTM=getMetricFromMetrics(rawdata,'TTM');
                //Assert len of above four should equal and align up
                for(ts in PodLevelAPT["datapoints"]){
                    current={};
                    //current["Timemark"]=convertEpoch(ts);
                    current["ts"]=ts;
                    current["PodName"]="this.pod";
                    current["PodLevelAPT"]=PodLevelAPT["datapoints"][ts];
                    current["ImpactedMin"]=ImpactedMin["datapoints"][ts];
                    current["Availability"]=Availability["datapoints"][ts];
                    current["TTM"]=TTM["datapoints"][ts];
                    returnData.push(current);
                }
                return returnData;
            };

            //Adaptor used for Rac Level, Min Resolution
            var adaptArgusPlusRenderDETAIL=function(rawdata){
                var returnData=[];
                var racs=extractCategory("metric",rawdata);
                //console.log(rawdata);
                for(var idx in racs){
                    var rac=racs[idx];
                    var listMetricCurrentRac = getMetricsFromRac(rawdata,rac);
                    APT=getDatapointsFromScope(listMetricCurrentRac,"APT");
                    ACT=getDatapointsFromScope(listMetricCurrentRac,"ACT");
                    CPU=getDatapointsFromScope(listMetricCurrentRac,"CPU");
                    Traffic=getDatapointsFromScope(listMetricCurrentRac,"Traffic");

                    for (var ts in APT){
                        currentTS={};
                        //currentTS["Timemark"]=convertEpochToMin(ts);
                        currentTS["ts"]=ts;
                        currentTS["Racnode"]=rac;
                        currentTS["value"]=APT[ts];
                        currentTS["AvgAPT"]=APT[ts];
                        currentTS["TrafficValue"]=Traffic[ts];
                        if ((ACT != null) && (typeof ACT!= "undefined") && (ts in ACT)){currentTS["AvgActSession"]=ACT[ts];}
                        if ((CPU != null) && (typeof CPU!= "undefined") && (ts in CPU)){currentTS["CPUValue"]=CPU[ts];}
                        returnData.push(currentTS);
                    }
                }
                return returnData;
            };

            var adaptArgusPlusRenderRACLevelHour=function(rawdata){
                var returnData=[];
                var racs=extractCategory("metric",rawdata);
                for(var idx in racs){
                    var rac=racs[idx];
                    var listMetricCurrentRac = getMetricsFromRac(rawdata,rac);
                    AVA=getDatapointsFromScope(listMetricCurrentRac,"AVA");
                    APT=getDatapointsFromScope(listMetricCurrentRac,"APT");
                    ImpactedMin=getDatapointsFromScope(listMetricCurrentRac,"ImpactedMin");
                    CollectedMin=getDatapointsFromScope(listMetricCurrentRac,"CollectedMin");
                    ACT=getDatapointsFromScope(listMetricCurrentRac,"ACT");
                    CPU=getDatapointsFromScope(listMetricCurrentRac,"CPU");
                    Traffic=getDatapointsFromScope(listMetricCurrentRac,"Traffic");
                    for (var ts in APT){
                        currentTS={};
                        //currentTS["Timemark"]=convertEpochToMin(ts);
                        currentTS["ts"]=ts;
                        currentTS["Racnode"]=rac;
                        currentTS["value"]=AVA[ts];
                        currentTS["AVA"]=AVA[ts];
                        currentTS["APT"]=APT[ts];
                        currentTS["ImpactedMin"]=ImpactedMin[ts];
                        currentTS["CollectedMin"]=CollectedMin[ts];
                        currentTS["Traffic"]=Traffic[ts];
                        if ((ACT != null) && (typeof ACT!= "undefined") && (ts in ACT)){currentTS["ACT"]=ACT[ts];}
                        if ((CPU != null) && (typeof CPU!= "undefined") && (ts in CPU)){currentTS["CPU"]=CPU[ts];}
                        returnData.push(currentTS);
                    }
                }
                return returnData;
            };

            //Report AVA from Argus plus POD LEVEL ONE-LINER
            function drawArgusPlusPOD(){
                //var URL="http://ewang-ltm.internal.salesforce.com:8080/argusws/metrics?expression=HEIMDALL_TOTALAVA(%20-10d:core.CHI.SP2.cs15:SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode1.Last_1_Min_Avg{device=*}:avg,%20-10d:core.CHI.SP2.cs15:SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode1.Last_1_Min_Avg{device=*}:avg,%20SUM(%20DOWNSAMPLE(-10d:system.CHI.SP2.cs15:CpuPerc.cpu.system{device=cs15-db1-1-chi.ops.sfdc.net}:avg,$1m-avg),%20DOWNSAMPLE(-10d:system.CHI.SP2.cs15:CpuPerc.cpu.user{device=cs15-db1-1-chi.ops.sfdc.net}:avg,$1m-avg)))";
                //StartTime = processGMTTime(StartTime);
                //EndTime = processGMTTime(EndTime);
                //var URL=CONFIG.wsUrl+"metrics?expression="+'HEIMDALL('+StartTime+':'+EndTime+':core.'+Pod+':SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, '+StartTime+':'+EndTime+':core.'+Pod+':SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, '+StartTime+':'+EndTime+':db.oracle.'+Pod+':*.active__sessions{device=*}:avg, $POD)';
                var URL=CONFIG.wsUrl+"metrics?expression="+expression;
                console.log(URL);
                $.getJSON(URL, function(rawdata) {
                    readydata=adaptArgusPlusRenderPOD(rawdata)
                    X_cat=extractCategory("ts",readydata);
                    X_cat.sort();
                    Y_cat=extractCategory("PodName",readydata);
                    returnJSON=[];
                    for (var index in readydata){
                        returnItem={};
                        returnItem.value=parseInt(readydata[index]["Availability"]);
                        returnItem.x=X_cat.indexOf(readydata[index]["ts"]);
                        returnItem.y=Y_cat.indexOf(readydata[index]["PodName"]);
                        returnItem.PodLevelAPT=parseFloat(readydata[index]["PodLevelAPT"]);
                        returnItem.ImpactedMin=parseFloat(readydata[index]["ImpactedMin"]);
                        returnItem.Availability=parseFloat(readydata[index]["Availability"]);
                        returnItem.TTM=parseFloat(readydata[index]["TTM"]);
                        returnItem.valueLabel=returnItem.value+"%";
                        returnJSON.push(returnItem);
                    }
                    //console.log(returnJSON);
                    $('#' + divId).css("height","20");
                    $('#' + divId).highcharts({
                        chart: {
                            type: 'heatmap',
                            marginTop: 1,
                            marginBottom: 1
                        },

                        labels:{
                            style: {
                                fontFamily: 'monospace',
                                color: "#f00"
                            }
                        },

                        plotOptions: {
                            series: {
                                borderWidth: 1,
                                borderColor: 'white',
                                turboThreshold: 1000000,
                                animation: {
                                    duration: 200
                                },
                                point: {
                                    events: {
                                        click: function(e){//console.log(e);
                                        }
                                    }
                                }
                            },
                        },

                        title: {
                            text: '',
                        },

                        subtitle: {
                            text: '<span id="helpBlock" class="help-block">DB AVA</span>',
                            useHTML: true
                        },

                        xAxis: {
                            categories: doFormatDateFromUNIX(X_cat),
                            //categories: doFormatDate(X_cat),
                            //min: 20,
                            //max: Math.min(20,X_cat.length-1)
                        },

                        yAxis: {
                            categories: Y_cat,
                            minPadding: 0,
                            maxPadding: 0,
                            startOnTick: false,
                            endOnTick: false,
                            tickWidth: 1,
                            reversed: true,
                            title: null
                        },

                        colorAxis: {
                            dataClassColor: 'category',
                            dataClasses: [{
                                from: 0,
                                to: 10,
                                color:'#B90009'
                            },{
                                from: 10,
                                to: 20,
                                color:'#C5221A'
                            },{
                                from: 20,
                                to: 30,
                                color:'#D23B2B'
                            },{
                                from: 30,
                                to: 40,
                                color:'#D23B2B'
                            },{
                                from:40,
                                to:50,
                                color:'#F98375'
                            },{
                                from:50,
                                to:60,
                                color:'#F3B3A2'
                            },{
                                from:60,
                                to:65,
                                color:'#9FD2A1'
                            },{
                                from:65,
                                to:75,
                                color:'#9DD56F'
                            },{
                                from:75,
                                to:85,
                                color:'#85C462'
                            },{
                                from:85,
                                to:90,
                                color:'#74B35A'
                            },{
                                from:90,
                                to:95,
                                color:'#62A247'
                            },{
                                from:95,
                                color:'#4E8E1C'
                            },{
                                to:-0.01,
                                from:-999999,
                                color:'#6E6E6E'
                            }]
                        },

                        legend: {
                            enabled: false,
                            align: 'right',
                            layout: 'vertical',
                            margin: 0,
                            verticalAlign: 'top',
                            y: 24,
                            symbolHeight: 280
                        },

                        tooltip: {
                            formatter: function () {
                                return this.series.xAxis.categories[this.point.x] +
                                    'Availability: <b>'+ this.point.value+'</b>%' +
                                    ' ImpactedMin: <b>' + this.point.ImpactedMin + '</b> min'+
                                    ' TTM:<b>'+ this.point.TTM +'</b> min'+
                                    ' PODAPT:<b>' + this.point.PodLevelAPT + 'ms';
                            }
                        },

                        series: [{
                            //threshold: 1,
                            borderWidth: 0,
                            turboThreshold: Number.MAX_VALUE,
                            dataLabels: {
                                enabled: false,
                                color: '#ffffff',
                                format: '{point.value}',
                                shadow: false,
                                style: {
                                    fontWeight: 'bold',
                                    fontSize: "10px",
                                    "textShadow": "0 0 0px contrast, 0 0 0px contrast"
                                }
                            },
                            data: returnJSON
                        }]
                    });
                }).error(function(jqXHR, textStatus, errorThrown){
                    errorHandle(jqXHR, textStatus, errorThrown);
                });
            }

            //Report APT HD from Argus plus
            function drawArgusPlusHDDETAIL(){
                //RAC
                //StartTime = processGMTTime(StartTime);
                //EndTime = processGMTTime(EndTime);
                //var URL=CONFIG.wsUrl+"metrics?expression="+'HEIMDALL('+StartTime+':'+EndTime+':core.'+Pod+':SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, '+StartTime+':'+EndTime+':core.'+Pod+':SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, '+StartTime+':'+EndTime+':db.oracle.'+Pod+':*.active__sessions{device=*}:avg, '+StartTime+':'+EndTime+':system.'+Pod+':CpuPerc.cpu.system{device=*-db*ops.sfdc.net}:avg, '+StartTime+':'+EndTime+':system.'+Pod+':CpuPerc.cpu.user{device=*-db*ops.sfdc.net}:avg, $RAC)';
                var URL=CONFIG.wsUrl+"metrics?expression="+expression;
                console.log(URL);
                $.getJSON(URL, function(plusdata) {
                    readydata=adaptArgusPlusRenderDETAIL(plusdata);
                    //readydata=adaptArgusPlusRenderRACLevel(plusdata);
                    X_cat=extractCategory("ts",readydata);
                    X_cat.sort();
                    Y_cat=extractCategory("Racnode",readydata);
                    returnJSON=[];
                    for (var index in readydata){
                        returnItem={};
                        returnItem.value=parseInt(readydata[index]["value"]);
                        returnItem.x=X_cat.indexOf(readydata[index]["ts"]);
                        returnItem.y=Y_cat.indexOf(readydata[index]["Racnode"]);
                        returnItem.AvgAPT=parseFloat(readydata[index]["AvgAPT"]);
                        returnItem.AvgActSession=parseFloat(readydata[index]["AvgActSession"]);
                        returnItem.TrafficValue=parseFloat(readydata[index]["TrafficValue"]);
                        returnItem.CPUValue=parseFloat(readydata[index]["CPUValue"]);
                        returnItem.ts=parseFloat(readydata[index]["ts"]);
                        returnJSON.push(returnItem);
                    }

                    $('#' + divId).highcharts({
                        chart: {
                            type: 'heatmap',
                            marginTop: 40,
                            marginBottom: 80
                        },

                        plotOptions: {
                            series: {
                                borderWidth: 1,
                                borderColor: 'white',
                                turboThreshold: 1000000,
                                animation: {
                                    duration: 200
                                }
                            }
                        },

                        title: {
                            text: ''
                        },

                        subtitle: {
                            text: '<span id="helpBlock" class="help-block">APT trend for each node over time at maximum resolution' +
                            ' (Green=low APT value, Grey=no data collected)</span>',
                            useHTML: true
                        },

                        xAxis: [{
                            categories: doFormatDateTimeFromUNIX(X_cat),
                            //showLastLabel: false,
                        }],

                        yAxis: {
                            categories: Y_cat,
                            minPadding: 0,
                            maxPadding: 0,
                            startOnTick: false,
                            endOnTick: false,
                            tickWidth: 1,
                            reversed: true,
                            title: null
                        },
                        colorAxis: {
                            dataClassColor: 'category',
                            dataClasses: [{
                                to: 999999,
                                from: 800,
                                color:'#B90009'
                            },{
                                to: 800,
                                from: 700,
                                color:'#C5221A'
                            },{
                                to:700,
                                from: 600,
                                color:'#D23B2B'
                            },{
                                to: 600,
                                from: 560,
                                color:'#D23B2B'
                            },{
                                to:560,
                                from:520,
                                color:'#F98375'
                            },{
                                to:520,
                                from:480,
                                color:'#F3B3A2'
                            },{
                                to:480,
                                from:440,
                                color:'#9FD2A1'
                            },{
                                to:440,
                                from:400,
                                color:'#9DD56F'
                            },{
                                to:400,
                                from:360,
                                color:'#85C462'
                            },{
                                to:360,
                                from:330,
                                color:'#74B35A'
                            },{
                                to:330,
                                from:300,
                                color:'#62A247'
                            },{
                                to:300,
                                from:0,
                                color:'#4E8E1C'
                            },{
                                to:0,
                                from:-10,
                                color:'#6E6E6E'
                            },{
                                to:-0.01,
                                from:-999999,
                                color:'#6E6E6E'
                            }]
                        },

                        legend: {
                            enabled: false,
                            align: 'right',
                            layout: 'vertical',
                            margin: 0,
                            verticalAlign: 'top',
                            y: 25,
                            symbolHeight: 280
                        },

                        tooltip: {
                            formatter: function () {
                                return 'Time: ' + this.series.xAxis.categories[this.point.x] +
                                    '<br>RacNode: ' + this.series.yAxis.categories[this.point.y] +
                                    '<br><br>APT:  <b>' + this.point.value + 'ms' +
                                    '<br>Traffic:  <b>' + this.point.TrafficValue +
                                    '<br>ACT:  <b>' + this.point.AvgActSession +
                                    '<br>CPU: <b>' + this.point.CPUValue+
                                    '<br>HBase Timestamp: <b>' + this.point.ts;
                            }
                        },

                        series: [{
                            borderWidth: 0,
                            //colsize: 24 * 36e5,
                            turboThreshold: Number.MAX_VALUE,
                            data: returnJSON,
                        }],

                    });
                }).error(function(jqXHR, textStatus, errorThrown) {
                    errorHandle(jqXHR, textStatus, errorThrown);
                });
            }

            //Report AVA HD from Argus plus
            function drawArgusPlusHDAVA(){
                //RACHOUR
                //StartTime = processGMTTime(StartTime);
                //EndTime = processGMTTime(EndTime);
                //var URL=CONFIG.wsUrl+"metrics?expression="+'HEIMDALL('+StartTime+':'+EndTime+':core.'+Pod+':SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, '+StartTime+':'+EndTime+':core.'+Pod+':SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, '+StartTime+':'+EndTime+':db.oracle.'+Pod+':*.active__sessions{device=*}:avg, '+StartTime+':'+EndTime+':system.'+Pod+':CpuPerc.cpu.system{device=*-db*ops.sfdc.net}:avg, '+StartTime+':'+EndTime+':system.'+Pod+':CpuPerc.cpu.user{device=*-db*ops.sfdc.net}:avg, $RACHOUR)';
                var URL=CONFIG.wsUrl+"metrics?expression="+expression;
                console.log(URL);
                $.getJSON(URL, function(rawdata) {
                    readydata=adaptArgusPlusRenderRACLevelHour(rawdata);
                    X_cat=extractCategory("ts",readydata);
                    Y_cat=extractCategory("Racnode",readydata);
                    returnJSON=[];
                    for (var index in readydata){
                        returnItem={};
                        returnItem.value=parseInt(readydata[index]["value"]);
                        returnItem.x=X_cat.indexOf(readydata[index]["ts"]);
                        returnItem.y=Y_cat.indexOf(readydata[index]["Racnode"]);
                        returnItem.APT=parseInt(readydata[index]["APT"]);
                        returnItem.ImpactedMin=parseInt(readydata[index]["ImpactedMin"]);
                        returnItem.CollectedMin=parseInt(readydata[index]["CollectedMin"]);
                        returnItem.ACT=parseInt(readydata[index]["ACT"]);
                        returnItem.CPU=parseInt(readydata[index]["CPU"]);
                        returnItem.Traffic=parseInt(readydata[index]["Traffic"]);
                        returnItem.valueLabel=returnItem.value+"%";
                        returnJSON.push(returnItem);
                    }

                    $('#' + divId).highcharts({
                        chart: {
                            type: 'heatmap',
                            marginTop: 40,
                            marginBottom: 80,
                            plotBorderWidth: 1
                        },

                        labels:{
                            style: {
                                fontFamily: 'monospace',
                                color: "#f00"
                            }
                        },

                        plotOptions: {
                            series: {
                                borderWidth: 1,
                                borderColor: 'white',
                                turboThreshold: 1000000,
                                animation: {
                                    duration: 200
                                },
                                point: {
                                    events: {
                                        click: function(e){//console.log(e);
                                        }
                                    }
                                }
                            },
                        },

                        title: {
                            text: '',
                        },

                        subtitle: {
                            text: '<span id="helpBlock" class="help-block">Heimdall DB Availblity: Percentage of availblity for each node over hourly resolution' +
                            ' (available is caculated based on weighted apt and act Green=highly available)</span>',
                            useHTML: true
                        },

                        xAxis: {
                            categories: doFormatDateFromUNIX(X_cat),
                            max: Math.min(20,X_cat.length-1)
                        },

                        scrollbar: {
                            enabled: true
                        },

                        yAxis: {
                            categories: Y_cat,
                            title: null,
                            reversed: true
                        },

                        colorAxis: {
                            dataClassColor: 'category',
                            dataClasses: [{
                                from: 0,
                                to: 10,
                                color:'#B90009'
                            },{
                                from: 10,
                                to: 20,
                                color:'#C5221A'
                            },{
                                from: 20,
                                to: 30,
                                color:'#D23B2B'
                            },{
                                from: 30,
                                to: 40,
                                color:'#D23B2B'
                            },{
                                from:40,
                                to:50,
                                color:'#F98375'
                            },{
                                from:50,
                                to:60,
                                color:'#F3B3A2'
                            },{
                                from:60,
                                to:65,
                                color:'#9FD2A1'
                            },{
                                from:65,
                                to:75,
                                color:'#9DD56F'
                            },{
                                from:75,
                                to:85,
                                color:'#85C462'
                            },{
                                from:85,
                                to:90,
                                color:'#74B35A'
                            },{
                                from:90,
                                to:95,
                                color:'#62A247'
                            },{
                                from:95,
                                color:'#4E8E1C'
                            },{
                                to:-0.01,
                                from:-999999,
                                color:'#6E6E6E'
                            }]
                        },

                        legend: {
                            enabled: false,
                            align: 'right',
                            layout: 'vertical',
                            margin: 0,
                            verticalAlign: 'top',
                            y: 24,
                            //symbolHeight: 280
                        },

                        tooltip: {
                            formatter: function () {
                                return 'Time: ' + this.series.xAxis.categories[this.point.x] +
                                    '<br>RacNode: ' + this.series.yAxis.categories[this.point.y] +
                                    '<br><br>DB Availablity:  <b>' + this.point.value + '%' +
                                    '<br>ImpactedMin:  <b>' + this.point.ImpactedMin + ' min' +
                                    '<br>(out-of)MonitoredMin:  <b>' + this.point.CollectedMin + ' min' +
                                    '<br>weighted APT:  <b>' + this.point.APT + 'ms' +
                                    '<br>weighted ACT:  <b>' + this.point.ACT + '' +
                                    '<br>weighted CPU:  <b>' + this.point.CPU + '' +
                                    '<br>Total Traffic: <b>' + this.point.Traffic + '' +
                                    '';
                            }
                        },

                        series: [{
                            //threshold: 1,
                            //borderWidth: 1,
                            dataLabels: {
                                enabled: true,
                                color: '#ffffff',
                                format: '{point.valueLabel}',
                                shadow: false,
                                style: {
                                    fontWeight: 'bold',
                                    fontSize: "10px",
                                    "textShadow": "0 0 0px contrast, 0 0 0px contrast"
                                }
                            },
                            data: returnJSON,
                        }],

                    });
                }).error(function(jqXHR, textStatus, errorThrown) {
                    errorHandle(jqXHR, textStatus, errorThrown);
                });
            }

            //Report total one liner
            function drawArgusPlusReport(){
                //StartTime = processGMTTime(StartTime);
                //EndTime = processGMTTime(EndTime);
                //$TOTAL
                //var URL=CONFIG.wsUrl+"metrics?expression="+'HEIMDALL('+StartTime+':'+EndTime+':core.'+Pod+':SFDC_type-Stats-name1-System-name2-trustAptRequestTimeRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, '+StartTime+':'+EndTime+':core.'+Pod+':SFDC_type-Stats-name1-System-name2-trustAptRequestCountRACNode*.Last_1_Min_Avg{device=*-app*-*.ops.sfdc.net}:avg, '+StartTime+':'+EndTime+':db.oracle.'+Pod+':*.active__sessions{device=*}:avg, $TOTAL)';
                var URL=CONFIG.wsUrl+"metrics?expression="+expression;
                $.getJSON(URL, function(rawdata){
                    //console.log(rawdata);
                    var ImpactedMin=getFirstItemInMetrics(getMetricFromMetrics(rawdata,'ImpactedMin')["datapoints"]);
                    var Availability=getFirstItemInMetrics(getMetricFromMetrics(rawdata,'Availability')["datapoints"]);
                    var AvailableMin=getFirstItemInMetrics(getMetricFromMetrics(rawdata,'AvailableMin')["datapoints"]);
                    var TTM=getFirstItemInMetrics(getMetricFromMetrics(rawdata,'TTM')["datapoints"]);
                    $('#' + divId).html('<br><h3><span id="helpBlock" class="help-block">Availability <b>'+parseFloat(Availability).toFixed(2)+'%</b>, ' +
                        'TotalAvailable ' +AvailableMin+' min, '+
                        'Impacted '+ImpactedMin+' min, TTM: '+TTM+' min.&nbsp;&nbsp;&nbsp;&nbsp;' +
                        //'POD: '+Pod+',&nbsp;(UTC) Start: '+ StartTime+',&nbsp;End: '+EndTime+
                        '</span></h3>'
                    );
                }).error(function(jqXHR, textStatus, errorThrown) {
                        errorHandle(jqXHR, textStatus, errorThrown);
                });
            }

            /**
             * Error handle
             * @param jqXHR
             * @param textStatus
             * @param errorThrown
             */
            var errorHandle=function(jqXHR, textStatus, errorThrown) {
                var errormessage=textStatus+errorThrown+" Detail: " + jqXHR.responseText;
                growl.info('We can not render this chart');
                $('#'+divId).html('<img src="img/error.png" />'+errormessage);
            };


            /*
             _   _  _____  _____ ___  _________   ___   _      _          ______ __   __ _____  _   _  _____  _   _       _____  _____ ______  _____
             | | | ||  ___||_   _||  \/  ||  _  \ / _ \ | |    | |         | ___ \\ \ / /|_   _|| | | ||  _  || \ | |     /  __ \|  _  || ___ \|  ___|
             | |_| || |__    | |  | .  . || | | |/ /_\ \| |    | |         | |_/ / \ V /   | |  | |_| || | | ||  \| |     | /  \/| | | || |_/ /| |__
             |  _  ||  __|   | |  | |\/| || | | ||  _  || |    | |         |  __/   \ /    | |  |  _  || | | || . ` |     | |    | | | ||    / |  __|
             | | | || |___  _| |_ | |  | || |/ / | | | || |____| |____     | |      | |    | |  | | | |\ \_/ /| |\  |     | \__/\\ \_/ /| |\ \ | |___
             \_| |_/\____/  \___/ \_|  |_/|___/  \_| |_/\_____/\_____/     \_|      \_/    \_/  \_| |_/ \___/ \_| \_/      \____/ \___/ \_| \_/\____/
             */

            //Report
            function drawReport(){
                URL=CONFIG.grahiteUrl+'getRate/?StartTime='+StartTime+'&EndTime='+EndTime+'&Pod='+Pod+'&Cfg='+Cfg;
                console.log(URL);
                $.getJSON(URL, function(ratedata){
                    var TotalRate=ratedata[0]["TotalRate"];
                    var AvgAPT=ratedata[0]["AvgAPT"];
                    var AvgACT=ratedata[0]["AvgACT"];
                    var ImpMin=ratedata[0]["ImpMin"];
                    var CollectedMin=ratedata[0]["CollectedMin"];
                    var TTM=ratedata[0]["TTM"];
                    var TTM_AVA=ratedata[0]["TTM_AVA"];
                    var TTM_TRIGGER=ratedata[0]["TTM_TRIGGER"];
                    $('#' + divId).html('<br><h3><span id="helpBlock" class="help-block">TotalAvailablity: <b>'+TotalRate+'%</b>, ' +
                        'Available '+CollectedMin+' min, Impacted '+ImpMin+' min, TTM: '+TTM+'='+TTM_AVA+'+'+TTM_TRIGGER+' mins.&nbsp;&nbsp;&nbsp;&nbsp;' +
                        'POD: '+Pod+',&nbsp;(UTC) Start: '+ StartTime+',&nbsp;End: '+EndTime+'</span></h3>');
                });

            }

            //TTM
            function drawTTM(){
                URL=CONFIG.grahiteUrl+'getTTM/?StartTime='+StartTime+'&EndTime='+EndTime+'&Pod='+Pod+'&Cfg='+Cfg;
                console.log(URL);
                $.getJSON(URL,function(readydata){
                    X_cat=extractCategory("Timemark",readydata);
                    Y_cat=extractCategory("PodName",readydata)
                    returnJSON=[];
                    for (var index in readydata){
                        returnItem={};
                        returnItem.value=parseInt(readydata[index]["PodLevelAptValue"]);
                        returnItem.x=X_cat.indexOf(readydata[index]["Timemark"]);
                        returnItem.y=Y_cat.indexOf(readydata[index]["PodName"]);
                        returnItem.SummationTrafficValue=parseFloat(readydata[index]["SummationTrafficValue"]);
                        returnJSON.push(returnItem);
                    }
                    $('#' + divId).css("height","20");
                    $('#' + divId).highcharts({
                        chart: {
                            type: 'heatmap',
                            marginTop: 1,
                            marginBottom: 1
                        },

                        labels:{
                            style: {
                                fontFamily: 'monospace',
                                color: "#f00"
                            }
                        },

                        plotOptions: {
                            series: {
                                borderWidth: 1,
                                borderColor: 'white',
                                turboThreshold: 1000000,
                                animation: {
                                    duration: 200
                                },
                                point: {
                                    events: {
                                        click: function(e){//console.log(e);
                                        }
                                    }
                                }
                            },
                        },

                        title: {
                            text: '',
                        },

                        subtitle: {
                            text: '<span id="helpBlock" class="help-block">TTM</span>',
                            useHTML: true
                        },

                        xAxis: {
                            categories: doFormatDate(X_cat),
                            //min: 20,
                            //max: Math.min(20,X_cat.length-1)
                        },

                        yAxis: {
                            categories: Y_cat,
                            minPadding: 0,
                            maxPadding: 0,
                            startOnTick: false,
                            endOnTick: false,
                            tickWidth: 1,
                            reversed: true,
                            title: null
                        },

                        colorAxis: {
                            dataClassColor: 'category',
                            dataClasses: [{
                                to: 999999,
                                from: 800,
                                color:'#B90009'
                            },{
                                to: 800,
                                from: 700,
                                color:'#C5221A'
                            },{
                                to:700,
                                from: 600,
                                color:'#D23B2B'
                            },{
                                to: 600,
                                from: 560,
                                color:'#D23B2B'
                            },{
                                to:560,
                                from:520,
                                color:'#F98375'
                            },{
                                to:520,
                                from:480,
                                color:'#F3B3A2'
                            },{
                                to:480,
                                from:440,
                                color:'#9FD2A1'
                            },{
                                to:440,
                                from:400,
                                color:'#9DD56F'
                            },{
                                to:400,
                                from:360,
                                color:'#85C462'
                            },{
                                to:360,
                                from:330,
                                color:'#74B35A'
                            },{
                                to:330,
                                from:300,
                                color:'#62A247'
                            },{
                                to:300,
                                from:0,
                                color:'#4E8E1C'
                            },{
                                to:0,
                                from:-10,
                                color:'#6E6E6E'
                            },{
                                to:-0.01,
                                from:-999999,
                                color:'#6E6E6E'
                            }]
                        },

                        legend: {
                            enabled: false,
                            align: 'right',
                            layout: 'vertical',
                            margin: 0,
                            verticalAlign: 'top',
                            y: 24,
                            symbolHeight: 280
                        },

                        tooltip: {
                            formatter: function () {
                                return this.series.xAxis.categories[this.point.x] +
                                    ' APT:<b>'+this.point.value + '</b>'+
                                    ' Traffic: <b>'+ this.point.SummationTrafficValue+'</b>';
                            }
                        },

                        series: [{
                            //threshold: 1,
                            borderWidth: 0,
                            turboThreshold: Number.MAX_VALUE,
                            dataLabels: {
                                enabled: false,
                                color: '#ffffff',
                                format: '{point.value}',
                                shadow: false,
                                style: {
                                    fontWeight: 'bold',
                                    fontSize: "10px",
                                    "textShadow": "0 0 0px contrast, 0 0 0px contrast"
                                }
                            },
                            data: returnJSON
                        }]
                    });
                });
            }

            //High Chart HeatMap
            function drawHeatmap(){
                var URL=CONFIG.grahiteUrl+'get/?Type=Ava&StartTime='+StartTime+'&EndTime='+EndTime+'&Pod='+Pod+'&Cfg='+Cfg;
                console.log(URL);
                $.getJSON(URL, function(readydata) {
                    X_cat=extractCategory("Timemark",readydata);
                    Y_cat=extractCategory("Racnode",readydata);
                    returnJSON=JSONProcessor(readydata);
                    $('#' + divId).highcharts({
                        chart: {
                            type: 'heatmap',
                            marginTop: 40,
                            marginBottom: 80,
                            plotBorderWidth: 1
                        },

                        labels:{
                            style: {
                                fontFamily: 'monospace',
                                color: "#f00"
                            }
                        },

                        plotOptions: {
                            series: {
                                borderWidth: 1,
                                borderColor: 'white',
                                turboThreshold: 1000000,
                                animation: {
                                    duration: 200
                                },
                                point: {
                                    events: {
                                        click: function(e){//console.log(e);
                                        }
                                    }
                                }
                            },
                        },

                        title: {
                            text: '',
                        },

                        subtitle: {
                            text: '<span id="helpBlock" class="help-block">Total Availblity: Percentage of availblity for each node over hourly resolution' +
                            ' (available is caculated based on weighted apt and act Green=highly available)</span>',
                            useHTML: true
                        },

                        xAxis: {
                            categories: doFormatDate(X_cat),
                            //min: 20,
                            max: Math.min(20,X_cat.length-1)
                        },

                        scrollbar: {
                            enabled: true
                        },

                        yAxis: {
                            categories: Y_cat,
                            title: null,
                            reversed: true
                        },

                        colorAxis: {
                            dataClassColor: 'category',
                            dataClasses: [{
                                from: 0,
                                to: 10,
                                color:'#B90009'
                            },{
                                from: 10,
                                to: 20,
                                color:'#C5221A'
                            },{
                                from: 20,
                                to: 30,
                                color:'#D23B2B'
                            },{
                                from: 30,
                                to: 40,
                                color:'#D23B2B'
                            },{
                                from:40,
                                to:50,
                                color:'#F98375'
                            },{
                                from:50,
                                to:60,
                                color:'#F3B3A2'
                            },{
                                from:60,
                                to:65,
                                color:'#9FD2A1'
                            },{
                                from:65,
                                to:75,
                                color:'#9DD56F'
                            },{
                                from:75,
                                to:85,
                                color:'#85C462'
                            },{
                                from:85,
                                to:90,
                                color:'#74B35A'
                            },{
                                from:90,
                                to:95,
                                color:'#62A247'
                            },{
                                from:95,
                                color:'#4E8E1C'
                            },{
                                to:-0.01,
                                from:-999999,
                                color:'#6E6E6E'
                            }]
                        },

                        legend: {
                            enabled: false,
                            align: 'right',
                            layout: 'vertical',
                            margin: 0,
                            verticalAlign: 'top',
                            y: 24,
                            //symbolHeight: 280
                        },

                        tooltip: {
                            formatter: function () {
                                return 'Time: ' + this.series.xAxis.categories[this.point.x] +
                                    '<br>RacNNode: ' + this.series.yAxis.categories[this.point.y] +
                                    '<br>We have collected '+this.point.CollectedMin+' valid data points.'+
                                    '<br><br>APT:  <b>' + this.point.AvgAPT + 'ms' +
                                    '<br>ActSession: <b>' + this.point.AvgActSession +
                                    '<br>AvailabilityRate: <b>'+ this.point.valueLabel+'</b>'+
                                    '<br>Impacted:<b>'+ this.point.ImpMin +'</b> min';
                            }
                        },

                        series: [{
                            //threshold: 1,
                            //borderWidth: 1,
                            dataLabels: {
                                enabled: true,
                                color: '#ffffff',
                                format: '{point.valueLabel}',
                                shadow: false,
                                style: {
                                    fontWeight: 'bold',
                                    fontSize: "10px",
                                    "textShadow": "0 0 0px contrast, 0 0 0px contrast"
                                }
                            },
                            data: returnJSON,
                        }],

                    });
                });

            }

            //Min Level Chart HeatMapHD for APT
            function drawHeatmapHDAPT(){
                var URL=CONFIG.grahiteUrl+'get/?Type=AvaHighResolution&StartTime='+StartTime+'&EndTime='+EndTime+'&Pod='+Pod+'&Cfg='+Cfg;
                console.log(URL);
                $.getJSON(URL, function(readydata) {
                    X_cat=extractCategory("Timemark",readydata);
                    Y_cat=extractCategory("Racnode",readydata);
                    returnJSON=JSONProcessorGeneric(readydata,"AvgAPT");
                    $('#' + divId).highcharts({
                        chart: {
                            type: 'heatmap',
                            marginTop: 40,
                            marginBottom: 80,
                            plotBorderWidth: 1
                        },

                        labels:{
                            style: {
                                fontFamily: 'monospace',
                                color: "#f00"
                            }
                        },

                        plotOptions: {
                            series: {
                                borderWidth: 1,
                                borderColor: 'white',
                                turboThreshold: 1000000,
                                animation: {
                                    duration: 200
                                }
                            }
                        },

                        title: {
                            text: '',
                        },

                        subtitle: {
                            text: '<span id="helpBlock" class="help-block">APT trend for each node over time at maximum resolution' +
                            ' (Green=low APT value, Grey=no data collected)</span>',
                            useHTML: true
                        },

                        xAxis: {
                            categories: doFormatDate(X_cat),
                            endOnTick: true,
                            //floor: 0,
                            //ceiling: 20,
                            max: Math.min(17,X_cat.length-1)
                        },

                        scrollbar: {
                            enabled: true
                        },


                        yAxis: {
                            categories: Y_cat,
                            title: null,
                            reversed: true
                        },

                        colorAxis: {
                            dataClassColor: 'category',
                            dataClasses: [{
                                to: 999999,
                                from: 800,
                                color:'#B90009'
                            },{
                                to: 800,
                                from: 700,
                                color:'#C5221A'
                            },{
                                to:700,
                                from: 600,
                                color:'#D23B2B'
                            },{
                                to: 600,
                                from: 560,
                                color:'#D23B2B'
                            },{
                                to:560,
                                from:520,
                                color:'#F98375'
                            },{
                                to:520,
                                from:480,
                                color:'#F3B3A2'
                            },{
                                to:480,
                                from:440,
                                color:'#9FD2A1'
                            },{
                                to:440,
                                from:400,
                                color:'#9DD56F'
                            },{
                                to:400,
                                from:360,
                                color:'#85C462'
                            },{
                                to:360,
                                from:330,
                                color:'#74B35A'
                            },{
                                to:330,
                                from:300,
                                color:'#62A247'
                            },{
                                to:300,
                                from:0,
                                color:'#4E8E1C'
                            },{
                                to:0,
                                from:-10,
                                color:'#6E6E6E'
                            },{
                                to:-0.01,
                                from:-999999,
                                color:'#6E6E6E'
                            }]
                        },

                        legend: {
                            enabled: false,
                            align: 'right',
                            layout: 'vertical',
                            margin: 0,
                            verticalAlign: 'top',
                            y: 25,
                            //symbolHeight: 280
                        },

                        tooltip: {
                            formatter: function () {
                                return 'Time: ' + this.series.xAxis.categories[this.point.x] +
                                    '<br>We have collected '+this.point.CollectedMin+' valid data points.'+
                                    '<br>Impacted:<b>'+ this.point.ImpMin +'</b> min'+
                                    '<br><br>APT:  <b>' + this.point.AvgAPT + 'ms' +
                                    '<br>ActSession: <b>' + this.point.AvgActSession +
                                    '<br>Spare indicator:<b>'+ this.point.Spare +'</b>'+
                                    '<br>Traffic: <b>'+ this.point.TrafficValue+'</b>'+
                                    '<br>DB CPU: <b>'+ this.point.CPUValue+'%</b>';
                            }
                        },

                        series: [{
                            //threshold: 1,
                            //borderWidth: 1,
                            dataLabels: {
                                enabled: true,
                                color: '#ffffff',
                                format: '{point.value}ms',
                                shadow: false,
                                style: {
                                    fontWeight: 'bold',
                                    fontSize: "10px",
                                    "textShadow": "0 0 0px contrast, 0 0 0px contrast"
                                }
                            },
                            data: returnJSON,
                        }],

                    });
                });
            }

            //Min Level HeatMapHD for ACT
            function drawHeatmapHDACT(){
                var URL=CONFIG.grahiteUrl+'get/?Type=AvaHighResolution&StartTime='+StartTime+'&EndTime='+EndTime+'&Pod='+Pod+'&Cfg='+Cfg;
                console.log(URL);
                $.getJSON(URL, function(readydata) {
                    X_cat=extractCategory("Timemark",readydata);
                    Y_cat=extractCategory("Racnode",readydata);
                    returnJSON=JSONProcessorGeneric(readydata,"AvgActSession");
                    $('#' + divId).highcharts({
                        chart: {
                            type: 'heatmap',
                            marginTop: 40,
                            marginBottom: 80,
                            plotBorderWidth: 1
                        },

                        labels:{
                            style: {
                                fontFamily: 'monospace',
                                color: "#f00"
                            }
                        },

                        plotOptions: {
                            series: {
                                borderWidth: 1,
                                borderColor: 'white',
                                turboThreshold: 1000000,
                                animation: {
                                    duration: 200
                                }
                            }
                        },

                        title: {
                            text: '',
                        },

                        subtitle: {
                            text: '<span id="helpBlock" class="help-block">ACT trend for each node over time at maximum resolution' +
                            ' (Green=low ACT value, Grey=no data collected)</span>',
                            useHTML: true
                        },

                        xAxis: {
                            categories: doFormatDate(X_cat),
                            endOnTick: true,
                            //floor: 0,
                            //ceiling: 20,
                            max: Math.min(17,X_cat.length-1)
                        },

                        scrollbar: {
                            enabled: true
                        },


                        yAxis: {
                            categories: Y_cat,
                            title: null,
                            reversed: true
                        },

                        colorAxis: {
                            dataClassColor: 'category',
                            dataClasses: [{
                                to: 999999,
                                from: 350,
                                color:'#B90009'
                            },{
                                to: 350,
                                from: 280,
                                color:'#C5221A'
                            },{
                                to:280,
                                from: 250,
                                color:'#D23B2B'
                            },{
                                to: 250,
                                from: 220,
                                color:'#D23B2B'
                            },{
                                to:220,
                                from:190,
                                color:'#F98375'
                            },{
                                to:190,
                                from:170,
                                color:'#F3B3A2'
                            },{
                                to:170,
                                from:150,
                                color:'#9FD2A1'
                            },{
                                to:150,
                                from:130,
                                color:'#9DD56F'
                            },{
                                to:130,
                                from:110,
                                color:'#85C462'
                            },{
                                to:110,
                                from:90,
                                color:'#74B35A'
                            },{
                                to:90,
                                from:70,
                                color:'#62A247'
                            },{
                                to:70,
                                from:0,
                                color:'#4E8E1C'
                            },{
                                to:0,
                                from:-10,
                                color:'#6E6E6E'
                            }]
                        },

                        legend: {
                            enabled: false,
                            align: 'right',
                            layout: 'vertical',
                            margin: 0,
                            verticalAlign: 'top',
                            y: 25,
                            //symbolHeight: 280
                        },

                        tooltip: {
                            formatter: function () {
                                return 'Time: ' + this.series.xAxis.categories[this.point.x] +
                                    '<br>We have collected '+this.point.CollectedMin+' valid data points.'+
                                    '<br>Impacted:<b>'+ this.point.ImpMin +'</b> min'+
                                    '<br><br>APT:  <b>' + this.point.AvgAPT + 'ms' +
                                    '<br>ActSession: <b>' + this.point.AvgActSession +
                                    '<br>Spare indicator:<b>'+ this.point.Spare +'</b>'+
                                    '<br>Traffic: <b>'+ this.point.TrafficValue+'</b>'+
                                    '<br>DB CPU: <b>'+ this.point.CPUValue+'%</b>';
                            }
                        },

                        series: [{
                            //threshold: 1,
                            //borderWidth: 1,
                            dataLabels: {
                                enabled: true,
                                color: '#ffffff',
                                format: '{point.value}',
                                shadow: false,
                                style: {
                                    fontWeight: 'bold',
                                    fontSize: "10px",
                                    "textShadow": "0 0 0px contrast, 0 0 0px contrast"
                                }
                            },
                            data: returnJSON,
                        }],

                    });
                });
            }

            //HD Level HeatMapHighResolution for APT
            function drawHeatmapHighResolutionAPT(){
                var URL=CONFIG.grahiteUrl+'get/?Type=AvaHighResolution&StartTime='+StartTime+'&EndTime='+EndTime+'&Pod='+Pod+'&Cfg='+Cfg;
                console.log(URL);
                $.getJSON(URL, function(readydata) {
                    X_cat=extractCategory("Timemark",readydata);
                    Y_cat=extractCategory("Racnode",readydata);
                    returnJSON=JSONProcessorGeneric(readydata,"AvgAPT");
                    $('#' + divId).highcharts({
                        chart: {
                            type: 'heatmap',
                            marginTop: 40,
                            marginBottom: 80
                        },

                        plotOptions: {
                            series: {
                                borderWidth: 1,
                                borderColor: 'white',
                                turboThreshold: 1000000,
                                animation: {
                                    duration: 200
                                }
                            }
                        },

                        title: {
                            text: ''
                        },

                        subtitle: {
                            text: '<span id="helpBlock" class="help-block">APT trend for each node over time at maximum resolution' +
                            ' (Green=low APT value, Grey=no data collected)</span>',
                            useHTML: true
                        },

                        xAxis: [{
                            categories: doFormatDate(X_cat),
                            //showLastLabel: false,
                        }],

                        yAxis: {
                            categories: Y_cat,
                            minPadding: 0,
                            maxPadding: 0,
                            startOnTick: false,
                            endOnTick: false,
                            tickWidth: 1,
                            reversed: true,
                            title: null
                        },
                        colorAxis: {
                            dataClassColor: 'category',
                            dataClasses: [{
                                to: 999999,
                                from: 800,
                                color:'#B90009'
                            },{
                                to: 800,
                                from: 700,
                                color:'#C5221A'
                            },{
                                to:700,
                                from: 600,
                                color:'#D23B2B'
                            },{
                                to: 600,
                                from: 560,
                                color:'#D23B2B'
                            },{
                                to:560,
                                from:520,
                                color:'#F98375'
                            },{
                                to:520,
                                from:480,
                                color:'#F3B3A2'
                            },{
                                to:480,
                                from:440,
                                color:'#9FD2A1'
                            },{
                                to:440,
                                from:400,
                                color:'#9DD56F'
                            },{
                                to:400,
                                from:360,
                                color:'#85C462'
                            },{
                                to:360,
                                from:330,
                                color:'#74B35A'
                            },{
                                to:330,
                                from:300,
                                color:'#62A247'
                            },{
                                to:300,
                                from:0,
                                color:'#4E8E1C'
                            },{
                                to:0,
                                from:-10,
                                color:'#6E6E6E'
                            }]
                        },


                        legend: {
                            enabled: false,
                            align: 'right',
                            layout: 'vertical',
                            margin: 0,
                            verticalAlign: 'top',
                            y: 25,
                            symbolHeight: 280
                        },

                        tooltip: {
                            formatter: function () {
                                return 'Time: ' + this.series.xAxis.categories[this.point.x] +
                                    '<br>We have collected '+this.point.CollectedMin+' valid data points.'+
                                    '<br>Impacted: <b>'+ this.point.ImpMin +'</b> min'+
                                    '<br><br>APT:  <b>' + this.point.AvgAPT + 'ms' +
                                    '<br>ActSession: <b>' + this.point.AvgActSession +
                                    '<br>Spare indicator: <b>'+ this.point.Spare +'</b>' +
                                    '<br>Traffic: <b>'+ this.point.TrafficValue+'</b>'+
                                    '<br>DB CPU: <b>'+ this.point.CPUValue+'%</b>';
                            }
                        },

                        series: [{
                            borderWidth: 0,
                            //colsize: 24 * 36e5,
                            turboThreshold: Number.MAX_VALUE,
                            data: returnJSON,

                        }],

                    });
                });
            }

            //HD Level  HeatMapHighResolution for ACT
            function drawHeatmapHighResolutionACT(){
                var URL=CONFIG.grahiteUrl+'get/?Type=AvaHighResolution&StartTime='+StartTime+'&EndTime='+EndTime+'&Pod='+Pod+'&Cfg='+Cfg;
                console.log(URL);
                $.getJSON(URL, function(readydata) {
                    X_cat=extractCategory("Timemark",readydata);
                    Y_cat=extractCategory("Racnode",readydata);
                    returnJSON=JSONProcessorGeneric(readydata,"AvgActSession");
                    $('#' + divId).highcharts({
                        chart: {
                            type: 'heatmap',
                            marginTop: 40,
                            marginBottom: 80
                        },

                        plotOptions: {
                            series: {
                                borderWidth: 1,
                                borderColor: 'white',
                                turboThreshold: 1000000,
                                animation: {
                                    duration: 200
                                }
                            }
                        },

                        title: {
                            text: ''
                        },

                        subtitle: {
                            text: '<span id="helpBlock" class="help-block">ACT trend for each node over time at maximum resolution' +
                            ' (Green=low ACT value, Grey=no data collected)</span>',
                            useHTML: true
                        },

                        xAxis: [{
                            categories: doFormatDate(X_cat),
                            //showLastLabel: false,
                        }],

                        yAxis: {
                            categories: Y_cat,
                            minPadding: 0,
                            maxPadding: 0,
                            startOnTick: false,
                            endOnTick: false,
                            tickWidth: 1,
                            reversed: true,
                            title: null
                        },

                        colorAxis: {
                            dataClassColor: 'category',
                            dataClasses: [{
                                to: 999999,
                                from: 350,
                                color:'#B90009'
                            },{
                                to: 350,
                                from: 280,
                                color:'#C5221A'
                            },{
                                to:280,
                                from: 250,
                                color:'#D23B2B'
                            },{
                                to: 250,
                                from: 220,
                                color:'#D23B2B'
                            },{
                                to:220,
                                from:190,
                                color:'#F98375'
                            },{
                                to:190,
                                from:170,
                                color:'#F3B3A2'
                            },{
                                to:170,
                                from:150,
                                color:'#9FD2A1'
                            },{
                                to:150,
                                from:130,
                                color:'#9DD56F'
                            },{
                                to:130,
                                from:110,
                                color:'#85C462'
                            },{
                                to:110,
                                from:90,
                                color:'#74B35A'
                            },{
                                to:90,
                                from:70,
                                color:'#62A247'
                            },{
                                to:70,
                                from:0,
                                color:'#4E8E1C'
                            },{
                                to:0,
                                from:-10,
                                color:'#6E6E6E'
                            }]
                        },


                        legend: {
                            enabled: false,
                            align: 'right',
                            layout: 'vertical',
                            margin: 0,
                            verticalAlign: 'top',
                            y: 25,
                            symbolHeight: 280
                        },

                        tooltip: {
                            formatter: function () {
                                return 'Time: ' + this.series.xAxis.categories[this.point.x] +
                                    '<br>We have collected '+this.point.CollectedMin+' valid data points.'+
                                    '<br>Impacted:<b>'+ this.point.ImpMin +'</b> min'+
                                    '<br><br>APT:  <b>' + this.point.AvgAPT + 'ms' +
                                    '<br>ActSession: <b>' + this.point.AvgActSession +
                                    '<br>Spare indicator:<b>'+ this.point.Spare +'</b>'+
                                    '<br>Traffic: <b>'+ this.point.TrafficValue+'</b>'+
                                    '<br>DB CPU: <b>'+ this.point.CPUValue+'%</b>';
                            }
                        },

                        series: [{
                            borderWidth: 0,
                            //colsize: 24 * 36e5,
                            turboThreshold: Number.MAX_VALUE,
                            data: returnJSON,
                        }],

                    });
                });
            }

            //Dataguard
            /*
             * aruikar@salesforce.com
             * @param: pod Category('na', 'cs', 'eu'), processedJson - JSON from helper function
             * generate string to render as HTML from the JSON lists
             * @returns: string
             */
            var generateHTMLString = function (processedJson, podCat){

                if(processedJson['count'] == 0){
                    return '<h3><span  id="helpBlock" class="help-block">'+ podCat.toUpperCase()+': - (0/0)</span></h3>'
                }

                var defaulterInfo = processedJson['defaulterInfo'];
                var defaulterInfoStr = '';

                for(var pod in defaulterInfo){
                    defaulterInfoStr += defaulterInfo[pod]['podName']+'('+defaulterInfo[pod]['pctCompliance'] +'%), ';
                }

                var missingStr = processedJson['missingInfo'].toString();

                return  '<span  id="helpBlock" class="help-block"><h3>'+ podCat.toUpperCase()+': '+ Math.round(processedJson['complyCount']*100 /processedJson['count'])+'% ('+processedJson['complyCount'] +'/'+ processedJson['count']+')' +
                    '<span style="font-size: 16px"> with '+ Math.round(processedJson['conf']/processedJson['count'])+'% confidence</span>' +
                    '<h4>Pods not met('+ (processedJson['count'] - processedJson['complyCount']) +'): '+ defaulterInfoStr.substr(0, defaulterInfoStr.length-2)+'</h4><h4>'+
                    'Missing Information('+processedJson['missingInfo'].length +'): '+missingStr
                    +'</h4></h3></span>';



            };

            var getDateTime = function (ts) {

                var t = new Date(ts*1000);
                return formateDate(t.getUTCMonth()+1)+"/"+ formateDate(t.getUTCDate())+"/"+ t.getFullYear()+", " +
                    ""+formateDate(t.getUTCHours())+":"+ formateDate(t.getUTCMinutes());

            };

            /*
             * aruikar@salesforce.com
             * @param: pod Category('na', 'cs', 'eu'), processedJson - JSON from helper function
             * generate string(table rows) to render as HTML from the JSON lists
             * @returns: string
             */
            var generateTableRows = function (sortedArray, option, threshold) {


                var rows = '';

                console.log(sortedArray);

                if(option == "prod") {

                    for (var i = sortedArray.length - 1; i > 0; i--) {

                        if ((sortedArray[i]['podName'].indexOf("cs") != -1)) {
                            continue;
                        }
                        else {

                            if (sortedArray[i]['lagValue'] > threshold) {
                                rows += '<tr class="danger">' + '<td>' + sortedArray[i]['podName'].toUpperCase() + '</td><td>' + sortedArray[i]['lagValue'] + '</td><td>' + getDateTime(sortedArray[i]['timestamp']) + '</td>' + '</tr>';
                            }
                            else {
                                rows += '<tr class="success">' + '<td>' + sortedArray[i]['podName'].toUpperCase() + '</td><td>' + sortedArray[i]['lagValue'] + '</td><td>' + getDateTime(sortedArray[i]['timestamp']) + '</td>' + '</tr>';
                            }
                        }

                    }
                }
                else{

                    for (var i = sortedArray.length - 1; i > 0; i--) {

                        if ((sortedArray[i]['podName'].indexOf("cs") != -1)) {

                            if (sortedArray[i]['lagValue'] > threshold) {
                                rows += '<tr class="danger">' + '<td>' + sortedArray[i]['podName'].toUpperCase() + '</td><td>' + sortedArray[i]['lagValue'] + '</td><td>' + getDateTime(sortedArray[i]['timestamp']) + '</td>' + '</tr>';
                            }
                            else {
                                rows += '<tr class="success">' + '<td>' + sortedArray[i]['podName'].toUpperCase() + '</td><td>' + sortedArray[i]['lagValue'] + '</td><td>' + getDateTime(sortedArray[i]['timestamp']) + '</td>' + '</tr>';
                            }
                        }

                    }

                }

                return rows;

            };

            /*
             * aruikar@salesforce.com
             * @param: none
             * trending series for a pod over a given time range.
             * 4 charts if pod has DR, else only 2 charts
             * @returns: void
             */
            function drawPodTimeseries(){
                $('#'+divId).html('');

                URL = CONFIG.grahiteUrl+'getPodDgLag/?startTime='+StartTime+'&endTime='+EndTime+'&Pod='+Pod;

                //arrays to hold data
                var chartDataLocalTr = [];
                var chartDataRemoteTr = [];
                var chartDataRemoteApply = [];
                var chartDataLocalApply = [];

                $.getJSON(URL, function (data) {

                    if(data['status'] == "FAILED"){
                        alert("Couldn't service the request :/");
                        return;
                    }

                    var pts = data['datapoints'];

                    for(var pt in pts){
                        temp = [];
                        temp1 = [];
                        temp2 = [];
                        temp3 = [];

                        temp.push(parseInt(pts[pt]["Timestamp"])*1000);
                        temp1.push(parseInt(pts[pt]["Timestamp"])*1000);

                        // no DR
                        if(pts[pt]["lags"][0] == -1)
                        {
                            temp.push(null);
                        }else {
                            temp.push(pts[pt]["lags"][0]);
                        }

                        if(pts[pt]["lags"][1] == -1)
                        {
                            temp1.push(null);
                        }else {
                            temp1.push(pts[pt]["lags"][1]);
                        }

                        if('X' != data['DR'] ){

                            temp2.push(parseInt(pts[pt]["Timestamp"])*1000);
                            temp3.push(parseInt(pts[pt]["Timestamp"])*1000);

                            if(pts[pt]["lags"][2] == -1)
                            {
                                temp2.push(null);
                            }else {
                                temp2.push(pts[pt]["lags"][2]);
                            }

                            if(pts[pt]["lags"][3] == -1)
                            {
                                temp3.push(null);
                            }else {
                                temp3.push(pts[pt]["lags"][3]);
                            }

                            chartDataRemoteTr.push(temp3);
                            chartDataRemoteApply.push(temp2);
                        }
                        chartDataLocalTr.push(temp1);
                        chartDataLocalApply.push(temp);
                    }

                    var siteInfo = '';
                    if(data['DR'] == 'X') {
                        siteInfo = ', Primary: '+data['PR'] + ', DR: -';
                    }else{
                        siteInfo = ', Primary: '+data['PR'] + ', DR: '+data['DR'];
                    }

                    $('#'+divId).append('<h3><span id="helpBlock" class="help-block">Pod: '+Pod+ siteInfo +'</span></h3>');

                    if(data['DR'] != 'X') {
                        drawPodDGlagTimeseries(divId, 'container3', chartDataRemoteTr, '(remote transport lag)', parseInt(thresholdLine));
                        drawPodDGlagTimeseries(divId, 'container4', chartDataRemoteApply, '(remote apply lag)', parseInt(thresholdLine));
                    }
                    drawPodDGlagTimeseries(divId, 'container1', chartDataLocalTr, '(local transport lag)', parseInt(thresholdLine));
                    drawPodDGlagTimeseries(divId, 'container2', chartDataLocalApply, '(local apply lag)', parseInt(thresholdLine));

                });
            }

            /*
             * aruikar@salesforce.com
             * @param: none
             * trending series for a pod over a given time range.
             * 4 charts if pod has DR, else only 2 charts
             * @returns: void
             */
            function drawMaxLagReport(){
                $('#'+divId).html('');
                $('#prodTable').html('');
                $('#csTable').html('');
                $('#'+divId).html('<img src="img/spin.gif" />');

                URL = CONFIG.grahiteUrl+'getMaxLag/?startTime='+StartTime+'&endTime='+EndTime+'&lagType=remoteTransportLag';

                $.getJSON(URL, function (data) {
                    //console.log(data);
                    $('#'+divId).html('');

                    var sortedArr = sortMaxLagDataPoints(data['data']);

                    $('#prodTable').append('<h3><span id="helpBlock" class="help-block">Production Pods(Max Remote Tr lag Report)</span></h3>');

                    $('#prodTable').append('<table class="table table-bordered table-hover">' +
                        '<thead><th>Production Pod</th> ' +
                        '<th>Max Lag(<i>sec</i>)</th>' +
                        '<th>Date-time</th> ' +
                        '</thead>' +
                        '<tbody>'+
                        generateTableRows(sortedArr, "prod", prodThreshold) +
                        '</tbody>' +
                        '</table>');

                    $('#csTable').append('<h3><span id="helpBlock" class="help-block">Sandbox Pods(Max Remote Tr lag Report)</span></h3>');
                    $('#csTable').append('<table class="table table-bordered table-hover">' +
                        '<thead><th>Sandbox Pod</th> ' +
                        '<th>Max Lag(<i>sec</i>)</th>' +
                        '<th>Date-time</th> ' +
                        '</thead>' +
                        '<tbody>'+
                        generateTableRows(sortedArr, "cs", csThreshold) +
                        '</tbody>' +
                        '</table>');
                })

            }

            /*
             * aruikar@salesforce.com
             * @param: none
             * data-guard lag SLA information table
             * @returns: void
             */
            function drawDGoWANSLAtable(){

                $('#'+divId).html('');
                var IGLpods = [];
                var IGLlist = IGLstr.split(',');
                // make IGL list if filter is "ON"
                if($('input[type=radio]:checked').val() != 0){

                    for(pod in IGLlist){
                        IGLpods.push(IGLlist[pod].trim());
                    }

                }

                URL = CONFIG.grahiteUrl+'getDGoWANSLA/?startTime='+StartTime+'&endTime='+EndTime+'&threshold='+dgLagThreshold;

                $.getJSON(URL, function(data){

                    if(!data['remoteTr'] || !data['remoteApply']){
                        alert('No information available :/\nCannot render Table');
                        return;
                    }

                    if(data['remoteTr']['status'] == "FAILED" || data['remoteApply']['status'] == "FAILED" ){
                        alert('No information available :/\nCannot render Table');
                        return;
                    }

                    var sDate = moment(StartTime, "YYYYMMDD.HH:mm");
                    var eDate = moment(EndTime, "YYYYMMDD.HH:mm");
                    var reportDetails = '<h3><span id="helpBlock" class="help-block">Dataguard lag report from '+sDate.format('DD MMM YYYY HH:mm:ss')+' to '+eDate.format('DD MMM YYYY HH:mm:ss')+'</span></h3>';
                    var note = '<h5><span id="helpBlock" class="help-block">Percentage pods complying to SLA(lag <= '+dgLagThreshold+'sec, '+ SLApc +'% of time). </span></h5>';



                    var processedJsonNA = DGoWANSLA(data['remoteTr'], 'na', IGLpods, parseFloat(SLApc));
                    var processedJsonAP = DGoWANSLA(data['remoteTr'], 'ap', IGLpods, parseFloat(SLApc));
                    var processedJsonEU = DGoWANSLA(data['remoteTr'], 'eu', IGLpods, parseFloat(SLApc));
                    var processedJsonCS = DGoWANSLA(data['remoteTr'], 'cs', IGLpods, parseFloat(SLApc));

                    var prodTotalCount = processedJsonNA['count'] + processedJsonAP['count']  + processedJsonEU['count'];
                    var prodTotalComplyCount = processedJsonNA['complyCount'] + processedJsonAP['complyCount']  + processedJsonEU['complyCount'];



                    var processedJsonNA_apply = DGoWANSLA(data['remoteApply'], 'na', IGLpods, parseFloat(SLApc));
                    var processedJsonAP_apply = DGoWANSLA(data['remoteApply'], 'ap', IGLpods, parseFloat(SLApc));
                    var processedJsonEU_apply = DGoWANSLA(data['remoteApply'], 'eu', IGLpods, parseFloat(SLApc));
                    var processedJsonCS_apply = DGoWANSLA(data['remoteApply'], 'cs', IGLpods, parseFloat(SLApc));
                    var prodTotalCount_apply = processedJsonNA_apply['count'] + processedJsonAP_apply['count']  + processedJsonEU_apply['count'];
                    var prodTotalComplyCount_apply = processedJsonNA_apply['complyCount'] + processedJsonAP_apply['complyCount']  + processedJsonEU_apply['complyCount'];



                    $('#'+divId).append(reportDetails+note+'<br/>'+'<div style="margin: 10px">' +
                        '<table class="table table-bordered table-hover">' +
                        '<thead style="text-align: center"> <tr> <th colspan="" >Aggregation Levels</th> <th colspan="" ><h3>NA</h3></th> <th colspan="" ><h3>AP</h3></th> <th colspan="" ><h3>EU</h3></th> <th colspan=""><h3>CS</h3></th></tr>' +
                        '<tbody id="table"> '+
                        '</tbody>' +
                        '<tr><th rowspan="2" scope="row"><h3>DGoWAN</h3> Remote Transport lag</th>' +
                        '<td>'+generateHTMLString(processedJsonNA, 'na')+'</td>' +
                        '<td>'+generateHTMLString(processedJsonAP, 'ap')+'</td>' +
                        '<td>'+generateHTMLString(processedJsonEU, 'eu')+'</td>' +
                        '<td rowspan="2">'+generateHTMLString(processedJsonCS, 'cs')+'</td>' +
                        '</tr>' +
                        '<td colspan="3"><span id="helpBlock" class="help-block"><h3>production pods: '+  Math.round(prodTotalComplyCount*100.0/prodTotalCount)  +'% ('+ prodTotalComplyCount+ '/' + prodTotalCount+')</h3></span></td>' +
                        '</tr>' +
                        '<tr ><th rowspan="2" scope="row"><h3>DGoWAN</h3> Remote Apply lag</th>' +
                        '<td>'+generateHTMLString(processedJsonNA_apply, 'na')+'</td>' +
                        '<td>'+generateHTMLString(processedJsonAP_apply, 'ap')+'</td>' +
                        '<td>'+generateHTMLString(processedJsonEU_apply, 'eu')+'</td>' +
                        '<td rowspan="2">'+generateHTMLString(processedJsonCS_apply, 'cs')+'</td>' +
                        '</tr>' +
                        '<td colspan="3"><span id="helpBlock" class="help-block"><h3>production pods: '+  Math.round(prodTotalComplyCount_apply*100.0/prodTotalCount_apply)+'% ('+ prodTotalComplyCount_apply+ '/' + prodTotalCount_apply+') </h3></span></td>' +
                        '</tr>' +
                        '<tr><th rowspan="2" scope="row"><h3>SRDF</h3> Local Transport lag</th>' +
                        '<td id="na"></td>' +
                        '<td id="ap"></td>' +
                        '<td id="eu"></td>' +
                        '<td rowspan="2" id="cs"></td>' +
                        '</tr>' +
                        '<td colspan="3" id="prod"><span id="helpBlock" class="help-block"><h3>production pods:</h3></span></td>' +
                        '</tr>' +
                        '</thead>' +
                        '</table>' +
                        '</div>');



                    var srdfURL = CONFIG.grahiteUrl+'getSRDFSLA/?startTime='+StartTime+'&endTime='+EndTime+'&threshold='+dgLagThreshold;
                    $.getJSON(srdfURL, function(data){

                        var processedJsonNA_local = DGoWANSLA(data['localTr'], 'na', IGLpods, parseFloat(SLApc));
                        var processedJsonAP_local = DGoWANSLA(data['localTr'], 'ap', IGLpods, parseFloat(SLApc));
                        var processedJsonEU_local = DGoWANSLA(data['localTr'], 'eu', IGLpods, parseFloat(SLApc));
                        var processedJsonCS_local = DGoWANSLA(data['localTr'], 'cs', IGLpods, parseFloat(SLApc));
                        var prodTotalCount_local = processedJsonNA_local['count'] + processedJsonAP_local['count']  + processedJsonEU_local['count'];
                        var prodTotalComplyCount_local = processedJsonNA_local['complyCount'] + processedJsonAP_local['complyCount']  + processedJsonEU_local['complyCount'];


                        $('#na').html(generateHTMLString(processedJsonNA_local, 'na'));
                        $('#ap').html(generateHTMLString(processedJsonAP_local, 'ap'));
                        $('#eu').html(generateHTMLString(processedJsonEU_local, 'eu'));
                        $('#cs').html(generateHTMLString(processedJsonCS_local, 'cs'));
                        $('#prod').html('<span id="helpBlock" class="help-block"><h3>production pods: '+ Math.round(prodTotalComplyCount_local*100.0/prodTotalCount_local)+'% ('+prodTotalComplyCount_local +'/'+ prodTotalCount_local +')</h3></span>');

                        // glossary for terms
                        var glossary = '<h4><span id="helpBlock" class="help-block">' +
                            '* Confidence - Represents the number of data points available over the given time period</span></h4>';

                        $('#'+divId).append(glossary);

                    });

                });

            }


            function generateTableCells(processedJSON, podCat, threshold){

                if(!processedJSON){
                    return '<h3><span  id="helpBlock" class="help-block">'+ podCat.toUpperCase()+': - (0/0)</span></h3>'
                }

                var complyPods = '';
                var ccount = 0;
                var dcount = 0;
                var defaulters = '';
                var missingInfp = '';
                var confidence = 0.0;
                var mis = 0;


                for(var pod in processedJSON){

                    if(processedJSON[pod]['conf'] == 0.00){

                        missingInfp += processedJSON[pod]['pod'] +', ';
                        mis++;
                        continue;
                    }


                    if(processedJSON[pod]['compliance']  >= threshold ){
                        complyPods += processedJSON[pod]['pod'] + '('+ processedJSON[pod]['compliance'] +'%), ';
                        ccount++;
                        confidence +=  parseFloat(processedJSON[pod]['conf']);
                        console.log("conf_"+processedJSON[pod]['conf']);
                    }
                    else{
                        defaulters += processedJSON[pod]['pod'] + '('+ processedJSON[pod]['compliance'] +'%), ';
                        dcount++;
                        confidence +=  parseFloat(processedJSON[pod]['conf']);
                        console.log("conf_"+processedJSON[pod]['conf']);
                    }
                }


                return  '<span  id="helpBlock" class="help-block"><h3>'+ podCat.toUpperCase()+': ' + Math.round(ccount*100 /(ccount+dcount))+ '% ('+ccount +'/'+ (ccount+dcount)+')' +
                    '<span style="font-size: 16px"> with '+ Math.round(confidence*1.0/(dcount+ccount)*1.0)+'% confidence</span>' +
                    '<h4>'+ 'Pods met '+'('+ ccount+'): '+ complyPods.substr(0, complyPods.length-2) +'</h4>' +
                    '<h4>'+ 'Pods not met '+'('+ dcount+'): '+ defaulters.substr(0, defaulters.length-2) +'</h4>' +
                    '<h4>Missing Information('+ mis +'): '+ missingInfp.substr(0, missingInfp.length -2)
                    +'</h4></h3></span>';

            }

            /*
             *
             * DG lag SLA Argus based
             *
             */
            function drawDGLagSLA(){
                $('#'+divId).html('<img src="img/spin.gif" />');

                //console.log(DRpods);

                //console.log("SLAPC-"+SLApc);

                // var URL=CONFIG.wsUrl+"metrics?expression="+expression;

                var requestURL =  CONFIG.wsUrl + "metrics?expression=" + expression;
                console.log(requestURL);


                $.getJSON(requestURL, function(rawdata){

                    $('#'+divId).html('');
                    //console.log(rawdata);


                    var reportDetails = '<h3><span id="helpBlock" class="help-block">Dataguard lag report</span></h3>';
                    var note = '<h5><span id="helpBlock" class="help-block">Percentage pods complying to SLA( threshold: '+ dgLagThreshold+' <i>sec</i>, '+SLApc+' % of time). </span></h5>';


                    //var conf = "remote_dg_transport_lag";

                    var dgTRObj = {};
                    var dgApplyObj = {};

                    var dgTRObj = lagSpecificList(rawdata, 'remote_dg_transport_lag');
                    var dgApplyObj = lagSpecificList(rawdata, 'remote_dataguard_lag');


                    var podsNA = countPods(dgTRObj['na'] || [], parseFloat(SLApc));
                    var podsAP = countPods(dgTRObj['ap'] || [], parseFloat(SLApc));
                    var podsEU = countPods(dgTRObj['eu'] || [], parseFloat(SLApc));
                    //var podsCS = countPods(dgTRObj['cs'] || [], 99.7);

                    var ApplypodsNA = countPods(dgApplyObj['na'] || [], parseFloat(SLApc));
                    var ApplypodsAP = countPods(dgApplyObj['ap'] || [], parseFloat(SLApc));
                    var ApplypodsEU = countPods(dgApplyObj['eu'] || [], parseFloat(SLApc));
                    //var ApplypodsCS = countPods(dgApplyObj['cs'] || [], 99.7);

                    $('#'+divId).append(reportDetails+note+'<br/>'+'<div style="margin: 10px">' +
                        '<table class="table table-bordered table-hover">' +
                        '<thead style="text-align: center"> <tr> <th colspan="" >Aggregation Levels</th> <th colspan="" ><h3>NA</h3></th> <th colspan="" ><h3>AP</h3></th> <th colspan="" ><h3>EU</h3></th> <th colspan=""><h3>CS</h3></th></tr>' +
                        '<tbody id="table"> '+
                        '</tbody>' +
                        '<tr><th rowspan="2" scope="row"><h3>DGoWAN</h3> Remote Transport lag</th>' +
                        '<td>'+ generateTableCells(dgTRObj['na'] || [], 'na', parseFloat(SLApc)) +'</td>' +
                        '<td>'+ generateTableCells(dgTRObj['ap'] || [], 'ap', parseFloat(SLApc)) +'</td>' +
                        '<td>'+ generateTableCells(dgTRObj['eu'] || [], 'eu', parseFloat(SLApc)) +'</td>' +
                        '<td rowspan="2" id="CS1">'+ generateTableCells(dgTRObj['cs'] || [], 'cs', parseFloat(99.7)) +'</td>' +
                        '</tr>' +
                        '<td colspan="3"><span id="helpBlock" class="help-block"><h3>production pods: '+  Math.round((podsNA['podsComplied']+ podsAP['podsComplied']+ podsEU['podsComplied'])*100.0/(podsNA['totalPods']+ podsEU['totalPods'] + podsAP['totalPods']) )  +'% ('+ (podsNA['podsComplied']+ podsAP['podsComplied']+ podsEU['podsComplied'])  + '/' + (podsNA['totalPods']+ podsEU['totalPods'] + podsAP['totalPods'])+')</h3></span></td>' +
                        '</tr>' +
                        '<tr ><th rowspan="2" scope="row"><h3>DGoWAN</h3> Remote Apply lag</th>' +
                        '<td>'+generateTableCells(dgApplyObj['na'] || [], 'na', parseFloat(SLApc))+'</td>' +
                        '<td>'+generateTableCells(dgApplyObj['ap'] || [], 'ap', parseFloat(SLApc)) +'</td>' +
                        '<td>'+generateTableCells(dgApplyObj['eu'] || [], 'eu', parseFloat(SLApc)) +'</td>' +
                        '<td rowspan="2" id="CS2">'+generateTableCells(dgApplyObj['cs'] || [], 'cs', parseFloat(99.7)) +'</td>' +
                        '</tr>' +
                        '<td colspan="3"><span id="helpBlock" class="help-block"><h3>production pods: '+  Math.round((ApplypodsNA['podsComplied']+ ApplypodsAP['podsComplied']+ ApplypodsEU['podsComplied'])*100.0/(ApplypodsNA['totalPods']+ ApplypodsEU['totalPods'] + ApplypodsAP['totalPods']) )  +'% ('+ (ApplypodsNA['podsComplied']+ ApplypodsAP['podsComplied']+ ApplypodsEU['podsComplied'])  + '/' + (ApplypodsNA['totalPods']+ ApplypodsEU['totalPods'] + ApplypodsAP['totalPods']) + ')</h3></span></td>' +
                        '</tr>' +
                        '</thead>' +
                        '</table>' +
                        '</div>');



                });




            }
        }//////END OF AVA DATA MAP


        function updateTreemap(config, data, divId, optionList, attributes){
            $('#'+divId).html('<img src="img/spin.gif" />');
            var options = getOptionsByTreemapType(config);
            var tempData = {};
            var tempPercentage = 0;
            /* y.li */
            var tempOverallWeightedAverage = 0;

            if (attributes.threshold && attributes.lower && attributes.upper) {

                var low = 'Healthy Pods<br>(threshold exceeded <br> < ' + parseFloat(attributes.lower)*100 + '%)';
                var middle = 'Warning Pods<br>(threshold exceeded <br> between ' + parseFloat(attributes.lower)*100 + '% and '
                    + parseFloat(attributes.upper)*100 + '%)';
                var high = 'Unhealthy Pods<br>(threshold exceeded <br> > ' + parseFloat(attributes.upper)*100 + '%)';

                tempData[low] = {};
                tempData[middle] = {};
                tempData[high] = {};

                if (attributes.filter) data = combineData(data, attributes.filter);

                for (var i = 0; i < data.length; i++) {

                    if ((attributes.method).toUpperCase() === 'THRESHOLD') {
                        tempPercentage = getAbovePercentage(data[i], parseFloat(attributes.threshold));
                    }
                    else if ((attributes.method).toUpperCase() === 'PEAKHOURTHRESHOLD' && attributes.filter) {
                        tempPercentage = getAbovePeakHourPercentage(data[i], parseFloat(attributes.threshold));
                    }

                    if (tempPercentage < parseFloat(attributes.lower))
                        tempData[low][data[i].tags.podId] = tempPercentage;
                    else if (tempPercentage >= parseFloat(attributes.lower) && tempPercentage < parseFloat(attributes.upper))
                        tempData[middle][data[i].tags.podId] = tempPercentage;
                    else if (tempPercentage >= parseFloat(attributes.upper))
                        tempData[high][data[i].tags.podId] = tempPercentage;
                }
                /* y.li */
                tempOverallWeightedAverage = getOverallWeightedAverage(data);

            }

            var points = [],
                level_1_object,
                level_1_num,
                level_2_object,
                level_2_num,
                category,
                pod;

            var colors = ['#00FF00', '#FF8000', '#FF0040'];
            level_1_num = 0;
            for (var category in tempData) {
                if (tempData.hasOwnProperty(category)) {
                    level_1_object = {
                        id: "id_" + level_1_num,
                        name: category,
                        color: colors[level_1_num]
                    };
                    level_2_num = 0;
                    for (var pod in tempData[category]) {
                        if (tempData[category].hasOwnProperty(pod)) {
                            level_2_object = {
                                id: level_1_object.id + "_" + level_2_num,
                                name: pod,
                                parent: level_1_object.id,
                                value: 1,
                                display: tempData[category][pod]
                            };
                            points.push(level_2_object);
                            level_2_num = level_2_num + 1;
                        }
                    }
                    level_1_object.display = level_2_num;
                    points.push(level_1_object);
                    level_1_num = level_1_num + 1;
                }
            }

            options.series[0].data = points;
            options.subtitle = {
                align: 'left',
                verticalAlign: 'top',
                floating: true,
                style: {
                    color: '#303030'
                },
                text: '<span style="font-size: 18px">Weighted (Trust Log Line Count) Average Across All Pods Selected: </span>' +
                '<span style="color: green; font-weight: bold; font-size: 18px">' +
                tempOverallWeightedAverage + '</span> ' +
                '<span style="font-size: 18px"> ms</span>',
                x: 0,
                y: 50
            };

            $('#' + divId).highcharts(options);
            //TODO
        }

        function getOptionsByTreemapType(config) {
            var options = config ? angular.copy(config) : {};
            options.chart = {
                marginTop: 70,
                marginBottom: 70,
                height: 600
            };
            options.series =  [{
                type: 'treemap',
                layoutAlgorithm: 'squarified',
                allowDrillToNode: true,
                dataLabels: {
                    enabled: false
                },
                levelIsConstant: false,
                levels: [{
                    level: 1,
                    dataLabels: {
                        enabled: true,
                        style: {
                            fontSize: '16px',
                            fontWeight: 'bold'
                        }
                    },
                    borderWidth: 2
                }]
            }];
            options.tooltip = {
                formatter: function () {
                    return this.point.display;
                }
            };

            options.title = {
                text: ''
            };
            return options;

        }

        function combineData(data, filter) {
            var result = [];
            if (filter) {
                data.sort(compareTag);
                for (var i = 0; i < data.length - 1; i++) {
                    if (compareTag(data[i], data[i + 1]) === 0) {
                        var metricData, countData;
                        if (data[i].metric === filter) {
                            metricData = data[i + 1];
                            countData = data[i];
                        } else {
                            metricData = data[i];
                            countData = data[i + 1];
                        }
                        var keys = [];
                        for (var j in data[i].datapoints) {
                            if (j in data[i + 1].datapoints) {
                                keys.push(j);
                            }
                        }
                        var metricDatapoints = {};
                        var countDatapoints = {};
                        for (var j = 0; j < keys.length; j++) {
                            metricDatapoints[keys[j]] = metricData.datapoints[keys[j]];
                            countDatapoints[keys[j]] = countData.datapoints[keys[j]];
                        }
                        metricData.datapoints = metricDatapoints;
                        metricData.countDatapoints = countDatapoints;
                        result.push(metricData);
                    }
                }
            } else {
                for (var i = 0; i < data.length; i++) {
                    var countDatapoints = {};
                    for (time in data[i].datapoints) {
                        countDatapoints[time] = 1;
                    }
                    data[i].countDatapoints = countDatapoints;
                    result.push(data[i]);
                }
            }
            return result;
        }

        function compareWeightedAverage(a,b) {
            if (getWeightedAverage(a) < getWeightedAverage(b)) return 1;
            if (getWeightedAverage(a) > getWeightedAverage(b)) return -1;
            return 0;
        }

        function getWeightedAverage(data) {
            var total = 0;
            var count = 0;
            for (var time in data.datapoints) {
                total += parseInt(data.datapoints[time]) * parseInt(data.countDatapoints[time]);
                count += parseInt(data.countDatapoints[time]);
            }
            if (count > 0)
                return total / count;
            else
                return 0;
        }

        function compareAverage(a,b) {
            if (getAverage(a) < getAverage(b)) return 1;
            if (getAverage(a) > getAverage(b)) return -1;
            return 0;
        }

        function getAverage(data) {
            var total = 0;
            var count = 0;
            for (var time in data.datapoints) {
                total += parseInt(data.datapoints[time]);
                count += 1;
            }
            if (count > 0)
                return total / count;
            else
                return 0;
        }

        function compareCount(a,b) {
            if (getCount(a) < getCount(b)) return 1;
            if (getCount(a) > getCount(b)) return -1;
            return 0;
        }

        function getCount(data) {
            var count = 0;
            for (var time in data.datapoints) {
                count += parseInt(data.countDatapoints[time]);
            }
            return count;
        }

        function comparePeakHourAverage(timeSpan, a, b) {
            if (getPeakHourAverage(timeSpan ,a) < getPeakHourAverage(timeSpan, b)) return 1;
            if (getPeakHourAverage(timeSpan, a) > getPeakHourAverage(timeSpan, b)) return -1;
            return 0;
        }

        function getPeakHourAverage(timeSpan, data) {
            var precalculation = getHourlySumAndCount(timeSpan, data);
            var sums = precalculation.sums;
            sums.push.apply(sums, sums);
            var counts = precalculation.counts;
            counts.push.apply(counts, counts);
            var maxCount = 0;
            var average = 0;
            for (var i = 0; i < timeSpan.span; i++) {
                var countsInterval = counts.slice(i, i + 10);
                var sumsInterval = sums.slice(i, i + 10);
                var thisCount = countsInterval.reduce(function(a, b) {
                    return a + b;
                });
                var thisSum = sumsInterval.reduce(function(a, b) {
                    return a + b;
                });
                if (thisCount > maxCount) {
                    maxCount = thisCount;
                    average = thisSum / thisCount;
                }
            }
            return average;
        }

        function getHourlySumAndCount(timeSpan, data) {
            var sums = Array.apply(null, Array(timeSpan.span)).map(Number.prototype.valueOf,0);
            var counts = Array.apply(null, Array(timeSpan.span)).map(Number.prototype.valueOf,0);
            var pivotHour = Math.floor(timeSpan.begin / 1000 / 60 / 60);
            for (var time in data.datapoints) {
                var hour = Math.floor(parseInt(time) / 1000 / 60 / 60);
                sums[hour - pivotHour] += parseInt(data.datapoints[time]) * parseInt(data.countDatapoints[time]);
                counts[hour - pivotHour] += parseInt(data.countDatapoints[time]);
            }
            var result = {};
            result.sums = sums;
            result.counts = counts;
            return result;
        }

        function getTimeSpan(data) {
            var begin = 9999999999999;
            var end = 0;
            for (var i = 0; i < data.length; i++) {
                for (var time in data[i].datapoints) {
                    begin = Math.min(begin, parseInt(time));
                    end = Math.max(end, parseInt(time));
                }
            }
            var span = Math.floor(end/1000/60/60) - Math.floor(begin/1000/60/60) + 1;
            return {begin: begin, end: end, span: span};
        }

        function compareTag(a, b) {
            if (JSON.stringify(a.tags) < JSON.stringify(b.tags)) return 1;
            if (JSON.stringify(a.tags) > JSON.stringify(b.tags)) return -1;
            return 0;
        }

        function compareAbovePercentage(threshold,a, b) {
            if (getAbovePercentage(a, threshold) < getAbovePercentage(b, threshold)) return 1;
            if (getAbovePercentage(a, threshold) > getAbovePercentage(b, threshold)) return -1;
            return 0;
        }

        function getAbovePercentage(data, threshold) {
            var total = 0;
            var above = 0;
            for (var time in data.datapoints) {
                total += 1;
                if (parseInt(data.datapoints[time]) >= threshold) {
                    above += 1;
                }
            }
            if (total > 0)
                return above / total;
            else
                return 0;
        }

        /* y.li */

        function getOverallWeightedAverage(data) {
            var sum = 0;
            var count = 0;
            for (var i = 0; i < data.length; i++) {
                for (var time in data[i].datapoints) {
                    sum += parseInt(data[i].datapoints[time]) * parseInt(data[i].countDatapoints[time]);
                    count += parseInt(data[i].countDatapoints[time]);
                }
            }
            return +(sum / count).toFixed(2);

        }

        function getAbovePeakHourPercentage(data, threshold) {
            //TODO
            var counts = Array.apply(null, Array(24)).map(Number.prototype.valueOf,0);
            for (var time in data.countDatapoints) {
                var hour = Math.floor(parseInt(time) / 1000 / 60 / 60) % 24;
                counts[hour] += parseInt(data.countDatapoints[time]);
            }
            counts.push.apply(counts, counts);
            var maxCount = 0;
            var startHour = 0;
            for (var i = 0; i < 24; i++) {
                var countsInterval = counts.slice(i, i + 10);
                var thisCount = countsInterval.reduce(function(a, b) {
                    return a + b;
                });
                if (thisCount > maxCount) {
                    maxCount = thisCount;
                    startHour = i;
                }
            }

            var total = 0;
            var above = 0;
            for (var time in data.datapoints) {
                var hour = Math.floor(parseInt(time) / 1000 / 60 / 60) % 24;
                if ((hour >= startHour && hour < startHour + 10)
                    || (hour + 24 >= startHour && hour + 24 < startHour + 10)) {
                    total += 1;
                    if (parseInt(data.datapoints[time]) >= threshold) {
                        above += 1;
                    }
                }
            }
            if (total > 0)
                return above / total;
            else
                return 0;

        }

        function convertHour(data) {
            hourlyDatapoints = {};
            hourlyCountDatapoints = {};
            for (var time in data.datapoints) {
                var hourlyTime = Math.floor(parseInt(time)/36e5)*36e5+18e5;
                if (hourlyDatapoints.hasOwnProperty(hourlyTime)) {
                    hourlyDatapoints[hourlyTime] += parseInt(data.datapoints[time]) * parseInt(data.countDatapoints[time]);
                    hourlyCountDatapoints[hourlyTime] += parseInt(data.countDatapoints[time]);
                } else {
                    hourlyDatapoints[hourlyTime] = parseInt(data.datapoints[time]) * parseInt(data.countDatapoints[time]);
                    hourlyCountDatapoints[hourlyTime] = parseInt(data.countDatapoints[time]);
                }
            }
            for (var time in hourlyDatapoints) {
                var avg = hourlyDatapoints[time] / hourlyCountDatapoints[time];
                hourlyDatapoints[time] = avg;
            }
            data.datapoints = hourlyDatapoints;
            data.countDatapoints = hourlyCountDatapoints;
            return data;
        }

        function abbreviateNumber(value) {
            var newValue = value;
            if (value >= 1000) {
                var suffixes = ["", "k", "m", "b","t"];
                var suffixNum = Math.floor( (""+value).length/3 );
                var shortValue = '';
                for (var precision = 2; precision >= 1; precision--) {
                    shortValue = parseFloat( (suffixNum != 0 ? (value / Math.pow(1000,suffixNum) ) : value).toPrecision(precision));
                    var dotLessShortValue = (shortValue + '').replace(/[^a-zA-Z 0-9]+/g,'');
                    if (dotLessShortValue.length <= 2) { break; }
                }
                if (shortValue % 1 != 0)  shortNum = shortValue.toFixed(1);
                newValue = shortValue+suffixes[suffixNum];
            }
            return newValue;
        }

        function copySeries(data) {
            var result = [];
            if (data) {
                for (var i = 0; i < data.length; i++) {
                    var series = $.map(data[i].datapoints, function (value, key) {
                        if(value != null)
                            return [[parseInt(key), parseFloat(value)]];
                        else return [[parseInt(key), value]];
                    });
                    result.push({name: createSeriesName(data[i]), data: series});
                }
            } else {
                result.push({name: 'result', data: []});
            }
            return result;
        };

        function createSeriesName(metric) {
            var scope = metric.scope;
            var name = metric.metric;
            var tags = createTagString(metric.tags);
            return scope + ':' + name + tags;
        };

        function createTagString(tags) {
            var result = '';
            if (tags) {
                var tagString ='';
                for (var key in tags) {
                    if (tags.hasOwnProperty(key)) {
                        tagString += (key + '=' + tags[key] + ',');
                    }
                }
                if(tagString.length) {
                    result += '{';
                    result += tagString.substring(0, tagString.length - 1);
                    result += '}';
                }
            }
            return result;
        };

        function populateAnnotations(annotationsList, chart){
            if (annotationsList && annotationsList.length>0 && chart) {
                for (var i = 0; i < annotationsList.length; i++) {
                    addAlertFlag(annotationsList[i],chart);
                }
            }
        };

        function addAlertFlag(annotationExpression,chart) {
            Annotations.query({expression: annotationExpression}, function (data) {
                if(data && data.length>0) {
                    var forName = createSeriesName(data[0]);
                    var series = copyFlagSeries(data);
                    series.linkedTo = forName;

                    for(var i=0;i<chart.series.length;i++){
                        if(chart.series[i].name == forName){
                            series.color = chart.series[i].color;
                            break;
                        }
                    }

                    chart.addSeries(series);
                }
            });
        };

        function copyFlagSeries(data) {
            var result;
            if (data) {
                result = {type: 'flags', shape: 'circlepin', stackDistance: 20, width: 16, lineWidth: 2};
                result.data = [];
                for (var i = 0; i < data.length; i++) {
                    var flagData = data[i];
                    result.data.push({x: flagData.timestamp, title: 'A', text: formatFlagText(flagData.fields)});
                }
            } else {
                result = null;
            }
            return result;
        };

        function formatFlagText(fields) {
            var result = '';
            if (fields) {
                for (var field in fields) {
                    if (fields.hasOwnProperty(field)) {
                        result += (field + ': ' + fields[field] + '<br/>');
                    }
                }
            }
            return result;
        };

        function setCustomOptions(options,optionList){
            for(var idx in optionList) {
                var propertyName = optionList[idx].name;
                var propertyValue = optionList[idx].value;
                var result = constructObjectTree(propertyName, propertyValue);
                copyProperties(result,options);
            }
            return options;
        }

        function copyProperties(from,to){
            for (var key in from) {
                if (from.hasOwnProperty(key)) {
                    if(!to[key] || typeof from[key] == 'string' || from[key] instanceof String ){//if from[key] is not an object and is last property then just copy so that it will overwrite the existing value
                        to[key]=from[key];
                    }else{
                        copyProperties(from[key],to[key]);
                    }
                }
            }
        }

        //It constructs the object tree.
        function constructObjectTree(name, value) {
            var result = {};
            var index = name.indexOf('.');
            if (index == -1) {
                result[name] = value;
                return result;
            } else {
                var property = name.substring(0, index);
                result[property] = constructObjectTree(name.substring(index + 1), value);
                return result;
            }
        };

        //The new heatmap function
        function updateHeatmap(config, data, divId, optionList, attributes) {
            if(data && data.length>0) {
                var top = attributes.top? parseInt(attributes.top) : data.length;
                var options = getOptionsByHeatmapType(config, top);
                var timeSpan = getTimeSpan(data);
                data = combineData(data, attributes.filter);
                if (attributes.filter && attributes.frequency) {
                    for (var i = data.length - 1; i >= 0; i--) {
                        if (getCount(data[i]) / timeSpan.span < parseInt(attributes.frequency)) {
                            data.splice(i, 1);
                        }
                    }
                }
                var method = attributes.method ? (attributes.method).toUpperCase() : 'AVERAGE';
                switch (method) {
                    case 'AVERAGE':
                        data.sort(compareAverage);
                        break;
                    case 'WEIGHTEDAVERAGE':
                        data.sort(compareWeightedAverage);
                        break;
                    case 'COUNT':
                        data.sort(compareCount);
                        break;
                    case 'PEAKHOURAVERAGE':
                        if (timeSpan.span >= 10) data.sort(comparePeakHourAverage.bind(null, timeSpan));
                        else data.sort(compareWeightedAverage);
                        break;
                    default:
                        growl.info('Invalid method attribute.');
                }
                // TODO
                data = data.slice(0, Math.min(top, data.length));
                data = data.map(convertHour);
                var orgAxis = data.map(createSeriesName);
                var dataSeries = [];
                for (var i = 0; i < data.length; i++) {
                    for (var time in data[i].datapoints) {
                        dataSeries.push([parseInt(time), data.length - i - 1, parseInt(data[i].datapoints[time])]);
                    }
                }
                options.series[0].data = dataSeries;
                options.yAxis.categories = orgAxis.reverse();
                setCustomOptions(options,optionList);
                Highcharts.setOptions({
                    global: {
                        useUTC: false
                    }
                });
                $('#' + divId).highcharts(options);
            }else {
                $('#' + divId).highcharts('StockChart', getOptionsByChartType(config, 'LINE'));
            }
        }

        function getOptionsByHeatmapType(config, top){
            var options = config ? angular.copy(config) : {};

            options.credits = {enabled: false};

            options.chart = {
                type: 'heatmap',
                marginTop: 0,
                marginBottom: 100
            };
            options.title = {text: ''};
            options.yAxis = {
                categories: null,
                title: null
            };

            options.chart.height = 30*top+100;
            options.xAxis = {
                type: 'datetime',
                tickInterval: 36e5
            };
            options.yAxis.labels = {
                formatter: this.value,
                useHTML: false
            }
            options.colorAxis = {
                dataClassColor: 'category',
                dataClasses: [{
                    from: 0,
                    to: 300,
                    color: '#00FF00'
                },{
                    from:300,
                    to:400,
                    color:'#FF8000'
                },{
                    from:400,
                    color:'#FF0040'
                }]
            };
            options.legend = {
                enabled: true
            };
            options.tooltip = {
                formatter: function () {
                    var options = {
                        weekday: "short", year: "numeric", month: "short",
                        day: "numeric", hour: "2-digit", minute: "2-digit"
                    };
                    var expression = (this.point.x - 18e5).toString() + ':' + (this.point.x + 18e5).toString() + ':';
                    return 'From <b>'
                        + (new Date(this.point.x - 18e5)).toLocaleTimeString("en-us", options) + '</b><br>to <b>'
                        + (new Date(this.point.x + 18e5)).toLocaleTimeString("en-us", options) + '</b>: <br><b>'
                        + this.point.value + '</b>';
                },
                useHTML: true
            };
            options.series = [{
                colsize: 36e5,
                name: '',
                borderWidth: 1,
                data: null,
                dataLabels: {
                    enabled: true,
                    color: 'black',
                    style: {
                        textShadow: 'none',
                        HcTextStroke: null,
                    },
                    formatter: function() {
                        return abbreviateNumber(this.point.value);
                    }
                }
            }];
            return options;
        }

        // 'populateSeries' below makes same API call, refactor both to separate factories
        this.getMetricData = function(metricExpression) {
            if (!metricExpression) return;

            var metricData =
                $http({
                    method: 'GET',
                    url: CONFIG.wsUrl + 'metrics',
                    params: {'expression': metricExpression}
                }).
                success(function(data, status, headers, config) {
                    if ( data && data.length > 0 ) {
                        return data[0];
                    } else{
                        growl.info('No data found for the metric expression: ' + JSON.stringify(metricExpression));
                        return;
                    }
                }).
                error(function(data, status, headers, config) {
                    growl.error(data.message);
                    return;
                });

            return metricData;
        };

        this.augmentExpressionWithControlsData = function(event, expression, controls) {
            var result = expression;

            for (var controlIndex in controls) {
                var controlName = '\\$' + controls[controlIndex].name + '\\$';
                var controlValue = controls[controlIndex].value;
                var controlType = controls[controlIndex].type;
                if ( controlType === "agDate" ) {
                    controlValue = isNaN(Date.parse(controlValue)) ? controlValue : Date.parse(controlValue);
                }
                controlValue = controlValue == undefined ? "" : controlValue;
                result = result.replace(new RegExp(controlName, "g"), controlValue);
            }

            result = result.replace(/(\r\n|\n|\r|\s+)/gm, "");
            return result;
        };

        this.updateIndicatorStatus = updateIndicatorStatus;

        this.buildViewElement = buildViewElement;

        function populateChart(metricList, annotationExpressionList, optionList, divId, attributes, elementType, scope){
            var objMetricCount = {};
            objMetricCount.totalCount = metricList.length;
            $('#' + divId).empty();
            $('#' + divId).show();

            // 'smallChart' currently viewed in the 'Services Dashboard'
            var smallChart = attributes.smallchart ? true : false;
            var chartType = attributes.type ? attributes.type : 'LINE';
            var highChartOptions = getOptionsByChartType(CONFIG, chartType, smallChart);

            setCustomOptions(highChartOptions, optionList);

            $('#' + divId).highcharts('StockChart', highChartOptions);

            var chart = $('#' + divId).highcharts('StockChart');

            // show loading spinner & hide 'no data message' during api request
            chart.showLoading();
            chart.hideNoData();

            // define series first; then build list for each metric expression
            var series = [];

            for (var i = 0; i < metricList.length; i++) {
                var metricExpression = metricList[i].expression;
                var metricOptions = metricList[i].metricSpecificOptions;

                // make api call to get data for each metric item
                populateSeries(metricList[i], highChartOptions, series, divId, attributes, annotationExpressionList, objMetricCount);
            }
            //populateAnnotations(annotationExpressionList, chart);
        };

        function updateIndicatorStatus(attributes, lastStatusVal) {
            if (lastStatusVal < attributes.lo) {
                $('#' + attributes.name + '-status').removeClass('red orange green').addClass('red');
            } else if (lastStatusVal > attributes.lo && lastStatusVal < attributes.hi) {
                $('#' + attributes.name + '-status').removeClass('red orange green').addClass('orange');
            } else if (lastStatusVal > attributes.hi) {
                $('#' + attributes.name + '-status').removeClass('red orange green').addClass('green');
            }
        };

        function buildViewElement(scope, element, attributes, dashboardCtrl, elementType, index, DashboardService, growl) {
            var elementId = 'element_' + elementType + index;
            var smallChartCss = ( attributes.smallchart ) ? 'class="smallChart"' : '';
            element.prepend('<div id=' + elementId + ' ' + smallChartCss +'></div>');

            scope.$on(dashboardCtrl.getSubmitBtnEventName(), function(event, controls){
                //console.log(dashboardCtrl.getSubmitBtnEventName() + ' event received.');
                populateView(event, controls);
            });

            function populateView(event, controls) {
                var updatedMetricList = [];
                var updatedAnnotationList = [];
                var updatedOptionList = [];
                scope.controls=controls;
                // TODO: move these 3 items to 'utils' folder
                for (var key in scope.metrics) {
                    if (scope.metrics.hasOwnProperty(key)) {

                        // get metricExpression, and name & color attributes from scope
                        var metrics = scope.metrics[key];
                        var metricExpression = metrics.expression;
                        var metricSpecificOptions = metrics.metricSpecificOptions;
                        var processedExpression = DashboardService.augmentExpressionWithControlsData(event, metricExpression, controls);

                        if (processedExpression.length > 0 /* && (/\$/.test(processedExpression)==false) */) {
                            var processedMetric = {};
                            processedMetric['expression'] = processedExpression;
                            processedMetric['name'] = metrics.name;
                            processedMetric['color'] = metrics.color;
                            processedMetric['metricSpecificOptions'] = getMetricSpecificOptionsInArray(metricSpecificOptions);

                            // update metric list with new processed metric object
                            updatedMetricList.push(processedMetric);
                        }
                    }
                }

                for (var key in scope.annotations) {
                    if (scope.annotations.hasOwnProperty(key)) {
                        var processedExpression = DashboardService.augmentExpressionWithControlsData(event, scope.annotations[key],controls);
                        if (processedExpression.length > 0 /* && (/\$/.test(processedExpression)==false) */) {
                            updatedAnnotationList.push(processedExpression);
                        }
                    }
                }

                for (var key in scope.options) {
                    if (scope.options.hasOwnProperty(key)) {
                        updatedOptionList.push({name: key, value: scope.options[key]});
                    }
                }

                if (updatedMetricList.length > 0) {
                    DashboardService.populateView(updatedMetricList, updatedAnnotationList, updatedOptionList, elementId, attributes, elementType, scope);
                } else {
                    growl.error('The valid metric expression(s) is required to display the chart.', {referenceId: 'growl-error'});
                    $('#' + elementId).hide();
                }
            }

            function getMetricSpecificOptionsInArray(metricSpecificOptions){
                var options = [];
                for (var key in metricSpecificOptions) {
                    if (metricSpecificOptions.hasOwnProperty(key)) {
                        options.push({'name': key, 'value': metricSpecificOptions[key]});
                    }
                }
                return options;
            }
        };

        function populateSeries(metricItem, highChartOptions, series, divId, attributes, annotationExpressionList, objMetricCount) {
            $http({
                method: 'GET',
                url: CONFIG.wsUrl + 'metrics',
                params: {'expression': metricItem.expression}
            }).success(function(data, status, headers, config){
                if (data && data.length > 0) {

                    // check to update services dashboard
                    if (attributes.smallchart) {
                        // get last status values & broadcast to 'agStatusIndicator' directive
                        var lastStatusVal = Object.keys(data[0].datapoints).sort().reverse()[0];
                        lastStatusVal = data[0].datapoints[lastStatusVal];
                        // updateServiceStatus(attributes, lastStatusVal);
                        updateIndicatorStatus(attributes, lastStatusVal);
                    }

                    // metric item attributes are assigned to the data (i.e. name, color, etc.)
                    var seriesWithOptions = copySeriesDataNSetOptions(data, metricItem);

                    // add each metric item & data to series list
                    Array.prototype.push.apply(series, seriesWithOptions);

                } else{
                    growl.info('No data found for the metric expression: ' + JSON.stringify(metricItem.expression));
                }

                objMetricCount.totalCount = objMetricCount.totalCount - 1;

                if (objMetricCount.totalCount == 0) {
                    bindDataToChart(divId, highChartOptions, series, annotationExpressionList);
                }
            }).error(function(data, status, headers, config) {
                growl.error(data.message);
                objMetricCount.totalCount = objMetricCount.totalCount - 1;

                if (objMetricCount.totalCount == 0) {
                   bindDataToChart(divId, highChartOptions, series, annotationExpressionList);
                }
            });
        };

        function bindDataToChart(divId, highChartOptions, series, annotationExpressionList) {
            // bind series data to highchart options
            highChartOptions.series = series;

            // display chart in DOM
            $('#' + divId).highcharts('StockChart', highChartOptions);

            var chart = $('#' + divId).highcharts('StockChart');

            // hide the loading spinner after data loads.
            if (chart) {
                chart.hideLoading();
            }

            // check if data exists, otherwise, show the 'no data' message.
            if ( chart && !chart.hasData() ) {
                chart.showNoData();
            }

            populateAnnotations(annotationExpressionList, chart);
        };

        function getMetricExpressionList(metrics){
            var result = [];
            for(var i=0;i<metrics.length; i++){
                result.push(metrics[i].expression);
            }
            return result;
        };

        /**
         * @Author ethan.wang@salesforce.com
         * THis is the starting point of every ava tree
         * @param data
         * @param scope
         * @param divId
         * @param options
         */
        function updateTable(data, scope, divId, options) {
            if(data && data.length > 0) {

                var allTimestamps = {};
                for(var i in data) {
                    var dps = data[i].datapoints;
                    for(var timestamp in dps) {
                        if(!allTimestamps[timestamp]) {
                            allTimestamps[timestamp] = [];
                        }
                    }
                }

                var columns = [{title: "timestamp", value: "Timestamp"}];
                for(var i in data) {
                    var dps = data[i].datapoints;
                    if(dps) {
                        columns.push({
                            title: "value" + i,
                            value: createSeriesName(data[i])
                        });

                        for(var timestamp in allTimestamps) {
                            var values = allTimestamps[timestamp];
                            if(dps[timestamp]) {
                                values.push(parseFloat(dps[timestamp]));
                            } else {
                                values.push(undefined);
                            }
                            allTimestamps[timestamp] = values;
                        }
                    }
                }

                var tData = [];
                for(var timestamp in allTimestamps) {
                    var obj = {
                            timestamp: parseInt(timestamp),
                            date: $filter('date')(timestamp, "medium")
                    };

                    for(var i in columns) {
                        if(columns[i].title !== "timestamp")
                            obj[columns[i].title] = allTimestamps[timestamp][i-1];
                    }
                    tData.push(obj);
                }

                var tableConfig = {
                        itemsPerPage: 10,
                        fillLastPage: true
                };

                for(var i in options) {
                    var option = options[i];
                    if(option.name && option.value)
                        tableConfig[option.name] = option.value;
                }

                scope.tData = tData;
                scope.config = tableConfig;

                var html = '<div style="overflow-x: scroll"><table class="table table-striped table-header-rotated" at-table at-paginated at-list="tData" at-config="config">';

                html += '<thead>';
                html += '<tr>';
                for(var i in columns) {
                    html += '<th class="rotate-45" at-attribute="' + columns[i].title + '"><div><span>' + columns[i].value + '</span></div></th>';
                }
                html += '</tr>';
                html += '</thead>';

                html += '<tbody>';
                html += '<tr>';

                for(var i in columns) {
                    if(columns[i].title === 'timestamp')
                        html += '<td at-sortable at-attribute="' + columns[i].title + '">{{ item.date }}</td>';
                    else
                        html += '<td at-sortable at-attribute="' + columns[i].title + '">{{ item.' + columns[i].title + '}}</td>';
                }

                html += '</tr>';
                html += '</tbody>';

                html += '</table></div>';

                html += '<at-pagination at-list="tData" at-config="config"></at-pagination>';

                $("#" + divId).empty();
                $compile($("#" + divId).prepend(html))(scope);
            }
        };

        function updateChart(config, data, divId, annotationExpressionList, optionList, attributes) {
            var chartType = attributes.type ? attributes.type : 'LINE';

            if (data && data.length > 0) {
                var options = getOptionsByChartType(config,chartType);
                options.series = copySeries(data);
                //options.chart={renderTo: 'container',defaultSeriesType: 'line'};
                setCustomOptions(options,optionList);
                $('#' + divId).highcharts('StockChart', options);
            } else {
                $('#' + divId).highcharts('StockChart', getOptionsByChartType(config, chartType));
            }

            var chart = $('#' + divId).highcharts('StockChart');
            //chart.chart={renderTo: 'container',defaultSeriesType: 'line'};
            //chart.renderTo='container';
            //chart.defaultSeriesType='line';

            populateAnnotations(annotationExpressionList, chart);
        };

        function resetChart(chart){
            chart.zoomOut();
        };

        function getOptionsByChartType(config, chartType, smallChart){
            var options = config ? angular.copy(config) : {};
            options.legend = {
                enabled: true,
                maxHeight: 62,
                itemStyle: {
                    fontWeight: 'normal',
                    fontSize: '10px'
                },
                navigation : {
                    style : {
                        fontWeight: 'normal',
                        fontSize: '10px'
                    }
                }
            };
            options.credits = {enabled: false};
            options.rangeSelector = {selected: 1, inputEnabled: false};
            options.xAxis = {
                type: 'datetime',
                ordinal: false
            };

            options.lang = {
                loading: '',    // override default 'Loading...' msg from displaying under spinner img.
                noData: 'No Data to Display'
            };

            // loading spinner for graph
            options.loading = {
                labelStyle: {
                    top: '25%',
                    backgroundImage: 'url("img/ajax-loader.gif")',
                    backgroundSize: '80px 80px',
                    backgroundRepeat: 'no-repeat',
                    display: 'inline-block',
                    width: '80px',
                    height: '80px',
                    backgroundColor: '#FFF'
                }
            };

            if(chartType && chartType.toUpperCase() === 'AREA'){
                options.plotOptions = {series: {animation: false}};
                options.chart = {animation: false, borderWidth: 1, borderColor: 'lightGray', borderRadius: 5, type: 'area'};
            }else  if(chartType && chartType.toUpperCase() === 'STACKAREA'){
                options.plotOptions = {
                    area: {
                        stacking: 'normal',
                       // lineWidth: 1.5,
                        dataGrouping: {
                            enabled: true//,
                          //  groupPixelWidth: 2
                        },
                        animation: false,
                        marker: {
                            enabled: false
                        }
                    }
                };
                options.chart = {animation: false, borderWidth: 1, borderColor: 'lightGray', borderRadius: 5, type: 'area'};
            }
            else {
                options.plotOptions = {series: {animation: false}};
                options.chart = {animation: false, borderWidth: 1, borderColor: 'lightGray', borderRadius: 5};
            }

            // override options for a 'small' chart, e.g. 'Services Status' dashboard
            if ( smallChart ) {
                options.legend.enabled = false;
                options.rangeSelector.enabled = false;

                options['scrollbar'] = {enabled: false};
                options['navigator'] = {enabled: false};

                options.chart.height = '120';
                options.chart.borderWidth = 0;

                // reset loading options, no spinner required
                options.lang = {
                    loading: 'Loading...'
                };
                options.loading = {};
            }

            return options;
        };

        function getHourlyAverage(timeSpan, data) {
            var sums = Array.apply(null, Array(timeSpan.span)).map(Number.prototype.valueOf,0);
            var counts = Array.apply(null, Array(timeSpan.span)).map(Number.prototype.valueOf,0);
            var pivotHour = Math.floor(timeSpan.begin / 1000 / 60 / 60);
            for (var time in data.datapoints) {
                var hour = Math.floor(parseInt(time) / 1000 / 60 / 60);
                sums[hour - pivotHour] += parseInt(data.datapoints[time]);
                counts[hour - pivotHour] += 1;
            }
            var avgs = [];
            for (var i = 0; i < timeSpan.span; i++) {
                if (counts[i] > 0) avgs.push(sums[i] / counts[i]);
                else avgs.push(null);
            }
            return avgs;
        };

        function copySeriesDataNSetOptions(data, metricItem) {
            var result = [];
            if (data) {
                for (var i = 0; i < data.length; i++) {
                    var series = [];

                    for (var key in data[i].datapoints) {
                        var timestamp = parseInt(key);
                        if (data[i].datapoints[key] != null) {
                            var value = parseFloat(data[i].datapoints[key]);
                            series.push([timestamp, value]);
                        }
                    }

                    var metricName = (metricItem.name) ? metricItem.name : createSeriesName(data[i]);
                    var metricColor = (metricItem.color) ? metricItem.color : null;
                    var objSeries = {
                        name: metricName,
                        color: metricColor,
                        data: series
                    };
                    var objSeriesWithOptions = setCustomOptions(objSeries, metricItem.metricSpecificOptions);

                    result.push(objSeriesWithOptions);
                }
            } else {
                result.push({name: 'result', data: []});
            }
            return result;
        };

        function getParsedValue(value){

            if(value instanceof Object || value.length==0){
                return value;
            }

            if(value=='true'){
                return true;
            }else if(value=='false'){
                return false;
            }else if(!isNaN(value)){
                return parseInt(value);
            }
            return value;
        };
    }]);
