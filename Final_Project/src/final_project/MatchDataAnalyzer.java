/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package final_project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author zhaojiyuan
 */
public class MatchDataAnalyzer extends Configured implements Tool {

    private static String matchList = "https://na.api.pvp.net/api/lol/na/v2.2/matchlist/by-summoner/";
    private static String summoner = "https://na.api.pvp.net/api/lol/na/v1.4/summoner/";
    private static String api_key = "?api_key=60e01f47-cbf0-4fb0-a9a5-bf89aa642921";
    private static String prefix = "{\"matches\"";

    public static void main(String[] args) throws Exception {
        JSONObject matchListJSON;
        JSONObject summonerJSON;
        JSONObject matchJSON;
        int res = 0;
        PrintWriter writer = new PrintWriter("champion-result.txt", "UTF-8");

        for (int i = 2; i < 45480241; i++) {
            String matchListQuery = matchList + String.valueOf(i) + api_key;
            String summonerQuery = summoner + String.valueOf(i) + api_key;
            URL matchListUrl = new URL(matchListQuery);
            URL summonerUrl = new URL(summonerQuery);

            try (BufferedReader matchListReader = new BufferedReader(new InputStreamReader(matchListUrl.openStream()))) {
                for (String matchListLine; (matchListLine = matchListReader.readLine()) != null;) {
                    if (matchListLine.startsWith(prefix)) {
                        try (BufferedReader summonerReader = new BufferedReader(new InputStreamReader(summonerUrl.openStream()))) {
                            for (String summonerLine; (summonerLine = summonerReader.readLine()) != null;) {

                                summonerJSON = new JSONObject(summonerLine);
                                String summonerName = summonerJSON.getJSONObject(String.valueOf(i)).getString("name");
                                System.out.println("Summoner id: " + i + ": " + summonerName);

                            }
                        }

                        matchListJSON = new JSONObject(matchListLine);
                        JSONArray matches = matchListJSON.getJSONArray("matches");
                        for (int j = 0; j < matches.length(); j++) {
                            MatchTuple matchTuple = new MatchTuple();

                            matchJSON = (JSONObject) matches.get(j);

                            matchTuple.setRole(matchJSON.getString("role"));
                            matchTuple.setSeason(matchJSON.getString("season"));
                            matchTuple.setPlatformId(matchJSON.getString("platformId"));
                            matchTuple.setRegion(matchJSON.getString("region"));
                            matchTuple.setMatchId(matchJSON.getInt("matchId"));
                            matchTuple.setChampion(matchJSON.getInt("champion"));
                            matchTuple.setQueue(matchJSON.getString("queue"));
                            matchTuple.setLane(matchJSON.getString("lane"));
                            matchTuple.setTimestamp(matchJSON.getInt("timestamp"));

                            System.out.println(matchTuple);

                        }
                        res = ToolRunner.run(new Configuration(), new MatchDataAnalyzer(), args);
                        System.out.println("====================================");
                    }

                }
            } catch (IOException exception) {
                continue;
            }

        }

        System.exit(res);
    }

    public static class MatchMapper extends Mapper<IntWritable, IntWritable, IntWritable, IntWritable> {

        private final static IntWritable ONE = new IntWritable(1);

        @Override
        protected void map(IntWritable key, IntWritable value, Context context) throws IOException, InterruptedException {
            super.map(key, value, context); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public static class MatchReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {

        @Override
        protected void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            super.reduce(key, values, context); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public int run(String[] args) throws Exception {
        Configuration conf = getConf();
        Job job = new Job(conf, "MatchDataAnalyzer");
        job.setJarByClass(MatchDataAnalyzer.class);
        final File f = new File(MatchDataAnalyzer.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        //String inFiles = f.getAbsolutePath().replace("/build/classes", "") + "/src/inFiles/ratings.dat";
        String outFiles = f.getAbsolutePath().replace("/build/classes", "") + "/src/outFiles/MatchStat";

        //Path in = new Path(inFiles);

        /*Creating Filesystem object with the configuration*/
        FileSystem fs = FileSystem.get(conf);
        /*Check if output path (args[1])exist or not*/
        if (fs.exists(new Path(outFiles))) {
            /*If exist delete the output path*/
            fs.delete(new Path(outFiles), true);
        }

        Path out = new Path(outFiles);
        //FileInputFormat.setInputPaths(job, in);
        FileOutputFormat.setOutputPath(job, out);
        job.setMapperClass(MatchMapper.class);
        //job.setCombinerClass(MovieLensReducer.class);
        job.setReducerClass(MatchReducer.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(MatchTuple.class);
        job.setOutputValueClass(DoubleWritable.class);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
        return 0;
    }
}
