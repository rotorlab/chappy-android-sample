# :fire: flamebase-database-android
Real time JSON database (android client).

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
private GChat chat;

// onCreate..

FlamebaseDatabase.initialize(this, server_cluster_ip, FirebaseInstanceId.getInstance().getToken());

FlamebaseDatabase.createListener(path, new FlamebaseDatabase.FlamebaseReference<GChat>() {
    @Override
    public void onObjectChanges(GChat value) {
        if (chat == null) {
            chat = value;
        } else {
            chat.setName(value.getName());
            chat.setMessages(value.getMessages());
            chat.setMember(value.getMember());
        }
        messageList.getAdapter().notifyDataSetChanged();
    }

    @Override
    public GChat update() {
        return chat;
    }

    @Override
    public void progress(String id, int value) {

    }

    @Override
    public String getTag() {
        return path + "_sync";
    }

    @Override
    public Type getType() {
        return new TypeToken<GChat>(){}.getType();
    }
});

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

