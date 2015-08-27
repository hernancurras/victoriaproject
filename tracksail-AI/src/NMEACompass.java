import java.util.Random;

public class NMEACompass extends NMEASensor {

    protected int convertCoords(int angle)
    {
        return ((360 + ((180 - angle) + 180)) + 90) % 360; 
    }

    public NMEACompass(Player p)
    {
        super(p);
        portnum=5557;
        numberOfSentences=1;
        sleepTime=200;
    }

    public String generateSentence(int sentenceNumber)
    {

        try{
            int heading=(player.getDirection()+360)%360;  
            heading = convertCoords(heading);                 
            

            double variance = 1.0;

            Random fRandom = new Random();
            double noise = fRandom.nextGaussian() * variance;
            
            noise=noise+(double)heading;

            if(noise<0)
            {
                noise=noise+360;
            }


            String buf = String.format("HCHDG,%1$3.1f,,,,",noise);

            char checksum=NMEACheckSum(buf);


            String final_buf = String.format("$%1$s*%2$X\n",buf,(byte)checksum);
            return final_buf;
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
        }
        return "";
        
    }
}
