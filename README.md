# :fire: flamebase-database-android

Real time JSON database (android client). Work with synchronized java objects stored as JSON objects

Before start using this lib, you have to initialize a flamebase database server cluster

### What is this?
Flamebase is an open source project that tries to emulate Firebase Database features as much as possible. I like Firebase but it's expensive for what it currently offers.
If you are doing an altruist project with Firebase, pray not to became successful, because the monthly amount will increase considerably.

In this repo you can find the proper lib for android client.
For now it still developing, so please be patient with errors and creation issues.

### Usage

- Import library:

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.flamebase:database:1.0.2'
}
```
- Init lib:
```java
FlamebaseDatabase.initialize(Context context, String cluster_ip, String token);
```
Listener for objects:
```java
final ObjectA objectA = new ObjectA();
objectA.setColor("blue");

FlamebaseDatabase.createListener(path, new ObjectBlower<ObjectA>() {

    @Override
    public ObjectA updateObject() {
        return objectA;
    }

    @Override
    public void onObjectChanged(ObjectA ref) {
        objectA.setColor(ref.getColor());
    }

    @Override
    public void progress(int value) {
        Log.e(TAG, "loading " + path + " : " + value + " %");
    }

}, ObjectA.class);

objectA.setColor("red");
FlamebaseDatabase.syncReference(path);
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
        for (Map.Entry<String, Member> entry : ref.entrySet()) {
            if (!contacts.containsKey(entry.getKey())) {
                contacts.put(entry.getKey(), entry.getValue());
            } else {
                contacts.get(entry.getKey()).setName(entry.getValue().getName());
                contacts.get(entry.getKey()).setEmail(entry.getValue().getEmail());
            }
        }
    }

    @Override
    public void progress(int value) {
        Log.e(TAG, "loading " + path + " : " + value + " %");
    }

}, Member.class);

Member member = new Member();
member.setName("pit");
member.setEmail("pit@hhh.com");
contacts.put(member.getName(), member);
FlamebaseDatabase.syncReference(path);
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
