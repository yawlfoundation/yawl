package au.edu.qut.yawl.util;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.StopWatch;

public class SimpleLogger {

   public Object log(ProceedingJoinPoint call) throws Throwable {
      try {
    	  System.err.println("LOG " + call.getSignature().toLongString());
         return call.proceed();
      } finally {
      }
   }
}
