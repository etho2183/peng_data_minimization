# Reusable Data Minimization Modules for Server and Client
The Data Minimization Blocks were a project created as part of the lecture "Privacy Engineering" at Technical University Berlin. 
Goal of the project is to develop a reusable library for crowd sensing services that enables programmers to easily make their new services more privacy-friendly. 
**This project is currently work in progress.**

# Documentation
## Settings
Settings are internally saved in a JSON format and need to be communicated to the privacy component by using the function setSettings(String json). The given String needs to be a valid JSON Object.
Settings:
- "roundAccel": bool -> rounds accelerator sensor values (x, y, z) to a given value list or interval.
- "roundBrightness": bool -> rounds brightness sensor values to a given value list or interval.
- "roundCompass": rounds compass sensor values (only bearing) to a given value list or interval.
- "roundGyro": bool -> rounds gyroscope sensor values (x, y, z) to a given value list or interval.

- "maxminAccel": bool -> sends only maximum and minimum value of acceleration sensor.
- "maxminBrightness": bool -> sends only maximum and minimum value of brightness sensor.
- "maxminCompass": bool -> sends only maximum and minimum value of compass bearing.
- "maxminGyro": bool -> sends only maximum and minimum value of gyroscope sensor.

- "avgAccel": bool -> sends only average of accelerator sensor values.
- "avgBrightness": bool -> sends only average of brightness sensor values.
- "avgCompass": bool -> sends only average of compass bearing values.
- "avgGyro": bool -> sends only average of gyroscope sensor values.

- "obfuscateGps": bool -> replace each Gps location by a fake location within a radius of the real one.
- "gpsRadius": num -> the radius for above operation in km. Default: 0.1 km.
- "usePeerToPeer": bool -> register in a peer to peer network of other service users and proxy data packets through them to hide the own IP address.

- "geoFenceList": Object [{"latitude": la, "longitude": lo, "radius": r}, ...] -> List of areas around given coordinates in which no data should be collected.
- "roundListBrightness": [num, num, ...] -> List of sorted values that numeric data rounds to. Example: [-2, 0, 1]; -0.9 -> 0. If only one value is given, e.g. [1], it acts as a interval such as [..., -1, 0, 1, 2, ---].
- "roundListCompass": As in "roundListBrightness" but for compass bearing. Values smaller than 0 and greater than 360 will autbe ignored.
- "roundIntervalAccel": num -> Interval for which accelerator values shall be rounded. Example: num=3 rounds new values to [..., -3, 0, 3, 6, ...].
- "roundIntervalGyro": num -> As in "roundIntervalAccel" but for gyroscope values.

## Functions
setter functions return 'true' on success and 'false' on failure.

- boolean setAccelData(double x, double y, double z) -> add one reading from the accelerator sensor.
- boolean setBrightData(double b) -> add one reading from the brightness sensor.
- boolean setGpsData(double lat, double lon) -> add one position read from the GPS module
- boolean setGyroData(double x, double y, double z) -> add one reading from the gyroscope sensor.
- boolean setCompassOrientation(double bearing) -> add one orientation read from the compass sensor.

- void setSettings(String json) -> set all relevant settings from one json formatted String.
