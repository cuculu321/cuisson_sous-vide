#include <OneWire.h>
#include <DallasTemperature.h>
#include <WiFi.h>
#include <PubSubClient.h>

// WiFi
const char *ssid = "";
const char *passwd = "";

// Pub/Sub
const char* mqttHost = "192.168.0.8"; // MQTTのIPかホスト名
const int mqttPort = 1883;       // MQTTのポート
WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);

const char* pub_topic = "micon/temp";     // 送信先のトピック名
char payload[10];                   // 送信するデータ

const char* sub_topic = "android/data"; //受信データのトピック名

unsigned long send_alarm = 60000;

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

//温度関係
float temp = 0;
float use_temp = 0; //平均値を求めるに使う変数
float temp_box[STACK_NUM] = {0};
float send_temp = 0; //送信データに使用する変数
int read_count = 0;
int target = 70;

//PI制御関係
float duty = 0;
float dt, preTime;
float vol;
float P, I, D, preP, pg, ig, pwm;

void setup_wifi() {

  delay(10);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, passwd);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  randomSeed(micros());

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}


void callback(char* sub_topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(sub_topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();

  // get temp
  if ((char)payload[0] == 't' && (char)payload[1] == 'e') {
    char temp_data10 = (char)payload[6];
    int te_data10 = ctoi(temp_data10) * 10;
    char temp_data1 = (char)payload[7];
    int te_data = ctoi(temp_data1) + te_data10;
    Serial.println(te_data);
    
  } else if((char)payload[0] == 't' && (char)payload[1] == 'i'){
    if((char)payload[8] == 'e'){
      char time_data10 = (char)payload[6];
      int ti_data10 = ctoi(time_data10) * 10;
      char time_data1 = (char)payload[7];
      int ti_data = ctoi(time_data1) + ti_data10;
      Serial.println(ti_data);
    }else{
      char time_data100 = (char)payload[6];
      int ti_data100 = ctoi(time_data100) * 100;
      char time_data10 = (char)payload[7];
      int ti_data10 = ctoi(time_data10) * 10;
      char time_data = (char)payload[8];
      int ti_data = ctoi(time_data) + ti_data10 + ti_data100;
      Serial.println(ti_data);
    }
  }
}

void reconnect() {
  // Loop until we're reconnected
  while (!mqttClient.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Create a random client ID
    String clientId = "ESP32Client-";
    clientId += String(random(0xffff), HEX);
    // Attempt to connect
    if (mqttClient.connect(clientId.c_str(), "kies", "wtpotnt")) {
      Serial.println("connected");
      mqttClient.subscribe(sub_topic);
    } else {
      Serial.print("failed, rc=");
      Serial.print(mqttClient.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

void mqtt_pub(char* pub_data){
  Serial.println("Publish");
  mqttClient.publish(pub_topic, pub_data);
}

void setup(void) {
  Serial.begin(115200);
  
  //温度センサー関係
  sensors.setResolution(SENSER_BIT);
  ledcSetup(LEDC_CHANNEL_0, LEDC_BASE_FREQ, LEDC_TIMER_8_BIT) ; // 8ビット精度で制御
  ledcAttachPin(HEAT_PIN, LEDC_CHANNEL_0) ;
  preTime = micros();

  //Wifiに接続
  setup_wifi();
  //Brokerサーバーへのアクセス
  mqttClient.setServer(mqttHost, 1883);
  mqttClient.setCallback(callback);
}
  
void loop(void) {
  pwm = power();
  ledcWrite(LEDC_CHANNEL_0, pwm);
  Serial.print("pwm: ");
  Serial.print(pwm);
  delay(10);
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

  if(millis()>=send_alarm){
    send_alarm+=60000;
    itoa(send_temp, payload, 10);
    mqtt_pub(payload);
  }
    
  if ( ! mqttClient.connected() ) {
    reconnect();
  }
  mqttClient.loop();
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

int ctoi(char c) {
  if (c >= '0' && c <= '9') {
    return c - '0';
  }
  return 0;
}

