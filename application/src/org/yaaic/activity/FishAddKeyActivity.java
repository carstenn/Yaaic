package org.yaaic.activity;

import org.yaaic.R;
import org.yaaic.fish.FishKeys;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FishAddKeyActivity extends Activity implements OnClickListener
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fishaddkey);
        ((Button) findViewById(R.id.fish_add_key_bnt)).setOnClickListener(this);

        Bundle data = getIntent().getExtras();
        setTitle(getTitle().toString() + " for " + data.getString("conversation"));
    }

    @Override
    public void onClick(View v)
    {
        Bundle data = getIntent().getExtras();
        FishKeys.getInstance().setKey(data.getString("server"), data.getString("conversation"), ((EditText) findViewById(R.id.fish_add_key_text)).getText().toString() );
        finish();
    }

}
