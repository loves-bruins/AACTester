package furtiveops.com.aactester;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("aac_enc");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.start_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
            }
        });

        findViewById(R.id.stop_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
            }
        });
    }

    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;


    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    private ByteArrayOutputStream pcmOutputStream;

    private void startRecording() {

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile() {
        // Write the output audio in byte

        short sData[] = new short[BufferElements2Rec];

        pcmOutputStream = new ByteArrayOutputStream();

        while (isRecording) {
            // gets the voice output from microphone to byte format

            recorder.read(sData, 0, BufferElements2Rec);
            System.out.println("Short wirting to file" + sData.toString());
            try {
                // // writes the data to file from buffer
                // // stores the voice buffer
                byte bData[] = short2byte(sData);
                pcmOutputStream.write(bData, 0, BufferElements2Rec * BytesPerElement);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;

            byte [] pcmBytes = pcmOutputStream.toByteArray();

            try {
                pcmOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            processBytes(pcmBytes);
        }
    }

    private void processBytes(final byte[] bytes)
    {
        if(AACEncoderInitialize() == 0)
        {
            ByteArrayInputStream pcmBytes = new ByteArrayInputStream(bytes);
            byte [] inputBytes = new byte[2048];
            byte [] aac = new byte[8192];
            ByteArrayOutputStream encodedBytes = new ByteArrayOutputStream();
            int length = 0;
            int offset = 0;
            try {
                while((length = pcmBytes.read(inputBytes)) > 0)
                {
                    int result = AACEncoderEncode(inputBytes, length, aac);
                    if(result < 0)
                    {
                        break;
                    }
                    else
                    {
                        encodedBytes.write(aac, 0, result);
                        offset += result;
                    }
                }
                AACEncoderCleanup();
                writeToFile(encodedBytes.toByteArray(), "/sdcard/compressed.aac");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void writeToFile(byte[] data, String fileName) throws IOException{
        FileOutputStream out = new FileOutputStream(fileName);
        out.write(data);
        out.close();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native int AACEncoderInitialize();
    public native int AACEncoderEncode(byte [] pcm, int pcmLength, byte[]aac);
    public native void AACEncoderCleanup();
}
