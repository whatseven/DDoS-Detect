import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.util.Map;

/**
 * Created by whatseven on 2017/5/22.
 */
public class TestBolt extends BaseRichBolt {

   private OutputCollector collector;

    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
       this.collector = collector;
    }

    public void execute(Tuple input) {
        System.out.println("========");

        String str= (String) input.getValueByField("msg");
        System.out.println(str);

        System.out.println("=======");
    }

  public void declareOutputFields(OutputFieldsDeclarer declarer) {
        //declarer.declare(new Fields("count"));
  }
}