import QtQuick 2.12
import QtQuick.Controls 2.5
import QtQuick.Controls.Styles 1.4

Page {
    id: page
    width: 640
    height: 480
    property alias switch_maxmin_gyro: switch_maxmin_gyro
    property alias switch_maxmin_bright: switch_maxmin_bright
    property alias switch_maxmin_accel: switch_maxmin_accel
    property alias button_applyDelay: button_applyDelay
    property alias textInput_delay: textInput_delay
    property alias switch_randomDelay: switch_randomDelay
    property alias label_title: label_title
    property alias switch_peer: switch_peer
    property alias button_full: button_full
    property alias button_simple: button_simple
    property alias button_none: button_none
    property alias button_addFence: button_addFence
    property alias switch_rng_gyro: switch_rng_gyro
    property alias switch_rng_bright: switch_rng_bright
    property alias switch_rng_accel: switch_rng_accel
    property alias switch_avg_gyro: switch_avg_gyro
    property alias switch_avg_bright: switch_avg_bright
    property alias switch_avg_accel: switch_avg_accel
    property alias textInput_rad: textInput_rad
    property alias textInput_lon: textInput_lon
    property alias textInput_lat: textInput_lat
    property alias switch_delay: switch_delay

    Flickable {
        id: flickable
        anchors.fill: parent

        Column {
            id: column
            anchors.fill: parent

            Label {
                id: label_title
                text: qsTr("Settings")
                anchors.horizontalCenter: parent.horizontalCenter
                horizontalAlignment: Text.AlignHCenter
                font.pixelSize: 27
            }

            Row {
                id: row
                height: 60
                spacing: 2
                anchors.left: parent.left
                anchors.leftMargin: 0
                anchors.right: parent.right
                anchors.rightMargin: 0

                Button {
                    id: button_none
                    width: page.width / 3;
                    height: 60
                    text: qsTr("none")
                    font.family: "Times New Roman"
                    anchors.left: parent.left
                    anchors.leftMargin: 0
                }

                Button {
                    id: button_simple
                    width: page.width / 3;
                    height: 60
                    text: qsTr("simple")
                    anchors.horizontalCenter: parent.horizontalCenter
                }

                Button {
                    id: button_full
                    width: page.width / 3;
                    height: 60
                    text: qsTr("full")
                    anchors.right: parent.right
                    anchors.rightMargin: 0
                }
            }


            Switch {
                id: switch_peer
                x: 0
                y: 0
                width: 300
                text: qsTr("use peer to peer")
                anchors.horizontalCenterOffset: 0
                anchors.horizontalCenter: parent.horizontalCenter
            }

            Switch {
                id: switch_delay
                width: 300
                text: qsTr("delay transmissions")
                anchors.horizontalCenterOffset: 0
                anchors.horizontalCenter: parent.horizontalCenter
            }

            Switch {
                id: switch_randomDelay
                width: 300
                text: qsTr("use random delay")
                anchors.horizontalCenterOffset: 0
                anchors.horizontalCenter: parent.horizontalCenter
            }

            Row {
                id: row1
                width: 310
                height: 40
                anchors.horizontalCenter: parent.horizontalCenter
                spacing: 10
                layoutDirection: Qt.LeftToRight

                Label {
                    id: label
                    text: qsTr("maximum delay in minutes")
                    anchors.verticalCenter: parent.verticalCenter
                }

                TextInput {
                    id: textInput_delay
                    width: 60
                    height: 40
                    text: qsTr("")
                    anchors.verticalCenter: parent.verticalCenter
                    inputMethodHints: Qt.ImhFormattedNumbersOnly
                    Rectangle {
                        id: rectangle3
                        x: 0
                        y: 33
                        width: 150
                        height: 40
                        color: "#00000000"
                        anchors.verticalCenter: parent.verticalCenter
                        anchors.top: parent.top
                        anchors.right: parent.right
                        anchors.left: parent.left
                        border.width: 1
                    }
                    font.pixelSize: 20
                    echoMode: TextInput.Normal
                }

                Button {
                    id: button_applyDelay
                    text: qsTr("Apply")
                }
            }

            Switch {
                id: switch_rng_accel
                width: 300;
                text: qsTr("round acceleration values")
                anchors.horizontalCenter: parent.horizontalCenter
            }

            Switch {
                id: switch_rng_bright
                width: 300;
                text: qsTr("round brightness values")
                anchors.horizontalCenter: parent.horizontalCenter
            }

            Switch {
                id: switch_rng_gyro
                width: 300;
                text: qsTr("round gyroscope values")
                anchors.horizontalCenter: parent.horizontalCenter
            }

            Switch {
                id: switch_avg_accel
                width: 300
                text: qsTr("average acceleration values")
                anchors.horizontalCenter: parent.horizontalCenter
            }

            Switch {
                id: switch_avg_bright
                width: 300
                text: qsTr("average brightness values")
                anchors.horizontalCenter: parent.horizontalCenter
            }

            Switch {
                id: switch_avg_gyro
                width: 300
                text: qsTr("average gyroscope values")
                anchors.horizontalCenter: parent.horizontalCenter
            }

            Switch {
                id: switch_maxmin_accel
                width: 300
                text: qsTr("maxMin accel values")
                anchors.horizontalCenter: parent.horizontalCenter
            }

            Switch {
                id: switch_maxmin_bright
                width: 300
                text: qsTr("maxMin brightness values")
                anchors.horizontalCenter: parent.horizontalCenter
            }

            Switch {
                id: switch_maxmin_gyro
                width: 300
                text: qsTr("maxMin gyro values")
                anchors.horizontalCenter: parent.horizontalCenter
            }



            Label {
                id: label_addFence
                text: qsTr("Add Geofence")
                anchors.horizontalCenterOffset: -11
                anchors.horizontalCenter: parent.horizontalCenter
                font.pixelSize: 20
            }

            Grid {
                id: grid
                width: 220
                height: 90
                columns: 2
                rows: 3
                spacing: 7
                anchors.horizontalCenter: parent.horizontalCenter

                Label {
                    id: label_latitude
                    text: qsTr("latitude")
                    font.pixelSize: 15
                }

                TextInput {
                    id: textInput_lat
                    width: 150
                    height: 20
                    text: qsTr("")
                    echoMode: TextInput.Normal
                    font.pixelSize: 20
                    inputMethodHints: Qt.ImhFormattedNumbersOnly

                    Rectangle {
                        id: rectangle2
                        x: 0
                        y: 33
                        width: 150
                        color: "#00000000"
                        border.width: 1
                        anchors.fill: parent
                    }
                }

                Label {
                    id: label_longitude
                    text: qsTr("longitude")
                    font.pixelSize: 15
                }

                TextInput {
                    id: textInput_lon
                    width: 150
                    height: 20
                    text: qsTr("")
                    font.pixelSize: 20
                    inputMethodHints: Qt.ImhFormattedNumbersOnly

                    Rectangle {
                        id: rectangle1
                        x: 0
                        y: 33
                        width: 150
                        color: "#00000000"
                        border.width: 1
                        anchors.fill: parent
                    }
                }

                Label {
                    id: label_radius
                    text: qsTr("radius")
                    font.pixelSize: 15
                }

                TextInput {
                    id: textInput_rad
                    width: 150
                    height: 20
                    text: qsTr("")
                    font.pixelSize: 20
                    inputMethodHints: Qt.ImhFormattedNumbersOnly

                    Rectangle {
                        id: rectangle
                        width: 120
                        color: "#00000000"
                        border.width: 1
                        anchors.fill: parent
                    }
                }
            }

            Button {
                id: button_addFence
                width: 240
                text: qsTr("Add")
                anchors.horizontalCenter: parent.horizontalCenter
            }






        }
    }
}




/*##^## Designer {
    D{i:2;anchors_height:200;anchors_width:200}D{i:3;anchors_width:600;anchors_x:0}D{i:4;anchors_width:600;anchors_x:0}
D{i:11;anchors_width:600;anchors_x:0}D{i:13;anchors_width:600;anchors_x:0}D{i:20;anchors_height:200;anchors_width:200}
D{i:12;anchors_height:200;anchors_width:200}D{i:1;anchors_height:200;anchors_width:200}
}
 ##^##*/
