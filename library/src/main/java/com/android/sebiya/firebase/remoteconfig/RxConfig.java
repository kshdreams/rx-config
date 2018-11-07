package com.android.sebiya.firebase.remoteconfig;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RxConfig {

    private static final String LOG_TAG = "RxConfig";

    private static final String PREF_NAME = "remote_config";

    private static final long DEFAULT_CACHE_TIME_SEC = 60 * 60; // 60 min

    public static class Config<T> {

        public final String key;

        public final boolean isChanged;

        public final T remote;

        public final T local;

        public final boolean fromRemote;

        private Config(String key, boolean fromRemote, T local, T remote) {
            this.key = key;
            this.local = local;
            this.remote = remote;
            this.fromRemote = fromRemote;
            if (remote != null) {
                this.isChanged = !remote.equals(local);
            } else if (local != null) {
                this.isChanged = !local.equals(remote);
            } else {
                this.isChanged = false;
            }
        }

        @Override
        public String toString() {
            return key + ", changed - " + isChanged + ", fromRemote - " + fromRemote + ", remote - " + remote
                    + ", local - " + local;
        }
    }

    public Single<Task<Void>> fetch() {
        return fetch(DEFAULT_CACHE_TIME_SEC);
    }

    public Single<Task<Void>> fetch(final long cacheExpiredTime) {
        return Single.create(new SingleOnSubscribe<Task<Void>>() {
            @Override
            public void subscribe(final SingleEmitter<Task<Void>> emitter) throws Exception {
                getRemoteConfig().fetch(cacheExpiredTime).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull final Task<Void> task) {
                        if (task.isSuccessful()) {
                            getRemoteConfig().activateFetched();
                        }
                        Log.d(LOG_TAG,
                                "fetch. last status - " + getRemoteConfig().getInfo().getLastFetchStatus()
                                        + ", fetch time - " + getRemoteConfig().getInfo().getFetchTimeMillis());
                        emitter.onSuccess(task);
                    }
                });
            }
        });
    }

    public Single<List<Config<String>>> getValues(final Context context) {
        return fetch().map(new Function<Task<Void>, List<Config<String>>>() {
            @Override
            public List<Config<String>> apply(final Task<Void> task) throws Exception {
                List<Config<String>> configs = new ArrayList<>();
                Set<String> keySet = getRemoteConfig().getKeysByPrefix("");
                if (keySet != null) {
                    for (String key : keySet) {
                        String remoteValue = getRemoteConfig().getString(key);
                        String localValue = getLocalConfig(context).getString(key, null);
                        configs.add(new Config<>(key, task.isSuccessful(), localValue, remoteValue));
                    }
                }
                return configs;
            }
        });
    }

    public Single<Config<String>> getString(final Context context, final String key) {
        return fetch().map(new Function<Task<Void>, Config<String>>() {
            @Override
            public Config<String> apply(final Task<Void> task) throws Exception {
                SharedPreferences localConfig = getLocalConfig(context);
                String localValue = localConfig.getString(key, null);
                String remoteValue = getRemoteConfig().getString(key);
                getRemoteConfig().getValue(key);
                Log.d(LOG_TAG,
                        "getString. remote - " + remoteValue
                                + ", local - " + localValue + ", successful - " + task.isSuccessful());
                return new Config<>(key,
                        task.isSuccessful(), localValue, remoteValue);
            }
        });
    }

    public <T> Completable applyConfig(final Context context, final Config<T> config) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter emitter) throws Exception {
                if (config.isChanged) {
                    if (config.remote instanceof String) {
                        getLocalConfig(context).edit().putString(config.key, (String) config.remote).apply();
                    }
                    // TODO : handling multiple type
                }
                emitter.onComplete();
            }
        });
    }

    private static SharedPreferences getLocalConfig(Context context) {
        return context
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    protected FirebaseRemoteConfig getRemoteConfig() {
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setConfigSettings(
                new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(false).build());
        return firebaseRemoteConfig;
    }
}
