package PostTest;

import org.oiue.tools.string.Md5;

public class md5test {

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        try {
            Md5 md5 = new Md5(("25fs5sh0").getBytes("utf-8"));
            byte b[] = md5.getDigest();
            System.out.println("md5:"+md5.getStringDigest().toLowerCase());
            
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

}
