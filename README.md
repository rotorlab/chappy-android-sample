<p align="center"><img width="10%" vspace="20" src="https://github.com/rotorlab/chappy-android-sample/raw/develop/app/src/main/res/mipmap-xxxhdpi/ic_launcher_rounded.png"></p>

# Chappy 
**Proof of Concept** app about the use of Rotor libraries (Core, Database and Notifications). Chappy has a **mvp architecture** for interacting dynamically between Rotor data sources and views.

The repository goal is to show a reactive engine, where every data change involves an interface change. Everything with the minimum number of requests.

Clone the repository and open the project in Android Studio:
```bash
git clone https://github.com/rotorlab/chappy-android-sample.git
```

### Run Chappy
All you need to start Chappy is running a [Rotor server-node](https://github.com/rotorlab/server-node) (and that's all). Data source model is defined in Android client, no logic is needed in back-end side.

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

 
<p align="center"><img width="30%" vspace="20" src="https://github.com/rotorlab/chappy-android-sample/raw/develop/sample1.png"></p>


License
-------
    Copyright 2018 RotorLab Organization

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
