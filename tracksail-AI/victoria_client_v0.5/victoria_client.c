#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <time.h>
#include <ncurses.h>
#include <stdint.h>
#include <unistd.h>

#include "simulator_interface.h"

/*Victoria Sailboat System 2014 - Hernan Curras hernancurras@gmail.com
Based on example_client for tracksail-AI (C)Copyright Colin Sauze 2005-2009*/

/*Angulo minimo de ceñida aceptable*/
#define HOW_CLOSE 37
#define PGAIN 1.7
#define RUDDERMAX 45
#define RUDDERMIN -45

/*Inicialización*/
uint8_t SAILABLE=1,PORT_TACK=0,STBD_TACK=0;
uint8_t DOWNWIND_SAILABLE=1,DOWNWIND_PORT_TACK=0,DOWNWIND_STBD_TACK=0;

/*calculates difference between two headings taking wrap around into account*/
int get_hdg_diff(int heading1,int heading2)
{
    int result;

    result = heading1-heading2;

    if(result<-180)
    {
        result = 360 + result;
        return result;
    } 
    
    if(result>180)
    {
        result = 0 - (360-result);
    }

    return result;
}

int calculate_sail_pos(int wind_dir, int new_sail_pos)
{
	//Calcular la posicion de las escotas, [0;18;36;54;72], con viento menor a 180º
	//Sailpos debería ser una linea de de optimización lineal
	//Viento desde la Derecha
        if(wind_dir <180)
        {
            if (wind_dir < 70)
                new_sail_pos = 0;
            else if (wind_dir < 80)
                new_sail_pos = 18;
            else if (wind_dir < 90)
                new_sail_pos = 36;
            else if (wind_dir < 110)
                new_sail_pos = 54;
            else
                new_sail_pos = 72;
        }

	//Calcular la posicion de las escotas, [0;342;324;306;288], con viento mayor a 290º
	//Viento desde la Izquierda
	else
        {
            if (wind_dir >= 290)
                new_sail_pos = 0;
            else if (wind_dir >= 280)
                new_sail_pos = 342;
            else if (wind_dir >= 270)
                new_sail_pos = 324;
            else if (wind_dir >= 250)
                new_sail_pos = 306;
            else
                new_sail_pos = 288;
        }
return new_sail_pos;
	
}

/*
works out if we should be tacking or not returns a new heading that reflects this
uses the #define HOW_CLOSE to decide how many degrees from the wind we should be
45 degrees is usually quoted to most people learning sailing but many boats (especially wing sailed robots) 
can sail closer and I suspect tracksail works a bit closer too.*/

int check_tacking(int relwind,int heading,int desired_heading)
{
        int truewind,tempwpthdg,temptruewind;

        truewind = relwind + heading;       // Calculate true wind direction
        if (truewind > 360) truewind -= 360;
        if((abs(truewind-desired_heading))>180)
        {
            if((360-(abs(truewind-desired_heading))) < HOW_CLOSE)
            {
                SAILABLE=0;
            }
            else
            {
                SAILABLE=1;
            }
        }
        //when difference less than 180
        else if (abs(truewind-desired_heading) < HOW_CLOSE)    // Only try to sail to within HOW_CLOSE degrees of the wind
        {
            SAILABLE = 0;
        }
        else
        {
            SAILABLE = 1;
            PORT_TACK = 0;
            STBD_TACK = 0;
        }

        if ((SAILABLE == 0) && (PORT_TACK ==0) && (STBD_TACK == 0)) // If we can't lay the course to the waypoint then...
        {
            temptruewind = truewind;
            tempwpthdg = desired_heading;
            if (desired_heading < HOW_CLOSE)
            {
                tempwpthdg += 180;
                temptruewind += 180;
                if (temptruewind > 360)
                {
                    temptruewind -= 360;
                }
            }
            if (desired_heading > (360 - HOW_CLOSE))
            {
                tempwpthdg -= 180;
                temptruewind -= 180;
                if (temptruewind < 0)
                {
                    temptruewind += 360;
                }
            }
            if (tempwpthdg > temptruewind)
            {
                PORT_TACK = 1;          // Set flag to stop boat "short tacking" to waypoint
            }
            else 
            {
                STBD_TACK = 1;          // Set flag to stop boat "short tacking" to waypoint
            }

        } // otherwise just sail directly to the waypoint

        if (SAILABLE == 0)
        {
             // Keep boat hard on wind on same tack until we can lay course for waypoint (enforce single tack)
            if (PORT_TACK == 1) 
            {
                desired_heading = truewind + HOW_CLOSE; // Sail HOW_CLOSE degrees off the wind on port tack 
            }

            if (STBD_TACK == 1) 
            {
                desired_heading = truewind - HOW_CLOSE; // Sail HOW_CLOSE degrees off the wind on stbd tack 
            }
        }

        if (desired_heading > 359)
        {
            desired_heading -= 360;
        }

        if (desired_heading < 0)
        {
            desired_heading += 360;
        }
	
        return desired_heading;
}


int main(int argc,char **argv)
{
    int wind_dir=0,heading=0,desired_heading=0,heading_error=0,new_sail_pos,new_rudder_pos;
    int id=0,abort=0,distance=0;
    int escotas=0;
    setup(); //inicia la conexion con el simulador server

    stop_running = abort;

    while(abort!=1)
    {

	heading=get_simulator_compass();
    wind_dir=get_simulator_wind();

	//el rumbo al siguiente waypoint
    desired_heading=get_desired_heading();
	//Revisamos si deberemos ceñir
    desired_heading=check_tacking(wind_dir,heading,desired_heading);
	//definimos la diferencia entre el rumbo deseado y el rumbo 
    heading_error=get_hdg_diff(heading,desired_heading);
        
	//Definimos un sector de 10º de error
    if(abs(heading_error)<5)
		{
		new_rudder_pos=0;
		}
	else
		{
		//Calcular la posicion del timón
		new_rudder_pos = (int)(heading_error * PGAIN);
		}
	
	//Ajustamos los limites entre [RUDDERMIN;RUDDERMAX] 										
	if(new_rudder_pos<RUDDERMIN)
		{
	    new_rudder_pos=RUDDERMIN;
		}
	else if(new_rudder_pos>RUDDERMAX)
		{
	    new_rudder_pos=RUDDERMAX;
		}
	
	/*Traduce el timon, cuando está en [-90;+90] y lo queremos en [90;270]*/
	if(new_rudder_pos<0)
        {
		set_simulator_rudder(-1*new_rudder_pos);
        }
	else
        {
		set_simulator_rudder(360-new_rudder_pos);
        }
	
	distance = get_wp_distance(distance);
	
	escotas = calculate_sail_pos(wind_dir,new_sail_pos);
	set_simulator_sail(calculate_sail_pos(wind_dir,new_sail_pos));
	
	//Telemetría
	printf("Id:%d Distancia:%dm Error Rumbo:%d Compass:%d Viento:%d Nueva Posicion Timon:%d Escotas:%d SAILABLE:%d\n",id,distance,heading_error,heading,wind_dir,new_rudder_pos,escotas,SAILABLE);
	
	usleep(250000); /*Ciclo de 4 Hz*/
	stop_running = abort;
	id++;
    }
    
    return 0;
}
