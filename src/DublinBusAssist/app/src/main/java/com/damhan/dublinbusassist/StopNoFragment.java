package com.damhan.dublinbusassist;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public class StopNoFragment extends Fragment {
    EditText textBox;
    Button button;
    TextView text;

    String URL = "http://rtpi.dublinbus.ie/DublinBusRTPIService.asmx?WSDL";
    String NAMESPACE = "http://dublinbus.ie/";
    String SOAP_ACTION = "http://dublinbus.ie/GetRealTimeStopData";
    String METHOD_NAME = "GetRealTimeStopData";
    String PARAMETER_NAME1 = "stopId";
    String PARAMETER_NAME2 = "forceRefresh";

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_search_by_stop_no, container, false);
        textBox = (EditText)v.findViewById(R.id.textBox);
        button = (Button)v.findViewById(R.id.button);
        text = (TextView)v.findViewById(R.id.text);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CallWebService().execute(textBox.getText().toString());
            }
        });

        return v;
    }

    public void setResText(HashMap<String, Long> busMins, TreeMap<String,String> journeyDetails, HashMap<String,Double> countRecords) {
        Log.i("BusMins, JourneyDeets", busMins.toString() + journeyDetails.toString());
        // Create a list from elements of HashMap
        List<Map.Entry<String, Long> > list =
                new LinkedList<Map.Entry<String, Long> >(busMins.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Long> >() {
            public int compare(Map.Entry<String, Long> o1,
                               Map.Entry<String, Long> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Long> temp = new LinkedHashMap<String, Long>();
        for (Map.Entry<String, Long> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        Log.i("Sorted", ""+temp);
        StringBuilder resultString = new StringBuilder();
        // print the sorted hashmap
        for (Map.Entry<String, Long> en : temp.entrySet()) {
            //System.out.println("Key = " + en.getKey() +
              //      ", Value = " + en.getValue());
            if (countRecords.get(en.getKey()) != 0.0) {
                resultString.append("" + journeyDetails.get(en.getKey()) + "     " +  en.getValue() + " ***  " );
            }
            else {
                resultString.append("" + journeyDetails.get(en.getKey()) + "     " +  en.getValue());
            }

            resultString.append(System.getProperty("line.separator"));

        }
        text.setText("Bus  | Minutes: " + System.getProperty("line.separator") + resultString );
}

    class CallWebService extends AsyncTask<String, Void, String> {
        @Override

        protected void onPostExecute(String s) {
            //text.setText("Buses: " + System.getProperty("line.separator") + s );
        }

        @Override
        protected String doInBackground(String... params) {
            SoapObject result;
            int i = 999;
            final StringBuilder resString = new StringBuilder();
            SoapObject soapObject = new SoapObject(NAMESPACE, METHOD_NAME);

            PropertyInfo propertyInfo = new PropertyInfo();
            propertyInfo.setName(PARAMETER_NAME1);
            propertyInfo.setValue(params[0]);
            propertyInfo.setType(String.class);

            PropertyInfo propertyInfo1 = new PropertyInfo();
            propertyInfo1.setName(PARAMETER_NAME2);
            propertyInfo1.setValue("false");
            propertyInfo1.setType(String.class);

            soapObject.addProperty(propertyInfo);
            soapObject.addProperty(propertyInfo1);

            SoapSerializationEnvelope envelope =  new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(soapObject);

            HttpTransportSE httpTransportSE = new HttpTransportSE(URL);

            try {
                httpTransportSE.call(SOAP_ACTION, envelope);
                SoapObject soapPrimitive = (SoapObject) envelope.bodyIn;

                SoapObject body = (SoapObject) soapPrimitive.getProperty(0);
                SoapObject diffGram = (SoapObject) body.getProperty("diffgram");
                SoapObject docEle = (SoapObject) diffGram.getProperty("DocumentElement");
                int propCount = docEle.getPropertyCount();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                final TreeMap<String, String> journeyDetails = new TreeMap<String,String>();
                final HashMap<String, Long> busMinutes = new HashMap<String, Long>();
                final HashMap<String, Double> recordCounts = new HashMap<String,Double>();
                for (i=0; i<propCount ; i++) {
                    final int busNum = i;
                    SoapObject soapData = (SoapObject) docEle.getProperty(i);
                    final String[] busLineNo = {soapData.getPropertyAsString("MonitoredVehicleJourney_PublishedLineName")};
                    String busExpectedArrival = soapData.getPropertyAsString("MonitoredCall_ExpectedArrivalTime");

                    final String journeyRef = soapData.getPropertyAsString("MonitoredVehicleJourney_VehicleRef");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.ENGLISH);
                    Calendar calendar = Calendar.getInstance();
                    Date curTime = calendar.getTime();
                    Log.i("date to parse:", ""+busExpectedArrival);
                    journeyDetails.put(journeyRef,busLineNo[0]);
                    Log.i("journeyDetailsMap",journeyDetails.toString());
                    try {
                        Date date = sdf.parse(busExpectedArrival);
                        long millis = date.getTime() - curTime.getTime();
                        final long diffSec = millis / 1000;
                        final long[] mins = {diffSec / 60};
                        Log.i("duration in  mins", "" + mins[0]);
//2019-04-24T11:13:28.087+01:00  019-04-24T11:12:32.087+01:00


                        Query query = reference.child("busses").orderByChild("JourneyRef").equalTo(journeyRef);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.i("Datachange","datachanged");
                                if (dataSnapshot.exists()) {
                                    // dataSnapshot is the "busses" node with all children with id
                                    int counter = 0;
                                    int diffCounter = 0;
                                    for (DataSnapshot busses : dataSnapshot.getChildren()) {
                                        // do something with the individual "busses.."
                                        counter ++;
                                        Log.i("dbinfo",   "" + counter + "     " + busses.child("JourneyRef").getValue(String.class));
                                        String aimedArrival = busses.child("AimedArrival").getValue(String.class);
                                        String expectedArrival = busses.child("ExpectedArrival").getValue(String.class);
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.ENGLISH);
                                        try {
                                            Date aimed = sdf.parse(aimedArrival);
                                            Date expected = sdf.parse(expectedArrival);
                                            long millis = expected.getTime() - aimed.getTime();
                                            long diffseconds = millis / 1000;
                                            if(diffseconds != 0) {
                                                diffCounter ++;
                                                Log.i("", ""+diffseconds);
                                                Log.i("aimed, expected", aimedArrival + "   " + expectedArrival);
                                            }
                                            else {
                                                Log.i("Aimed, expected", "Matched");
                                            }
                                        }
                                        catch(ParseException e) {
                                            resString.append("    " + " Malformed Data");
                                        }


                                    }
                                    double dd = ((double) diffCounter) / counter;
                                    recordCounts.put(journeyRef,dd);
                                    busMinutes.put(journeyRef,mins[0]);
                                    Log.i("bus minutes", busMinutes.toString());
                                    Log.i("recordCounts", recordCounts.toString());
                                    setResText(busMinutes,journeyDetails, recordCounts);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.i("Error occured", databaseError.toString());
                            }
                        });
                    }
                    catch(ParseException e) {
                        resString.append("    " + " APIError");
                    }
                    resString.append(System.getProperty("line.separator"));
                }






                Log.i("ResLog", resString.toString());
                Log.i("propCount",Integer.toString(propCount));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return resString.toString();
        }

    }
}



