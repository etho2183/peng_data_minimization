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
  qDebug() << "reading settings: avgAccel: " << obj.value("avgAccel") << obj.value("avgAccel").toBool();
  if (!obj.value("avgAccel").isUndefined()) map.insert("avgAccel", obj.value("avgAccel").toBool());
  else                                      map.insert("avgAccel", false);
  if (!obj.value("avgBrightness").isUndefined())  map.insert("avgBrightness", obj.value("avgBrightness").toBool());
  else                                            map.insert("avgBrightness", false);
  if (!obj.value("avgGyro").isUndefined())  map.insert("avgGyro", obj.value("avgGyro").toBool());
  else                                      map.insert("avgGyro", false);
  if (!obj.value("rangeAccel").isUndefined()) map.insert("rangeAccel", obj.value("rangeAccel").toBool());
  else                                        map.insert("rangeAccel", false);
  if (!obj.value("rangeBrightness").isUndefined())  map.insert("rangeBrightness", obj.value("rangeBrightness").toBool());
  else                                              map.insert("rangeBrightness", false);
  if (!obj.value("rangeGyro").isUndefined())  map.insert("rangeGyro", obj.value("rangeGyro").toBool());
  else                                        map.insert("rangeGyro", false);
  if (!obj.value("peerToPeer").isUndefined()) map.insert("peer", obj.value("peer").toBool());
  else                                        map.insert("peer", false);
  if (!obj.value("delay").isUndefined()) map.insert("delay", obj.value("delay").toBool());
  else                                   map.insert("delay", false);

  return map;
}
