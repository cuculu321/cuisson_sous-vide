#include <OneWire.h>
#include <DallasTemperature.h>
  
#define temp_pin 36 // データ(黄)で使用するポート番号
#define SENSER_BIT    9      // 精度の設定bit
  
OneWire oneWire(temp_pin);
DallasTemperature sensors(&oneWire);
  
void setup(void) {
  Serial.begin(9600); //温度表示確認用
  sensors.setResolution(SENSER_BIT);
}
  
void loop(void) {
  sensors.requestTemperatures();              // 温度取得要求
  Serial.println(sensors.getTempCByIndex(0)); //温度の取得&シリアル送信
}
