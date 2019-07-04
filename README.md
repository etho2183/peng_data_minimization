# Reusable Data Minimization Modules for Server and Client
The Data Minimization Blocks were a project created as part of the lecture "Privacy Engineering" at Technical University Berlin. 
Goal of the project is to develop a reusable library for crowd sensing services that enables programmers to easily make their new services more privacy-friendly. It obfuscates sensor readings and links them to the next collected coordinate. Several obfuscation settings are supported and explained in the following lines.
There is also a functionality to give the user feedback about the quantity and quality of data that has been sent to a server.
A Qt QML and C++ example application that uses the Java code is available at ".\example implementation". Information on how to link Java and C++ is given at the section "Using the .jar from inside C++ code through JNI".
**This project is in an unfinished state and beyond local data collection, obfuscation and feedback, no networking functionalities are implemented.**

# Documentation
## Settings
Settings are internally saved in a JSON format and need to be communicated to the privacy component by using the function setSettings(String json). The given String needs to be a valid JSON Object. An example file is located at .\settings.json.
### Rounding
- "roundAccel": bool -> rounds accelerator sensor values (x, y, z) to a given interval.
- "roundBrightness": bool -> rounds brightness sensor values to a given value list or interval.
- "roundCompass": rounds compass sensor values (only bearing) to a given value list or interval.
- "roundGyro": bool -> rounds gyroscope sensor values (x, y, z) to a given interval.

### Rounding parameters
All lists need to be in order, meaning smallest value at front ant biggest at end.
- "roundListBrightness": [num, num, ...] -> List of sorted values that numeric data rounds to. Example: [-2, 0, 1]; -0.9 -> 0. If only one value is given, e.g. [1], it acts as a interval such as [..., -1, 0, 1, 2, ---].
- "roundListCompass": As in "roundListBrightness" but for compass bearing. Values smaller than 0 and greater than 360 will autbe ignored.
- "roundIntervalAccel": num -> Interval for which accelerator values shall be rounded. Example: num=3 rounds new values to [..., -3, 0, 3, 6, ...].
- "roundIntervalGyro": num -> As in "roundIntervalAccel" but for gyroscope values.

### Maximum and minimum values
- "maxminAccel": bool -> sends only maximum and minimum value of acceleration sensor.
- "maxminBrightness": bool -> sends only maximum and minimum value of brightness sensor.
- "maxminCompass": bool -> sends only maximum and minimum value of compass bearing.
- "maxminGyro": bool -> sends only maximum and minimum value of gyroscope sensor.

### Averaging
- "avgAccel": bool -> sends only average of accelerator sensor values.
- "avgBrightness": bool -> sends only average of brightness sensor values.
- "avgCompass": bool -> sends only average of compass bearing values.
- "avgGyro": bool -> sends only average of gyroscope sensor values.

### Spatial obfuscation
- "obfuscateGps": bool -> replace each Gps location by a fake location within a radius of the real one.
- "gpsRadius": num -> the radius for above operation in km. Default: 0.1 km.
- "geoFenceList": Object [{"latitude": la, "longitude": lo, "radius": r}, ...] -> List of areas around given coordinates in which no data should be collected.

### Temporal obfuscation
- 'temporalObfuscation': bool -> enable temporal obfuscation. Data will be sent at scecified times, if 'useDelays' is 'false'
- 'useDelays': bool -> add random delays to each coordinate. After the delay, it gets sent to server
---> WARNING: using random delays might break the order of the coordinates. If your service relies on ordered packets and you want to apply temporal obfuscation, consider sending at fixed times
- 'maxDelay': num -> highest possible delay in minutes
- 'sendingTimes': [num, num, ...] -> times of day in minutes since midnight at which all buffered data should be sent to a server. The list needs to be sorted so that the smallest value it at front.
---> WARNING: since no actual storage takes place, terminating the application will lead to loss of all buffered data

### Decentralized identity obfuscation (not implemented)
- "usePeerToPeer": bool -> register in a peer to peer network of other service users and proxy data packets through them to hide the own IP address.

## Functions
setter functions return 'true' on success and 'false' on failure.

- boolean setAccelData(double x, double y, double z) -> add one reading from the accelerator sensor.
- boolean setBrightData(double b) -> add one reading from the brightness sensor.
- boolean setGpsData(double lat, double lon) -> add one position read from the GPS module
- boolean setGyroData(double x, double y, double z) -> add one reading from the gyroscope sensor.
- boolean setCompassOrientation(double bearing) -> add one orientation read from the compass sensor.

- void setSettings(String json) -> set all relevant settings from one json formatted String.

## Adding new data types
When adding new data types to support other sensor readings, changes need to be made at various places inside the code:
- Boolean values as members of the ClientDataMinimizer class for specifying obfuscation functions such as rounding and averaging need to be added
- For rounding functions, a buffer of type List<Double> needs to be added. In case rounding only has to support fixed intervals, it may be a simple Double value
- In the helper class 'Coordinate', a container for the sensor readings is needed. Also, boolean values representing the applied obfuscation functions need to be added
- In setGpsData(...), the previously mentioned buffer should be flushed into the reading container of the Coordinate class
- When using calcRound(double val, char type), a new type needs to be specified. It is used to choose the correct rounding List or interval
- In setSettings(...), the preferences for the obfuscation functions on the new type need to be read out and saved
- In the sendData(...) function, when generating the feedback String, the new data type should also be represented  

## Using the .jar from inside QT C++ code through JNI
First, a QAndroidJniEnvironment is needed to find the class inside of the .jar
```C++
QAndroidJniEnvironment env;
jQAndroidJniObject javaClass = QAndroidJniObject(env.findClass("[package_name]/ClientDataMinimizer"));
```
Now to use a method from the created ClientDataMinimizer instantiation, write:
```C++
javaClass.callMethod<void>([method_name], "([input])[output]", [param1], [param2], ...);
```
[input] are the Java JNI types in the syntax TypeTypeType...
[output] is the output Java JNI type. 
The JNI types are listed here: https://doc.qt.io/qt-5/qandroidjniobject.html#jni-types
In this project, Double type (D) is used as input and void (V) is the return value.
An example function call would look like this:
```C++
javaClass.callMethod<void>("setAccelData", "(DDD)V", x, y, z);
```
## Receive feedback (in QT C++) from the Minimization Block about quantity and quality of sent data
The Java code calls a method 'giveDataFeedback(String s)' that does not have a definition:
```Java
public native void giveDataFeedback(String text);
```
When calling it, a C++ function is executed:
```C++
void JavaBridge::textFromJava(JNIEnv *env, jobject jobj, jstring text) // JavaBridge is the self-written C++ class tasked with JNI communication
{
  Q_UNUSED(env);
  Q_UNUSED(jobj);
  QString s = env->GetStringUTFChars(text, 0);
  // Display the QString to the user
}
```
To map this function to the native Java function, the following code is used
```C++
JNINativeMethod methods[] {{"giveDataFeedback", "(Ljava/lang/String;)V", reinterpret_cast<void *>(textFromJava)}};
QAndroidJniEnvironment env;
jclass objectClass = env->GetObjectClass(javaClass.object<jobject>());
env->RegisterNatives(objectClass, methods, sizeof (methods) / sizeof (methods[0]));
env->DeleteLocalRef(objectClass);
```
