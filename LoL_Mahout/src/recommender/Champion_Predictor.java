/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recommender;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.*;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author zhaojiyuan
 */
public class Champion_Predictor {

    private static String championInfo = "https://global.api.pvp.net/api/lol/static-data/na/v1.2/champion/";
    private static String api_key = "?api_key=60e01f47-cbf0-4fb0-a9a5-bf89aa642921";

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.apache.mahout.cf.taste.common.TasteException
     */
    public static void main(String[] args) throws Exception {

        PrintWriter writer = new PrintWriter("mahout_output.csv", "UTF-8");

        // TODO code application logic here
        //step 1. create the model
        DataModel model = new FileDataModel(new File("mahout_input.csv"));

        //step 2.find users with similar tastes
        UserSimilarity similarity = new TanimotoCoefficientSimilarity(model);

        //step 3.find user neignborhood
        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);

        //step 4.create the recommender engine
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        String summonerName1;
        String summonerName2;
        //step 5.ask the recommender to give recommendations
        for (int i = 1; i < 433; i++) {
            try {
                List<RecommendedItem> recommendations = recommender.recommend(i, 5, true);
                for (RecommendedItem recommendation : recommendations) {

                    String championRequestString = championInfo + String.valueOf(i) + api_key;
                    URL championRequestUrl = new URL(championRequestString);

                    String championRequestString1 = championInfo + String.valueOf(recommendation.getItemID()) + api_key;
                    URL championRequestUrl1 = new URL(championRequestString1);

                    try (BufferedReader in = new BufferedReader(
                            new InputStreamReader(championRequestUrl.openStream()))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            JSONObject championJSONObject = (JSONObject) new JSONParser().parse(inputLine);

                            try (BufferedReader in1 = new BufferedReader(
                                    new InputStreamReader(championRequestUrl1.openStream()))) {
                                String inputLine1;
                                while ((inputLine1 = in1.readLine()) != null) {
                                    JSONObject championJSONObject1 = (JSONObject) new JSONParser().parse(inputLine1);

                                    System.out.println("Champion " + championJSONObject.get("name") + " might work well with Champion: " + championJSONObject1.get("name") + " (predicted win rate: " + String.format("%.4f", recommendation.getValue()) + ")");
                                    writer.println(championJSONObject.get("name") + "," + championJSONObject1.get("name") + "," + String.format("%.4f", recommendation.getValue()));

                                }
                            }

                            

                        }
                    }
                }
            } catch (Exception e) {
            }

        }
        writer.close();
    }

}
