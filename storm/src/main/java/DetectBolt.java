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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by whatseven on 2017/5/22.
 */
public class DetectBolt extends BaseWindowedBolt {

    private OutputCollector collector;
    CopyOnWriteArrayList<HashMap<String,String>> in;
    ArrayList<HashMap<String,String>> sidi;
    ArrayList<HashMap<String,String>> sidp;
    ArrayList<HashMap<String,String>> dpdi;

    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;
        sidi=new ArrayList<HashMap<String,String>>();
        sidp=new ArrayList<HashMap<String,String>>();
        dpdi=new ArrayList<HashMap<String,String>>();

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

            List<Tuple> tuplesInWindow = inputWindow.get();
            for(Tuple tuple: tuplesInWindow) {
                    //in = (CopyOnWriteArrayList<HashMap<String, String>>) tuple.getValueByField("count");
                    Gson gson=new Gson();
                    TypeToken<CopyOnWriteArrayList<HashMap<String, String>>> t= new TypeToken<CopyOnWriteArrayList<HashMap<String, String>>>(){};
                    in=gson.fromJson((String) tuple.getValueByField("count"), t.getType());
                    //System.out.println(in);
                    if (null!=in&&!in.isEmpty()) {
                        for(HashMap<String,String> map : in) {
                            //System.out.println("From detect"+map);
                            //HashMap<String, String> map = in.get(i);
                            if (map!=null&&!map.isEmpty()) {
                                //System.out.println(map);
                                String fieldName=map.get("name");
                                //System.out.println(fieldName);
                                if (fieldName!=null&&fieldName.equals("sidi")) {
                                    HashMap<String, String> temp=new HashMap<String, String>();
                                    temp.put("t_sidi",map.get("t_sidi"));
                                    temp.put("sidi",map.get("sidi"));
                                    sidi.add(temp);
                                    //System.out.println("11111-"+map.get("t_sidi")+"-"+map.get("sidi"));
                                }else if (fieldName!=null&&fieldName.equals("sidp")) {
                                    HashMap<String, String> temp=new HashMap<String, String>();
                                    temp.put("t_sidp",map.get("t_sidp"));
                                    //System.out.println("1-"+map.get("t_sidp"));
                                    temp.put("sidp",map.get("sidp"));
                                    //System.out.println("2-"+map.get("sidp"));
                                    sidp.add(temp);
                                }else if (fieldName!=null&&fieldName.equals("dpdi")) {
                                    HashMap<String, String> temp=new HashMap<String, String>();
                                    temp.put("t_dpdi",map.get("t_dpdi"));
                                    temp.put("dpdi",map.get("dpdi"));
                                    dpdi.add(temp);
                                }
                                //t_sum+=Integer.parseInt(map.get("t_sidi"));
                            }
                        }
                    }
                    //in.clear();
            }
            double s_sidi=0,s_t_sidi=0,s_dpdi=0,s_t_dpdi=0,s_sidp=0,s_t_sidp=0;
            for (int i = 0; i <sidi.size(); i++) {
                HashMap<String, String> temp=sidi.get(i);
                //if (!temp.isEmpty())
                if(temp.get("t_sidi")!=null)
                    s_t_sidi+= Double.parseDouble(temp.get("t_sidi"));
            }
            for (int i = 0; i <sidp.size(); i++) {
                HashMap<String, String>temp=sidp.get(i);
                if(temp.get("t_sidp")!=null)
                    s_t_sidp+= Double.parseDouble(temp.get("t_sidp"));
            }
            for (int i = 0; i <dpdi.size(); i++) {
                HashMap<String, String>temp=dpdi.get(i);
                if(temp.get("t_dpdi")!=null)
                    s_t_dpdi+= Double.parseDouble(temp.get("t_dpdi"));
            }
            //计算
            for (int i = 0; i <sidi.size(); i++) {
                HashMap<String, String>temp=sidi.get(i);
                if(temp.get("t_sidi")!=null)
                    s_sidi+= Double.parseDouble(temp.get("t_sidi"))/s_t_sidi* Double.parseDouble(temp.get("sidi"));
            }
            for (int i = 0; i <sidp.size(); i++) {
                HashMap<String, String>temp=sidp.get(i);
                if(temp.get("t_sidp")!=null)
                    s_sidp+= Double.parseDouble(temp.get("t_sidp"))/s_t_sidp* Double.parseDouble(temp.get("sidp"));
            }
            for (int i = 0; i <dpdi.size(); i++) {
                HashMap<String, String>temp=dpdi.get(i);
                if(temp.get("t_dpdi")!=null)
                    s_dpdi+= Double.parseDouble(temp.get("t_dpdi"))/s_t_dpdi* Double.parseDouble(temp.get("dpdi"));
            }
            //System.out.println(s_sidi+"-"+s_sidp+"-"+s_dpdi);
            sidi.clear();
            sidp.clear();
            dpdi.clear();
            //System.out.println(t_sum);
            //psql=conn.prepareStatement("INSERT into detect(sidi,sidp,dpdi) VALUE (?,?,?)");
            psql=conn.prepareStatement("UPDATE detect set sidi=?,sidp=?,dpdi=?  WHERE id=2080");
            psql.setString(1, String.valueOf(s_sidi) );
            psql.setString(2, String.valueOf(s_sidp) );
            psql.setString(3, String.valueOf(s_dpdi) );
            psql.executeUpdate();
            Long timestamp = new Date().getTime();
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));
            System.out.println(date);
            conn.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }


}
