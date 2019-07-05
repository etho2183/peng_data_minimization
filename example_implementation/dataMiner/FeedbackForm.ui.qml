import QtQuick 2.4
import QtQuick.Controls 2.5

Page {
    id: page
    width: 400
    height: 400
    property alias label_title: label_title
    property alias textArea: textArea

    Label {
        id: label_title
        y: 12
        text: qsTr("Data sent to server")
        anchors.right: parent.right
        anchors.rightMargin: 0
        anchors.left: parent.left
        anchors.leftMargin: 0
        horizontalAlignment: Text.AlignHCenter
        font.pixelSize: 27
    }

    Flickable {
        id: flickable
        anchors.topMargin: 50
        anchors.fill: parent
        flickableDirection: Flickable.VerticalFlick

        TextArea.flickable: TextArea {
            id: textArea
            text: qsTr("")
            anchors.fill: parent
            wrapMode: Text.WrapAnywhere
            readOnly: true
        }
    }
}
