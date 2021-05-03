import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

/**
 * 
 * <Replace this with a short description of the class.>
 * 
 * @author Giulio
 */
public class PlaySound implements Runnable{

    private InputStream waveStream;
    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
	private int frame;
	private int size;
	private ArrayList<byte[]> SoundArray;
	private AudioFormat audioFormat;
	int startFrame;
	int endFrame;
	
    /**
     * CONSTRUCTOR
     */
    public PlaySound(InputStream waveStream, int startFrame, int endFrame) {
		this.waveStream = waveStream;
		this.startFrame = startFrame;
		this.endFrame = endFrame;
		try{
			createSoundArray();
		}catch(PlayWaveException e){
			e.printStackTrace();
			return;
		}
    }

	public int getFrame(){//get the number of audio frames PER VIDEO FRAME
		return this.frame;
	}
	public int getSize(){//get the number of bytes per audio frame
		return this.size;
	}
	public AudioFormat getAudioFormat(){
		return this.audioFormat;
	}
	public ArrayList<byte[]> getSoundArray(){
		return this.SoundArray;
	}
	public void run(){
		try{
			play(this.startFrame, this.endFrame);
		}
		catch(PlayWaveException e){
			e.printStackTrace();
			return;
		}
	}
	
	public void createSoundArray() throws PlayWaveException{
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
		this.frame = (int)this.audioFormat.getFrameRate() / 30;
		//number of bytes in 1 audio frame
		this.size = this.audioFormat.getFrameSize();

		//read a video frame worth of bytes and store it in arraylist
		int readBytes = 0;
		try {	
			while(readBytes != -1){
				
				byte[] audioBuffer = new byte[this.frame*this.size];
				readBytes = audioInputStream.read(audioBuffer,0,this.frame*this.size);
				this.SoundArray.add(audioBuffer);
			}
		} catch (IOException e1) {
			throw new PlayWaveException(e1);
		}
		//trim off extra sound so it matches number of video frames
		while(this.SoundArray.size() > 16200){
			this.SoundArray.remove(this.SoundArray.size()-1);
		}
	} 

    public void play(int startFrame, int endFrame) throws PlayWaveException {

	Info info = new Info(SourceDataLine.class, audioFormat);

	// opens the audio channel
	SourceDataLine dataLine = null;
	try {
	    dataLine = (SourceDataLine) AudioSystem.getLine(info);
	    dataLine.open(this.audioFormat);
	} catch (LineUnavailableException e1) {
	    throw new PlayWaveException(e1);
	}

	FloatControl gainControl = (FloatControl) dataLine.getControl(FloatControl.Type.MASTER_GAIN);
	gainControl.setValue(-20.0f);
	// Starts the music :P
	dataLine.start();
	for(int i = startFrame; i < endFrame; i++){
		dataLine.write(this.SoundArray.get(i), 0, this.frame*this.size);
	}


	    // plays what's left and and closes the audioChannel
	    dataLine.drain();
	    dataLine.close();

    }
}
