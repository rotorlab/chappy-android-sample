<p align="center"><img width="10%" vspace="20" src="https://github.com/rotorlab/chappy-android-sample/raw/develop/app/src/main/res/mipmap-xxxhdpi/ic_launcher_rounded.png"></p>

Chappy: sample of real-time changes
-------------------------------------------
 
Sample app of the use of Rotor libraries (Core and Database). Clone the repo and open the project in Android Studio:
```bash
git clone https://github.com/rotorlab/chappy-android-sample.git
```
### Datamodel sample
```java
public class Chat {

    @SerializedName("name")
    @Expose
    String name;

    @SerializedName("members")
    @Expose
    Map<String, Member> members;

    @SerializedName("messages")
    @Expose
    Map<String, Message> messages;

    public Chat(String name, Map<String, Member> members, Map<String, Message> messages) {
        this.name = name;
        this.members = members;
        this.messages = messages;
    }
    
    /* getter and setter methods */
    
}
```
### Interaction sample
Define a chat listener and add messages:
```java
private Chat chat;

class ChatActivity ..

@Override protected void onCreate(Bundle savedInstanceState) {
    
    final String path = "/chats/welcome_chat";
    
    /* object instances, list adapter, etc.. */
    
    Database.listen(path, new Reference<Chat>(Chat.class) {
    
        @Override public void onCreate() {
            chat = new Chat();
            chat.setTitle("Foo Chat");
            Database.sync(path);
        }
            
        @Override public Chat onUpdate() {
            return chat;
        }
    
        @Override public void onChanged(Chat chat) {
            ChatActivity.this.chat = chat;
            
            // update screent title
            ChatActivity.this.setTitle(chat.getName());
            
            // order messages
            Map<String, Message> messageMap = new TreeMap<>(new Comparator<String>() {
                @Override public int compare(String o1, String o2) {
                    Long a = Long.valueOf(o1);
                    Long b = Long.valueOf(o2);
                    if (a > b) {
                        return 1;
                    } else if (a < b) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            messageMap.putAll(chat.getMessages());
            chat.setMessages(messageMap);
    
            // update list
            messageList.getAdapter().notifyDataSetChanged();
            messageList.smoothScrollToPosition(0);
        }
    
        @Override public void progress(int value) {
            // print progress
        }
    
    });
     
    sendButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
            SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            String username = prefs.getString("username", null);
            if (name != null) {
                Message message = new Message(username, messageText.getText().toString());
                chat.getMessages().put(String.valueOf(new Date().getTime()), message);
        
                Database.sync(path);
        
                messageText.setText("");
            }
        }
    });
}
```
You can do changes or wait for them. All devices listening the same object will receive this changes to stay up to date:
 
<p align="center"><img width="30%" vspace="20" src="https://github.com/rotorlab/chappy-android-sample/raw/develop/sample1.png"></p>


License
-------
    Copyright 2018 Efraín Espada

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
