package com.sloths.speedy.shortsounds.model;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The AudioMixer class is used for generating a single .wav file for a given ShortSound.
 * This class combines all the raw data files for each ShortSoundTrack into a single file.
 */
public class AudioMixer {
    private ShortSound shortSound;
    public static final int BUFFER_SIZE = AudioRecorder.BUFFER_SIZE;

    /**
     * Creates a new Audio Mixer
     * @param shortSound The ShortSound to mix with the AudioMixer
     */
    public AudioMixer( ShortSound shortSound ) {
        this.shortSound = shortSound;
    }

    /**
     * Generates a single audio file from all of the track files
     * in the ShortSound
     * @return A single wav file of the compiled ShortSound
     * @throws IOException
     */
    public File generateAudioFile() throws IOException {
        File mixedFile = new File( ShortSoundTrack.STORAGE_PATH, shortSound.getTitle() + ".raw" );
        OutputStream master = new DataOutputStream( new FileOutputStream( mixedFile ) );
        List<DataInputStream> trackStreams = new ArrayList<>();
        // Add all the tracks to the list of input streams
        List<ShortSoundTrack> tracks = shortSound.getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            trackStreams.add( new DataInputStream(
                    new FileInputStream(
                            new File( ShortSoundTrack.STORAGE_PATH, tracks.get(i).getFileName() ) ) ) );
        }
        // Proceed to combine the raw audio files
        while( trackStreams.size() > 0 ) {
            List<byte[]> chunksToMix = new ArrayList<>();
            for (int i = 0; i < trackStreams.size(); i++) {
                DataInputStream trackInput = trackStreams.get(i);
                if ( trackInput.available() > 0 ) {
                    // We read another chunk of data
                    byte tempBuf[] = new byte[BUFFER_SIZE];
                    trackInput.read(tempBuf);
                    chunksToMix.add( tempBuf );
                } else {
                    // We have reached the end of the stream
                    DataInputStream stream = trackStreams.remove(i);
                    stream.close();
                    i--;
                }
            }
            byte mixedBuf[] = mixByteArrays( chunksToMix );
            // Write the short to output
            master.write( mixedBuf );
            master.flush();
        }
        master.flush();
        master.close();
        return writeWavHeader( mixedFile );
    }

    /**
     * Mix byte arrays simply by adding bytes and taking their average.
     * @param chunksToMix A list of the byte arrays to mix
     * @return a single byte array of the averages from the list of byte arrays passed in
     */
    private byte[] mixByteArrays(List<byte[]> chunksToMix) {
        byte mixedBuf[] = new byte[BUFFER_SIZE];
        // For each byte in the arrays
        for (int i = 0; i < BUFFER_SIZE; i++) {
            long mixedByte = 0;
            for (int j = 0; j < chunksToMix.size(); j++) {
                mixedByte+= chunksToMix.get(j)[i];
            }
            // Hard cap
            if ( mixedByte > Byte.MAX_VALUE )
                mixedByte = Byte.MAX_VALUE;
            else if ( mixedByte < Byte.MIN_VALUE )
                mixedByte = Byte.MIN_VALUE;
            mixedBuf[i] = (byte)(mixedByte);
        }
        return mixedBuf;
    }

    /**
     * Method for testing purposes.
     * Getter for shortsound
     * @return ShortSound this.shortsound
     */
    public ShortSound getShortSound() {
        return this.shortSound;
    }

    /**
     * Writes the header of the wav file
     * @param fileToConvert the file to convert from raw to wav by adding a header
     * @return a wav file - the raw file passed in as a parameter with a header added
     * @throws IOException
     */
    private File writeWavHeader( File fileToConvert ) throws IOException {
        // Constants used in the .wav header
        long mySubChunk1Size = 16;  // 16bit PCM
        int myBitsPerSample = 16;
        int myFormat = 1;  // 1 = PCM
        long myChannels = 2;  // Stereo
        long mySampleRate = ShortSoundTrack.SAMPLE_RATE;  // Sample rate
        long myByteRate = mySampleRate * myChannels * myBitsPerSample / 8;
        int myBlockAlign = (int) (myChannels * myBitsPerSample / 8);
        System.out.println("Length of file: " + fileToConvert.length());
        long myDataSize = fileToConvert.length() - 44;  // account for header
        long myChunk2Size =  myDataSize * myChannels * myBitsPerSample/8;
        long myChunkSize = 36 + myChunk2Size;

        // Open up the output file.
        File mixedWavFile = new File( ShortSoundTrack.STORAGE_PATH, shortSound.getTitle() + ".wav" );
        OutputStream os;
        os = new FileOutputStream( mixedWavFile );
        BufferedOutputStream bos = new BufferedOutputStream(os);
        DataOutputStream outFile = new DataOutputStream(bos);

        // Write the .wav header
        outFile.writeBytes("RIFF");                                 // 00 - RIFF
        outFile.write(intToByteArray((int)myChunkSize), 0, 4);      // 04 - how big is the rest of this file?
        outFile.writeBytes("WAVE");                                 // 08 - WAVE
        outFile.writeBytes("fmt ");                                 // 12 - fmt
        outFile.write(intToByteArray((int)mySubChunk1Size), 0, 4);  // 16 - size of this chunk
        outFile.write(shortToByteArray((short)myFormat), 0, 2);     // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
        outFile.write(shortToByteArray((short)myChannels), 0, 2);   // 22 - mono or stereo? 1 or 2?  (or 5 or ???)
        outFile.write(intToByteArray((int)mySampleRate), 0, 4);     // 24 - samples per second (numbers per second)
        outFile.write(intToByteArray((int)myByteRate), 0, 4);       // 28 - bytes per second
        outFile.write(shortToByteArray((short)myBlockAlign), 0, 2); // 32 - # of bytes in one sample, for all channels
        outFile.write(shortToByteArray((short)myBitsPerSample), 0, 2);  // 34 - how many bits in a sample(number)?  usually 16 or 24
        outFile.writeBytes("data");                                 // 36 - data
        outFile.write(intToByteArray((int)myDataSize), 0, 4);       // 40 - how big is this data chunk

        // Copy the raw data over
        DataInputStream inputStream = new DataInputStream( new FileInputStream( fileToConvert ) );
        byte[] buf = new byte[4096];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            outFile.write(buf, 0, len);
        }
        inputStream.close();

        outFile.flush();
        outFile.close();
        return mixedWavFile;
    }

    /**
     * Helper for writing the .wav header by converting an int to a byte[].
     * @param i integer to convert to a byte array
     * @return the byte array conversion of the integer
     */
    private static byte[] intToByteArray (int i) {
        byte[] b = new byte[4];
        b[0] = (byte) (i & 0x00FF);
        b[1] = (byte) ((i >> 8) & 0x000000FF);
        b[2] = (byte) ((i >> 16) & 0x000000FF);
        b[3] = (byte) ((i >> 24) & 0x000000FF);
        return b;
    }

    /**
     * Helper for writing the .wav header by converting a short to a byte[].
     * @param data short to convert to a byte array
     * @return the byte array converted from the short
     */
    public static byte[] shortToByteArray(short data) {
        return new byte[]{(byte)(data & 0xff),(byte)((data >>> 8) & 0xff)};
    }
}
