# rx-config

this is simple rx library to use firebase remote config.

[![](https://jitpack.io/v/kshdreams/rx-config.svg)](https://jitpack.io/#kshdreams/rx-config)


##### GETTING STARTED
rx-config releases are available via JitPack.
```
// Project level build.gradle
// ...
repositories {
    maven { url 'https://jitpack.io' }
}
// ...

// Module level build.gradle
dependencies {
    implementation 'com.github.kshdreams:rx-config:0.0.2'
}
```


#### How To Use
The most basic case is as follows:

##### query all configs in firebase remote.
```Java
mRxConfig.getValues(MainActivity.this)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<Config<String>>>() {
            @Override
            public void accept(final List<Config<String>> configs) throws Exception {
                // TODO :

            }
        });
```

#### get specific values and apply that value to local.
```Java
mRxConfig.getString(MainActivity.this, "market_app_version")
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<Config<String>>() {
            @Override
            public void accept(final Config<String> config) throws Exception {
                // This is value from firebase remote server
                String remote = config.remote;
                // This is value from local preference
                String local = config.local;

                // apply remote value to local after you handle config changes.
                mRxConfig.applyConfig(MainActivity.this, config).subscribe();
            }
        });
```


## License
```
Copyright 2018 kshdreams

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```