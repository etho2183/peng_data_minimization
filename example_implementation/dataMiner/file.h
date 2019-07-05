#ifndef FILE_H
#define FILE_H

#include "javabridge.h"

#include <QObject>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonArray>

class File : public QObject
{
  Q_OBJECT
public:
  explicit File(JavaBridge *jBridge, QObject *parent = 0);

  // read a file saved at "/storage/emulated/0/dataMiner/" with a given name
  // return: the saved text in the file or an error message
  Q_INVOKABLE QString readFile(QString name);

  //writes a JSON coded string into a file
  Q_INVOKABLE void writeFile(QString name, QJsonDocument content);

  // adds a JSON object to the JSON file based on latitude, longitude and timestamp values
  //Q_INVOKABLE void addLocationToFile(double lat, double lon, unsigned long time);

  // sets or creates a setting in the settings file
  Q_INVOKABLE void setSetting(QString key, QString value, bool fence = false);

  Q_INVOKABLE QVariantMap getSettings();

private:
  QJsonDocument settings = QJsonDocument();
  JavaBridge *jBridge;

signals:

public slots:
};

#endif // FILE_H
