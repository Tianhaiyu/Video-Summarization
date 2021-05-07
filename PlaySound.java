import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.xml.transform.Source;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * <Replace this with a short description of the class.>
 *
 * @author Giulio
 */
public class PlaySound implements Runnable {

  private InputStream waveStream;
  private int frame;
  private int size;
  private ArrayList<byte[]> SoundArray;
  private AudioFormat audioFormat;
  private Info info;
  private SourceDataLine dataLine = null;
  private FloatControl gainControl;
  private Timer playTimer;
  private long delay;

  /**
   * CONSTRUCTOR
   */
  public PlaySound(InputStream waveStream) {
    this.waveStream = waveStream;
    try {
      createSoundArray();
    } catch (PlayWaveException e) {
      e.printStackTrace();
      return;
    } finally{
      try{        
        initializeStream();
        }catch(PlayWaveException e){
          e.printStackTrace();
        }
    }
  }
  public PlaySound(InputStream waveStream, ArrayList<byte[]> array) {
      this.waveStream = waveStream;
      this.SoundArray = array;
      try{        
      initializeStream();
      }catch(PlayWaveException e){
        e.printStackTrace();
      }
  }
  public void initializeStream() throws PlayWaveException{
    
    this.info = new Info(SourceDataLine.class, audioFormat);

    // opens the audio channel
    try {
      this.dataLine = (SourceDataLine) AudioSystem.getLine(this.info);
      this.dataLine.open(this.audioFormat);
    } catch (LineUnavailableException e1) {
      throw new PlayWaveException(e1);
    }

    gainControl = (FloatControl) dataLine.getControl(
      FloatControl.Type.MASTER_GAIN
    );
    gainControl.setValue(-20.0f);
    // Starts the music :P
    dataLine.start();
  }

  public int getFrame() { //get the number of audio frames PER VIDEO FRAME
    return this.frame;
  }

  public int getSize() { //get the number of bytes per audio frame
    return this.size;
  }

  public AudioFormat getAudioFormat() {
    return this.audioFormat;
  }
  public void setDelay(long d){
    this.delay = d;
  }
  public ArrayList<byte[]> getSoundArray() {
    return this.SoundArray;
  }

  public void setSoundArray(ArrayList<byte[]> array){
    this.SoundArray = array;
  }
  public void stopTimer(){
    dataLine.flush();
    this.playTimer.cancel();
  }
  public void run() {
    startTimer();
  }
  public void startTimer(){
    
    playTimer = new Timer();
    // try{
    //   Thread.sleep(this.delay - System.currentTimeMillis());
    // } catch(Exception e){
    //   e.printStackTrace();
    // }
    // try{
    //   for(int i = 0; i < 15; i++)
    //     play(SoundArray.remove(0));
    // }catch(PlayWaveException e){
    //   e.printStackTrace();
    // }

    playTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        try{
          if(SoundArray.size() > 0)
            play(SoundArray.remove(0));
        }catch(PlayWaveException e){
          e.printStackTrace();
        }
      }
    }, new Date(this.delay), 100);
    playTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        try{
          if(SoundArray.size() > 0)
            play(SoundArray.remove(0));

        }catch(PlayWaveException e){
          e.printStackTrace();
        }
      }
    }, new Date(this.delay + 333), 100);
    playTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        try{
          if(SoundArray.size() > 0)
            play(SoundArray.remove(0));
        }catch(PlayWaveException e){
          e.printStackTrace();
        }
      }
    }, new Date(this.delay + 666), 100);
    

  }
  public void createSoundArray() throws PlayWaveException {
    AudioInputStream audioInputStream = null;
    try {
      //audioInputStream = AudioSystem.getAudioInputStream(this.waveStream);

      //add buffer for mark/reset support, modified by Jian
      InputStream bufferedIn = new BufferedInputStream(this.waveStream);
      audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
    } catch (UnsupportedAudioFileException e1) {
      throw new PlayWaveException(e1);
    } catch (IOException e1) {
      throw new PlayWaveException(e1);
    }

    this.SoundArray = new ArrayList<byte[]>();

    this.audioFormat = audioInputStream.getFormat();
    //number of audio frames per 1 video frame
    this.frame = (int) this.audioFormat.getFrameRate() / 30;
    //number of bytes in 1 audio frame
    this.size = this.audioFormat.getFrameSize();
    //read a video frame worth of bytes and store it in arraylist
    int readBytes = 0;
    try {
      while (readBytes != -1) {
        byte[] audioBuffer = new byte[this.frame * this.size];
        readBytes =
          audioInputStream.read(audioBuffer, 0, this.frame * this.size);
        this.SoundArray.add(audioBuffer);
      }
    } catch (IOException e1) {
      throw new PlayWaveException(e1);
    }
    //trim off extra sound so it matches number of video frames
    while (this.SoundArray.size() > 16200) {
      this.SoundArray.remove(this.SoundArray.size() - 1);
    }
  }

  public void play(byte[]  soundClip) throws PlayWaveException {

        dataLine.write(soundClip, 0, this.frame * this.size);  

    // plays what's left and and closes the audioChannel
  }
}
