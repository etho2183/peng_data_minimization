package org.dataMiner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Math;
import java.nio.channels.CancelledKeyException;
import java.util.*;

// helper class to represent coordinates
// all data linked to coordinates is saved here
class Coordinate
{
    public double latitude;
    public double longitude;
    public double radius;               // useful for geoFences

    // boolean values representing the applied obfuscation to the data containers
    public boolean accelRounding = false;  // set to 'true', if at least one data sample has been rounded
    public boolean brightRounding = false;
    public boolean compassRounding = false;
    public boolean gyroRounding = false;
    public boolean spatialObfuscation = false;
    public boolean temporalObfuscation = false;
    public boolean maxMinAccel = false;
    public boolean maxMinBright = false;
    public boolean maxMinCompass = false;
    public boolean maxMinGyro = false;
    public boolean avgAccel = false;
    public boolean avgBright = false;
    public boolean avgCompass = false;
    public boolean avgGyro = false;

    public List<Triple> acceleration;   // container for linked acceleration data
    public List<Double> brightness;     // container for linked brightness data
    public List<Double> compass;        // container for linked compass data
    public List<Triple> gyroscope;      // container for linked gyroscope data

    // constructor for coordinates that act as GeoFences
    public Coordinate(double lat, double lon, double radius)
    {
        this.latitude = lat;
        this.longitude = lon;
        this.radius = radius;           // radius in kilometres
        // no initialization of lists, since this type of coordinate is only used as a geoFence
    }

    // constructor for regular coordinates that act as containers for all linked sensor data
    public Coordinate(double lat, double lon)
    {
        this.latitude = lat;
        this.longitude = lon;
        this.brightness = new ArrayList<>();
        this.acceleration = new ArrayList<>();
        this.gyroscope = new ArrayList<>();
        this.compass = new ArrayList<>();
        radius = 0;
    }

    // function taken from http://www.movable-type.co.uk/scripts/latlong.html
    // calculates the distance from 'this' coordinate to another in metres
    public double distanceTo(Coordinate other)
    {
        double earthRadius = 6371000; // metres
        double latRad = Math.toRadians(this.latitude);
        double otherLatRad = Math.toRadians(other.latitude);
        double deltaLatRad = Math.toRadians(other.latitude - this.latitude);
        double deltaLonRad = Math.toRadians(other.longitude - this.longitude);

        double a =  Math.sin(deltaLatRad/2) * Math.sin(deltaLatRad/2) + Math.cos(latRad) *
                    Math.cos(otherLatRad) * Math.sin(deltaLonRad/2) * Math.sin(deltaLonRad/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;
        return dist;
    }

    // if this coordinate is a geoFence, check whether coordinate 'other' is outside the marked area
    // returns 'true', if the 'other' is not inside the forbidden area
    public Boolean checkFence(Coordinate other)
    {
        if ( this.radius == 0) return true;

        if (this.distanceTo(other) <= this.radius) return false;
        else return true;
    }

    // toString function used for testing
    public String toString()
    {
        return "{latitude=" + this.latitude + ", longitude=" + longitude +", radius=" + radius +
                ", brightness=" + this.brightness + ", acceleration=" + this.acceleration +
                ", gyroscope="+ this.gyroscope + ", compass=" + compass + "}";
    }
}

// helper class to represent two values in one object
class Tuple<E>
{
    public E a;
    public E b;

    public Tuple(E a, E b)
    {
        this.a = a;
        this.b = b;
    }

    // toString function used for testing
    public String toString() { return "{a=" + this.a + ", b=" + b + "}"; }

}

// helper class to represent three values in one object
// in this project, all Triple object were of type 'Double'
class Triple
{
    public double x;
    public double y;
    public double z;

    public Triple(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // toString function used for testing
    public String toString()
    {
        return "{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
    }

    // copy function to create a real copy instead of a reference
    public Triple copy()
    {
        Triple t = new Triple(this.x, this.y, this.z);
        return t;
    }
}


public class ClientDataMinimizer
{
    // settings (defaults: enable highest minimization and obfuscation)

    // the rounding function always refers to the roundList* and rondInterval* lists
    private boolean roundAccel = true;          // use rounding function for accelerator values
    private boolean roundBrightness = true;     // use rounding function for brightness values
    private boolean roundCompass = true;        // use rounding function for compass values (boundaries 0-360 will be kept)
    private boolean roundGyro = true;           // use rounding function for gyroscope values

    // lists used for rounding. A list might look like: [-3, -1, 0, 4, 10] and values would be rounded to the nearest number
    // If a list contains only one entry, it it treated as a rounding interval
    private double roundIntervalBrightness;   // rounding list for brightness values
    private double roundIntervalCompass;      // rounding list for compass values
    private double roundIntervalAccel;          // rounding interval for accelerator values
    private double roundIntervalGyro;           // rounding interval for gyroscope values

    // whether only to send maximum and minimum values
    private boolean maxminAccel = false;
    private boolean maxminBrightness = false;
    private boolean maxminGyro = false;
    private boolean maxminCompass = false;

    // whether only to send the average of the collected values
    // Overwrites the maximum and minimum function. If both are 'true', still only the average is sent.
    private boolean avgAccel = true;
    private boolean avgBrightness = true;
    private boolean avgGyro = true;
    private boolean avgCompass = true;

    // whether to apply spatial obfuscation to incoming gps values by 'gpsRadius' in km.
    private boolean obfuscateGPS = true;
    private double gpsRadius = 0.1;

    // whether to use the decentralized identity obfuscation function
    // THIS IS NOT IMPLEMENTED, BUT KEPT FOR THE FUTURE
    private boolean usePeerToPeer = true;

    // whether to apply temporal obfuscation to collected data packets
    private boolean useTempObfuscation = true;
    private boolean timerAsDelay = false;       // if true, use random delay up to 'maxDelay', if false, send at specified times of day, as specified in 'sendingTimes'
    // ^- WARNING: using random delays might break the order of the coordinates. If your service relies on ordered packets and you want to apply temporal obfuscation, consider sending at fixed times
    private List<Integer> sendingTimes;         // list of times of day in minutes since midnight on which all buffered data should be sent.
    // ^-  WARNING: since no actual storage takes place, terminating the application will lead to loss of all buffered data
    private double maxDelay = 720;              // maximum random delay in minutes -> 720 = 12 hours
    Timer timer;

    private List<Coordinate> geoFenceList;      // list of all GeoFences (coordinates with a radius) inside which no data shall be collected

    // buffers for all sensor data that has been collected but yet linked to a coordinate
    private List<Triple> accelBuffer;
    private List<Double> brightnessBuffer;
    private List<Triple> gyroBuffer;
    private List<Double> compassBuffer;

    // buffer of coordinates that are waiting to be sent by the timer, in case temporal obfuscation is enabled
    private List<Coordinate> coordBuffer;

    // function definition of a function that is implemented in the C++ part of the example implementation
    public native void giveDataFeedback(String text);

    // constructor first initializes all buffers and sets default values for rounding
    public ClientDataMinimizer()
    {
        accelBuffer = new ArrayList<>();
        brightnessBuffer = new ArrayList<>();
        gyroBuffer = new ArrayList<>();
        coordBuffer = new ArrayList<>();
        geoFenceList = new ArrayList<>();
        compassBuffer = new ArrayList<>();

        roundIntervalBrightness = 1;
        roundIntervalGyro = 1;
        roundIntervalAccel = 1;
        roundIntervalCompass = 1;

        timer = new Timer();
        sendingTimes = new ArrayList<>();
        sendingTimes.add(0);    // default: send at midnight and
        sendingTimes.add(720);  // send at noon
    }

    public static void main(String[] args)
    {

    }

    // sends the passed list of coordinates to a server
    // NOTICE: The networking functionalities were not implemented yet so all this function does is create the feedback
    // string and drop the data that is supposed to be sent
    private void sendData(List<Coordinate> inputList)
    {
        String feedback = "Data packet containing " + inputList.size() + " entries:\n";
        //construct json String and feedback String that can be sent to server
        if (inputList.isEmpty()) return;

        JSONArray jsonList = new JSONArray();       // actual data to be sent to server
        for (Coordinate c : inputList)
        {
            // transform Coordinate object into JSON object
            JSONObject obj = new JSONObject();
            obj.put("latitude", c.latitude);
            obj.put("longitude", c.longitude);

            // copy coordinate acceleration container to JSON object
            JSONArray accel = new JSONArray();
            for (Triple t : c.acceleration)
            {
                JSONObject a = new JSONObject();
                a.put("x", t.x);
                a.put("y", t.y);
                a.put("z", t.z);
                accel.put(a);
            }
            obj.put("acceleration", accel);

            // copy coordinate brightness container to JSON object
            JSONArray bright = new JSONArray();
            for (Double b : c.brightness)
            {
                bright.put(b);
            }
            obj.put("brightness", bright);

            // copy coordinate gyroscope container to JSON object
            JSONArray gyro = new JSONArray();
            for (Triple t : c.gyroscope)
            {
                JSONObject g = new JSONObject();
                g.put("x", t.x);
                g.put("y", t.y);
                g.put("z", t.z);
                accel.put(g);
            }
            obj.put("gyroscope", gyro);

            // add coordinate JSON object to JSON list that is going to be sent to server
            jsonList.put(obj);

            // append information about sent data to feedback string
            feedback += "\nCoordinate: " + c.latitude + ", " + c.longitude;
            if (c.spatialObfuscation)  feedback += " (obfuscated)";
            if (c.temporalObfuscation) feedback += " (delayed)";
            feedback += "\nAcceleration: " + c.acceleration.size() + " values";
            if (c.accelRounding)    feedback += " (rounded)";
            if (c.avgAccel)         feedback += " (averaged)";
            else if (c.maxMinAccel) feedback += " (only max and min)";
            feedback += "\nBrightness: " + c.brightness.size() + " values";
            if (c.brightRounding)    feedback += " (rounded)";
            if (c.avgBright)         feedback += " (averaged)";
            else if (c.maxMinBright) feedback += " (only max and min)";
            feedback += "\nGyroscope: " + c.gyroscope.size() + " values";
            if (c.gyroRounding)    feedback += " (rounded)";
            if (c.avgGyro)         feedback += " (averaged)";
            else if (c.maxMinGyro) feedback += " (only max and min)";
            feedback += "\nCompass: " + c.compass.size() + " values";
            if (c.compassRounding)    feedback += " (rounded)";
            if (c.avgCompass)         feedback += " (averaged)";
            else if (c.maxMinCompass) feedback += " (only max and min)";
            feedback += "\n";
        }
        inputList.clear();

        // TODO: implement sending data to a server

        // call parent class in C++ to pass it the feedback String
        giveDataFeedback(feedback);
    }

    // compute the average value of a List of Doubles
    private double averageDouble(List<Double> list)
    {
        if (list.size() == 0) return -1;
        double sum = 0;
        for (int i = 0; i < list.size(); i++)
        {
            sum += list.get(i);
        }
        return sum/list.size();
    }

    // compute the average of a List of Triples
    // averages over the x, y and z members and returns each average in the according member of the return object
    private Triple averageTriple(List<Triple> list)
    {
        if (list.size() == 0) return new Triple(-1,-1,-1);
        Triple sum = new Triple(0,0,0);
        for (Triple t : list)
        {
            sum.x += t.x;
            sum.y += t.y;
            sum.z += t.z;
        }
        sum.x = sum.x / list.size();
        sum.y = sum.y / list.size();
        sum.z = sum.z / list.size();
        return sum;
    }

    // add accelerator reading data to the buffer
    public boolean setAccelData(double x, double y, double z)
    {
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z))
        {
            System.out.println("Dropping accel data due to NaN");
            return false;
        }
        Triple accel = new Triple(x, y, z);
        if (roundAccel)
        {
            accel.x = calcRound(x, roundIntervalAccel);
            accel.y = calcRound(y, roundIntervalAccel);
            accel.z = calcRound(z, roundIntervalAccel);
        }
        accelBuffer.add(accel);
        return true;
    }

    // add brightness data to the buffer
    public boolean setBrightData(double brightness)
    {
        // TODO: timestamps are also needed
        if (Double.isNaN(brightness))
        {
            System.out.println("Dropping brightness data due to NaN");
            return false;
        }
        double brightness2 = brightness;
        if (roundBrightness)
        {
            brightness2 = calcRound(brightness2, roundIntervalBrightness);
        }
        brightnessBuffer.add(brightness2);
        return true;
    }

    // add a coordinate and fill its containers with the content of the sensor buffers
    public boolean setGpsData(double latitude, double longitude)
    {
        if (Double.isNaN(latitude) || Double.isNaN(longitude))
        {
            // invalid coordinate results in clearing of all buffers, since it may become irrelevant at the next spatial point
            accelBuffer.clear();
            compassBuffer.clear();
            gyroBuffer.clear();
            brightnessBuffer.clear();
            System.out.println("Dropping coordinate and all related data due to NaN");
            return false;
        }
        Coordinate coord = new Coordinate(latitude, longitude);

        if (obfuscateGPS)
        {
            coord = obfuscateCoordinate(coord);
            coord.spatialObfuscation = true;
        }
        if (geoFenceList != null)
        {
            for (Coordinate c : geoFenceList)
            {
                if (!coord.checkFence(c))
                {
                    // clearing buffers, since it it likely that this data was collected inside a GeoFence
                    accelBuffer.clear();
                    compassBuffer.clear();
                    gyroBuffer.clear();
                    brightnessBuffer.clear();
                    System.out.println("GPS data NOT saved due to geoFences");
                    return false;
                }
            }
        }

        // check whether previously collected sensor data was rounded
        coord.accelRounding = roundAccel;
        coord.brightRounding = roundBrightness;
        coord.compassRounding = roundCompass;
        coord.gyroRounding = roundGyro;

        // flush acceleration buffer into coordinate and apply additional obfuscation
        if (avgAccel && accelBuffer.size() > 0)
        {
            coord.acceleration.add(averageTriple(accelBuffer));
            coord.avgAccel = true;
        }
        else
        {
            if (maxminAccel){
                Tuple<Triple> tuple = maxMinTriple(accelBuffer);
                coord.acceleration.add(tuple.a);
                coord.acceleration.add(tuple.b);
                coord.maxMinAccel = true;
            }
            else copyTripleList(accelBuffer, coord.acceleration);
        }
        accelBuffer.clear();

        // flush brightness buffer into coordinate and apply additional obfuscation
        if (avgBrightness && brightnessBuffer.size() > 0)
        {
            coord.brightness.add(averageDouble(brightnessBuffer));
            coord.avgBright = true;
        }
        else
        {
            if (maxminBrightness){
                Tuple<Double> tuple = maxMinDouble(brightnessBuffer);
                coord.brightness.add(tuple.a);
                coord.brightness.add(tuple.b);
                coord.maxMinBright = true;
            }
            else coord.brightness = new ArrayList<>(brightnessBuffer);
        }
        brightnessBuffer.clear();

        // flush gyroscope buffer into coordinate and apply additional obfuscation
        if (avgGyro && gyroBuffer.size() > 0)
        {
            coord.gyroscope.add(averageTriple(gyroBuffer));
            coord.avgGyro = true;
        }
        else
        {
            if (maxminGyro){
                Tuple<Triple> tuple = maxMinTriple(gyroBuffer);
                coord.gyroscope.add(tuple.a);
                coord.gyroscope.add(tuple.b);
                coord.maxMinGyro = true;
            }
            else copyTripleList(gyroBuffer, coord.gyroscope);
        }
        gyroBuffer.clear();

        // flush compass buffer into coordinate and apply additional obfuscation
        if (avgCompass && compassBuffer.size() > 0)
        {
            coord.compass.add(averageDouble(compassBuffer));
            coord.avgCompass = true;
        }
        else
        {
            if (maxminCompass){
                Tuple<Double> tuple = maxMinDouble(compassBuffer);
                coord.compass.add(tuple.a);
                coord.compass.add(tuple.b);
                coord.maxMinCompass = true;
            }
            else coord.compass = new ArrayList<>(compassBuffer);
        }
        compassBuffer.clear();

        // apply temporal obfuscation
        if (useTempObfuscation)
        {
            coord.temporalObfuscation = true;
            if (timerAsDelay)   // apply a random delay
            {
                // set the timer to use send one coordinate with sendData() after a random delay
                long delay = (long) Math.ceil(Math.random() * maxDelay * 60 * 1000);
                List<Coordinate> oneCoord = new ArrayList<>();
                oneCoord.add(coord);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendData(oneCoord);
                    }
                }, delay);
            }
            else                // send at fixed times
            {
                // it is enough to add the coordinate to the buffer, since the timer should already be set to send its contents at a fixed time
                coordBuffer.add(coord);
            }
        }
        else
        {
            List<Coordinate> oneCoord = new ArrayList<>();
            oneCoord.add(coord);
            sendData(oneCoord);
        }
        return true;
    }

    // add gyroscope data to the buffer
    public boolean setGyroData(double x, double y, double z)
    {
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z))
        {
            System.out.println("Dropping gyro data due to NaN");
            return false;
        }
        Triple gyro = new Triple(x, y, z);
        if (roundGyro)
        {
            gyro.x = calcRound(x, roundIntervalGyro);
            gyro.y = calcRound(y, roundIntervalGyro);
            gyro.z = calcRound(z, roundIntervalGyro);
        }
        gyroBuffer.add(gyro);
        return true;
    }

    // add compass data to the buffer
    public boolean setCompassOrientation(double orientation)
    {
        if (Double.isNaN(orientation))
        {
            System.out.println("Dropping compass data due to NaN");
            return false;
        }
        double result = orientation;
        if (roundCompass)
        {
            result = calcRound(orientation, roundIntervalCompass);
            if (( result >= 360 ) || ( result < 0 )) result = 0;
        }
        compassBuffer.add(result);
        return true;
    }

    // rounds the value based on the related rounding interval
    private double calcRound(double val, double interval)
    {
        if (interval == 0) return 0;
        double mult = 1;
        double value = val;
        if (val < 0)
        {
            value = value * -1;
            mult = -1;
        }
        double higherNum = interval;
        double lowerNum = 0;
        while (!(value <= higherNum) && !(value >= lowerNum))
        {
            lowerNum = higherNum;
            higherNum += interval;
        }
        if ((higherNum - value) <= (value - lowerNum))  value = higherNum;
        else                                            value = lowerNum;

        return value * mult;
    }

    // receives a JSON coded object String that includes all settings
    // an example settings file can be found at https://github.com/etho2183/peng_data_minimization
    public void setSettings(String settings)
    {
        //System.out.println("Settings received: " + settings);
        JSONObject json = new JSONObject(settings);

        // set rounding settings
        roundAccel = getBoolean(json, "roundAccel");
        roundBrightness = getBoolean(json, "roundBrightness");
        roundGyro = getBoolean(json,"roundGyro");
        roundCompass = getBoolean(json, "roundCompass");

        // set rounding parameters
        roundIntervalAccel = getDouble(json,"roundIntervalAccel");
        roundIntervalBrightness = getDouble(json,"roundIntervalBrightness");
        roundIntervalGyro = getDouble(json,"roundIntervalGyro");
        roundIntervalCompass = getDouble(json, "roundIntervalCompass");

        // set averaging settings
        avgAccel = getBoolean(json,"avgAccel");
        avgBrightness = getBoolean(json,"avgBrightness");
        avgGyro = getBoolean(json,"avgGyro");
        avgCompass = getBoolean(json, "avgCompass");

        // set maxMin settings
        maxminAccel = getBoolean(json, "maxminAccel");
        maxminBrightness = getBoolean(json, "maxminBrightness");
        maxminGyro = getBoolean(json, "maxminGyro");
        maxminCompass = getBoolean(json, "maxminCompass");

        // set spatial obfuscation settings
        obfuscateGPS = getBoolean(json,"obfuscateGps");
        gpsRadius = getDouble(json, "gpsRadius");

        // set decentralized obfuscation settings
        usePeerToPeer = getBoolean(json,"peerToPeer");

        // set temporal obfuscation settings
        Boolean useTempObfuscation_old = useTempObfuscation;
        useTempObfuscation = getBoolean(json, "temporalObfuscation");
        Boolean timerAsDelay_old = timerAsDelay;
        timerAsDelay = getBoolean(json, "useDelays");
        maxDelay = getDouble(json, "maxDelay");
        List<Integer> oldTimes = new ArrayList<>(sendingTimes);
        sendingTimes = getIntList(json, "sendingTimes");
        // if a setting changes, reschedule the timer to the new settings
        if ( (useTempObfuscation_old != useTempObfuscation) || (timerAsDelay != timerAsDelay_old) || !oldTimes.equals(sendingTimes))
        {
            if (useTempObfuscation && !timerAsDelay)
            {
                // schedule the next data sending
                // scheduled tasks are not removed. When scheduling all data to be sent at time A and updating it to time B (B after A), then both times will be used (A only once)
                scheduleSending();
            }
        }

        try
        {
            // coordinate List needs special attention
            // TODO: maybe there is a more elegant cast?
            JSONArray jsonFences = json.getJSONArray("geoFences");
            geoFenceList.clear();
            JSONObject currentObj;
            for (int i = 0; i < jsonFences.length(); i++)
            {
                currentObj = jsonFences.getJSONObject(i);
                geoFenceList.add(new Coordinate(currentObj.getDouble("latitude"),
                                                currentObj.getDouble("longitude"),
                                                currentObj.getDouble("radius")));
            }
        }
        catch (JSONException e)
        {
            System.out.println("Coordinate List \"geoFences\" not found");
        }
    }

    // function to read out boolean values from a JSONObject and to handle exceptions
    private Boolean getBoolean(JSONObject o, String key)
    {
        boolean result;
        try
        {
            result = o.getBoolean(key);
        }
        catch (JSONException e)
        {
            System.out.println("Boolean value \"" + key + "\" not found");
            return false;
        }
        return result;
    }

    // function to read out double values from a JSONObject and to handle exceptions
    private Double getDouble(JSONObject o, String key)
    {
        double result;
        try
        {
            result = o.getDouble(key);
        }
        catch (JSONException e)
        {
            System.out.println("Double value \"" + key + "\" not found");
            return 0.1;
        }
        return result;
    }

    // function to read out Lists from a JSONObject and to handle exceptions
    private List<Integer> getIntList(JSONObject o, String key)
    {
        List<Integer> result = new ArrayList<>();
        try
        {
            JSONArray arr = o.getJSONArray(key);
            //for (Object obj : arr)
            //{
            //    result.add( (Integer) obj);
            //}
            for (int i = 0; i < arr.length(); i++)
            {
                result.add( (Integer) arr.get(i));
            }
        }
        catch (JSONException e)
        {
            System.out.println("Integer list \"" + key + "\" not found");
            return new ArrayList<>();
        }
        return result;
    }

    private void scheduleSending()
    {
        if (useTempObfuscation && !timerAsDelay && !sendingTimes.isEmpty())
        {
            // previously scheduled data to be sent is still sent. This means that the default setting (send at noon and midnight) is executed once
            Calendar cal = Calendar.getInstance();
            int now = cal.get(Calendar.HOUR_OF_DAY) * 60  + cal.get(Calendar.MINUTE); // minutes since midnight
            int time = 0;
            for (int i = 0; i < sendingTimes.size(); i++) {
                int current = sendingTimes.get(i);
                // if the current time is past the last entry in sendingTimes, send at the first one
                if (i + 1 == sendingTimes.size()) {
                    time = sendingTimes.get(0);
                    break;
                }
                int next = sendingTimes.get(i + 1);
                if ((now > current) && (now < next)) {
                    time = next;
                    break;
                }
                if (now == current) {
                    time = next;
                }
            }

            if (time < now)     cal.add(Calendar.DATE, 1);
            cal.set(Calendar.HOUR_OF_DAY, time/60);
            cal.set(Calendar.MINUTE, time%60);
            cal.set(Calendar.SECOND, 0);
            //final int offset = time;

            timer.schedule(new TimerTask() {
                               @Override
                               public void run() {
                                   sendData(coordBuffer);
                                   scheduleSending();
                               }
                           }, cal.getTime() );
            System.out.println("Sending data at: " + cal.getTime());
        }
    }

    // function to calculate the minimum and maximum value of a double List
    private Tuple<Double> maxMinDouble(List<Double> list)
    {
        if (!list.isEmpty())
        {
            double min = list.get(0);
            double max = min;
            for (double e : list)
            {
                if (e < min) min = e;
                if (e > max) max = e;
            }
            return new Tuple<>(min, max);
        }
        else return new Tuple<>(0.0,0.0);
    }

    // function to calculate the minimum and maximum value of a Triple List
    // maximum and minimum are judged on the square product of all members (x, y, z)
    private Tuple<Triple> maxMinTriple(List<Triple> list)
    {
        if (!list.isEmpty())
        {
            Triple min = list.get(0);
            double minSquareProduct = min.x * min.x * min.y * min.y * min.z * min.z;
            Triple max = min.copy();
            double maxSquareProduct = max.x * max.x * max.y * max.y * max.z * max.z;
            for (Triple e : list)
            {
                double elemSquareProduct = e.x * e.x * e.y * e.y * e.z * e.z;
                if (elemSquareProduct < minSquareProduct) min = e;
                if (elemSquareProduct > maxSquareProduct) max = e;
            }
            return new Tuple<>(min, max);
        }
        else return new Tuple<>(new Triple(0,0,0), new Triple(0,0,0));
    }

    // return a coordinate inside a given radius of the input coordinate
    // this function does not copy the contents of the sensor containers, so it should be called BEFORE filling them
    // from http://www.movable-type.co.uk/scripts/latlong.html
    private Coordinate obfuscateCoordinate(Coordinate coord)
    {
        double bearing = Math.random() * 360;

        double latRad = Math.toRadians(coord.latitude);
        double lonRad = Math.toRadians(coord.longitude);

        double angDist =  gpsRadius/6371;  // distance / earthRadius
        double newLatitude = Math.asin( Math.sin(latRad) * Math.cos(angDist) +
                                        Math.cos(latRad) * Math.sin(angDist) * Math.cos(bearing) );
        double newLongitude = lonRad + Math.atan2( Math.sin(bearing) * Math.sin(angDist) * Math.cos(latRad),
                                                      Math.cos(angDist) - Math.sin(latRad) * Math.sin(newLatitude));

        return new Coordinate(Math.toDegrees(newLatitude), Math.toDegrees(newLongitude));
    }

    // helper function to copy the contents of one List of Triple objects to another
    private void copyTripleList(List<Triple> origin, List<Triple> target)
    {
        for (Triple t : origin)
        {
            target.add(t.copy());
        }
    }
}
