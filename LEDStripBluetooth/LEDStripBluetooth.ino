#include <Adafruit_NeoPixel.h>
#define LEDPIN 6
#define LEDNUMBER 288
#define BRIGHTNESS 10

// Parameter 1 = number of pixels in strip
// Parameter 2 = pin number (most are valid)
// Parameter 3 = pixel type flags, add together as needed:
//   NEO_KHZ800  800 KHz bitstream (most NeoPixel products w/WS2812 LEDs)
//   NEO_KHZ400  400 KHz (classic 'v1' (not v2) FLORA pixels, WS2811 drivers)
//   NEO_GRB     Pixels are wired for GRB bitstream (most NeoPixel products)
//   NEO_RGB     Pixels are wired for RGB bitstream (v1 FLORA pixels, not v2)
Adafruit_NeoPixel strip = Adafruit_NeoPixel(LEDNUMBER, LEDPIN, NEO_GRB + NEO_KHZ800);


uint32_t color1, color2;
int r1,g1,b1,r2,g2,b2;
int operationMode = 1;                // 1 = startMode, 2 = stopMode, 3 = configMode
int currentDropPos = 90;
String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete
boolean runningTF = true;
int testTF = 1;

void setup(){

  pinMode(13, OUTPUT);

  // initialize serial:
  Serial.begin(9600);
  
  // reserve 200 bytes for the inputString:
  inputString.reserve(200);

  //Set default Color:
  r1 = 16;
  r2 = 100;
  g1 = 78;
  g2 = 200;
  b1 = 139;
  b2 = 160;
  color1 = strip.Color(r1,g1,b1);
  color2 = strip.Color(r2,g2,b2);

  strip.begin();
  strip.show(); // Initialize all pixels to 'off'
  strip.setBrightness(BRIGHTNESS);
} 

void loop(){
  
  // Check Serial incoming stream:
  serialEvent();
  
  // print the string when a newline arrives:
  if (stringComplete) {
    if( inputString == "%" ){      // stopmode
      //Serial.println("Switch to STOPMODE");
      Serial.print("%OK#");
      operationMode = 2;
    }
    else if( inputString == "*"){  // startmode
      //Serial.println("Switch to STARTMODE");
      Serial.print("*OK#");
      currentDropPos = 90;
      operationMode = 1;
    }
    else if( inputString == "&"){  // Configmode
      //Serial.println("Switch to CONFIGMODE");
      Serial.print("&OK#");
      operationMode = 3;
    }
    else if( operationMode == 3 && inputString.charAt(0) == '@'){  // color config 
      if(inputString == "@" ){
         Serial.print('$');
         Serial.print(r1);Serial.print(';');
         Serial.print(g1);Serial.print(';');
         Serial.print(b1);Serial.print(';');
         Serial.print(r2);Serial.print(';');
         Serial.print(g2);Serial.print(';');
         Serial.print(b2);Serial.print('#');
      }else{
        int index[5];
        for(int i=0;i<=4;i++){
            if(i == 0){
              index[i] = inputString.indexOf(";");
            }
            else{
              index[i] = inputString.indexOf(";",index[i-1]+1);
            }
        }
        r1 =   inputString.substring(1,index[0]).toInt();
        g1 =   inputString.substring(index[0]+1,index[1]).toInt();
        b1 =   inputString.substring(index[1]+1,index[2]).toInt();
        r2 =   inputString.substring(index[2]+1,index[3]).toInt();
        g2 =   inputString.substring(index[3]+1,index[4]).toInt();
        b2 =   inputString.substring(index[4]+1).toInt();
        color1 = strip.Color(r1,g1,b1);
        color2 = strip.Color(r2,g2,b2);
        Serial.print(inputString+"#");
       }
    }
    
    // clear the string:
    inputString = "";
    stringComplete = false;
  }

  switch(operationMode){
    case 1:       // Start Mode:
      startmode();
      break;
    case 2:       // Stop Mode:
      stopmode();
      break;
    case 3:       // Config Mode:
      configmode();
      break;
    default:
      break;
    }
}

void serialEvent() {
  while (Serial.available()) {
    char inChar = (char)Serial.read();
    if (inChar == '#') {
      stringComplete = true;
    }else{
      inputString += inChar;
    }
  }
}

void startmode(){

  // Mode identifier: LED 13 Blink
//  if(testTF == 1){
//    digitalWrite(13,HIGH);
//    testTF = 0;
//  }else{
//    digitalWrite(13,LOW);
//    testTF = 1;
//  }
//  delay(100);
  
  //Eiszapfen:
    for(int i = 0; i < 90; i++) { 
        strip.setPixelColor(i, color1);
    }
    for(int i = 0; i < 8; i++) { 
        strip.setPixelColor(LEDNUMBER-i, color2);
    }    
    for(int dot = 90; dot < LEDNUMBER; dot++) { 
      for(int i = 0; i<5;i++){
        strip.setPixelColor(dot+i, color2);
       }
       strip.show();
      for(int i = 0; i<5;i++){
        strip.setPixelColor(dot+i, strip.Color(0,0,0)); 
       }
    }
}

void stopmode(){
  // Mode identifier: LED 13 OFF
  // digitalWrite(13,LOW);
  
  for(int i=0; i<LEDNUMBER; i++){ 
    strip.setPixelColor(i, strip.Color(0,0,0)); //change RGB color value here
  }
  strip.show();
  delay(500);
}

void configmode(){
  // Mode identifier: LED 13 ON
  // digitalWrite(13,HIGH);
  
  for(int i=0; i<=LEDNUMBER; i++){
    if(i < 90){
      strip.setPixelColor(i, color1); //change RGB color value here 
    }
    else if( i>= 90 && i < 95){
      strip.setPixelColor(i, color2);
    }
    else if ( i >= (LEDNUMBER - 3)){
       strip.setPixelColor(i, color2);
    }
    else{
       strip.setPixelColor(i, strip.Color(0,0,0)); 
    }
  }
  strip.show();
  delay(500);
}

