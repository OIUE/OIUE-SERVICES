package org.oiue.service.bytes.bytes;

import java.io.Serializable;
import java.util.Map;

import org.oiue.service.bytes.api.BytesDecodeEncoded;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.bytes.ByteUtil;

@SuppressWarnings({ "serial"})
public class ByteDecodeEncoded implements BytesDecodeEncoded,Serializable {

    Logger logger;

    public ByteDecodeEncoded(LogService logService) {
        this.logger=logService.getLogger(this.getClass());
    }

    @Override
    public byte[] encoded(byte[] s, int index, int size, Object value) {
        byte[]  rtn=null;
        if(s!=null&&s.length>index+size){
            rtn = s;
        }else{
            rtn = new byte[index+size];
            if(s!=null)
                System.arraycopy(s, 0, rtn, 0, s.length);
        }
        if (value!=null) {
            byte[]  v ;
            if(value instanceof byte[]){
                v = (byte[]) value;
            }else if(value instanceof Byte[]){
                v = (byte[]) value;
            }else if(value instanceof String){
                v = ((String)value).getBytes();
            }else if(value instanceof Integer){
                v = ByteUtil.int2bytes(Integer.valueOf(value+""), size);
            }else{
                throw new RuntimeException("no convert to bytes:"+value.getClass());
            }
            System.arraycopy(v,0  , rtn,index,size);
        }
        
//        if(logger.isDebugEnabled()){
//            logger.debug("uint rtn:"+ByteUtil.toHexString(rtn));
//        }
        return rtn;
    }

    @Override
    public Object decode(byte[] s, int index, int size, Map<?, ?> d) {
        if(s==null || s.length<index+size)
            throw new RuntimeException("undecode s["+ByteUtil.toHexString(s)+"] ,index="+index+",size="+size+",d="+d);
        byte[] strb=new byte[size];
        System.arraycopy(s,index, strb,0,size);
        return strb;
    }

}