# :fire: flamebase-database-android

Real time JSON database (android client). Work with synchronized java objects stored as JSON objects.

### What is this?
Flamebase is an open source project that tries to emulate Firebase Database features as much as possible. I like Firebase but it's expensive for what it currently offers.
If you are doing an altruist project with Firebase, pray not to became successful, because the monthly amount will increase considerably.

In this repo you can find the proper lib for android client.
For now it still developing, so please be patient with errors.

### Requirements
**1º redis-server:** Amazing Pub/Sub for real-time changes. Simply install and start it.

**2º flamebase-database-server-cluster:** It will be our server cluster for storing json objects. Server cluster is run with **node** framework.

Check out [flamebase-database-server-cluster repo](https://github.com/flamebase/flamebase-database-server-cluster) for more information.

### Usage
- Import library:

```groovy
android {
 
    defaultConfig {
        multiDexEnabled true
    }
    
}
 
dependencies {
    implementation 'com.flamebase:database:1.4.0'
}
```
- Initialize library:
```java
// redis ips starts with redis://, port is not included
FlamebaseDatabase.initialize(Context context, String cluster_ip, String redis_ip, new StatusListener() {
 
    @Override
    public void connected() {
        /* only called when flamebase service starts and connects with cluster
        *  it won't be fired if service is already started
        */
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
    * object is gonna be synchronized with server
    */
    @Override
    public ObjectA updateObject() {
        return objectA;
    }
 
    /**
    * called after reference is synchronized with server
    * or is connected to be used (1st sync).
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