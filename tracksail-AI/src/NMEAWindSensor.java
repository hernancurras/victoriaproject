import java.util.Random;

public class NMEAWindSensor extends NMEASensor
{
    public NMEAWindSensor(Player p)
    {
        super(p);
        portnum=5556;
        numberOfSentences=1;
    }

    public String generateSentence(int sentenceNumber)
    {

        try{

            //needs to be relative to boat
            int wind = player.getGame().getWindDirection();
            //make relative to the boat
            wind = ((player.getDirection() - wind) + 360 ) % 360;
                        
            //invert the angle by 180 degrees
            wind = (wind + 180) % 360;
            
            double variance = 2.0;

            Random fRandom = new Random();
            double noise = fRandom.nextGaussian() * variance;
            
            noise=noise+(double)wind;

            if(noise<0)
            {
                noise=noise+360;
            }


            String buf = String.format("IIMWV,%1$3.1f,R,010.00,N,A",noise);


            char checksum=NMEACheckSum(buf);


            String final_buf = String.format("$%1$s*%2$X\n",buf,(byte)checksum);
            return final_buf + "$WIXDR,C,021.0,C,,*51\n";
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
        }
        return "";
        
    }
}


