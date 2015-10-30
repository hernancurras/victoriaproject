Victoria Project 
================


Victoria Project is an Autonomous Autopilot 
-------------------------------------------

* Victoria Project input/actuators are delivered by Arduino. 
* Victoria Project Planner is a high level language, based on Python, with a Real Time Queue engine, based to run on Raspberry Pi or ARM boards.
* Integration_v1 is the first version of the autonomous I/O board developed for Arduino.
* Tracksail-AI is a simulator.
* victoria_client, is the first draft of the Autonomous Autopilot engine.

Integration_v1: Actuators. Created from a Arduino Nano
------------------------------------------------------

1. Boat, actually is just the hardware need, a hull, a bulb, a sail
2. Windvane (AS5030), to get the wind direction, analog sensor >1.8*2 degrees of error.
[AS5030](http://ams.com/eng/content/download/11929/212604/AS5030_Datasheet_v2-3.pdf "AS5030_Datasheet_v2-3.pdf")
3. GPS (SkyNav SKM53 DS), a regular GPS COM 9600bps, 40Channels.
[SkyNav SKM53 DS](www.nooelec.com/files/SKM53_Datasheet.pdf "SkyNav SKM53 DS")
4. RF APC220 TX-RX, a full-duplex 100mW 966 Mhz telemetry long range device.
[RF APC220 TX-RX](http://www.dfrobot.com/image/data/TEL0005/APC220_Manual_en.pdf "RF APC220 TX-RX") 
5. SDCARD, Storage MicroSD device.
6. RC Servos
10. Serial Remote console.

Tracksail-AI
------------
Nothing to add here... just a dependency of [tracksail-ai](http://github.com/boatd/tracksail-ai "tracksail-ai")


victoria_client
---------------
The sail engine, ready to run on the simulator Tracksail-AI.
Future editions are going to be ported to Python.















~                                                            
