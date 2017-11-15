import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.windowing.TupleWindow;

import java.util.Map;

/**
 * Created by whatseven on 2017/5/22.
 */
public class SlidingWindowBolt extends BaseWindowedBolt {

   private OutputCollector collector;

    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
       this.collector = collector;
    }
    public void execute(TupleWindow inputWindow) {
        int sum=0;
        System.out.print("一个窗口内的数据");
       for(Tuple tuple: inputWindow.get()) {
           int str=(Integer) tuple.getValueByField("intsmaze");
         System.out.print(" "+str);
           sum+=str;
    }
        System.out.println("======="+sum);
 //        collector.emit(new Values(sum));
   }

  public void declareOutputFields(OutputFieldsDeclarer declarer) {
       declarer.declare(new Fields("count"));
  }
}