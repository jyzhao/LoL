/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package final_project;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.concurrent.TimeUnit;


/**
 *
 * @author zhaojiyuan
 */
public class MatchDataFetcher {

    private static String matchList = "https://na.api.pvp.net/api/lol/na/v2.2/match/";
    private static String api_key = "?api_key=60e01f47-cbf0-4fb0-a9a5-bf89aa642921";
    private static String prefix = "{\"matchId\"";

    public static void main(String[] args) throws Exception {

        PrintWriter writer = new PrintWriter("match-1m.json", "UTF-8");

        for (int i = 1807845889; i < (1807845889+1000000); i++) {
            String matchListQuery = matchList + String.valueOf(i) + api_key;
            URL matchListUrl = new URL(matchListQuery);

            try (BufferedReader matchListReader = new BufferedReader(new InputStreamReader(matchListUrl.openStream()))) {
                for (String matchListLine; (matchListLine = matchListReader.readLine()) != null;) {
                    if (matchListLine.startsWith(prefix) && matchListLine.contains("RANKED_SOLO_5x5")) {
                        System.out.println(matchListLine);
                        writer.println(matchListLine);
                        TimeUnit.MILLISECONDS.sleep(1205);
                    }
                }

            } catch (Exception e) {
                continue;
            }

        }

        writer.close();

    }
}
