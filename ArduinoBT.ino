//Codigo para encender Led a distancia

#include <SoftwareSerial.h>

SoftwareSerial BtSerial(8, 9); // RX, TX
int LED=10;
int LED7=7;
int ledState1=LOW;
char estado='0';
char orden;
void setup(){
  BtSerial.begin(38400);
  Serial.begin(9600);  
  pinMode(LED,OUTPUT);
  pinMode(LED7,OUTPUT);  
}

void loop(){
 //BtSerial.print('1'); 
 if(BtSerial.available()>0){   
    estado = BtSerial.read(); 
    Serial.println(estado);
 }  
 if (estado =='1'){
   digitalWrite(LED,HIGH);   
   ledState1=HIGH;
   estado='2';
  }
  if(ledState1==HIGH){
    delay(3000); 
    digitalWrite(LED,LOW);    
    ledState1=LOW;
  }
  
  
}  
