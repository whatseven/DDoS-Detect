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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Math.log;

/**
 * Created by whatseven on 2017/5/22.
 */
public class SIDPCountBolt extends BaseWindowedBolt {

    private OutputCollector collector;
    HashMap<String,HashMap<String,Integer>> sidp;
    CopyOnWriteArrayList<HashMap<String,String>> detect_out;
    Iterator<Map.Entry<String,Integer>> entries;

    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;
        sidp=new HashMap<String,HashMap<String,Integer>>();
        detect_out=new CopyOnWriteArrayList<HashMap<String,String>>();

    }

    public void execute(TupleWindow inputWindow) {
        int sum=0;
        for(Tuple tuple: inputWindow.get()) {

            String s_ip=(String) tuple.getValueByField("s_ip");
            String d_port=((Double) tuple.getValueByField("d_port")).toString() ;
            //System.out.println(d_port);
            //统计SIDP
            if (sidp.get(d_port)==null)
                sidp.put(d_port,new HashMap<String, Integer>());
            if(sidp.get(d_port).get(s_ip)==null)
                sidp.get(d_port).put(s_ip,0);
            sidp.get(d_port).put(s_ip,sidp.get(d_port).get(s_ip)+1);
            //System.out.println("统计后添加"+d_port+"->"+sidp.get(d_port).get(s_ip));
        }
        //计算SIDP
        for (Map.Entry<String,HashMap<String,Integer>> dp:sidp.entrySet()){
            int t_sum=0;//一个D_PORT的流总数
            double tt_sum=0;//一个D_PORT的SIDP

            //计算一个D_PORT的流总数
            for (Map.Entry<String,Integer> si:dp.getValue().entrySet()){
                t_sum+=si.getValue();
            }
            //计算一个D_IP的SIDP
            for (Map.Entry<String,Integer> si:dp.getValue().entrySet()){
                double num=(double)si.getValue()/t_sum;
                tt_sum+= num * log(num)/log(2);
                //System.out.println((double)t_entry.getValue()+"-"+(double)t_sum);
            }
            //System.out.println("计算后"+tt_sum);
            //放入hash表
            HashMap<String,String> temp =new HashMap<String,String>();
            temp.put("name","sidp");
            temp.put("d_port",dp.getKey());
            temp.put("sidp", String.valueOf(tt_sum));
            temp.put("t_sidp", String.valueOf(t_sum));
            detect_out.add(temp);
        }

        //发送
        collector.emit(new Values(new Gson().toJson(detect_out)));
        //System.out.println(new Gson().toJson(detect_out));
        detect_out.clear();
        sidp.clear();

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("count"));
    }

}
