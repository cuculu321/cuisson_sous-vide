#include <OneWire.h>
#include <DallasTemperature.h>
  
#define temp_pin 22 // データ(黄)で使用するポート番号
#define SENSER_BIT 9      // 精度の設定bit
#define HEAT_PIN 32 //SSRにつなぐポート

#define STACK_NUM 7

#define Kp 2.7
#define Ki 0.01

#define LEDC_CHANNEL_0 0
#define LEDC_TIMER_8_BIT 8

#define LEDC_BASE_FREQ 5000

OneWire oneWire(temp_pin);
DallasTemperature sensors(&oneWire);

float temp = 0;
float use_temp = 0; //平均値を求めるに使う変数
float temp_box[STACK_NUM] = {0};
float send_temp = 0; //送信データに使用する変数
int read_count = 0;
int target = 60;

float duty = 0;
float dt, preTime;
float vol;
float P, I, D, preP, pg, ig, pwm;

void setup(void) {
  Serial.begin(9600); //温度表示確認用
  
  sensors.setResolution(SENSER_BIT);
  ledcSetup(LEDC_CHANNEL_0, LEDC_BASE_FREQ, LEDC_TIMER_8_BIT) ; // 8ビット精度で制御
  ledcAttachPin(HEAT_PIN, LEDC_CHANNEL_0) ;
  preTime = micros();
}
  
void loop(void) {
  pwm = power();
  ledcWrite(LEDC_CHANNEL_0, pwm);
  Serial.print("pwm: ");
  Serial.print(pwm);
  sensors.requestTemperatures();              // 温度取得要求
  temp = sensors.getTempCByIndex(0);

  //エラーの値を取り除く
  if(temp >= -100){ 
    use_temp += temp;
    temp_box[read_count] = temp;
    read_count++;
  }
  if(read_count >= STACK_NUM){
    send_temp = use_temp/STACK_NUM;
    read_count = 0;
    use_temp = 0;
    pg = p();
    ig = i();
    duty = pg + ig;
    Serial.print(" duty: ");
    Serial.print(duty);
    Serial.print(" temp: ");
    Serial.println(send_temp);
  }

}

float p(){
/*** P制御
temp    温度データ
targe   目標の温度
Kp      比例ゲイン
***/ 
  float d = target - send_temp;
  if(d < 0){
    return 0;
  }
  return d / target * Kp;
}

float i(){
  float s = 0;
  for(int i = 0; i < STACK_NUM-1; i++){
    int d1 = target - temp_box[i];
    int d2 = target - temp_box[i+1];
    s += (d1 + d2) / 2*Ki;
  }
  return s;
}

int power(){
  int power = duty * 100 / 2 + 10;
  if(power > 255){
    return 255;
  }else if(power < 0){
    return 0;
  }
  return power;
}

