package com.eg.demo;  
  
/** 
 * 半角全角转换及替换半角全角空白 
 * @author maochangming 
 */  
public class Test {  
    public static void main(String[] args) throws Exception {  
        try {  
            //去首尾空格，不管是全角半角：  
            String s = "nihaoｈｋ　　　　ｎｉｈｅｈｅ　　　　　";  
            System.out.println("s.length=" + s.length());  
            // s = s.replaceAll("^\\s*|\\s*$", "");  
            s = s.replaceAll("(^[ |　]*|[ |　]*$)", "");  
            s = s.replaceAll("　", "");  
            System.out.println("s.length=" + s.length());  
            System.out.println("s===" + s);  
  
            String QJstr = "HELLO";  
            String QJstr1 = "ＨＥＬＬＯ";  
  
            String result = BQchange(QJstr);  
            String result1 = QBchange(QJstr1);  
  
            System.out.println(QJstr + "\n" + result);  
            System.out.println(QJstr1 + "\n" + result1);  
        } catch (Exception ex) {  
            throw new Exception("ERROR:" + ex.getMessage());  
        }  
    }  
  
    /** 
     * 半角转全角 
     * @param QJstr 
     * @return 
     */  
    public static final String BQchange(String QJstr) {  
        String outStr = "";  
        String Tstr = "";  
        byte[] b = null;  
  
        for (int i = 0; i < QJstr.length(); i++) {  
            try {  
                Tstr = QJstr.substring(i, i + 1);  
                b = Tstr.getBytes("unicode");  
            } catch (java.io.UnsupportedEncodingException e) {  
                e.printStackTrace();  
            }  
  
            if (b[3] != -1) {  
                b[2] = (byte) (b[2] - 32);  
                b[3] = -1;  
                try {  
                    outStr = outStr + new String(b, "unicode");  
                } catch (java.io.UnsupportedEncodingException e) {  
                    e.printStackTrace();  
                }  
            } else  
                outStr = outStr + Tstr;  
        }  
  
        return outStr;  
    }  
  
    /** 
     * 全角转半角 
     * @param QJstr 
     * @return 
     */  
    public static final String QBchange(String QJstr) {  
        String outStr = "";  
        String Tstr = "";  
        byte[] b = null;  
  
        for (int i = 0; i < QJstr.length(); i++) {  
            try {  
                Tstr = QJstr.substring(i, i + 1);  
                b = Tstr.getBytes("unicode");  
            } catch (java.io.UnsupportedEncodingException e) {  
                e.printStackTrace();  
            }  
  
            if (b[3] == -1) {  
                b[2] = (byte) (b[2] + 32);  
                b[3] = 0;  
                try {  
                    outStr = outStr + new String(b, "unicode");  
                } catch (java.io.UnsupportedEncodingException e) {  
                    e.printStackTrace();  
                }  
            } else  
                outStr = outStr + Tstr;  
        }  
  
        return outStr;  
    }  
  
}  