import java.util.ArrayList;

public class LogicalShot {

    private int shotId;
    private int startFrameId;
    private int endFrameId;
    private double score;
    private int duration;
    private int framesToKeep; 
    private ArrayList<Frame> frameList;
    private double audioScore = 0;

    public LogicalShot(int shotId, ArrayList<Frame> frameList) {
        this.frameList = frameList;
        this.shotId = shotId;
        this.startFrameId = frameList.get(0).getFrameId();
        this.endFrameId = frameList.get(frameList.size() - 1).getFrameId();
        this.duration = endFrameId - startFrameId;
        this.framesToKeep = 0;
    }
    
    public void setAudioScore(double audioWeight) {
        this.audioScore = audioWeight;
    }
    
    public double getAudioScore() {
        return this.audioScore;
    }
    
    public void calculateTotalScore() {
        this.score = calculateScore();
    }

    public int getDuraion() {
        return this.duration;
    }

    public int getShotId() {
        return this.shotId;
    }

    public double getScore() {
        return this.score;
    }

    public int getStartFrameId() {
        return this.startFrameId;
    }

    public int getFramesToKeep() {
        return this.framesToKeep;
    }

    public void setFramesToKeep(int newVal) {
        this.framesToKeep = newVal;
    }

    private double calculateScore() {
        double totalVariance = 0;
        double maxVariance = Integer.MIN_VALUE;
        double totalBrightness = 0;
        for (Frame frame: frameList) {
            maxVariance = Math.max(maxVariance, frame.getColorVariance());
            totalVariance += frame.getColorVariance();
            totalBrightness += frame.getColorBrightnessPercent();
        }
        double varianceScore = totalVariance / (maxVariance * frameList.size());
        double brightnessScore = totalBrightness / frameList.size();
        double score = varianceScore * 0.3 + brightnessScore * 0.3 + audioScore * 0.3;
        return score;
    }
}
