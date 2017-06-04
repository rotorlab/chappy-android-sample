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
- Database synchronization works through Firebase Cloud Messaging 

```java
public class FMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        refreshGroupChat(getApplicationContext(), remoteMessage);
    }
    
    public static void refreshGroupChat(Context context, RemoteMessage remoteMessage) {
        
        new RealtimeDatabase<GContact>(context, GChat.class, remoteMessage) {

            @Override
            public void onObjectChanges(final GChat value) {
                
            }

            @Override
            public void progress(String id, int value) {
                
            }

            @Override
            public String getTag() {
                return "chat_sync";
            }
        };
    }
}
```

