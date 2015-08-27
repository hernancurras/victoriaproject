import java.io.*;  
import java.net.*;  


public abstract class NMEASensor extends Thread
{
    int portnum=5556;
    int sleepTime=1000;
    int numberOfSentences=1;
    Player player;
    DatagramSocket s;

    public NMEASensor(Player p)
    {
        player = p;
        try
        {
            s = new DatagramSocket();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
    gets the number of seconds to sleep between strings
    @return the number of seconds to sleep
    */
    public int getSleepTime()
    {
        return sleepTime;
    }

    /**
    sets the number of seconds to sleep between strings
    @param sleepTime - the number of seconds to sleep
    */
    public void setSleepTime(int sleepTime)
    {
        this.sleepTime = sleepTime;
    }

    public void sendPacket(String data,InetAddress dest)
    {
        try
        {

            byte[] sendData = data.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, dest, portnum);  
            s.send(sendPacket);  
           
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }


    /**
    method to generate an NMEA string
    @param sentenceNum - the number of the possible sentences that can be sent, starting from 0
    @return the NMEA string
    */
    public abstract String generateSentence(int sentenceNum);

    public void run()
    {
        String sentence;
        //System.out.println("NMEA Sensor main loop\n");

        while(true)
        {
            for(int i=0;i<numberOfSentences;i++)
            {
                sentence = generateSentence(i);
                //System.out.println("Sending " + sentence);
                sendPacket(sentence,InetAddress.getLoopbackAddress());
            }
           
            try
            {
                Thread.sleep(getSleepTime());
            }
            catch(InterruptedException e)
            {
            }
            
        }
    }


    char NMEACheckSum(String chars) {
        char check = 0;
        int c;
        // iterate over the string, XOR each byte with the total sum:
        for (c = 0; c < chars.length(); c++) {
            check = (char)((int)check ^ (int)chars.charAt(c));
        }
        // return the result
        return check;
    }


}