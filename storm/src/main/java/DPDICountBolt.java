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
public class DPDICountBolt extends BaseWindowedBolt {

    private OutputCollector collector;
    HashMap<String,HashMap<String,Integer>> dpdi;
    CopyOnWriteArrayList<HashMap<String,String>> detect_out;

    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;
        dpdi=new HashMap<String,HashMap<String,Integer>>();
        detect_out=new CopyOnWriteArrayList<HashMap<String,String>>();

    }

    public void execute(TupleWindow inputWindow) {
        int sum=0;
        for(Tuple tuple: inputWindow.get()) {
            String d_ip=(String) tuple.getValueByField("d_ip");
            String d_port=((Double) tuple.getValueByField("d_port")).toString() ;

            //统计DPDI
            if (dpdi.get(d_ip)==null)
                dpdi.put(d_ip,new HashMap<String, Integer>());
            if(dpdi.get(d_ip).get(d_port)==null)
                dpdi.get(d_ip).put(d_port,0);
            dpdi.get(d_ip).put(d_port,dpdi.get(d_ip).get(d_port)+1);
        }
        //计算SIDI
        for (Map.Entry<String,HashMap<String,Integer>> di:dpdi.entrySet()){
            int t_sum=0;//一个D_IP的流总数
            double tt_sum=0;//一个D_IP的SIDI

            //计算一个D_IP的流总数
            for (Map.Entry<String,Integer> dp:di.getValue().entrySet()){
                t_sum+=dp.getValue();
            }
            //计算一个D_IP的DPDI
            for (Map.Entry<String,Integer> dp:di.getValue().entrySet()){
                double num=(double)dp.getValue()/t_sum;
                tt_sum+= num * log(num)/log(2);
            }
            //System.out.println("计算后"+t_dpdi);
            //放入hash表
            HashMap<String,String> temp =new HashMap<String,String>();
            temp.put("name","dpdi");
            temp.put("d_ip",di.getKey());
            temp.put("dpdi", String.valueOf(tt_sum));
            temp.put("t_dpdi", String.valueOf(t_sum));
            detect_out.add(temp);
        }
        //发送
        collector.emit(new Values(new Gson().toJson(detect_out)));
        //System.out.println(new Gson().toJson(detect_out));
        detect_out.clear();
        dpdi.clear();

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("count"));
    }

}
