CLI Chat Room Implementation Report
======

Reliable Unicast
-----
Messages are sent through reliable unicast, which uses UDP but checks for returning ACK messages.
To simulate real network, each packet is delayed with a mean delay and max deviation, configurable in the config file.
Further, each pocket might be dropped, i.e. not sent in the first, with a probability that is configurable as well.
Whenever a message is sent, the sender will wait for an `ACK` message with the digest of original message.
If it times out, the original message will be sent to the receiver again.

Multicast
-----
A total of four types of multicast are implemented - `BASIC_MULTICAST`, `RELIABLE_MULTICAST`, `RELIABLE_CAUSAL_ORDERING` and `RELIABLE_TOTAL_ORDERING`.

### Basic Multicast ###
Send the message to every member in the group, which is defined in the config file.

Some correct members may not receive some messages due to network losses.

### Reliable Multicast ###
Initiator b-multicasts the message to every member in the group (including itself), which is defined in the config file.
All other members will b-multicast the message to the whole group again if it has not received this message, before delivering the message itself.

R-multicast guarantees all correct members will deliver the message.

### Causal Ordering Multicast ###
Each member is attached with a vector timestamp begining at 0's.
The initiator `j` b-multicast the message to the whole group, including itself, with an increment timestamp.
The receiver `i` will hold back the message and wait till `V_j[j] = V_i[j]` and `V_j[k] <= V_i[k]` (`k != j`). Then `i` can CO-deliver the message, and increment the timestamp: `V_i[j] = V_i[j] + 1`.

CO-multicast guarantees that if multicast(g,m) happens before multicast(g,m’) then any correct process that delivers m’ will have already delivered m.

### Total Ordering Multicast ###
There will be a special member other than preset members call sequencer.
The sequencer keeps a lamport timestamp that starts with 0. Whenever it receives a message, it immediately b-multicasts the message to everyone else. Then, it will increment its timestamp.
Each regular member is attached with a lamport timestamp that starts with 0. The initiator just b-multicasts the message to everyone including sequencer and itself. On receiving message from other regular members, it will always hold back the message. When it receives sequencer's message, it waits till its own timestamp is equal to the attached sequence in the message and delivers the message. Increment its timestamp afterwards.

TO-multicast guarantees that if a correct process delivers message m before m’ (independent of the senders), then any other correct process that delivers m’ will have already delivered m.
