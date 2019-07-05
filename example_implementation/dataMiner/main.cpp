#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QtQuick>

#include "javabridge.h"
#include "file.h"

int main(int argc, char *argv[])
{
  QCoreApplication::setAttribute(Qt::AA_EnableHighDpiScaling);

  QGuiApplication app(argc, argv);

  QQmlApplicationEngine engine;

  // Option 1 to register
  JavaBridge *jBridge = new JavaBridge(&engine);
  File *file = new File(jBridge, &engine);
  jBridge->setSettings(file->readFile("settings.json"));
  engine.rootContext()->setContextProperty("JavaBridge", jBridge);
  engine.rootContext()->setContextProperty("File", file);

  engine.load(QUrl(QStringLiteral("qrc:/main.qml")));
  if (engine.rootObjects().isEmpty())
    return -1;

  // Option 2 to register
  //qmlRegisterType<JavaBridge>("java", 1, 0, "JavaBridge");

  return app.exec();
}
