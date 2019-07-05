#include "file.h"

#include <QFile>
#include <QTextStream>
#include <QDebug>
#include <QStandardPaths>
#include <QDir>


QString readFile(QString name);
QString writeFile(QString name, QString content);

File::File(JavaBridge *jBridge, QObject *parent) : QObject(parent)
{
  this->jBridge = jBridge;
}

// read a file saved at "/storage/emulated/0/dataMiner/" with a given name
// return: the saved text in the file or an error message
QString File::readFile(QString name)
{
  QString path = "/storage/emulated/0/dataMiner/" + name;
  //qDebug() << "read File: path: " + path;
  QFile file(path);
  if(!file.exists()){
      qDebug() << "(readFile) " << name << "file does not exist, creating...";
      QDir dir = QDir("/storage/emulated/0");
      qDebug() << "dir exists: " << dir.exists();
      dir.mkdir("dataMiner");

      bool created = file.open(QIODevice::WriteOnly);
      if (!created)
      {
        file.close();
        qDebug() << "(readFile) File could not be created";
        return "File could not be created";
      }
      file.close();
      qDebug() << "(readFile) File created successfully";
  }
  if(!file.open(QIODevice::ReadOnly)){
      qDebug() << "(readFile) " << name << "error opening file for read";
      return "error opening file";
  }
  QString text;
  QTextStream in(&file);
  QString line;
  while(!in.atEnd()){
      line = in.readLine();
      text += line;
  }
  file.close();
  if (text.isNull())
  {
    settings = QJsonDocument();
    return "{}";
  }
  else
  {
    settings = QJsonDocument::fromJson(text.toLocal8Bit());
    return text;
  }
}

//writes a JSON coded string into a file
void File::writeFile(QString name, QJsonDocument content)
{
  QString path = "/storage/emulated/0/dataMiner/" + name;
  QFile file(path);
  if (!file.open(QIODevice::ReadWrite)){
      qDebug() << "error opening or creating file for write";
      return;
  }
  if(!file.resize(0)){
      qDebug() << "resizing file did not work";
      return;
  }
  QByteArray ba = content.toJson();
  const char *c_str = ba.data();
  if(-1 == file.write(c_str)){
      qDebug() << "could not write to file";
  }
  file.close();
}

void File::setSetting(QString key, QString value, bool fence)
{
  bool isBool = false;
  bool boolVal = false;
  if (value == "true")
  {
    isBool = true;
    boolVal = true;
  }
  if (value == "false")
  {
    isBool = true;
    boolVal = false;
  }

  if (settings.isEmpty())
  {
    settings = QJsonDocument::fromJson(readFile("settings.json").toLocal8Bit());
    if (settings.isNull())
    {
      qDebug() << "(setSetting) settings.json could not be parsed";
      return;
    }
  }
  QJsonObject jsonObj = settings.object();

  if (fence)  // used when setting an array of geoFence coordinates
  {
    QJsonDocument doc = QJsonDocument::fromJson(value.toUtf8());  // parses the value containing an array
    if (doc.isNull())
    {
      qDebug() << "(setSetting) could not parse the value object";
      return;
    }
    QJsonObject obj = doc.object();
    jsonObj.insert(key, QJsonValue(obj));
  }
  else
  {
    if (isBool)   jsonObj.insert(key, QJsonValue(boolVal));
    else          jsonObj.insert(key, QJsonValue(value));
  }

  settings = QJsonDocument(jsonObj);
  writeFile("settings.json", settings);
  jBridge->setSettings(settings.toJson());
}

QVariantMap File::getSettings()
{
  QVariantMap map;
  QJsonObject obj = settings.object();
  qDebug() << "print object: " << settings.toJson();
  /*
  if (!obj.value("roundAccel").isUndefined()) map.insert("roundAccel", obj.value("roundAccel").toBool());
  else                                        map.insert("roundAccel", false);
  if (!obj.value("roundBrightness").isUndefined())  map.insert("roundBrightness", obj.value("roundBrightness").toBool());
  else                                              map.insert("roundBrightness", false);
  if (!obj.value("roundGyro").isUndefined())  map.insert("roundGyro", obj.value("roundGyro").toBool());
  else                                        map.insert("roundGyro", false);
  if (!obj.value("roundCompass").isUndefined())  map.insert("roundCompass", obj.value("roundCompass").toBool());
  else                                           map.insert("roundCompass", false);
  if (!obj.value("avgAccel").isUndefined()) map.insert("avgAccel", obj.value("avgAccel").toBool());
  else                                      map.insert("avgAccel", false);
  if (!obj.value("avgBrightness").isUndefined())  map.insert("avgBrightness", obj.value("avgBrightness").toBool());
  else                                            map.insert("avgBrightness", false);
  if (!obj.value("avgGyro").isUndefined())  map.insert("avgGyro", obj.value("avgGyro").toBool());
  else                                      map.insert("avgGyro", false);
  if (!obj.value("peerToPeer").isUndefined()) map.insert("peer", obj.value("peer").toBool());
  else                                        map.insert("peer", false);
  if (!obj.value("delay").isUndefined()) map.insert("delay", obj.value("delay").toBool());
  else                                   map.insert("delay", false);
  */
  // it's an example implementation, we can skip error detection
  map.insert("roundAccel", obj.value("roundAccel").toBool());
  map.insert("roundBrightness", obj.value("roundBrightness").toBool());
  map.insert("roundGyro", obj.value("roundGyro").toBool());
  map.insert("roundCompass", obj.value("roundCompass").toBool());

  map.insert("maxminAccel", obj.value("maxminAccel").toBool());
  map.insert("maxminBrightness", obj.value("maxminBrightness").toBool());
  map.insert("maxminCompass", obj.value("maxminCompass").toBool());
  map.insert("maxminGyro", obj.value("maxminGyro").toBool());

  map.insert("avgAccel", obj.value("avgAccel").toBool());
  map.insert("avgBrightness", obj.value("avgBrightness").toBool());
  map.insert("avgCompass", obj.value("avgCompass").toBool());
  map.insert("avgGyro", obj.value("avgGyro").toBool());

  map.insert("obfuscateGps", obj.value("obfuscateGps").toBool());

  map.insert("temporalObfuscation", obj.value("temporalObfuscation").toBool());
  map.insert("useDelays", obj.value("useDelays").toBool());

  map.insert("usePeerToPeer", obj.value("usePeerToPeer").toBool());

  return map;
}
