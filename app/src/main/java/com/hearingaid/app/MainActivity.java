package com.hearingaid.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    private AudioProcessor audioProcessor;
    private Button btnStartStop;
    private SeekBar seekBarNoiseReduction;
    private SeekBar seekBarMasterVolume;
    private TextView tvNoiseReduction;
    private TextView tvMasterVolume;
    
    private FrequencyControlFragment leftEarFragment;
    private FrequencyControlFragment rightEarFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        audioProcessor = new AudioProcessor();
        
        initializeViews();
        setupTabs();
        checkPermissions();
    }
    
    private void initializeViews() {
        btnStartStop = findViewById(R.id.btnStartStop);
        seekBarNoiseReduction = findViewById(R.id.seekBarNoiseReduction);
        seekBarMasterVolume = findViewById(R.id.seekBarMasterVolume);
        tvNoiseReduction = findViewById(R.id.tvNoiseReduction);
        tvMasterVolume = findViewById(R.id.tvMasterVolume);
        
        btnStartStop.setOnClickListener(v -> toggleProcessing());
        
        // تنظیم SeekBar کاهش نویز
        seekBarNoiseReduction.setMax(100);
        seekBarNoiseReduction.setProgress(50);
        seekBarNoiseReduction.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float level = progress / 100.0f;
                audioProcessor.setNoiseReductionLevel(level);
                tvNoiseReduction.setText(String.format("کاهش نویز: %d%%", progress));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // تنظیم SeekBar صدا
        seekBarMasterVolume.setMax(200);
        seekBarMasterVolume.setProgress(100);
        seekBarMasterVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 100.0f;
                audioProcessor.setMasterVolume(volume);
                tvMasterVolume.setText(String.format("صدا: %d%%", progress));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private void setupTabs() {
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        
        // ایجاد Fragment ها
        leftEarFragment = FrequencyControlFragment.newInstance(true, audioProcessor.getLeftEarGains());
        rightEarFragment = FrequencyControlFragment.newInstance(false, audioProcessor.getRightEarGains());
        
        FrequencyControlAdapter adapter = new FrequencyControlAdapter(this);
        adapter.addFragment(leftEarFragment, getString(R.string.left_ear));
        adapter.addFragment(rightEarFragment, getString(R.string.right_ear));
        
        viewPager.setAdapter(adapter);
        
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(getString(R.string.left_ear));
                    } else {
                        tab.setText(getString(R.string.right_ear));
                    }
                }).attach();
    }
    
    private void toggleProcessing() {
        if (audioProcessor.isProcessing()) {
            audioProcessor.stop();
            btnStartStop.setText(getString(R.string.start));
            btnStartStop.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            if (checkPermissions()) {
                audioProcessor.start();
                btnStartStop.setText(getString(R.string.stop));
                btnStartStop.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            }
        }
    }
    
    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "دسترسی به میکروفون اعطا شد", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.audio_permission_required), Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioProcessor != null) {
            audioProcessor.stop();
        }
    }
}

