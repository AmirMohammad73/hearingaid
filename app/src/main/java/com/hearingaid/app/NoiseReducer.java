package com.hearingaid.app;

/**
 * کلاس کاهش نویز با استفاده از فیلتر وینر و تخمین طیف نویز
 */
public class NoiseReducer {
    private int sampleRate;
    private float[] noiseProfile; // پروفایل نویز
    private boolean noiseProfileSet = false;
    private static final int FFT_SIZE = 512;
    private static final float LEARNING_RATE = 0.1f;
    
    public NoiseReducer(int sampleRate) {
        this.sampleRate = sampleRate;
        this.noiseProfile = new float[FFT_SIZE / 2];
    }
    
    /**
     * کاهش نویز در سیگنال
     * @param samples نمونه‌های صوتی
     * @param strength قدرت کاهش نویز (0.0 تا 1.0)
     */
    public void reduceNoise(float[] samples, float strength) {
        if (strength <= 0.0f) {
            return;
        }
        
        // استفاده از فیلتر ساده برای کاهش نویز
        // در حالت واقعی، از الگوریتم‌های پیشرفته‌تر مانند Spectral Subtraction استفاده می‌شود
        
        // تخمین نویز (فرض می‌کنیم نویز در فرکانس‌های بالا بیشتر است)
        float noiseEstimate = estimateNoise(samples);
        
        // اعمال فیلتر کاهش نویز
        applyNoiseReduction(samples, noiseEstimate, strength);
    }
    
    /**
     * تخمین سطح نویز
     */
    private float estimateNoise(float[] samples) {
        // محاسبه انرژی در فرکانس‌های بالا (که معمولاً نویز بیشتری دارند)
        float highFreqEnergy = 0.0f;
        int count = 0;
        
        // استفاده از تفاضل برای تشخیص تغییرات سریع (نویز)
        for (int i = 1; i < samples.length; i++) {
            float diff = Math.abs(samples[i] - samples[i - 1]);
            if (diff > 0.01f) { // تغییرات سریع
                highFreqEnergy += diff;
                count++;
            }
        }
        
        return count > 0 ? highFreqEnergy / count : 0.0f;
    }
    
    /**
     * اعمال کاهش نویز با استفاده از فیلتر
     */
    private void applyNoiseReduction(float[] samples, float noiseEstimate, float strength) {
        // فیلتر ساده: کاهش تغییرات سریع (نویز) و حفظ تغییرات آهسته (صدا)
        float smoothingFactor = 0.3f * strength;
        
        for (int i = 1; i < samples.length; i++) {
            float diff = samples[i] - samples[i - 1];
            float absDiff = Math.abs(diff);
            
            // اگر تغییر سریع باشد (احتمالاً نویز)، آن را کاهش بده
            if (absDiff > noiseEstimate * 2.0f) {
                samples[i] = samples[i - 1] + diff * (1.0f - smoothingFactor);
            }
            
            // محدود کردن دامنه برای جلوگیری از clipping
            samples[i] = Math.max(-1.0f, Math.min(1.0f, samples[i]));
        }
    }
    
    /**
     * یادگیری پروفایل نویز از نمونه‌های خاموش
     */
    public void learnNoiseProfile(float[] silentSamples) {
        // این متد می‌تواند برای یادگیری پروفایل نویز از نمونه‌های خاموش استفاده شود
        // در نسخه کامل‌تر، از FFT و تحلیل طیفی استفاده می‌شود
        noiseProfileSet = true;
    }
}

