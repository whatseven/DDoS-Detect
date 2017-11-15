import com.google.gson.Gson;
import org.apache.storm.spout.Scheme;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.List;


public class TestMessageScheme implements Scheme {
    private HashMap ip_set;
    private Gson gson;
    private static final Logger LOGGER = LoggerFactory.getLogger(TestMessageScheme.class);


    public List<Object> deserialize(ByteBuffer byteBuffer) {
        try {
            Charset charset = null;
            CharsetDecoder decoder = null;
            CharBuffer charBuffer = null;
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            //用这个的话，只能输出来一次结果，第二次显示为空
            // charBuffer = decoder.decode(buffer);
            charBuffer = decoder.decode(byteBuffer.asReadOnlyBuffer());
            String msg = charBuffer.toString();
            //获取到str后
            gson=new Gson();
            ip_set=new HashMap<String,String>();
            ip_set=gson.fromJson(msg,ip_set.getClass());

            return new Values(ip_set.get("IPV4_SRC_ADDR")
                    ,ip_set.get("IPV4_DST_ADDR")
                    ,ip_set.get("L4_DST_PORT")
                    ,ip_set.get("LAST_SWITCHED")
                    ,ip_set.get("IN_PKTS")
                    ,ip_set.get("PROTOCOL")
                    ,ip_set.get("IN_BYTES"));
        }  catch (CharacterCodingException e) {
            e.printStackTrace();
        }

        //TODO: what happend if returns null?
        return null;
    }


    public Fields getOutputFields() {
        return new Fields("s_ip","d_ip","d_port","time","pktNum","prot","pktSize");
    }

}