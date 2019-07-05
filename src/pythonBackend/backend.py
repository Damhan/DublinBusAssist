#!/usr/bin/python
#import sys; sys.path.append('/home/damhanrichardson/.local/lib/python2.7/site-packages')
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
from lxml import etree as ET
from zeep import Client
import re
import xmljson, json
import xmltodict
import time


# Fetch the service account key JSON file contents
cred = credentials.Certificate('dublin-bus-assist-firebase-adminsdk-awbzn-ca13fffe6f.json')
# Initialize the app with a service account, granting admin privileges
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://dublin-bus-assist.firebaseio.com/'
})

ref = db.reference('/busses')


client = Client('http://rtpi.dublinbus.ie/DublinBusRTPIService.asmx?WSDL')
client.settings(raw_response=True)

stopList = [7158, 7158, 7048, 7159, 7388, 7017, 7018, 7030, 7021, 7163, 1893, 1912, 1894, 1895, 1896, 1897, 6088, 1898, 1858, 1859, 1860, 4492, 1861, 1862, 1863, 1864, 1865, 1866, 1850, 1867, 4489, 4747, 1882, 7379, 4903]

def getBusses(client,ref):
	i = 0
	for stop in stopList:
		with client.settings(raw_response = True):
			results = client.service.GetRealTimeStopData(stopId=stop, forceRefresh=False)
		res = xmltodict.parse(results.text)
		stopRes = res['soap:Envelope']['soap:Body']['GetRealTimeStopDataResponse']['GetRealTimeStopDataResult']

		busses = stopRes['diffgr:diffgram']['DocumentElement']['StopData']
		for bus in busses:
		    i += 1
                    print(i)
		    x =  {
			'Number' : bus['MonitoredVehicleJourney_LineRef'],
			'ExpectedArrival' : bus['MonitoredCall_ExpectedArrivalTime'],
			'AimedArrival': bus['MonitoredCall_AimedArrivalTime'],
			'JourneyRef': bus['MonitoredVehicleJourney_VehicleRef'],
			'StopNumber': bus['MonitoredStopVisit_MonitoringRef']
			
		    }
		    print(x)
		    ref.push(x)


while True:

	getBusses(client,ref)
	print("Busses updated, sleeping..")
	time.sleep(120)
	print("Sleep finished, getting busses..")
