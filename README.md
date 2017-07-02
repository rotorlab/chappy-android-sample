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

    @Override
    public String getTag() {
        return path + "_sync";
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

    @Override
    public String getTag() {
        return path + "_sync";
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


### Sample GChat class

```java
public class GChat {

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("members")
    @Expose
    List<String> member;

    @SerializedName("messages")
    @Expose
    Map<String, Message> messages;

    public GChat(String name, List<String> member, Map<String, Message> messages) {
        this.name = name;
        this.member = member;
        this.messages = messages;
    }

    public List<String> getMember() {
        return member;
    }

    public void setMember(List<String> member) {
        this.member = member;
    }

    public Map<String, Message> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, Message> messages) {
        this.messages = messages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```
```java
public class Message {

    @SerializedName("author")
    @Expose
    String author;

    @SerializedName("text")
    @Expose
    String text;

    public Message(String author, String text) {
        this.author = author;
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
```

