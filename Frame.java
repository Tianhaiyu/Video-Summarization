import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.math.*;
import java.util.*;
import java.util.Arrays;
import java.util.Timer;
import javax.swing.*;

public class Frame {

    // constants
    private final int width = 320;
    private final int height = 180;
    private final int fps = 30;

    private int frameId;
    private double colorVariance;
    private double colorBrightnessPercent;

    public Frame(String pathToFrames, int frameId) {
        this.frameId = frameId;
        byte[] pixelData = readFrame(pathToFrames, frameId);
        double numBrightBlock = 0;
        double totalAvgDiff = 0;
        int[][][] pixelDataSummary = getPixelDataSummary(convertTo3d(pixelData));
        for (int y = 0; y < height / 4 - 1; y++) {
            for (int x = 0; x < width / 4 - 1; x++) {
                if (pixelDataSummary[0][y][x] >= 128 || pixelDataSummary[1][y][x] >= 128
                        || pixelDataSummary[2][y][x] >= 128)
                    numBrightBlock++;
                double avgDiffR = (Math.pow(pixelDataSummary[0][y][x] - pixelDataSummary[0][y + 1][x], 2)
                        + Math.pow(pixelDataSummary[0][y][x] - pixelDataSummary[0][y][x + 1], 2)
                        + Math.pow(pixelDataSummary[0][y][x] - pixelDataSummary[0][y + 1][x + 1], 2)) / 3;
                double avgDiffG = (Math.pow(pixelDataSummary[1][y][x] - pixelDataSummary[1][y + 1][x], 2)
                        + Math.pow(pixelDataSummary[1][y][x] - pixelDataSummary[1][y][x + 1], 2)
                        + Math.pow(pixelDataSummary[1][y][x] - pixelDataSummary[1][y + 1][x + 1], 2)) / 3;
                double avgDiffB = (Math.pow(pixelDataSummary[2][y][x] - pixelDataSummary[2][y + 1][x], 2)
                        + Math.pow(pixelDataSummary[2][y][x] - pixelDataSummary[2][y][x + 1], 2)
                        + Math.pow(pixelDataSummary[2][y][x] - pixelDataSummary[2][y + 1][x + 1], 2)) / 3;
                totalAvgDiff += Math.max(Math.max(avgDiffR, avgDiffG), avgDiffB);
            }
        }
        this.colorBrightnessPercent = numBrightBlock / (height * width / 16) * 100;
        this.colorVariance = totalAvgDiff / (height * width / 16);
    }

    public int getFrameId() {
        return this.frameId;
    }

    public double getColorVariance() {
        return this.colorVariance;
    }

    public double getColorBrightnessPercent() {
        return this.colorBrightnessPercent;
    }
    
    private byte[] readFrame(String pathToFrames, int frameId) {
        int frameLength = width * height * 3;
        byte[] bytes = new byte[frameLength];

        try {
            String imgPath = pathToFrames + "frame" + frameId + ".rgb";
            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);
            raf.read(bytes);
            raf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * Divide the original pixel array into 4x4 blocks, calculate avg pixel values
     * for each block in R, G, B channel respectively
     *
     * @return int array that contains the average value of each block
     */
    private int[][][] getPixelDataSummary(int[][][] pixelData) {
        int[][][] res = new int[3][height / 4][width / 4];
        for (int y = 0; y < height; y += 4) {
            for (int x = 0; x < width; x += 4) {
                int sumR = 0, sumG = 0, sumB = 0;
                for (int i = y; i < y + 4; i++) {
                    for (int j = x; j < x + 4; j++) {
                        sumR += pixelData[0][i][j];
                        sumG += pixelData[1][i][j];
                        sumB += pixelData[2][i][j];
                    }
                }
                res[0][y / 4][x / 4] = sumR / 16;
                res[1][y / 4][x / 4] = sumG / 16;
                res[2][y / 4][x / 4] = sumB / 16;
            }
        }
        return res;
    }

    private int[][][] convertTo3d(byte[] pixelData) {
        int[][][] res = new int[3][height][width];
        int ind = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // byte a = 0;
                byte r = pixelData[ind];
                byte g = pixelData[ind + height * width];
                byte b = pixelData[ind + height * width * 2];

                res[0][y][x] = r & 0xff;
                res[1][y][x] = g & 0xff;
                res[2][y][x] = b & 0xff;
                ind++;
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return "Frame " + frameId + ": variance " + colorVariance + " brightness " + colorBrightnessPercent;
    }
}
