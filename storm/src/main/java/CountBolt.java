import com.google.gson.Gson;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;

import java.util.*;

public class CountBolt extends BaseWindowedBolt {

    private OutputCollector collector;
    private HashMap<String,HashMap<String,Double>> out;


    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("statics"));
    }

    public void execute(TupleWindow inputWindow) {
        //init
        out=new HashMap<String,HashMap<String,Double>>();
        out.put("size",new HashMap<String, Double>());
        out.get("size").put("total",0.0);
        out.put("num",new HashMap<String, Double>());
        out.get("num").put("total",0.0);
        out.put("prot",new HashMap<String, Double>());

        List<Map.Entry<String, Double>> listTemp = new ArrayList<Map.Entry<String, Double>>();
        HashMap<String,Double> sizeSort=new HashMap<String, Double>();
        HashMap<String,Double> numSort=new HashMap<String, Double>();
        for(Tuple tuple: inputWindow.get()){
            double size=(Double) tuple.getValueByField("pktSize");
            double num=(Double) tuple.getValueByField("pktNum");
            String protocal=((Double) tuple.getValueByField("prot")).toString();
            if(null==out.get("prot").get(protocal))
                out.get("prot").put(protocal,1.0);
            else
                out.get("prot").put(protocal,out.get("prot").get(protocal)+1);
            out.get("num").put("total",out.get("num").get("total")+num);
            out.get("size").put("total",out.get("num").get("total")+size);

            if (null == sizeSort.get((String) tuple.getValueByField("s_ip")))
                sizeSort.put((String) tuple.getValueByField("s_ip"),1.0);
            else
                sizeSort.put((String) tuple.getValueByField("s_ip"),sizeSort.get((String) tuple.getValueByField("s_ip"))+size);

            if (null == numSort.get((String) tuple.getValueByField("s_ip")))
                numSort.put((String) tuple.getValueByField("s_ip"),1.0);
            else
                numSort.put((String) tuple.getValueByField("s_ip"),numSort.get((String) tuple.getValueByField("s_ip"))+num);

        }
        //将map中的元素放入list中
        listTemp.addAll(sizeSort.entrySet());
        Collections.sort(listTemp,new Comparator<Map.Entry<String, Double>>(){
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (int)(o2.getValue()-o1.getValue());
            }
            //逆序（从大到小）排列，正序为“return o1.getValue()-o2.getValue”
        });
        for (int i=0;i<3&&i<listTemp.size();i++){
            out.get("size").put(listTemp.get(i).getKey(),listTemp.get(i).getValue());
        }

        List<Map.Entry<String, Double>> listTemp1 = new ArrayList<Map.Entry<String, Double>>();
        //将map中的元素放入list中
        listTemp1.addAll(numSort.entrySet());
        Collections.sort(listTemp1,new Comparator<Map.Entry<String, Double>>(){
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (int)(o2.getValue()-o1.getValue());
            }
            //逆序（从大到小）排列，正序为“return o1.getValue()-o2.getValue”
        });
        for (int i=0;i<3&&i<listTemp1.size();i++){
            out.get("num").put(listTemp1.get(i).getKey(),listTemp1.get(i).getValue());
        }

        String outStr=new Gson().toJson(out);
        collector.emit(new Values(outStr));
        //System.out.println(outStr);
        out.clear();
    }
}
