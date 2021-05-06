import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.math.*;
import java.util.*;
import java.util.Arrays;
import java.util.Timer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

public class AudioProcessor{
    final int width = 320;
    final int height = 180;
    final int fps = 30;

    ArrayList<byte[]> soundBlock;
    ArrayList<BufferedImage> FramesArray = new ArrayList<>();
    ArrayList<Integer> breaks  = new ArrayList<>();

    String audioFilePath;
    private double bytesPerVidFrame = 0;
    
    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128k
    ArrayList<Double> audioWs = new ArrayList<Double>(); // audio weights based on shots

    public AudioProcessor(String pathToWav, ArrayList<Integer> breaksIndex) throws UnsupportedAudioFileException, IOException, PlayWaveException{
        this.audioFilePath = pathToWav;
        this.breaks = breaksIndex;
        
        File soundFile = new File(this.audioFilePath);
        InputStream inputStream = new FileInputStream(soundFile);
        InputStream waveStream = inputStream;
        InputStream bufferedIn = new BufferedInputStream(waveStream);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
        AudioFormat audioFormat = audioInputStream.getFormat();
        
        double bytesPerSample = audioFormat.getFrameSize();
        double sampleRate = audioFormat.getFrameRate();
        bytesPerVidFrame = bytesPerSample*sampleRate/fps;
        //do the calculation on audio weights
        audioWs = calAudioWeights(breaks, audioInputStream);
    }

    public ArrayList<Double> getAudioWeights(){
        return audioWs;
    }

    //convert number of frames to number of bytes
    public long framesToBytes(double frames) {
        return (long)(frames*bytesPerVidFrame);
    }

    public ArrayList<Double> calAudioWeights(ArrayList<Integer> breaks, AudioInputStream audioInputStream) throws PlayWaveException{
        ArrayList<Double> audioWeights = new ArrayList<Double>();
        int readBytes = 0;
        byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
        long byteCount = 0;
        int index = 0;
        double weightTotal = 0;
        int weightCount = 0;
        double localMax = Double.NEGATIVE_INFINITY;
        
        //get the first break point between shots
        long breakPoint = framesToBytes(breaks.get(index++));
        try {

            // keep reading from audio input stream to the audio buffer array
            while(readBytes != -1) {
                readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);

            // More bytes to read
            if(readBytes >= 0) {

                for(int i=1;i<readBytes;i++) { 
                    //not getting to the next break point, keep summing up
                    if(byteCount<breakPoint) {
                        weightTotal+=audioBuffer[i]; 
                        weightCount++;
                    }           
                    else { 
                        //at the break point, add to the sum
                        audioWeights.add(weightTotal/weightCount);
                        //reset to calculate the next break point
                        weightTotal = 0;
                        weightTotal+=audioBuffer[i];
                        weightCount = 1;
                        if(index<breaks.size()) {
                            breakPoint=framesToBytes(breaks.get(index++));
                        }
                        else {
                        readBytes = -1;
                        }
                    }
                    byteCount++;
                }
            }
            }

            while(audioWeights.size()<breaks.size()) {
                audioWeights.add(weightTotal/weightCount);
            }

            // Normalize the audio weights
            for(int i=0;i<audioWeights.size();i++) {
                if(audioWeights.get(i)>localMax) {
                    localMax = audioWeights.get(i);
                }
            }
            //values are negatives, max is approaching to zero
            for(int i=0;i<audioWeights.size();i++) {
                double val = audioWeights.get(i);
                audioWeights.set(i, localMax/val);
            }
        }
        catch (IOException e1) {
            throw new PlayWaveException(e1);
        }
        return audioWeights;
    }

}