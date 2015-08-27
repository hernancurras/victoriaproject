import java.util.Random;

public class GPSUtils
{
    public static final double BASE_LAT = 52.41194851276237;
    public static final double BASE_LON = -4.093008041381836;
    /**
    calculates a position given the current position, a bearing (in degrees) and a distance (in km)
    @param bearing - the bearing from the current position in degrees
    @param lat1 - the latitude in degrees
    @param lon1 - the longitude in degrees 
    @param distance - the distance to project in km
    @return a 2 element array with the projected lat and long 
    */
    public static  double[] projectPoint(double bearing,double dist,double lat1,double lon1)
    {
        double lat2,lon2;
        //dist = dist/6367;  // d = angular distance covered on earth's surface
        double result[] = new double[2];

                

        //convert to radians
        bearing = Math.toRadians(bearing);
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);

        lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist/6367) + Math.cos(lat1)*Math.sin(dist/6367)*Math.cos(bearing) );
        lon2 = lon1 + Math.atan2(Math.sin(bearing)*Math.sin(dist/6367)*Math.cos(lat1), Math.cos(dist/6367)-Math.sin(lat1)*Math.sin(lat2));

        result[0]=Math.toDegrees(lat2);
        result[1]=Math.toDegrees(lon2);

        return result;
    }

    /**
    gets the distance between lat1,lon1 and lat2,lon2 in km
    @param lat1 - the start latitude (in degrees)
    @param lon1 - the start longitude (in degrees)
    @param lat2 - the end latitude (in degrees)
    @param lon2 - the start longitude (in degrees)
    @return the distance in km between the 2 points
    */
    public static double getDistance(double lat1,double lon1,double lat2,double lon2 )
    {
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        double theta = lon2-lon1;
        double distance = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(theta));
        if(distance<0) 
        {
            distance=distance + Math.PI;
        }
        //halfway between equatorial radius (6378km) and polar radius(6357km)
        distance = distance * 6367.0;
        return distance;
    }
    
    /**
    calculates the great circle heading between one point and another
    Does not work if one latitude is polar!!!
    @param lat1 - the start latitude (in degrees)
    @param lon1 - the start longitude (in degrees)
    @param lat2 - the end latitude (in degrees)
    @param lon2 - the start longitude (in degrees)
    @return the heading in degrees between the 2 points
    */
    public static double getCourse(double lat1,double lon1,double lat2,double lon2 )
    {
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        double C;
        double L = lon2 - lon1;
        
        double cosD = Math.sin( lat1 )*Math.sin( lat2 ) + Math.cos( lat1 )*Math.cos( lat2 )*Math.cos( L );
        
        double D = Math.acos( cosD );
        
        double cosC = ( Math.sin( lat2 ) - cosD*Math.sin( lat1 ) ) / ( Math.sin( D )*Math.cos( lat1 ) );
        
        // numerical error can result in |cosC| slightly > 1.0 
        if( cosC > 1.0 )
        {
            cosC = 1.0;
        }
        if( cosC < -1.0 )
        {
            cosC = -1.0;
        }
        
        C = 180.0*Math.acos( cosC )/Math.PI;
        
        if( Math.sin( L ) < 0.0 )
        {
            C = 360.0 - C;
        }
        
        return ( 100*C )/100.0;
    }




    /**
    converts x/y co-ordinates to a WGS84 latitude and longitude
    @param x - the x co-ordinate in metres 
    @param y - the y co-ordinate in metres
    @return a 2 element array with the latitude and longitude
    */
    public static double[] xYToLatLon(double x,double y)
    {
        double org_azimuth;

        if(Math.abs(x)<0.00000001)
        {
            x=x+0.00000001;
        }

        if(Math.abs(y)<0.00000001)
        {
            y=y+0.00000001;
        }

        //figure out the angle from the origin to our current location 

        //handle potential divide by 0
        if(Math.abs(x)>0.00000000001)
        {
            org_azimuth=Math.atan(Math.abs(y)/Math.abs(x));
        }
        else
        {
            org_azimuth=Math.atan(0.0);
        }

        org_azimuth=org_azimuth*(180/Math.PI);

        if(org_azimuth<0)
        {
            org_azimuth=org_azimuth+360;
        }
        //current point is above and right of last one
        if(x>0.0&&y>0.0)
        {
            org_azimuth=90-org_azimuth;
        }
        
        //current point is above and left of the last one
        else if(x<0.0&&y>0.0)
        {
            org_azimuth=270+org_azimuth;
        }
        
        //below and right
        else if(x>0.0&&y<0.0)
        {
            org_azimuth=90+org_azimuth;
        }
        
        //below and left
        else if(x<0.0&&y<0.0)
        {
            org_azimuth=270-org_azimuth;
        }
    

      
        //distance from the origin in metres
        double org_dist=Math.abs(Math.sqrt(Math.pow(x,2)+Math.pow(y,2)));



        //project a point to figure out our lat/lon
        double [] gps_position=GPSUtils.projectPoint(org_azimuth,org_dist/1000.0,BASE_LAT,BASE_LON);

        return gps_position;
    }

  /**
    converts x/y co-ordinates to a WGS84 latitude and longitude and adds guassian noise to the position to simulate the lack of accuracy in a real GPS
    @param x - the x co-ordinate in metres 
    @param y - the y co-ordinate in metres
    @param noiseDistance - the maximum number of metres away from the true position we should report to be
    @return a 2 element array with the latitude and longitude
    */
    public static double[] xYToLatLonWithNoise(double x,double y,double noiseDistance)
    {
        double org_azimuth;

        if(Math.abs(x)<0.00000001)
        {
            x=x+0.00000001;
        }

        if(Math.abs(y)<0.00000001)
        {
            y=y+0.00000001;
        }

        //figure out the angle from the origin to our current location 

        //handle potential divide by 0
        if(Math.abs(x)>0.00000000001)
        {
            org_azimuth=Math.atan(Math.abs(y)/Math.abs(x));
        }
        else
        {
            org_azimuth=Math.atan(0.0);
        }

        org_azimuth=org_azimuth*(180/Math.PI);

        if(org_azimuth<0)
        {
            org_azimuth=org_azimuth+360;
        }
        //current point is above and right of last one
        if(x>0.0&&y>0.0)
        {
            org_azimuth=90-org_azimuth;
        }
        
        //current point is above and left of the last one
        else if(x<0.0&&y>0.0)
        {
            org_azimuth=270+org_azimuth;
        }
        
        //below and right
        else if(x>0.0&&y<0.0)
        {
            org_azimuth=90+org_azimuth;
        }
        
        //below and left
        else if(x<0.0&&y<0.0)
        {
            org_azimuth=270-org_azimuth;
        }
    

      
        //distance from the origin in metres
        double org_dist=Math.abs(Math.sqrt(Math.pow(x,2)+Math.pow(y,2)));

        Random fRandom = new Random();
        double noise = fRandom.nextGaussian() * Math.sqrt(noiseDistance);
        
        org_dist = org_dist + noise;


        //project a point to figure out our lat/lon
        double [] gps_position=GPSUtils.projectPoint(org_azimuth,org_dist/1000.0,BASE_LAT,BASE_LON);

        return gps_position;
    }

    /**
    Converts a latitude and longitude back into and x and y co-ordinate
    @param lat - the latitude in degrees
    @param lon - the longitude in degrees
    @return a 2 element array with the x and y co-ordinate associated with the specified latitude and longitude
    */
    public static double[] latLonToXY(double lat,double lon)
    {
        double x=0,y=0;
        double pos[] = new double[2];
        
        double course = getCourse(lat,lon,BASE_LAT,BASE_LON);
        double distance = getDistance(lat,lon,BASE_LAT,BASE_LON)*1000;

        System.out.println("distance = " + distance + " course = " + course);
        //we are directly west of the origin
        if(course==90.0)
        {
            System.out.println("directly west");
            y=0;
            x=-distance;
        }
        //directly east
        else if(course==270.0)
        {
            System.out.println("directly east");
            y=0;
            x=distance;
        }

        //directly north
        else if(course==180.0)
        {
            System.out.println("directly north");
            x=0;
            y=distance;
        }
        //directly south
        else if(course==0.0||course==360.0)
        {
            System.out.println("directly south");
            x=0;   
            y=-distance;
        }

    
        //north of the base point
        else if(lat>BASE_LAT)
        {
            System.out.println("North of base point");
            x = Math.cos(Math.toRadians(90-Math.abs(course)))*distance; //opp  = hyp * cos theta
            y = -Math.sin(Math.toRadians(90-Math.abs(course)))*distance; //adj = hyp * sin theta
            
            //west of the base point
            if(course>0)
            {
                System.out.println("West of base point");           
                x=x*-1;
            }

        }

        //south of the base point
        else if(lat<BASE_LAT)
        {
            System.out.println("South of base point");
            
            x = Math.cos(Math.toRadians(90-Math.abs(course)))*distance; //opp  = hyp * cos theta
            y = -Math.sin(Math.toRadians(90-Math.abs(course)))*distance; //adj = hyp * sin theta

            //west of the base point
            if(course>0)
            {
                System.out.println("West of base point");
                x=x*-1;
            }

        }

        pos[0]=x;
        pos[1]=y;
        return pos;
    }
/*
    public static void main(String args[])
    {

        System.out.println("X=-1000 Y=-10");
        double pos[] = xYToLatLon(-1000,-10);
        System.out.println("Lat: " + pos[0] + " Lon: " + pos[1]);
        double pos2[] = latLonToXY(pos[0],pos[1]);
        System.out.println("X: " + pos2[0] + " Y: " + pos2[1]);
        System.out.println("-------------");


        System.out.println("X=-1000 Y=10");
        pos = xYToLatLon(-1000,10);
        System.out.println("Lat: " + pos[0] + " Lon: " + pos[1]);
        pos2 = latLonToXY(pos[0],pos[1]);
        System.out.println("X: " + pos2[0] + " Y: " + pos2[1]);
        System.out.println("-------------");


        System.out.println("X=1000 Y=10");
        pos = xYToLatLon(1000,10);
        System.out.println("Lat: " + pos[0] + " Lon: " + pos[1]);
        pos2 = latLonToXY(pos[0],pos[1]);
        System.out.println("X: " + pos2[0] + " Y: " + pos2[1]);
        System.out.println("-------------");


        System.out.println("X=1000 Y=-10");
        pos = xYToLatLon(1000,-10);
        System.out.println("Lat: " + pos[0] + " Lon: " + pos[1]);
        pos2 = latLonToXY(pos[0],pos[1]);
        System.out.println("X: " + pos2[0] + " Y: " + pos2[1]);
        System.out.println("-------------");


        System.out.println("X=0 Y=-10");
        pos = xYToLatLon(0,-10);
        System.out.println("Lat: " + pos[0] + " Lon: " + pos[1]);
        pos2 = latLonToXY(pos[0],pos[1]);
        System.out.println("X: " + pos2[0] + " Y: " + pos2[1]);
        System.out.println("-------------");


        System.out.println("X=0 Y=10");
        pos = xYToLatLon(0,10);
        System.out.println("Lat: " + pos[0] + " Lon: " + pos[1]);
        pos2 = latLonToXY(pos[0],pos[1]);
        System.out.println("X: " + pos2[0] + " Y: " + pos2[1]);
        System.out.println("-------------");

        System.out.println("X=10 Y=0");
        pos = xYToLatLon(10,0);
        System.out.println("Lat: " + pos[0] + " Lon: " + pos[1]);
        pos2 = latLonToXY(pos[0],pos[1]);
        System.out.println("X: " + pos2[0] + " Y: " + pos2[1]);
        System.out.println("-------------");

        System.out.println("X=-10 Y=0");
        pos = xYToLatLon(-10,0);
        System.out.println("Lat: " + pos[0] + " Lon: " + pos[1]);
        pos2 = latLonToXY(pos[0],pos[1]);
        System.out.println("X: " + pos2[0] + " Y: " + pos2[1]);
        System.out.println("-------------");

    }*/

}