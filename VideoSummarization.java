import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.math.*;
import java.util.*;
import java.util.Arrays;
import java.util.Timer;
import javax.swing.*;

/**
 * plays a wave file using PlaySound class
 *
 * @author Giulio
 */
public class VideoSummarization {

  /**
   * <Replace this with one clearly defined responsibility this method does.>
   *
   * @param args the name of the wave file to play
   */
  int width = 320;
  int height = 180;
  int fps = 30;
  ArrayList<BufferedImage> FramesArray;
  JFrame frame;
  JLabel label;
  // params use for break shots
  private int threshold = 25;
  private int sec_buffer = 1;

  private int[][][] histogramPrev = new int[4][4][4];
  private int[][][] histogramNext = new int[4][4][4];
  ArrayList<Integer> shots = new ArrayList<Integer>();

  public static void main(String[] args) throws IOException {
    // Play the video
    if (args.length != 3) {
      System.err.println(
          "usage: java VideoSummarization [path to wav] [path to folder containing frames] [-flag] \n flag: -p(process), -o(play)");
      return;
    } else if (args[2].equals("-p")) {
      VideoProcessor vp = new VideoProcessor(args[0], args[1]);
    } else if (args[2].equals("-o")) {
      VideoSummarization vs = new VideoSummarization();

      vs.PlayInSync(args[0], args[1]);
    } else {
      System.err.println(
          "usage: java VideoSummarization [path to wav] [path to folder containing frames] [-flag] \n flag: -p(process), -o(play)");
      return;
    }
  }

  private static FileInputStream getWavInput(String pathToWav) {
    FileInputStream wavInput = null;
    try {
      wavInput = new FileInputStream(pathToWav);
      // inputStream = this.getClass().getResourceAsStream(filename);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return wavInput;
  }

  public void PlayInSync(String pathToWav, String pathToFrames) {
    // opens the inputStream
    FileInputStream wavInput = getWavInput(pathToWav);

    // framesarray holds all frames
    ArrayList<BufferedImage> FramesArray = new ArrayList<>();

    for (int i = 0; i < (30 * 60 * 9); i++) {
      // loads buffered images into array
      BufferedImage OneFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      if (i % 30 == 0)
        System.out.println("Loading video: " + i / 30 + "/" + 60 * 9);
      try {
        int frameLength = width * height * 3;
        String imgPath = pathToFrames + "frame" + i + ".rgb";
        File file = new File(imgPath);
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        raf.seek(0);

        long len = frameLength;
        byte[] bytes = new byte[(int) len];

        raf.read(bytes);

        int ind = 0;
        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            // byte a = 0;
            byte r = bytes[ind];
            byte g = bytes[ind + height * width];
            byte b = bytes[ind + height * width * 2];

            int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
            // int pix = ((a << 24) + (r << 16) + (g << 8) + b);
            OneFrame.setRGB(x, y, pix);
            ind++;
          }
        }
        raf.close();

        FramesArray.add(OneFrame);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // frame and label for displaying
    // TODO:add GUI for play pause
    frame = new JFrame();
    label = new JLabel();

    GridBagLayout gLayout = new GridBagLayout();
    frame.getContentPane().setLayout(gLayout);

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = 0;

    frame.getContentPane().add(label, c);
    frame.setVisible(true);
    Timer wavTimer = new Timer();
    Timer framesTimer = new Timer();

    // small delay to make sure they start in sync
    long startTime = System.currentTimeMillis() + 1000;

    // START AND END FRAME
    int startFrame = 0;
    int endFrame = 16200;

    Thread wavThread = new Thread(new PlaySound(wavInput, startFrame, endFrame));

    // initializes new thread to play sound
    wavTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        try {
          wavThread.start();
        } catch (Exception e) {
          e.printStackTrace();
          return;
        }
      }
    }, new Date(startTime));

    // use 3 threads to play video with 333ms and 666ms offset
    // kinda janky but other methods result in a desync of 1ms every 3 frames
    // when setting delay time = 1000/30 due to rounding error

    framesTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        ImageIcon icon = new ImageIcon(FramesArray.remove(0));
        label.setIcon(icon);
        frame.pack();
      }
    }, new Date(startTime), 100);

    framesTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        ImageIcon icon = new ImageIcon(FramesArray.remove(0));
        label.setIcon(icon);
        frame.pack();
      }
    }, new Date(startTime + 333), 100);

    framesTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        ImageIcon icon = new ImageIcon(FramesArray.remove(0));
        label.setIcon(icon);
        frame.pack();
      }
    }, new Date(startTime + 666), 100);
  }
}
