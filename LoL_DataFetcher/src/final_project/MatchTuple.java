/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package final_project;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;

/**
 *
 * @author zhaojiyuan
 */
public class MatchTuple implements WritableComparable<MatchTuple> {

    private String role;
    private String season;
    private String platformId;
    private String region;
    private int matchId;
    private int champion;
    private String queue;
    private String lane;
    private int timestamp;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public int getChampion() {
        return champion;
    }

    public void setChampion(int champion) {
        this.champion = champion;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
    
    
    

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeChars(role);
        out.writeChars(season);
        out.writeChars(platformId);
        out.writeChars(region);
        out.writeInt(matchId);
        out.writeInt(champion);
        out.writeChars(queue);
        out.writeChars(lane);
        out.writeInt(timestamp);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        role = in.readLine();
        season = in.readLine();
        platformId = in.readLine();
        region = in.readLine();
        matchId = in.readInt();
        champion = in.readInt();
        queue = in.readLine();
        lane = in.readLine();
        timestamp = in.readInt();
    }

    
    @Override
    public int compareTo(MatchTuple o) {
        return (int) (this.getTimestamp()- o.getTimestamp());
    }
    

    @Override
    public String toString() {
        return "MatchTuple{" + "role=" + role + ", season=" + season + ", platformId=" + platformId + ", region=" + region + ", matchId=" + matchId + ", champion=" + champion + ", queue=" + queue + ", lane=" + lane + ", timestamp=" + timestamp + '}';
    }

   

}
