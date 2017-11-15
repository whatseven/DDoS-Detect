import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.windowing.TupleWindow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

/**
 * Created by whatseven on 2017/5/22.
 */
public class StaticsBolt extends BaseWindowedBolt {

    private OutputCollector collector;
    private HashMap<String, HashMap<String, Double>> out;

    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    public void execute(TupleWindow inputWindow) {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://192.168.135.1:3306/ddos?useSSL=false";
        String user = "root";
        String password = "root";
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, user, password);
            // statement用来执行SQL语句
            Statement statement = conn.createStatement();
            // 要执行的SQL语句
            PreparedStatement psql=null;

            Gson gson = new Gson();
            double sizeTotal = 0, numTotal = 0;
            HashMap<String, Double> sizeRank = new HashMap<String, Double>();
            HashMap<String, Double> numRank = new HashMap<String, Double>();
            HashMap<String, Double> protRank = new HashMap<String, Double>();

            for (Tuple tuple : inputWindow.get()) {
                TypeToken<HashMap<String, HashMap<String, Double>>> t = new TypeToken<HashMap<String, HashMap<String, Double>>>() {
                };
                out = gson.fromJson((String) tuple.getValueByField("statics"), t.getType());

                Iterator iter = out.get("size").entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    //calculate the sum
                    if (entry.getKey().equals("total"))
                        sizeTotal += (Double) entry.getValue();
                    else {
                        String dip = (String) entry.getKey();
                        double num = (Double) entry.getValue();
                        if (null == sizeRank.get(dip))
                            sizeRank.put(dip, 0.0);
                        else
                            sizeRank.put(dip, sizeRank.get(dip) + num);
                    }
                }

                iter = out.get("num").entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    //calculate the sum
                    if (entry.getKey().equals("total"))
                        numTotal += (Double) entry.getValue();
                        //calculate the rank
                    else {
                        String dip = (String) entry.getKey();
                        double num = (Double) entry.getValue();
                        if (null == numRank.get(dip))
                            numRank.put(dip, 0.0);
                        else
                            numRank.put(dip, numRank.get(dip) + num);
                    }
                }

                iter = out.get("prot").entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String prot = (String) entry.getKey();
                    double num = (Double) entry.getValue();
                    if (null == protRank.get(prot))
                        protRank.put(prot, 0.0);
                    else
                        protRank.put(prot, protRank.get(prot) + num);
                }
            }

            //Calculate the rank of size and store
            List<Map.Entry<String, Double>> listTemp1 = new ArrayList<Map.Entry<String, Double>>();
            listTemp1.addAll(sizeRank.entrySet());
            Collections.sort(listTemp1,new Comparator<Map.Entry<String, Double>>(){
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    return (int)(o2.getValue()-o1.getValue());
                }
                //逆序（从大到小）排列，正序为“return o1.getValue()-o2.getValue”
            });
            //psql=conn.prepareStatement("INSERT into statics(sizeSum,numSum,firstSizeIP,firstSizeNum,secondSizeIP,secondSizeNum,thirdSizeIP,thirdSizeNum,firstProt,firstProtNum,secondProt,secondProtNum,thirdProt,thirdProtNum,firstNumIP,firstNumNum,secondNumIP,secondNumNum,thirdNumIP,thirdNumNum) VALUE (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            psql=conn.prepareStatement("UPDATE statics set sizeSum=?,numSum=?,firstSizeIP=?,firstSizeNum=?,secondSizeIP=?,secondSizeNum=?,thirdSizeIP=?,thirdSizeNum=?,firstProt=?,firstProtNum=?,secondProt=?,secondProtNum=?,thirdProt=?,thirdProtNum=?,firstNumIP=?,firstNumNum=?,secondNumIP=?,secondNumNum=?,thirdNumIP=?,thirdNumNum=? WHERE id=550");
            psql.setString(1, String.valueOf(sizeTotal) );
            //System.out.println("size:"+sizeTotal);
            psql.setString(2, String.valueOf(numTotal));
            //System.out.println("num:"+numTotal);
            int iter=listTemp1.size()>3?3:listTemp1.size();
            for (int i=0;i<iter;i++){
                psql.setString(3+i*2, listTemp1.get(i).getKey());
                psql.setString(4+i*2, String.valueOf(listTemp1.get(i).getValue()));
            }
            for (int i=0;i<3-iter;i++){
                psql.setString(7-i*2, null);
                psql.setString(8-i*2, null);
            }

            //Calculate the rank of size and store
            List<Map.Entry<String, Double>> listTemp2 = new ArrayList<Map.Entry<String, Double>>();
            listTemp2.addAll(numRank.entrySet());
            Collections.sort(listTemp2,new Comparator<Map.Entry<String, Double>>(){
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    return (int)(o2.getValue()-o1.getValue());
                }
                //逆序（从大到小）排列，正序为“return o1.getValue()-o2.getValue”
            });
            iter=listTemp2.size()>3?3:listTemp2.size();
            for (int i=0;i<iter;i++){
                psql.setString(15+i*2, listTemp2.get(i).getKey());
                psql.setString(16+i*2, String.valueOf(listTemp2.get(i).getValue()));
            }
            for (int i=0;i<3-iter;i++){
                psql.setString(19-i*2, null);
                psql.setString(20-i*2, null);
            }

            //prot
            List<Map.Entry<String, Double>> listTemp3 = new ArrayList<Map.Entry<String, Double>>();
            listTemp3.addAll(protRank.entrySet());
            Collections.sort(listTemp3,new Comparator<Map.Entry<String, Double>>(){
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    return (int)(o2.getValue()-o1.getValue());
                }
                //逆序（从大到小）排列，正序为“return o1.getValue()-o2.getValue”
            });
            iter=listTemp3.size()>3?3:listTemp3.size();
            for (int i=0;i<iter;i++){
                psql.setString(9+i*2, listTemp3.get(i).getKey());
                psql.setString(10+i*2, String.valueOf(listTemp3.get(i).getValue()));
            }
            for (int i=0;i<3-iter;i++){
                psql.setString(13-i*2, null);
                psql.setString(14-i*2, null);
            }
            //System.out.println(psql);
            psql.executeUpdate();
    //        System.out.println("TotalSize-"+sizeTotal);
    //        System.out.println("TotalNum-"+numTotal);
    //        System.out.println("Prot-"+listTemp.get(0).getKey()+"-"+listTemp.get(0).getValue());

            conn.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
