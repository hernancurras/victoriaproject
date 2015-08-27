/*
  ReadAnalogVoltage
  Reads an analog input on pin 0, converts it to voltage, and prints the result to the serial monitor.
  Attach the center pin of a potentiometer to pin A0, and the outside pins to +5V and ground.
*/

void setup() {
  Serial.begin(9600);
}

void loop() {
  
  
  int true_wind = 0;
  
  int wind_vane = analogRead(A0); /*ANA pin - AS5030*/
  int sail_pos = analogRead(A1);  /*MAST-POT cursor @ 10K ohm, */
  
  wind_vane = map(wind_vane, 0, 1023, 0, 360);
  sail_pos = map(sail_pos, 0, 1023, 0, 360);
    
  wind_vane = (wind_vane - 285);
  if (wind_vane < 0) (wind_vane = wind_vane+360);
  
  true_wind = sail_pos + wind_vane;
  if (true_wind > 360) (true_wind = true_wind-360);

  Serial.print(wind_vane);
  Serial.print("-");
  Serial.print(sail_pos);
  Serial.print("-");
  Serial.println(true_wind); 
  delay(100);
}
