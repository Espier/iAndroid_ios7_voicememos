package org.espier.voicememos7.ui;


public class AudioData {
    
    long time;
    int amplitude;
    int db;
    /**
     * @return the db
     */
    public int getDb() {
        return db;
    }
    /**
     * @param db the db to set
     */
    public void setDb(int db) {
        this.db = db;
    }
    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }
    /**
     * @return the amplitude
     */
    public int getAmplitude() {
        return amplitude;
    }
    /**
     * @param amplitude the amplitude to set
     */
    public void setAmplitude(int amplitude) {
        this.amplitude = amplitude;
    }
    

}
