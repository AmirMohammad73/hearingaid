package com.hearingaid.app;

import java.util.HashMap;
import java.util.Map;

/**
 * تنظیمات تقویت برای فرکانس‌های مختلف
 */
public class FrequencyGainSettings {
    private Map<FrequencyBand, Float> gains;
    
    public FrequencyGainSettings() {
        gains = new HashMap<>();
        // مقدار پیش‌فرض: بدون تقویت
        for (FrequencyBand band : FrequencyBand.values()) {
            gains.put(band, 1.0f); // 1.0 = بدون تغییر
        }
    }
    
    /**
     * تنظیم تقویت برای یک باند فرکانسی (بر حسب dB)
     * @param band باند فرکانسی
     * @param gainDb تقویت بر حسب دسی‌بل (مثبت = تقویت، منفی = کاهش)
     */
    public void setGain(FrequencyBand band, float gainDb) {
        // تبدیل dB به ضریب خطی: gain = 10^(gainDb/20)
        float linearGain = (float) Math.pow(10.0, gainDb / 20.0);
        gains.put(band, linearGain);
    }
    
    /**
     * دریافت تقویت خطی برای یک باند
     */
    public float getGain(FrequencyBand band) {
        return gains.getOrDefault(band, 1.0f);
    }
    
    /**
     * دریافت تقویت بر حسب dB
     */
    public float getGainDb(FrequencyBand band) {
        float linearGain = getGain(band);
        return (float) (20.0 * Math.log10(linearGain));
    }
    
    /**
     * تنظیم تقویت برای یک فرکانس خاص (Hz)
     */
    public void setGainForFrequency(float frequencyHz, float gainDb) {
        FrequencyBand band = FrequencyBand.getBandForFrequency(frequencyHz);
        if (band != null) {
            setGain(band, gainDb);
        }
    }
    
    /**
     * دریافت تقویت برای یک فرکانس خاص
     */
    public float getGainForFrequency(float frequencyHz) {
        FrequencyBand band = FrequencyBand.getBandForFrequency(frequencyHz);
        if (band != null) {
            return getGainDb(band);
        }
        return 0.0f;
    }
}

