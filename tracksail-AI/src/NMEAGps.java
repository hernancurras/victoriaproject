import java.util.Calendar;
import java.util.GregorianCalendar;

public class NMEAGps extends NMEASensor
{
    double avg_speed,last_easting=0.0,last_northing=0.0;
    long curtime,last_fix_time;

    protected int convertCoords(int angle)
    {
        return ((360 + ((180 - angle) + 180)) + 90) % 360; 
    }

    public NMEAGps(Player p)
    {
        super(p);
        portnum=5558;
        numberOfSentences=2;
        sleepTime=1000;
    }

    public String generateSentence(int sentenceNumber)
    {

        double distanceTravelled=0;
        double northing,easting;
        char eastWest,northSouth;
        int degNorthing,degEasting;
        double minNorthing,minEasting;
        double speed=0,speedKnots=0.0,gpsHdg=0;
        String finalString;
        String latMinutes;
        String lonMinutes;

        try
        {

            double gps_position[] = GPSUtils.xYToLatLonWithNoise(player.getPosition().getX(),player.getPosition().getY(),1);

            /*double variance = 1.0;

            Random fRandom = new Random();
            double noise = fRandom.nextGaussian() * variance;
            
            noise=noise+(double)heading;

            if(noise<0)
            {
                noise=noise+360;
            }*/

            northing = gps_position[0];
            easting = gps_position[1];

            if(last_easting!=0.0&&last_northing!=0.0)
            {
                distanceTravelled=GPSUtils.getDistance(northing,easting,last_northing,last_easting);
            }
            
  
        
            curtime=System.currentTimeMillis();

           
            if(curtime-last_fix_time>1000&&sentenceNumber==1) //only do this for RMC strings
            {
        
                distanceTravelled = distanceTravelled * 1000; //put into metres
                curtime = curtime/1000;
                
                speed=distanceTravelled/(curtime-last_fix_time);
                //avg_speed=avg_speed+((speed-avg_speed)/1.0);
                speedKnots=1.94384449*speed;
            

                //gpsHdg = GPSUtils.getCourse(last_northing,last_easting,northing,easting); //gps noise causes this to be too unreliable, need to filter it


                last_easting=easting;
                last_northing=northing;
                last_fix_time=curtime;

            }
               
            


            int heading=(player.getDirection()+360)%360; 
            gpsHdg = convertCoords(heading);



            degNorthing=(int)(Math.floor(Math.abs(northing)));
            degEasting=(int)(Math.floor(Math.abs(easting)));
     
        
            minNorthing=(Math.abs(northing)-degNorthing)*60;
            minEasting=(Math.abs(easting)-degEasting)*60;
        
            if(minNorthing>=60.0)
            {
                minNorthing=0.0;
                degNorthing++;
            }

            if(minEasting>=60.0)
            {
                minEasting=0.0;
                degEasting++;
            }      
     
            if(gps_position[0]<0.0)
            {
                northSouth='S';
            }
            else
            {
                northSouth='N';
            }
            
            if(gps_position[1]<0.0)
            {
                eastWest='W';
            }
            else
            {
                eastWest='E';
            }

            if(minNorthing>=10)
            {
                latMinutes = String.format("%1$2.4f",minNorthing);
            }
            else
            {
                latMinutes = String.format("0%1$2.4f",minNorthing);
            }
            
            if(minEasting>=10)
            {
                lonMinutes = String.format("%1$2.4f",minEasting);
            }
            else
            {
                lonMinutes = String.format("0%1$2.4f",minEasting);
            }
            
            Calendar now = new GregorianCalendar();
                
            //$GPGGA,172329.454,5224.9907,N,00404.0629,W,0,00,0.0,0.0,M,,,,0000*1E
            //    $GPGGA,092204,4250.5589,S,14718.5084,E,1,04,24.4,19.7,M,,,,0000*1F
            //$GPRMC,222246.000,A,5224.9642,N,00404.8780,W,0.11,320.90,210107,,,A*7C",
             
            if(sentenceNumber==0)
            {
                finalString = String.format("GPGGA,%1$02d%2$02d%3$02d.000,%4$02d%5$s,%6$s,%7$03d%s,%8$s,1,10,1.0,0.0M,,,,0000",now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE),now.get(Calendar.SECOND),degNorthing,latMinutes,northSouth,degEasting,lonMinutes,eastWest);
            }
            else
            {
        
                finalString = String.format("GPRMC,%1$02d%2$02d%3$02d.000,A,%4$02d%5$s,%6$c,%7$03d%8$s,%9$c,%10$3.2f,%11$3.2f,%12$02d%13$02d%14$02d,,,A",now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE),now.get(Calendar.SECOND),degNorthing,latMinutes,northSouth,degEasting,lonMinutes,eastWest,speedKnots,gpsHdg,now.get(Calendar.DAY_OF_MONTH),now.get(Calendar.MONTH),now.get(Calendar.YEAR));
            }
            char checksum=NMEACheckSum(finalString);
            return String.format("$%1$s*%2$X\n",finalString,(byte)checksum);
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
        }
        return "";
    }
}
