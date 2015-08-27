
import java.util.Random;

/** 
Generate pseudo-random floating point values, with an 
approximately Gaussian (normal) distribution.

Many physical measurements have an approximately Gaussian 
distribution; this provides a way of simulating such values. 
*/
public final class StdGaussian {
  
  public static void main(String... aArgs){
    StdGaussian gaussian = new StdGaussian();
    double MEAN = 1.0f; 
    double VARIANCE = 5.0f;
    for (int idx = 1; idx <= 1000; ++idx){
      log( gaussian.getGaussian(MEAN, VARIANCE));
    }
  }
    
  private Random fRandom = new Random();
  
  private double getGaussian(double aMean, double aVariance){
      return aMean + fRandom.nextGaussian() * aVariance;
  }

  private static void log(Object aMsg){
    System.out.println(String.valueOf(aMsg));
  }
} 

