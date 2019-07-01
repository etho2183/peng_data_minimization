package org.dataMiner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Math;
import java.util.*;

class Coordinate
{
    public double latitude;
    public double longitude;
    public double radius; // useful for geoFences
    public List<Double> brightness;
    public List<Triple> acceleration;
    public List<Triple> gyroscope;
    public List<Double> compass;

    public Coordinate(double lat, double lon, double radius)
    {
        this.latitude = lat;
        this.longitude = lon;
        this.radius = radius;
        // no initialization of lists, since this type of coordinate is only used as a geoFence
    }


    public Coordinate(double lat, double lon)
    {
        this.latitude = lat;
        this.longitude = lon;
        this.radius = -1;
        this.brightness = new ArrayList<>();
        this.acceleration = new ArrayList<>();
        this.gyroscope = new ArrayList<>();
        this.compass = new ArrayList<>();
    }

    // some functions from http://www.movable-type.co.uk/scripts/latlong.html
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
    public Boolean checkFence(Coordinate other)
    {
        if (this.radius == -1) return true;

        if (this.distanceTo(other) <= this.radius) return false;
        else return true;
    }

    public String toString()
    {
        return "{latitude=" + this.latitude + ", longitude=" + longitude +", radius=" + radius +
                ", brightness=" + this.brightness + ", acceleration=" + this.acceleration +
                ", gyroscope="+ this.gyroscope + ", compass=" + compass + "}";
    }
}

class Tuple<E>
{
    public E a;
    public E b;

    public Tuple(E a, E b)
    {
        this.a = a;
        this.b = b;
    }

    public String toString() { return "{a=" + this.a + ", b=" + b + "}"; }

}

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

    public String toString()
    {
        return "{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
    }

    public Triple copy()
    {
        Triple t = new Triple(this.x, this.y, this.z);
        return t;
    }
}


public class Main
{
    //settings (defaults: all enabled)
    private boolean roundAccel = true;
    private boolean roundBrightness = true;
    private boolean roundGyro = true;
    private boolean roundCompass = true;

    private boolean maxminAccel = true;
    private boolean maxminBrightness = true;
    private boolean maxminGyro = true;
    private boolean maxminCompass = true;

    private boolean avgAccel = true;
    private boolean avgBrightness = true;
    private boolean avgGyro = true;
    private boolean avgCompass = true;

    private boolean obfuscateGPS = true;
    private double gpsRadius = 0.1;
    private boolean usePeerToPeer = true;

    private List<Coordinate> geoFenceList;

    private List<Double> roundListBrightness;
    private double roundIntervalAccel;
    private double roundIntervalGyro;
    private List<Double> roundListCompass;

    private List<Triple> accelBuffer;
    private List<Double> brightnessBuffer;
    private List<Triple> gyroBuffer;
    private List<Double> compassBuffer;
    private List<Coordinate> coordBuffer;

    public native void giveDataFeedback(String text);

    public Main()
    {
        accelBuffer = new ArrayList<>();
        brightnessBuffer = new ArrayList<>();
        gyroBuffer = new ArrayList<>();
        coordBuffer = new ArrayList<>();
        geoFenceList = new ArrayList<>();
        compassBuffer = new ArrayList<>();
    }

    public static void main(String[] args)
    {

    }

    private void sendData()
    {
        String feedback = "";
        //construct json String and feedback String that can be sent to server
        JSONArray list = new JSONArray();
        for (Coordinate c : coordBuffer)
        {
            System.out.println("sending coordinate: " + c);
            JSONObject obj = new JSONObject();
            obj.put("latitude", c.latitude);
            obj.put("longitude", c.longitude);

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

            JSONArray bright = new JSONArray();
            for (Double b : c.brightness)
            {
                bright.put(b);
            }
            obj.put("brightness", bright);

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

            list.put(obj);

            if (feedback != "")     feedback += "\n";
            feedback += "Coordinate: " + c.latitude + ", " + c.longitude +
                        " | acceleration: " + c.acceleration.size() + " values" +
                        " | brightness: " + c.brightness.size() + " values" +
                        " | gyroscope: " + c.gyroscope.size() + " values";

        }
        coordBuffer.clear();

        giveDataFeedback(feedback);
    }

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
            accel.x = calcRound(x, 'a');
            accel.y = calcRound(y, 'a');
            accel.z = calcRound(z, 'a');
        }
        accelBuffer.add(accel);
        return true;
    }

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
            brightness2 = calcRound(brightness2, 'b');
        }
        brightnessBuffer.add(brightness2);
        return true;
    }

    public boolean setGpsData(double latitude, double longitude)
    {
        if (Double.isNaN(latitude) || Double.isNaN(longitude))
        {
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
        }
        if (geoFenceList != null)
        {
            for (Coordinate c : geoFenceList)
            {
                if (!coord.checkFence(c))
                {
                    System.out.println("GPS data NOT saved due to geoFences");
                    return false;
                }
            }
        }

        // flush acceleration buffer into coordinate
        if (avgAccel && accelBuffer.size() > 0)   coord.acceleration.add(averageTriple(accelBuffer));
        else
        {
            if (maxminAccel){
                Tuple<Triple> tuple = maxMinTriple(accelBuffer);
                coord.acceleration.add(tuple.a);
                coord.acceleration.add(tuple.b);
            }
            else copyTripleList(accelBuffer, coord.acceleration);
        }
        accelBuffer.clear();

        // flush brightness buffer into coordinate
        if (avgBrightness && brightnessBuffer.size() > 0)  coord.brightness.add(averageDouble(brightnessBuffer));
        else
        {
            if (maxminBrightness){
                Tuple<Double> tuple = maxMinDouble(brightnessBuffer);
                coord.brightness.add(tuple.a);
                coord.brightness.add(tuple.b);
            }
            else coord.brightness = new ArrayList<>(brightnessBuffer);
        }
        brightnessBuffer.clear();

        if (avgGyro && gyroBuffer.size() > 0)   coord.gyroscope.add(averageTriple(gyroBuffer));
        else
        {
            if (maxminGyro){
                Tuple<Triple> tuple = maxMinTriple(gyroBuffer);
                coord.gyroscope.add(tuple.a);
                coord.gyroscope.add(tuple.b);
            }
            else copyTripleList(gyroBuffer, coord.gyroscope);
        }
        gyroBuffer.clear();

        if (avgCompass && compassBuffer.size() > 0)  coord.compass.add(averageDouble(compassBuffer));
        else
        {
            if (maxminCompass){
                Tuple<Double> tuple = maxMinDouble(compassBuffer);
                coord.compass.add(tuple.a);
                coord.compass.add(tuple.b);
            }
            else coord.compass = new ArrayList<>(compassBuffer);
        }
        compassBuffer.clear();

        coordBuffer.add(coord);
        return true;
    }

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
            gyro.x = calcRound(x, 'g');
            gyro.y = calcRound(y, 'g');
            gyro.z = calcRound(z, 'g');
        }
        gyroBuffer.add(gyro);
        return true;
    }

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
            result = calcRound(orientation, 'c');
        }
        compassBuffer.add(result);
        return true;
    }

    // returns an obfuscated value based on the first (lowest) entry of the roundList that is higher than the value
    // if the roundList is empty, return 0
    private double calcRound(double val, char type)
    {
        double interval = 1;
        if (( type == 'b' ) ||( type=='c' ))
        {
            List<Double> rounds = new ArrayList<>();
            if ( type == 'b' )    rounds = roundListBrightness;
            if ( type == 'c' )    rounds = roundListCompass;
            if ( rounds.isEmpty() ) return 0;
            double value = val;
            if (rounds.size() == 0)
            {
                return 0;
            }
            if (rounds.size() == 1)
            {
                if ( type == 'b' )  return calcRound(val, 'B');
                if ( type == 'c' )  return calcRound(val, 'C');
                return 0; // should never happen
            }
            else
            {
                double roundElemA = 0;
                double roundElemB = 0;
                for (int i = 0; i < rounds.size(); i++) {
                    roundElemA = rounds.get(i);
                    // the value is smaller than the smallest round element
                    if ((value <= roundElemA) && (i == 0))
                    {
                        value = roundElemA;
                        break;
                    }

                    // the element is bigger than the biggest round element
                    if (i+1 == rounds.size())
                    {
                        value = roundElemA;
                        break;
                    }

                    roundElemB = rounds.get(i+1);
                    // the value lies between two round elements
                    if ((value > roundElemA) && (value <= roundElemB))
                    {
                        double a = value - roundElemA;
                        double b = roundElemB - value;
                        if (a < b)  value = roundElemA;
                        else        value = roundElemB;
                        break;
                    }

                }
            }
            return value;
        }
        if ((type == 'a') || (type == 'B') || (type=='C') || (type=='g'))
        {
            if (type == 'a') interval = roundIntervalAccel;
            if (type == 'B') interval = roundListBrightness.get(0);
            if (type == 'C') interval = roundListCompass.get(0);
            if (type == 'g') interval = roundIntervalGyro;
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
            if ( ( type == 'C' ) && (( value >= 360 ) || ( value*mult < 0 )) ) value = 0;

            return value * mult;
        }
        return 0; // should only happen if "type" is incorrect
    }

    public void setSettings(String settings)
    {
        //System.out.println("Settings received: " + settings);
        JSONObject json = new JSONObject(settings);

        // set booleans
        roundAccel = getBoolean(json, "roundAccel");
        roundBrightness = getBoolean(json, "roundBrightness");
        roundGyro = getBoolean(json,"roundGyro");
        roundCompass = getBoolean(json, "roundCompass");

        avgAccel = getBoolean(json,"avgAccel");
        avgBrightness = getBoolean(json,"avgBrightness");
        avgGyro = getBoolean(json,"avgGyro");
        avgCompass = getBoolean(json, "avgCompass");

        maxminAccel = getBoolean(json, "maxminAccel");
        maxminBrightness = getBoolean(json, "maxminBrightness");
        maxminGyro = getBoolean(json, "maxminGyro");
        maxminCompass = getBoolean(json, "maxminCompass");

        obfuscateGPS = getBoolean(json,"obfuscateGps");
        gpsRadius = getDouble(json, "gpsRadius");
        usePeerToPeer = getBoolean(json,"peerToPeer");

        // set Lists
        roundIntervalAccel = getDouble(json,"roundListAccel");
        roundListBrightness = getList(json,"roundListBrightness");
        roundIntervalGyro = getDouble(json,"roundListGyro");
        roundListCompass = getList(json, "roundListCompass");
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
            //System.out.println("List: " + geoFenceList);
        }
        catch (JSONException e)
        {
            System.out.println("Coordinate List \"geoFences\" not found");
        }
    }

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

    private List<Double> getList(JSONObject o, String key)
    {
        List<Double> result;
        try
        {
            result = (List<Double>) (Object) o.getJSONArray(key).toList();
        }
        catch (JSONException e)
        {
            System.out.println("Double list \"" + key + "\" not found");
            return new ArrayList<>();
        }
        return result;
    }

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

    // from http://www.movable-type.co.uk/scripts/latlong.html
    private static Coordinate obfuscateCoordinate(Coordinate coord)
    {
        double distance = 0.1;                  // kilometres, will be taken from settings file
        double bearing = Math.random() * 360;

        double latRad = Math.toRadians(coord.latitude);
        double lonRad = Math.toRadians(coord.longitude);

        double angDist =  distance/6371;  // distance / earthRadius
        double newLatitude = Math.asin( Math.sin(latRad) * Math.cos(angDist) +
                                        Math.cos(latRad) * Math.sin(angDist) * Math.cos(bearing) );
        double newLongitude = lonRad + Math.atan2( Math.sin(bearing) * Math.sin(angDist) * Math.cos(latRad),
                                                      Math.cos(angDist) - Math.sin(latRad) * Math.sin(newLatitude));

        return new Coordinate(Math.toDegrees(newLatitude), Math.toDegrees(newLongitude));
    }

    void copyTripleList(List<Triple> origin, List<Triple> target)
    {
        for (Triple t : origin)
        {
            target.add(t.copy());
        }
    }
}
