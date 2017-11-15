import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.Map;

public class RandomSentenceSpout extends BaseRichSpout {

    private static final long serialVersionUID = 5028304756439810609L;

    private SpoutOutputCollector collector;

    int intsmaze = 0;

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("s_ip", "d_ip", "d_port", "time", "pktNum", "prot", "pktSize"));
    }

    public void open(Map conf, TopologyContext context,
                     SpoutOutputCollector collector) {
        this.collector = collector;
    }

    public void nextTuple() {
        Values temp = new Values("1", "2", "1", "1", "1", "1", "1");
        //System.out.println("发送数据:" + temp);
        collector.emit(temp);
        temp = new Values("3", "2", "1", "1", "1", "1", "1");
        collector.emit(temp);
        temp = new Values("2", "1", "1", "1", "1", "1", "1");
        collector.emit(temp);

        try {
            Thread.sleep(2000);
//         Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}