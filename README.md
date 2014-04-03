CLI Chat Room
================
This is a command line chat room that supports causal ordering and total ordering when delivering messages. Each client can be run on different machines as long as the IP's and identifiers are known beforehand. Messages are sent through UDP sockets which are made reliable by ACK implementation.

Configuration
-----
Here is a sample configuration file with all configurable parameters.

There are four options for types of multicast, namely `BASIC_MULTICAST`, `RELIABLE_MULTICAST`, `RELIABLE_CAUSAL_ORDERING` and `RELIABLE_TOTAL_ORDERING`. Note that when using `RELIABLE_CAUSAL_ORDERING`, `vectorStamp` has to be `true`; `RELIABLE_TOTAL_ORDERING `, `false`.

```json
{
    "vectorStamp": true,
    "roomSize": 3,
    "portBase": 60000,
    "interactive": true,
    "meanDelay": 50,
    "delayDeviation": 50,
    "nonInteractiveMsgInterval": 5000,
    "members": [
        {
            "identifier": 0,
            "ip": "localhost"
        },
        {
            "identifier": 1,
            "ip": "localhost"
        }
    ],
    "multicastType": "RELIABLE_TOTAL_ORDERING"
}
```

Usage
------
To run `ChatRoom.jar`:

```bash
java -jar dist/ChatRoom.jar CONFIG_FILE
```

To run `Member.jar`:

```bash
java -jar dist/Member.jar CONFIG_FILE MEMBER_IDENTIFIER
```

### Non-interactive Demo ###

In `dist/config.json`, toggle `interactive` to `false`.

```bash
java -jar dist/ChatRoom.jar dist/config.json
```

### Interactive Demo ###

In `dist/config.json`, toggle `interactive` to `true`.

If the `multicastType` is `RELIABLE_TOTAL_ORDERING`, you have to run the chat room first:

```bash
java -jar dist/ChatRoom.jar dist/config.json
```

Next, you need to start THE SAME NUMBER of members as is set in `dist/config.json`, each in a separate terminal, and fill in the corresponding `IDENTIFIER` you set in the config file. For example, with the default config file, you need to run:

```bash
java -jar dist/Member.jar dist/config.json 0
```

in one terminal, and

```
java -jar dist/Member.jar dist/config.json 1
```

in another.

Now your two terminals can chat. :]