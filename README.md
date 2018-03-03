<p align="center"><img width="10%" vspace="20" src="https://github.com/flamebase/flamebase-database-android/raw/develop/app/src/main/res/mipmap-xxxhdpi/ic_launcher_rounded.png"></p>
<table>
<img width="10%" vspace="20" src="https://github.com/flamebase/flamebase-database-android/raw/develop/sample1.png">|
```java
class Test {

}
``

# :fire: flamebase-database-android

Real time JSON database (android client). Work with synchronized java objects stored as JSON objects.

### What is this?
Flamebase is an open source project that tries to emulate Firebase Database features as much as possible. In this repo you can find the proper lib for android client.
For now it still developing, so please be patient with errors.

### Requirements
**1º redis-server:** Amazing Pub/Sub engine for real-time changes. Simply install and start it.

**2º flamebase-server:** It will be our server cluster for storing json objects. Server cluster is run with **node** framework.

Check out [flamebase-server repo](https://github.com/flamebase/flamebase-server) for more information.

### Usage
- Import library:

```groovy
android {
 
    defaultConfig {
        multiDexEnabled true
    }
    
}
 
dependencies {
    implementation 'com.flamebase:database:1.5.0'
}
```
- Initialize library:
```java
// redis ips starts with redis://, port is not included
FlamebaseDatabase.initialize(getApplicationContext(), "http://10.0.2.2:1507/", "redis://10.0.2.2", new StatusListener() {
 
    @Override
    public void connected() {
        /* fired only when initialized method is called and library is connected to redis */
    }
    
    @Override
    public void reconnecting() {
        /* library is trying to connect to redis */
    }
 
});
 
// debug logs
FlamebaseDatabase.setDebug(true);
```
- Listener for objects:
```java
ObjectA objectA = null;
 
FlamebaseDatabase.createListener(path, new ObjectBlower<ObjectA>() {
 
    /**
    * gets new differences from local object
    */
    @Override
    public ObjectA updateObject() {
        return objectA;
    }
 
    /**
    * called after reference is synchronized with server
    * or is ready to be used.
    * 
    * null param means there is nothing stored on db
    */
    @Override
    public void onObjectChanged(ObjectA ref) {
        if (ref == null) {                          
            objectA = new ObjectA();
            objectA.setColor("blue");
            FlamebaseDatabase.syncReference(path);
        } else {                                    
            objectA = ref;                          
        }
    }
 
    /**
    * long server updates, from 0 to 100
    */
    @Override
    public void progress(int value) {
        Log.e(TAG, "loading " + path + " : " + value + " %");
    }
 
}, ObjectA.class);
```
- Listener for maps:
```java
Map<String, Member> contacts = null;
 
FlamebaseDatabase.createListener(path, new MapBlower<Member>() {
 
    @Override
    public Map<String, Member> updateMap() {
        return contacts;
    }
 
    @Override
    public void onMapChanged(Map<String, Member> ref) {
        // the same for maps
    }
 
    @Override
    public void progress(int value) {
        // percent
    }
 
}, Member.class);
```
- Remove listener in server by calling:
```java
FlamebaseDatabase.removeListener(path);
```

Background updates (not optional)
------------------
Flamebase Database library works in background in order to receive updates when application is on background or foreground. You must add FlamebaseService to your `AndroidManifest.xml` file:
```xml
<application>
 
    <service
        android:name="com.flamebase.database.FlamebaseService"
        android:enabled="true"
        android:exported="true" />
 
</application>
```
This service is controlled when the application is present and must be `bind` or `unbind`. Add in activities:
```java
@Override
protected void onResume() {
    super.onResume();
    FlamebaseDatabase.onResume();
}
 
@Override
protected void onPause() {
    FlamebaseDatabase.onPause();
    super.onPause();
}
```
In the sample app chats still receiving updates on background, when the application is reopened there is no need to ask for updates.

Limitations 
-----------
**List objects aren't supported:** List objects gives problems when differences are being generated. If you plan to store iterations you can dispose a map object with the object types you want:
```json
{
    "0": 1,
    "1": "item2",
    "2": {
        "title": "title 3",
        "body": "body 3",
        "delay": 3
    }
}
```
