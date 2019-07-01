#ifndef JAVABRIDGE_H
#define JAVABRIDGE_H

#include <QObject>
#include <QVector>
#include <QVariantList>
#include <QAndroidJniEnvironment>
#include <QAndroidJniObject>

class JavaBridge : public QObject
{
  Q_OBJECT
  Q_PROPERTY(QVariantList coordinate  READ getCoordinate  WRITE setCoordinate)
  Q_PROPERTY(QVariantList accelValue  READ getAccelValue  WRITE setAccelValue)
  Q_PROPERTY(QVariant     brightValue READ getBrightValue WRITE setBrightValue)
  Q_PROPERTY(QVariantList gyroValue   READ getGyroValue   WRITE setGyroValue)
  Q_PROPERTY(QString      settings    READ getSettings    WRITE setSettings)

public:
  explicit JavaBridge(QObject *parent = nullptr);

  QVariantList getCoordinate() { return m_coord; }
  QVariantList getAccelValue() { return m_accelValue; }
  QVariant     getBrightValue(){ return m_brightValue; }
  QVariantList getGyroValue()  { return m_gyroValue; }
  QString      getSettings()   { return m_settings; }

  Q_INVOKABLE void setCoordinate(QVariantList coordinate);
  Q_INVOKABLE void setAccelValue(QVariantList accelValue);
  Q_INVOKABLE void setBrightValue(QVariant brightValue);
  Q_INVOKABLE void setGyroValue(QVariantList gyroValue);
  Q_INVOKABLE void setSettings(QString settings);

private:
  QVariantList m_accelValue;
  QVariant     m_brightValue;
  QVariantList m_gyroValue;
  QVariantList m_coord;
  QString      m_settings;
  QAndroidJniObject javaClass;

  static JavaBridge *self;

  static void textFromJava(JNIEnv *env, jobject jobj, jstring text);
  void registerNativeMethods();
  void emit_text_signal(QString text) { emit text_signal(text); }

public slots:

signals:
  void text_signal(QString text);
};

#endif // JAVABRIDGE_H
