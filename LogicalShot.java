import java.util.ArrayList;

public class LogicalShot {

    private int shotId;
    private int startFrameId;
    private int endFrameId;
    private int score;
    private int duration;
    private ArrayList<Frame> frameList;

    public LogicalShot(int shotId, ArrayList<Frame> frameList) {
        this.frameList = frameList;
        this.shotId = shotId;
        this.startFrameId = frameList.get(0).getFrameId();
        this.endFrameId = frameList.get(frameList.size() - 1).getFrameId();
        this.duration = endFrameId - startFrameId;
        this.score = calculateScore();
    }

    public int getDuraion() {
        return this.duration;
    }

    public int getShotId() {
        return this.shotId;
    }

    public int getScore() {
        return this.score;
    }

    private int calculateScore() {
        int score = 0;
        return score;
    }
}
