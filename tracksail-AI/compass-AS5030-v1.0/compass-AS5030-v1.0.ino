/*
  ReadAnalogVoltage
  Reads an analog input on pin 0, converts it to voltage, and prints the result to the serial monitor.
  Attach the center pin of a potentiometer to pin A0, and the outside pins to +5V and ground.
*/

void setup() {
  Serial.begin(9600);
}

void loop() {
  // read the input on analog pin 0:
  int sensorValue = analogRead(A0);
  sensorValue = map(sensorValue, 0, 1023, 0, 360);
  
  sensorValue = (sensorValue - 285);
  if (sensorValue < 0) (sensorValue = sensorValue+360);

  Serial.println(sensorValue);
  delay(100);
}
