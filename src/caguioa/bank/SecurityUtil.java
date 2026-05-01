
package caguioa.bank;
import java.security.MessageDigest;

public class SecurityUtil {

    public static String hashPin(String pin){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes("UTF-8"));

            StringBuilder sb = new StringBuilder();
            for(byte b : hash){
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        }catch(Exception e){
            return null;
        }
    }
}
