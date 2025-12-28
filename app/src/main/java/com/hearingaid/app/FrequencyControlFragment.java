package com.hearingaid.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

public class FrequencyControlFragment extends Fragment {
    private static final String ARG_IS_LEFT_EAR = "is_left_ear";
    
    private boolean isLeftEar;
    private FrequencyGainSettings gainSettings;
    private Map<FrequencyBand, SeekBar> frequencySeekBars;
    private Map<FrequencyBand, TextView> frequencyTextViews;
    
    public static FrequencyControlFragment newInstance(boolean isLeftEar, FrequencyGainSettings gainSettings) {
        FrequencyControlFragment fragment = new FrequencyControlFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_LEFT_EAR, isLeftEar);
        fragment.setArguments(args);
        fragment.gainSettings = gainSettings;
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frequency_control, container, false);
        
        if (getArguments() != null) {
            isLeftEar = getArguments().getBoolean(ARG_IS_LEFT_EAR);
        }
        
        frequencySeekBars = new HashMap<>();
        frequencyTextViews = new HashMap<>();
        
        setupFrequencyControls(view);
        
        return view;
    }
    
    private void setupFrequencyControls(View view) {
        // تنظیم SeekBar برای هر باند فرکانسی
        setupFrequencyControl(view, R.id.seekBar125, R.id.tv125, FrequencyBand.LOW_125, "125 Hz");
        setupFrequencyControl(view, R.id.seekBar250, R.id.tv250, FrequencyBand.LOW_250, "250 Hz");
        setupFrequencyControl(view, R.id.seekBar500, R.id.tv500, FrequencyBand.MID_500, "500 Hz");
        setupFrequencyControl(view, R.id.seekBar1000, R.id.tv1000, FrequencyBand.MID_1000, "1000 Hz");
        setupFrequencyControl(view, R.id.seekBar2000, R.id.tv2000, FrequencyBand.MID_2000, "2000 Hz");
        setupFrequencyControl(view, R.id.seekBar4000, R.id.tv4000, FrequencyBand.HIGH_4000, "4000 Hz");
        setupFrequencyControl(view, R.id.seekBar8000, R.id.tv8000, FrequencyBand.HIGH_8000, "8000 Hz");
    }
    
    private void setupFrequencyControl(View view, int seekBarId, int textViewId, 
                                       FrequencyBand band, String label) {
        SeekBar seekBar = view.findViewById(seekBarId);
        TextView textView = view.findViewById(textViewId);
        
        // محدوده: -20 dB تا +20 dB (0 = بدون تغییر)
        seekBar.setMax(400); // -20 تا +20 با گام 0.1
        seekBar.setProgress(200); // مقدار پیش‌فرض: 0 dB
        
        // تنظیم مقدار اولیه از gainSettings
        float currentGainDb = gainSettings.getGainDb(band);
        int progress = (int) ((currentGainDb + 20.0f) * 10.0f);
        seekBar.setProgress(progress);
        updateFrequencyText(textView, label, currentGainDb);
        
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float gainDb = (progress / 10.0f) - 20.0f;
                    gainSettings.setGain(band, gainDb);
                    updateFrequencyText(textView, label, gainDb);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        frequencySeekBars.put(band, seekBar);
        frequencyTextViews.put(band, textView);
    }
    
    private void updateFrequencyText(TextView textView, String label, float gainDb) {
        String sign = gainDb >= 0 ? "+" : "";
        textView.setText(String.format("%s: %s%.1f dB", label, sign, gainDb));
    }
}

