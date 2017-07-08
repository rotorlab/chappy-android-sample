# :fire: flamebase-database-android

Real time JSON database (android client). Work with synchronized java objects stored as JSON objects.

### What is this?
Flamebase is an open source project that tries to emulate Firebase Database features as much as possible. I like Firebase but it's expensive for what it currently offers.
If you are doing an altruist project with Firebase, pray not to became successful, because the monthly amount will increase considerably.

In this repo you can find the proper lib for android client.
For now it still developing, so please be patient with errors.

### Requirements
Before use this lib you must initialize a **flamebase-database-server-cluster** which will be our server cluster for storing json objects.
The server cluster is run with **node** framework. Check out the [repository](https://github.com/flamebase/flamebase-database-server-cluster) for more information.

### Usage
Import library:

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.flamebase:database:1.2.0'
}
```
Initialize library:
```java
FlamebaseDatabase.initialize(Context context, String cluster_ip, String token);
```
Listener for objects:
```java
final ObjectA objectA = new ObjectA();
objectA.setColor("blue");

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
    * or is ready to be used (1st sync.
    * 
    * null param means there is nothing stored on db
    */
    @Override
    public void onObjectChanged(ObjectA ref) {
        if (ref == null) {                          // there is nothing saved on server
            objectA = new ObjectA();
            objectA.setColor("blue");
            FlamebaseDatabase.syncReference(path);  // synchronize changes
        } else {
            objectA.setColor(ref.getColor());       // get up to date val
        }
    }

    /**
    * long server updates
    */
    @Override
    public void progress(int value) {
        Log.e(TAG, "loading " + path + " : " + value + " %");
    }

}, ObjectA.class);
```
Listener for maps:
```java
Map<String, Member> contacts = new HashMap<>();

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

- Database synchronization works through Firebase Cloud Messaging 

```java
public class FMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        FlamebaseDatabase.onMessageReceived(remoteMessage);
    }
}
```
