import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;

import java.util.concurrent.TimeUnit;

public class WindowsTopology {

  public static void main(String[] args) throws Exception {
      ZkHosts zkHosts=new ZkHosts("192.168.135.138:2181");

      String topic = "testKafka-1";
      String zkRoot = "/"+topic;
      String spoutId = "myKafka";

      SpoutConfig spoutConfig = new SpoutConfig(zkHosts, topic, zkRoot, spoutId);
      //spoutConfig. = false;
      Config conf = new Config();
      spoutConfig.scheme = new SchemeAsMultiScheme(new TestMessageScheme());

      TopologyBuilder builder = new TopologyBuilder();
      builder.setSpout("msgKafkaSpout", new KafkaSpout(spoutConfig));
      //builder.setSpout("testSpout",new RandomSentenceSpout(),1);

      builder.setBolt("countBolt", new CountBolt()
              .withWindow(new BaseWindowedBolt.Duration(2, TimeUnit.SECONDS),new BaseWindowedBolt.Duration(2, TimeUnit.SECONDS)),2)
              .shuffleGrouping("msgKafkaSpout");

      builder.setBolt("sidiBolt", new SIDICountBolt()
              .withWindow(new BaseWindowedBolt.Duration(2, TimeUnit.SECONDS),new BaseWindowedBolt.Duration(2, TimeUnit.SECONDS)),1)
              .fieldsGrouping("msgKafkaSpout",new Fields("d_ip"));

      builder.setBolt("sidpBolt", new SIDPCountBolt()
              .withWindow(new BaseWindowedBolt.Duration(2, TimeUnit.SECONDS),new BaseWindowedBolt.Duration(2, TimeUnit.SECONDS)),1)
              .fieldsGrouping("msgKafkaSpout",new Fields("d_port"));

      builder.setBolt("dpdiBolt", new DPDICountBolt()
              .withWindow(new BaseWindowedBolt.Duration(2, TimeUnit.SECONDS),new BaseWindowedBolt.Duration(2, TimeUnit.SECONDS)),1)
              .fieldsGrouping("msgKafkaSpout",new Fields("d_ip"));

      builder.setBolt("StaticsBolt", new StaticsBolt()
              .withWindow(new BaseWindowedBolt.Duration(2,TimeUnit.SECONDS),new BaseWindowedBolt.Duration(2, TimeUnit.SECONDS)),1)
              .globalGrouping("countBolt");


      builder.setBolt("DetectBolt", new DetectBolt()
              .withWindow(new BaseWindowedBolt.Duration(2,TimeUnit.SECONDS),new BaseWindowedBolt.Duration(2, TimeUnit.SECONDS)),1)
              .globalGrouping("sidiBolt").globalGrouping("sidpBolt").globalGrouping("dpdiBolt");

      if (args.length == 0) {
          String topologyName = "localKafkaTopicTopology";
          LocalCluster cluster = new LocalCluster();
          cluster.submitTopology(topologyName, conf, builder.createTopology());
          Utils.sleep(1000000);
          cluster.killTopology(topologyName);
          cluster.shutdown();
      } else {
          conf.setNumWorkers(1);
          StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
          Utils.sleep(100000);
      }
  }
}