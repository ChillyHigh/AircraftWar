package edu.hitsz.thread;

import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;
import java.io.*;

public class MusicThread extends Thread {


    //音频文件名
    private String filename;
    private AudioFormat audioFormat;
    private byte[] samples;
    private boolean loop;
    private volatile boolean running = true;
    private volatile SourceDataLine dataLine;

    public MusicThread(String filename) {
        this(filename, false);
    }

    public MusicThread(String filename, boolean loop) {
        //初始化filename
        this.filename = filename;
        this.loop = loop;
        setDaemon(true);
        reverseMusic();
    }

    public void stopMusic() {
        running = false;
        SourceDataLine line = dataLine;
        if (line != null) {
            line.stop();
            line.flush();
            line.close();
        }
        interrupt();
    }

    public void reverseMusic() {
        try {
            //定义一个AudioInputStream用于接收输入的音频数据，使用AudioSystem来获取音频的音频输入流
            InputStream inputStream = MusicThread.class.getResourceAsStream(filename);
            if (inputStream == null) {
                throw new IOException("未找到音频资源: " + filename);
            }
            AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(inputStream));
            //用AudioFormat来获取AudioInputStream的格式
            audioFormat = stream.getFormat();
            samples = getSamples(stream);
            stream.close();
        } catch (UnsupportedAudioFileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public byte[] getSamples(AudioInputStream stream) {
        int size = (int) (stream.getFrameLength() * audioFormat.getFrameSize());
        if (size <= 0) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            try {
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return outputStream.toByteArray();
        }
        byte[] samples = new byte[size];
        DataInputStream dataInputStream = new DataInputStream(stream);
        try {
            dataInputStream.readFully(samples);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return samples;
    }

    public void play(InputStream source) {
        int size = (int) (audioFormat.getFrameSize() * audioFormat.getSampleRate());
        byte[] buffer = new byte[size];
        //源数据行SoureDataLine是可以写入数据的数据行
        SourceDataLine dataLine = null;
        //获取受数据行支持的音频格式DataLine.info
        Info info = new Info(SourceDataLine.class, audioFormat);
        try {
            dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open(audioFormat, size);
        } catch (LineUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (dataLine == null) {
            return;
        }
        this.dataLine = dataLine;
        dataLine.start();
        try {
            int numBytesRead = 0;
            while (running && numBytesRead != -1) {
                //从音频流读取指定的最大数量的数据字节，并将其放入缓冲区中
                numBytesRead =
                        source.read(buffer, 0, buffer.length);
                //通过此源数据行将数据写入混频器
                if (running && numBytesRead != -1) {
                    dataLine.write(buffer, 0, numBytesRead);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (running) {
            dataLine.drain();
        }
        this.dataLine = null;
        dataLine.close();

    }

    @Override
    public void run() {
        if (samples == null || audioFormat == null) {
            return;
        }
        do {
            InputStream stream = new ByteArrayInputStream(samples);
            play(stream);
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (running && loop);
    }
}
