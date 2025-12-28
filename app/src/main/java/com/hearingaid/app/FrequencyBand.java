package com.hearingaid.app;

/**
 * باندهای فرکانسی مختلف برای تنظیمات سمعک
 */
public enum FrequencyBand {
    LOW_125(125, 250),
    LOW_250(250, 500),
    MID_500(500, 1000),
    MID_1000(1000, 2000),
    MID_2000(2000, 4000),
    HIGH_4000(4000, 8000),
    HIGH_8000(8000, 16000);
    
    private final float centerFreq;
    private final float bandwidth;
    
    FrequencyBand(float centerFreq, float upperFreq) {
        this.centerFreq = centerFreq;
        this.bandwidth = upperFreq - centerFreq;
    }
    
    public float getCenterFrequency() {
        return centerFreq;
    }
    
    public float getBandwidth() {
        return bandwidth;
    }
    
    /**
     * پیدا کردن باند مناسب برای یک فرکانس خاص
     */
    public static FrequencyBand getBandForFrequency(float frequencyHz) {
        for (FrequencyBand band : values()) {
            float lower = band.centerFreq - band.bandwidth / 2;
            float upper = band.centerFreq + band.bandwidth / 2;
            if (frequencyHz >= lower && frequencyHz < upper) {
                return band;
            }
        }
        // اگر خارج از محدوده بود، نزدیک‌ترین را برگردان
        if (frequencyHz < LOW_125.centerFreq) {
            return LOW_125;
        }
        return HIGH_8000;
    }
    
    /**
     * دریافت فیلتر IIR برای این باند
     */
    public IIRFilter getFilter(int sampleRate) {
        // ایجاد فیلتر باند-پس برای این فرکانس
        float q = 2.0f; // فاکتور کیفیت
        return new IIRFilter(sampleRate, centerFreq, bandwidth, q);
    }
}

