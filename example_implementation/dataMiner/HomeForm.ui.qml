import QtQuick 2.12
import QtQuick.Controls 2.5

Page {
    id: page
    property alias switch_accel: switch_accel
    width: 640
    height: 480
    property alias rectangle_background: rectangle_background
    property alias switch_enable: switch_enable
    property alias switch_gps: switch_gps
    property alias switch_gyro: switch_gyro
    property alias switch_bright: switch_bright
    property alias rectangle_title: rectangle_title
    font.pixelSize: 60


    Rectangle {
        id: rectangle_background
        color: "#ff8d8d"
        anchors.fill: parent

        Column {
            id: column
            x: 0
            y: 100
            spacing: 9
            anchors.right: parent.right
            anchors.rightMargin: 0
            anchors.left: parent.left
            anchors.bottom: parent.bottom
            anchors.top: parent.top
            anchors.topMargin: 0

            Rectangle {
                id: rectangle_title
                height: 70
                color: "#ff6666"
                anchors.right: parent.right
                anchors.rightMargin: 0
                anchors.left: parent.left
                anchors.leftMargin: 0

                Label {
                    id: label
                    x: 198
                    y: 19
                    text: qsTr("Driving Lane Mapper")
                    anchors.verticalCenter: parent.verticalCenter
                    anchors.horizontalCenter: parent.horizontalCenter
                    font.pixelSize: 27
                }
            }

            Rectangle {
                id: rectangle1
                height: 10
                color: "#000000"
                anchors.left: parent.left
                anchors.leftMargin: 0
                anchors.right: parent.right
                anchors.rightMargin: 0
            }

            Switch {
                id: switch_enable
                width: 300
                text: qsTr("Enable/Disable")
                anchors.horizontalCenter: parent.horizontalCenter
                font.pointSize: 27
                font.pixelSize: 27
            }

            Switch {
                id: switch_gps
                width: 300
                height: 40
                text: qsTr("GPS collection")
                font.pixelSize: 27
                anchors.horizontalCenter: parent.horizontalCenter
                checked: false
            }


            Switch {
                id: switch_accel
                width: 300
                text: qsTr("Accelerometer")
                anchors.horizontalCenter: parent.horizontalCenter
                font.pixelSize: 27
                checked: false
            }




            Switch {
                id: switch_bright
                width: 300
                text: qsTr("Brightness Sensor")
                checked: false
                anchors.horizontalCenter: parent.horizontalCenter
                font.pixelSize: 27
            }

            Switch {
                id: switch_gyro
                width: 300
                text: qsTr("Gyroscope")
                checked: false
                anchors.horizontalCenter: parent.horizontalCenter
                font.pixelSize: 27
            }




        }
    }
}






/*##^## Designer {
    D{i:2;anchors_height:1691;anchors_width:1080}
}
 ##^##*/
