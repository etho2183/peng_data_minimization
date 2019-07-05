import QtQuick 2.12
import QtQuick.Controls 2.5
import QtSensors 5.12
import QtPositioning 5.12


ApplicationWindow {
    visible: true
    width: 640
    height: 480
    title: qsTr("Tabs")

    Timer {
        id: timer_collectAll;
        interval: 500;
        repeat: false;
        triggeredOnStart: false;
        onTriggered: extractAndDeactivate();
    }

    PositionSource {
        id: positionSource;
        active: false;
        preferredPositioningMethods: PositionSource.SatellitePositioningMethods
        updateInterval: 3000
        property bool firstValue: true
        onPositionChanged: {
            if (firstValue)
            {
                firstValue = false;
                return;
            }
            var lat = positionSource.position.coordinate.latitude;
            var lon = positionSource.position.coordinate.longitude;
            JavaBridge.setCoordinate([lat, lon]);
        }
    }

    Accelerometer {
        id: sensor_accel;
        dataRate: 500;
        active: false;
        onReadingChanged: {
            var x = sensor_accel.reading.x;
            var y = sensor_accel.reading.y;
            var z = sensor_accel.reading.z;
            JavaBridge.setAccelValue([x,y,z]);
        }
    }

    Gyroscope {
        id: sensor_gyro;
        dataRate: 500;
        active: false;
        onReadingChanged: {
            var x = sensor_gyro.reading.x;
            var y = sensor_gyro.reading.y;
            var z = sensor_gyro.reading.z;
            JavaBridge.setGyroValue([x, y, z]);
        }
    }

    LightSensor {
        id: sensor_light;
        dataRate: 100;
        active: false;
        onReadingChanged: {
            // TOTO: something shitty here
            var b = sensor_light.reading.illuminance;
            JavaBridge.setBrightValue(b);
        }
    }

    SwipeView {
        id: swipeView
        anchors.fill: parent
        currentIndex: tabBar.currentIndex

        HomeForm {
            id: homePage;

            function updateSwitches()
            {
                var num = 0;
                var enabled = switch_enable.checked;
                if (switch_gps.checked && enabled)
                {
                    num++;
                    positionSource.active = true;
                }
                else
                {
                    positionSource.active = false;
                }

                if (switch_accel.checked && enabled)
                {
                    num++;
                    sensor_accel.active = true;
                }
                else
                {
                    sensor_accel.active = false;
                }
                if (switch_bright.checked && enabled)
                {
                    num++;
                    sensor_light.active = true;
                }
                else
                {
                    sensor_light.active = false;
                }
                if (switch_gyro.checked && enabled)
                {
                    num++;
                    sensor_gyro.active = true;
                }
                else
                {
                    sensor_gyro.active = false;
                }
                if (enabled)
                {
                    rectangle_background.color = "#8dff8d";
                    num++;
                }
                else
                {
                    rectangle_background.color = "#ff8d8d";
                    num = 0;
                }
                if (num == 0)   homePage.rectangle_title.color = "#ff6666";
                if (num == 1)   homePage.rectangle_title.color = "#66ff66";
                if (num == 2)   homePage.rectangle_title.color = "#4dff4d";
                if (num == 3)   homePage.rectangle_title.color = "#33ff33";
                if (num == 4)   homePage.rectangle_title.color = "#1aff1a";
                if (num == 5)   homePage.rectangle_title.color = "#00ff00";
            }

            switch_enable.onCheckedChanged: {
                updateSwitches();
            }

            switch_accel.onCheckedChanged: {
                updateSwitches();
            }

            switch_bright.onCheckedChanged: {
                updateSwitches();
            }

            switch_gyro.onCheckedChanged: {
                updateSwitches();
            }

            switch_gps.onCheckedChanged: {
                updateSwitches();
            }

        }

        FeedbackForm {
            id: feedbackPage;

            Connections {
                target: JavaBridge;
                onText_signal: {
                    feedbackPage.textArea.append("---------------------------------------")
                    feedbackPage.textArea.append(text);
                }
            }
        }

        SettingsForm {
            id: settingsPage;

            Component.onCompleted: {
                var map = File.getSettings();
                switch_avg_accel.checked = map.avgAccel;
                switch_avg_bright.checked = map.avgBrightness;
                switch_avg_gyro.checked = map.avgGyro;
                switch_rng_accel.checked = map.roundAccel;
                switch_rng_bright.checked = map.roundBrightness;
                switch_rng_gyro.checked = map.roundGyro;
                switch_peer.checked = map.peer;
                switch_delay.checked = map.delay;
            }

            button_applyDelay.onClicked:
            {
                var delay = textInput_delay.text;
                File.setSetting("maxDelay", delay, false);
            }

            button_none.onClicked: {
                switch_avg_accel.checked = false;
                switch_avg_bright.checked = false;
                switch_avg_gyro.checked = false;
                switch_delay.checked = false;
                switch_peer.checked = false;
                switch_rng_accel.checked = false;
                switch_rng_bright.checked = false;
                switch_rng_gyro.checked = false;
            }

            button_simple.onClicked: {
                switch_avg_accel.checked = false;
                switch_avg_bright.checked = false;
                switch_avg_gyro.checked = false;
                switch_delay.checked = false;
                switch_peer.checked = false;
                switch_rng_accel.checked = true;
                switch_rng_bright.checked = true;
                switch_rng_gyro.checked = true;
            }

            button_full.onClicked: {
                switch_avg_accel.checked = true;
                switch_avg_bright.checked = true;
                switch_avg_gyro.checked = true;
                switch_delay.checked = true;
                switch_peer.checked = true;
                switch_rng_accel.checked = false;
                switch_rng_bright.checked = false;
                switch_rng_gyro.checked = false;
            }

            switch_avg_gyro.onCheckedChanged: {
                if (switch_avg_gyro.checked)    File.setSetting("avgGyro", "true", false);
                else                            File.setSetting("avgGyro", "false", false);
            }

            switch_avg_bright.onCheckedChanged: {
                if (switch_avg_bright.checked)  File.setSetting("avgBrightness", "true", false);
                else                            File.setSetting("avgBrightness", "false", false);
            }

            switch_avg_accel.onCheckedChanged: {
                if (switch_avg_accel.checked)   File.setSetting("avgAccel", "true", false);
                else                            File.setSetting("avgAccel", "false", false);
            }

            switch_maxmin_accel.onCheckedChanged: {
                if (switch_maxmin_accel.checked)   File.setSetting("maxminAccel", "true", false);
                else                               File.setSetting("maxminAccel", "false", false);
            }

            switch_maxmin_bright.onCheckedChanged: {
                if (switch_maxmin_bright.checked)   File.setSetting("maxminBrightness", "true", false);
                else                                File.setSetting("maxminBrightness", "false", false);
            }

            switch_maxmin_gyro.onCheckedChanged: {
                if (switch_maxmin_gyro.checked)   File.setSetting("maxminGyro", "true", false);
                else                              File.setSetting("maxminGyro", "false", false);
            }

            switch_rng_gyro.onCheckedChanged: {
                if (switch_rng_gyro.checked)    File.setSetting("roundGyro", "true", false);
                else                            File.setSetting("roundGyro", "false", false);
            }

            switch_rng_bright.onCheckedChanged: {
                if (switch_rng_bright.checked)  File.setSetting("roundBrightness", "true", false);
                else                            File.setSetting("roundBrightness", "false", false);
            }

            switch_rng_accel.onCheckedChanged: {
                if (switch_rng_accel.checked)   File.setSetting("roundAccel", "true", false);
                else                            File.setSetting("roundAccel", "false", false);

            }

            switch_delay.onCheckedChanged: {
                if (switch_delay.checked)        File.setSetting("temporalObfuscation", "true", false);
                else                             File.setSetting("temporalObfuscation", "false", false);
            }

            switch_randomDelay.onCheckedChanged: {
                if (switch_randomDelay.checked)        File.setSetting("useDelays", "true", false);
                else                                   File.setSetting("useDelays", "false", false);
            }

            switch_peer.onCheckedChanged: {
                if (switch_peer.checked)        File.setSetting("peerToPeer", "true", false);
                else                            File.setSetting("peerToPeer", "false", false);
            }

        }
    }

    footer: TabBar {
        id: tabBar
        currentIndex: swipeView.currentIndex

        TabButton {
            text: qsTr("Collector")
        }

        TabButton {
            text: qsTr("Log")
        }

        TabButton {
            text: qsTr("Settings")
        }
    }


    function extractAndDeactivate ()
    {
        var log = page2.textArea_dataLog;

        console.log("\n#############################################################\n");
        // get all readings
        var accel = sensor_accel.reading;
        var alti = sensor_alti.reading;
        var ambiLight = sensor_ambiLight.reading;
        var ambiTemp = sensor_ambiTemp.reading;
        var compass = sensor_compass.reading;
        var gyro = sensor_gyro.reading;
        var holster = sensor_holster.reading;
        var irProx = sensor_irProx.reading;
        var lid = sensor_lid.reading;
        var light = sensor_light.reading;
        var magnet = sensor_magnet.reading;
        var orient = sensor_orient.reading;
        var pressure = sensor_pressure.reading;
        var prox = sensor_prox.reading;
        var rotation = sensor_rotation.reading;
        var tap = sensor_tap.reading;
        var tilt = sensor_tilt.reading;

        // print all readings
        log.append("####################");
        if (accel != null )
        {
            log.append("accelleration: " + accel.x + ", " + accel.y + ", " + accel.z);
            console.log("accelleration: " + accel.x + ", " + accel.y + ", " + accel.z);
        }
        if (alti != null )
        {
            log.append("altitude: " + alti.altitude);
            console.log("altitude: " + alti.altitude);
        }
        if (ambiLight != null )
        {
            log.append("ambientLight: " + ambiLight.lightLevel);
            console.log("ambientLight: " + ambiLight.lightLevel);
        }
        if (ambiTemp != null )
        {
            log.append("ambientTemperature: " + ambiTemp.temperature);
            console.log("ambientTemperature: " + ambiTemp.temperature);
        }
        if (compass != null )
        {
            log.append("compass: azimuth: " + compass.azimuth + ", calibrationLevel: " + compass.calibrationLevel);
            console.log("compass: azimuth: " + compass.azimuth + ", calibrationLevel: " + compass.calibrationLevel);
        }
        if (gyro != null )
        {
            log.append("gyroscope: " + gyro.x + ", " + gyro.y + ", " + gyro.z);
            console.log("gyroscope: " + gyro.x + ", " + gyro.y + ", " + gyro.z);
        }
        if (holster != null )
        {
            log.append("holster: " + holster.holstered);
            console.log("holster: " + holster.holstered);
        }
        if (irProx != null )
        {
            log.append("IR Proximity: " + irProx.reflectance);
            console.log("IR Proximity: " + irProx.reflectance);
        }
        if (lid != null )
        {
            log.append("lid: front: " + lid.frontLidClosed + "back: " + lid.backLidClosed)
            console.log("lid: front: " + lid.frontLidClosed + "back: " + lid.backLidClosed);
        }
        if (light != null )
        {
            log.append("light: " + light.illuminance);
            console.log("light: " + light.illuminance);
        }
        if (magnet != null )
        {
            log.append("magnet: " + magnet.x + ", " + magnet.y + ", " + magnet.z + ", calibration: " + magnet.calibrationLevel);
            console.log("magnet: " + magnet.x + ", " + magnet.y + ", " + magnet.z + ", calibration: " + magnet.calibrationLevel);
        }
        if (orient != null )
        {
            log.append("orientation: " + orient.orientation);
            console.log("orientation: " + orient.orientation);
        }
        if (pressure != null )
        {
            log.append("pressure: " + pressure.pressure + ", temperature: " + pressure.temperature);
            console.log("pressure: " + pressure.pressure + ", temperature: " + pressure.temperature);
        }
        if (prox != null )
        {
            log.append("proximity: " + prox.near);
            console.log("proximity: " + prox.near);
        }
        if (rotation != null )
        {
            log.append("rotation: " + rotation.x + ", " + rotation.y + ", " + rotation.z);
            console.log("rotation: " + rotation.x + ", " + rotation.y + ", " + rotation.z);
        }
        if (tap != null )
        {
            log.append("tap: doubleTap: " + tap.doubleTap + ", tapDirection: " + tap.tapDirection);
            console.log("tap: doubleTap: " + tap.doubleTap + ", tapDirection: " + tap.tapDirection);
        }
        if (tilt != null )
        {
            log.append("tilt: x: " + tilt.xRotation + ", y: " + tilt.yRotation);
            console.log("tilt: x: " + tilt.xRotation + ", y: " + tilt.yRotation);
        }

        // disable all sensors
        sensor_accel.active = false;
        sensor_alti.active = false;
        sensor_ambiLight.active = false;
        sensor_ambiTemp.active = false;
        sensor_compass.active = false;
        sensor_gyro.active = false;
        sensor_holster.active = false;
        sensor_irProx.active = false;
        sensor_lid.active = false;
        sensor_light.active = false;
        sensor_magnet.active = false;
        sensor_orient.active = false;
        sensor_pressure.active = false;
        sensor_prox.active = false;
        sensor_rotation.active = false;
        sensor_tap.active = false;
        sensor_tilt.active = false;

        page2.textArea_dataLog.append("test");
        page2.textArea_dataLog.append("test2");
    }

    Altimeter {
        id: sensor_alti;
        dataRate: 100;
        active: false;
    }

    AmbientLightSensor {
        id: sensor_ambiLight;
        dataRate: 100;
        active: false;
    }

    AmbientTemperatureSensor {
        id: sensor_ambiTemp;
        dataRate: 100;
        active: false;
    }

    Compass {
        id: sensor_compass;
        dataRate: 100;
        active: false;
    }

    //DistanceSensor

    HolsterSensor {
        id: sensor_holster;
        dataRate: 100;
        active: false;
    }

    //HumiditySensor

    IRProximitySensor {
        id: sensor_irProx;
        dataRate: 100;
        active: false;
    }

    LidSensor {
        id: sensor_lid;
        dataRate: 100;
        active: false;
    }

    Magnetometer {
        id: sensor_magnet;
        dataRate: 100;
        active: false;
    }

    OrientationSensor {
        id: sensor_orient;
        dataRate: 100;
        active: false;
    }

    PressureSensor {
        id: sensor_pressure;
        dataRate: 100;
        active: false;
    }

    ProximitySensor {
        id: sensor_prox;
        dataRate: 100;
        active: false;
    }

    RotationSensor {
        id: sensor_rotation;
        dataRate: 100;
        active: false;
    }

    TapSensor {
        id: sensor_tap;
        dataRate: 100;
        active: false;
    }

    TiltSensor {
        id: sensor_tilt;
        dataRate: 100;
        active: false;
    }
}
