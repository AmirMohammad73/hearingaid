package com.hearingaid.app;

/**
 * فیلتر IIR (Infinite Impulse Response) برای پردازش فرکانسی
 * استفاده از فیلتر باند-پس برای تقویت فرکانس‌های خاص
 */
public class IIRFilter {
    private float[] a; // ضرایب فیلتر (denominator)
    private float[] b; // ضرایب فیلتر (numerator)
    private float[] xHistory; // تاریخچه ورودی
    private float[] yHistory; // تاریخچه خروجی
    private int order;
    
    /**
     * ساخت فیلتر باند-پس
     * @param sampleRate نرخ نمونه‌برداری
     * @param centerFreq فرکانس مرکزی
     * @param bandwidth پهنای باند
     * @param q فاکتور کیفیت
     */
    public IIRFilter(int sampleRate, float centerFreq, float bandwidth, float q) {
        this.order = 2;
        this.a = new float[order + 1];
        this.b = new float[order + 1];
        this.xHistory = new float[order + 1];
        this.yHistory = new float[order + 1];
        
        // محاسبه ضرایب فیلتر باند-پس با استفاده از تبدیل بیلیترال
        designBandPassFilter(sampleRate, centerFreq, bandwidth, q);
    }
    
    /**
     * طراحی فیلتر باند-پس
     */
    private void designBandPassFilter(int sampleRate, float centerFreq, float bandwidth, float q) {
        float w0 = 2.0f * (float) Math.PI * centerFreq / sampleRate;
        float bw = 2.0f * (float) Math.PI * bandwidth / sampleRate;
        float alpha = (float) Math.sin(w0) * (float) Math.sinh((float) Math.log(2.0) / 2.0 * q * w0 / (float) Math.sin(w0));
        
        float cosw0 = (float) Math.cos(w0);
        float a0 = 1.0f + alpha;
        
        // ضرایب فیلتر
        b[0] = alpha / a0;
        b[1] = 0.0f;
        b[2] = -alpha / a0;
        
        a[0] = 1.0f;
        a[1] = -2.0f * cosw0 / a0;
        a[2] = (1.0f - alpha) / a0;
    }
    
    /**
     * پردازش نمونه‌ها با اعمال تقویت
     */
    public void process(float[] samples, float gain) {
        for (int i = 0; i < samples.length; i++) {
            samples[i] = filterSample(samples[i]) * gain;
        }
    }
    
    /**
     * فیلتر کردن یک نمونه
     */
    private float filterSample(float input) {
        // جابجایی تاریخچه
        for (int i = order; i > 0; i--) {
            xHistory[i] = xHistory[i - 1];
            yHistory[i] = yHistory[i - 1];
        }
        xHistory[0] = input;
        
        // محاسبه خروجی
        float output = 0.0f;
        for (int i = 0; i <= order; i++) {
            output += b[i] * xHistory[i];
        }
        for (int i = 1; i <= order; i++) {
            output -= a[i] * yHistory[i];
        }
        output /= a[0];
        
        yHistory[0] = output;
        return output;
    }
    
    /**
     * ریست فیلتر
     */
    public void reset() {
        for (int i = 0; i <= order; i++) {
            xHistory[i] = 0.0f;
            yHistory[i] = 0.0f;
        }
    }
}

