package com.android.sebiya.firebase.remoteconfig.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.android.sebiya.firebase.remoteconfig.RxConfig;
import com.android.sebiya.firebase.remoteconfig.RxConfig.Config;
import com.android.sebiya.simplearrayadapter.AbsArrayAdapter.AbsViewBinder;
import com.android.sebiya.simplearrayadapter.AbsArrayAdapter.OnItemClickListener;
import com.android.sebiya.simplearrayadapter.SimpleArrayAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";

    private SimpleArrayAdapter<RxConfig.Config<String>> mAdapter;
    private FloatingActionButton mFab;

    private RxConfig mRxConfig = new RxConfig();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = SimpleArrayAdapter.<RxConfig.Config<String>>with(this)
                .setLayoutResId(R.layout.list_item)
                .addViewBinder(R.id.text1, new AbsViewBinder<RxConfig.Config<String>, TextView>() {
                    @Override
                    protected void bindView(final TextView textView, final Config<String> stringConfig) {
                        textView.setText("key : " + stringConfig.key + ", configApplied : " + !stringConfig.isChanged);
                    }
                })
                .withItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(final View view, final int position) {
                        RxConfig.Config<String> config = mAdapter.getItemByAdapterPosition(position);
                        Snackbar.make(mFab, config.toString(), Snackbar.LENGTH_INDEFINITE).show();
                    }
                })
                .build();

        recyclerView.setAdapter(mAdapter);

        loadValues();

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                List<RxConfig.Config<String>> items = mAdapter.getItems();
                if (items == null || items.isEmpty()) {
                    return;
                }

                for (RxConfig.Config<String> config : items) {
                    mRxConfig.applyConfig(MainActivity.this, config).subscribe();
                }

                loadValues();
            }
        });
    }

    private void loadValues() {
        mRxConfig.getValues(MainActivity.this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Config<String>>>() {
                    @Override
                    public void accept(final List<Config<String>> configs) throws Exception {
                        Log.d(LOG_TAG, "getValues. configs - " + configs);
                        mAdapter.swapArray(configs);
                    }
                });
    }
}
