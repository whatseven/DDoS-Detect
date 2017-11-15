import com.google.gson.Gson;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Math.log;

/**
 * Created by whatseven on 2017/5/22.
 */
public class SIDICountBolt extends BaseWindowedBolt {

    private OutputCollector collector;
    HashMap<String,HashMap<String,Integer>> sidi;
    CopyOnWriteArrayList<HashMap<String,String>> detect_out;

    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;
        sidi=new HashMap<String,HashMap<String,Integer>>();
        detect_out=new CopyOnWriteArrayList<HashMap<String,String>>();

    }

    public void execute(TupleWindow inputWindow) {
        int sum=0;
        for(Tuple tuple: inputWindow.get()) {
            //统计SIDI
            String d_ip=(String) tuple.getValueByField("d_ip");
            String s_ip=(String) tuple.getValueByField("s_ip");
            //统计SIDI
            if (sidi.get(d_ip)==null)
                sidi.put(d_ip,new HashMap<String, Integer>());
            if(sidi.get(d_ip).get(s_ip)==null)
                sidi.get(d_ip).put(s_ip,0);
            sidi.get(d_ip).put(s_ip,sidi.get(d_ip).get(s_ip)+1);

            //System.out.println("统计后添加"+s_ip+"->"+d_ip);

        }
        //计算SIDI
        for (Map.Entry<String,HashMap<String,Integer>> di:sidi.entrySet()){
            int t_sum=0;//一个D_IP的流总数
            double tt_sum=0;//一个D_IP的SIDI

            //计算一个D_IP的流总数
            for (Map.Entry<String,Integer> si:di.getValue().entrySet()){
                t_sum+=si.getValue();
            }
            //计算一个D_IP的SIDI
            for (Map.Entry<String,Integer> si:di.getValue().entrySet()){
                double num=(double)si.getValue()/t_sum;
                tt_sum+= num * log(num)/log(2);
            }
//            System.out.println("计算后"+tt_sum);
//            System.out.println("计算后"+t_sum);
            //放入hash表
            HashMap<String,String> temp =new HashMap<String,String>();
            temp.put("name","sidi");
            temp.put("d_ip",di.getKey());
            temp.put("sidi", String.valueOf(tt_sum));
            temp.put("t_sidi", String.valueOf(t_sum));
            detect_out.add(temp);

        }

        //发送
        Gson gson=new Gson();

        collector.emit(new Values(gson.toJson(detect_out)));
        //System.out.println("From count"+detect_out);
        detect_out.clear();
        sidi.clear();
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
       declarer.declare(new Fields("count"));
    }

}
