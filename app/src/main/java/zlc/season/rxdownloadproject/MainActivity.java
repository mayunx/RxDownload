package zlc.season.rxdownloadproject;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import zlc.season.rxdownload3.core.DownloadConfig;
import zlc.season.rxdownload3.extension.ApkInstallExtension;
import zlc.season.rxdownloadproject.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);

        binding.contentMain.basicDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, BasicDownloadActivity.class));
            }
        });


        DownloadConfig.Builder builder = DownloadConfig.Builder.Companion.create(this)
                .enableService(true)
                .enableNotification(true)
                .addExtension(ApkInstallExtension.class);

        DownloadConfig.INSTANCE.init(builder);
    }

}
