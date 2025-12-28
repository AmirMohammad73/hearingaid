package com.hearingaid.app;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * کلاس اصلی پردازش صدا برای سمعک
 * شامل ضبط صدا، پردازش فرکانسی، کاهش نویز و پخش
 */
public class AudioProcessor {
    private static final String TAG = "AudioProcessor";
    
    // تنظیمات صوتی
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO; // بیشتر دستگاه‌ها مونو پشتیبانی می‌کنند
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, CHANNEL_CONFIG_IN, AUDIO_FORMAT) * 2;
    
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private Thread processingThread;
    private boolean isProcessing = false;
    
    // تنظیمات تقویت فرکانسی (برای هر گوش جداگانه)
    private FrequencyGainSettings leftEarGains;
    private FrequencyGainSettings rightEarGains;
    
    // تنظیمات کاهش نویز
    private float noiseReductionLevel = 0.5f; // 0.0 = خاموش، 1.0 = حداکثر
    private float masterVolume = 1.0f;
    
    // برای کاهش نویز
    private NoiseReducer noiseReducer;
    
    public AudioProcessor() {
        leftEarGains = new FrequencyGainSettings();
        rightEarGains = new FrequencyGainSettings();
        noiseReducer = new NoiseReducer(SAMPLE_RATE);
    }
    
    /**
     * شروع پردازش صدا
     */
    public void start() {
        if (isProcessing) {
            return;
        }
        
        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG_IN,
                    AUDIO_FORMAT,
                    BUFFER_SIZE
            );
            
            audioTrack = new AudioTrack(
                    android.media.AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AUDIO_FORMAT,
                    BUFFER_SIZE,
                    AudioTrack.MODE_STREAM
            );
            
            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED ||
                audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                Log.e(TAG, "خطا در راه‌اندازی ضبط یا پخش صدا");
                return;
            }
            
            audioRecord.startRecording();
            audioTrack.play();
            isProcessing = true;
            
            processingThread = new Thread(this::processAudio);
            processingThread.start();
            
            Log.d(TAG, "پردازش صدا شروع شد");
        } catch (Exception e) {
            Log.e(TAG, "خطا در شروع پردازش", e);
        }
    }
    
    /**
     * توقف پردازش صدا
     */
    public void stop() {
        if (!isProcessing) {
            return;
        }
        
        isProcessing = false;
        
        try {
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
            
            if (audioTrack != null) {
                audioTrack.stop();
                audioTrack.release();
                audioTrack = null;
            }
            
            if (processingThread != null) {
                processingThread.join(1000);
            }
            
            Log.d(TAG, "پردازش صدا متوقف شد");
        } catch (Exception e) {
            Log.e(TAG, "خطا در توقف پردازش", e);
        }
    }
    
    /**
     * حلقه اصلی پردازش صدا
     */
    private void processAudio() {
        short[] buffer = new short[BUFFER_SIZE];
        short[] processedBuffer = new short[BUFFER_SIZE * 2]; // استریو = 2 * مونو
        
        while (isProcessing) {
            int samplesRead = audioRecord.read(buffer, 0, buffer.length);
            
            if (samplesRead > 0) {
                // پردازش بافر (ورودی مونو، خروجی استریو)
                processBuffer(buffer, processedBuffer, samplesRead);
                
                // پخش صدا (خروجی استریو است)
                int samplesWritten = audioTrack.write(processedBuffer, 0, samplesRead * 2);
                if (samplesWritten < 0) {
                    Log.e(TAG, "خطا در نوشتن به AudioTrack: " + samplesWritten);
                }
            }
        }
    }
    
    /**
     * پردازش بافر صوتی: تقویت فرکانسی، کاهش نویز
     * ورودی مونو است و به استریو تبدیل می‌شود
     */
    private void processBuffer(short[] input, short[] output, int length) {
        // ورودی مونو است، پس length نمونه داریم
        int numSamples = length;
        float[] monoChannel = new float[numSamples];
        float[] leftChannel = new float[numSamples];
        float[] rightChannel = new float[numSamples];
        
        // تبدیل ورودی مونو به float
        for (int i = 0; i < numSamples; i++) {
            monoChannel[i] = input[i] / 32768.0f;
        }
        
        // کپی به هر دو کانال (برای پردازش جداگانه)
        System.arraycopy(monoChannel, 0, leftChannel, 0, numSamples);
        System.arraycopy(monoChannel, 0, rightChannel, 0, numSamples);
        
        // کاهش نویز
        if (noiseReductionLevel > 0) {
            noiseReducer.reduceNoise(leftChannel, noiseReductionLevel);
            noiseReducer.reduceNoise(rightChannel, noiseReductionLevel);
        }
        
        // تقویت فرکانسی برای هر کانال (جداگانه)
        applyFrequencyGain(leftChannel, leftEarGains);
        applyFrequencyGain(rightChannel, rightEarGains);
        
        // اعمال صدا
        for (int i = 0; i < numSamples; i++) {
            leftChannel[i] *= masterVolume;
            rightChannel[i] *= masterVolume;
        }
        
        // تبدیل به short و ترکیب به استریو (خروجی)
        int outputLength = numSamples * 2; // استریو = 2 * مونو
        for (int i = 0; i < numSamples; i++) {
            output[i * 2] = (short) Math.max(-32768, Math.min(32767, leftChannel[i] * 32767.0f));
            output[i * 2 + 1] = (short) Math.max(-32768, Math.min(32767, rightChannel[i] * 32767.0f));
        }
    }
    
    /**
     * اعمال تقویت فرکانسی با استفاده از فیلترهای IIR
     */
    private void applyFrequencyGain(float[] samples, FrequencyGainSettings gains) {
        // استفاده از فیلترهای باند برای هر فرکانس
        for (FrequencyBand band : FrequencyBand.values()) {
            float gain = gains.getGain(band);
            if (Math.abs(gain - 1.0f) > 0.01f) { // اگر تقویت نیاز باشد
                applyBandGain(samples, band, gain);
            }
        }
    }
    
    /**
     * اعمال تقویت برای یک باند فرکانسی خاص
     */
    private void applyBandGain(float[] samples, FrequencyBand band, float gain) {
        // فیلتر باند-پس ساده با استفاده از FFT یا فیلتر IIR
        // برای سادگی، از فیلتر IIR استفاده می‌کنیم
        
        IIRFilter filter = band.getFilter(SAMPLE_RATE);
        if (filter != null) {
            filter.process(samples, gain);
        }
    }
    
    // Getter و Setter ها
    public FrequencyGainSettings getLeftEarGains() {
        return leftEarGains;
    }
    
    public FrequencyGainSettings getRightEarGains() {
        return rightEarGains;
    }
    
    public void setNoiseReductionLevel(float level) {
        this.noiseReductionLevel = Math.max(0.0f, Math.min(1.0f, level));
    }
    
    public float getNoiseReductionLevel() {
        return noiseReductionLevel;
    }
    
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(2.0f, volume));
    }
    
    public float getMasterVolume() {
        return masterVolume;
    }
    
    public boolean isProcessing() {
        return isProcessing;
    }
}

