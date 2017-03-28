/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package final_project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import static org.apache.hadoop.hbase.util.Bytes.toBytes;
import org.apache.hadoop.mapreduce.Mapper;

/**
 *
 * @author zhaojiyuan
 */
public class LoL_Predictor {

    private static String championInfo = "https://global.api.pvp.net/api/lol/static-data/na/v1.2/champion/";
    private static String summoner = "https://na.api.pvp.net/api/lol/na/v1.4/summoner/";
    private static String api_key = "?api_key=60e01f47-cbf0-4fb0-a9a5-bf89aa642921";
    private static String tableName = "champion";
    
    public static ArrayList<Match> matchArrayList = new ArrayList<>();

    
    public static void main(String[] args) throws MalformedURLException, FileNotFoundException, IOException {
        importData();
        Mahout();
        //String championSerialNumber = "1111";
        
        //Result championRecord = retrieveData(championSerialNumber);
        //byte[] championId = championRecord.getValue(Bytes.toBytes("championId"), Bytes.toBytes("Id"));
        //byte[] championKills = championRecord.getValue(Bytes.toBytes("championInfo"), Bytes.toBytes("kills"));
        //System.out.println(Bytes.toString(championId) + " " + Bytes.toString(championKills));
    }
    
    public static void Mahout() throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter("mahout_input.txt", "UTF-8");
        int i = 1;
        matchArrayList.stream().forEach((next) -> {
            for (Match match1 : matchArrayList) {
                if (next.matchId == match1.matchId && next.teamId == match1.teamId && next.championId != match1.championId) {
                    //System.out.println(next.championId + "," + match1.championId + "," + next.result);
                    writer.println(next.championId + "," + match1.championId + "," + next.result);
                }
            }
        });
        writer.close();
    }

    public static void importData() throws IOException {
        /*
        Configuration conf = HBaseConfiguration.create();
        HBaseAdmin admin = new HBaseAdmin(conf);

        //creating table descriptor
        HTableDescriptor table = new HTableDescriptor(toBytes(tableName));

        //creating column family descriptor
        HColumnDescriptor championInfoFamily = new HColumnDescriptor(toBytes("championInfo"));
        HColumnDescriptor championIdFamily = new HColumnDescriptor(toBytes("championId"));

        //adding coloumn family to HTable
        table.addFamily(championInfoFamily);
        table.addFamily(championIdFamily);

        if (admin.tableExists(tableName)) {
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
        }

        admin.createTable(table);
        // Instantiating HTable class
        HTable hTable = new HTable(conf, tableName);
        System.out.println("Table created: " + hTable.getName());
        */
        JSONParser parser = new JSONParser();
        ArrayList<MatchJSON> matchJsonArrayList = new ArrayList<>();
        
        //PrintWriter writer = new PrintWriter("champion-result.txt", "UTF-8");
        JSONObject team1;
        JSONObject team2;
        PrintWriter writer = new PrintWriter("champion_mahout_input.txt", "UTF-8");

        
        File dir = new File("input");
        File[] files = dir.listFiles((File dir1, String name) -> name.endsWith(".json"));
        int j = 0;

        for (File file : files) {

            System.out.println("Reading " + file.getName() + " ......");
            try {
                Object object = parser.parse(new FileReader(file));
                JSONObject matchesJsonObject = (JSONObject) object;
                JSONArray matchJsonArray = (JSONArray) matchesJsonObject.get("matches");
                MatchJSON matchJSON = new MatchJSON();
                int i = 1;

                for (Object o : matchJsonArray) {
                    JSONObject matchJSONObject = (JSONObject) o;

                    matchJSON.setMatchId((long) matchJSONObject.get("matchId"));

                    matchJSON.setRegion((String) matchJSONObject.get("region"));

                    matchJSON.setPlatformId((String) matchJSONObject.get("platformId"));
                    matchJSON.setMatchMode((String) matchJSONObject.get("matchMode"));
                    matchJSON.setMatchType((String) matchJSONObject.get("matchType"));
                    matchJSON.setMatchCreation((long) matchJSONObject.get("matchCreation"));
                    matchJSON.setMatchDuration(Integer.parseInt(matchJSONObject.get("matchDuration").toString()));
                    matchJSON.setQueueType((String) matchJSONObject.get("queueType"));
                    matchJSON.setMapId(Integer.parseInt(matchJSONObject.get("mapId").toString()));
                    matchJSON.setSeason((String) matchJSONObject.get("season"));
                    matchJSON.setMatchVersion((String) matchJSONObject.get("matchVersion"));
                    matchJSON.setParticipants((JSONArray) matchJSONObject.get("participants"));
                    matchJSON.setParticipantIdentities((JSONArray) matchJSONObject.get("participantIdentities"));
                    matchJSON.setTeams((JSONArray) matchJSONObject.get("teams"));
                    matchJSON.setTimeline((JSONObject) matchJSONObject.get("timeline"));

                    matchJsonArrayList.add(matchJSON);
                    //System.out.println("Match " + i++ + " ID: " + matchJSON.getMatchId());
                    team1 = (JSONObject) matchJSON.getTeams().get(0);
                    team2 = (JSONObject) matchJSON.getTeams().get(1);
                    for (Object object1 : matchJSON.getParticipants()) {
                        JSONObject participant = (JSONObject) object1;
                        JSONObject stats = (JSONObject) participant.get("stats");
                        int result = ((boolean) stats.get("winner")) ? 1 : 0;
                        j++;
                        /*
                        // Instantiating Put class
                        // accepts a row name.

                        Put p = new Put(Bytes.toBytes(String.valueOf(j)));
                        
                        // adding values using add() method
                        // accepts column family name, qualifier/row name ,value
                        p.add(Bytes.toBytes("championId"),
                                Bytes.toBytes("Id"), Bytes.toBytes(participant.get("championId").toString()));

                        p.add(Bytes.toBytes("championInfo"),
                                Bytes.toBytes("kills"), Bytes.toBytes(stats.get("kills").toString()));

                        p.add(Bytes.toBytes("championInfo"),
                                Bytes.toBytes("deaths"), Bytes.toBytes(stats.get("deaths").toString()));

                        p.add(Bytes.toBytes("championInfo"),
                                Bytes.toBytes("assits"), Bytes.toBytes(stats.get("assists").toString()));

                        p.add(Bytes.toBytes("championInfo"),
                                Bytes.toBytes("result"), Bytes.toBytes(result));

                        // Saving the put Instance to the HTable.
                        hTable.put(p);
                        */
                        //writer.println(matchJSON.getMatchId() + "" + participant.get("teamId") + "," + participant.get("championId") + "," + result);
                        //System.out.println(matchJSON.getMatchId() + "," + participant.get("teamId") + "," + participant.get("championId") + "," + result);
                        Match match = new Match();

                        match.setMatchId(matchJSON.getMatchId());
                        match.setResult(result);
                        match.setChampionId(Integer.valueOf(participant.get("championId").toString()));
                        match.setTeamId(Integer.valueOf(participant.get("teamId").toString()));
                        //System.out.println(match);
                        matchArrayList.add(match);
                        
                        //System.out.println(match.getChampionId());
                        
                        
                        /*
                         String championRequestString = championInfo + String.valueOf(participant.get("championId")) + api_key;
                         URL championRequestUrl = new URL(championRequestString);
                         try (BufferedReader in = new BufferedReader(
                         new InputStreamReader(championRequestUrl.openStream()))) {
                         String inputLine;
                         while ((inputLine = in.readLine()) != null) {
                         //System.out.println(inputLine);
                         JSONObject championJSONObject = (JSONObject) new JSONParser().parse(inputLine);
                        
                         //System.out.println(result + ": " + championJSONObject.get("name"));
                         //champion name, kills, deaths, assists, minions killed, victory/defeated
                         //writer.println(championJSONObject.get("name") + "," + stats.get("kills") + "," + stats.get("deaths") + "," + stats.get("assists") + "," + result);
                        
                         }
                         }
                         */
                    }
                    //System.out.println("Team 1: First Dragon: " + team1.get("firstDragon") + "; First Baron: " + team1.get("firstBaron") + "; Winner: " + team1.get("winner"));
                    //System.out.println("Team 2: First Dragon: " + team2.get("firstDragon") + "; First Baron: " + team2.get("firstBaron") + "; Winner: " + team2.get("winner"));
                }

            } catch (Exception e) {
            }
            writer.close();
        }
        
        //System.out.println("Total memory used: " + (float) Runtime.getRuntime().totalMemory() / 1024 / 1024 / 1024 + "GB");
        //writer.close();
        //hTable.close();
    }
    
    public static Result retrieveData(String championSerialNumber) throws IOException{
        Configuration conf = HBaseConfiguration.create();
        HTable hTable = new HTable(conf, tableName);
        Get get = new Get(toBytes(championSerialNumber));

        Result result = hTable.get(get);
        
        return result;
    }
    
    //public static class LoLMapper extends Mapper<Object, Object, Object, Object>
}

