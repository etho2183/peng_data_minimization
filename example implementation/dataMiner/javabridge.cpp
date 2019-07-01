#include <QAndroidJniObject>
#include <QDebug>

#include "javabridge.h"

JavaBridge* JavaBridge::self = nullptr;

JavaBridge::JavaBridge(QObject *parent) : QObject(parent)
{
  QAndroidJniEnvironment env;
  javaClass = QAndroidJniObject(env.findClass("org/dataMiner/Main"));
  registerNativeMethods();
  self = this;
}

void JavaBridge::setAccelValue(QVariantList accelValue)
{
  double x = accelValue.first().toDouble();
  double y = accelValue.at(1).toDouble();
  double z = accelValue.last().toDouble();

  javaClass.callMethod<void>("setAccelData", "(DDD)V", x, y, z);
}

void JavaBridge::setBrightValue(QVariant brightValue)
{
  double brightness = brightValue.toDouble();

  javaClass.callMethod<void>("setBrightData", "(D)V", brightness);
}

void JavaBridge::setGyroValue(QVariantList gyroValue)
{
  double x = gyroValue.first().toDouble();
  double y = gyroValue.at(1).toDouble();
  double z = gyroValue.last().toDouble();

  javaClass.callMethod<void>("setGyroData", "(DDD)V", x, y, z);
}

void JavaBridge::setCoordinate(QVariantList coordinate)
{
  double lat = coordinate.first().toDouble();
  double lon = coordinate.last().toDouble();

  javaClass.callMethod<void>("setGpsData", "(DD)V", lat, lon);
}

void JavaBridge::setSettings(QString settings)
{
  m_settings = settings;
  //qDebug() << "setting java settings:" << settings;
  QAndroidJniObject jniObj = QAndroidJniObject::fromString(settings);
  javaClass.callMethod<void>("setSettings", "(Ljava/lang/String;)V", jniObj.object<jstring>());
}

void JavaBridge::textFromJava(JNIEnv *env, jobject jobj, jstring text)
{
  Q_UNUSED(env);
  Q_UNUSED(jobj);
  QString s = env->GetStringUTFChars(text, 0);
  //qDebug() << "received text from java: " + s;
  if (self)
    self->emit_text_signal(s);
  else
    qDebug() << "JavaBridge::textFromJava: self was not set";
}

void JavaBridge::registerNativeMethods()
{
  JNINativeMethod methods[] {{"giveDataFeedback", "(Ljava/lang/String;)V", reinterpret_cast<void *>(textFromJava)}};
  QAndroidJniEnvironment env;
  jclass objectClass = env->GetObjectClass(javaClass.object<jobject>());
  env->RegisterNatives(objectClass, methods, sizeof (methods) / sizeof (methods[0]));
  env->DeleteLocalRef(objectClass);
}
