ChatApp
=======

An app we created for ELEC 602, designed to be used for P2P chatting.

Section 1: List of Functionality Ideas & Requirements

Requirements

Include more than one activity
Adhere to the “best practices” provided by Google
Adhere to the same message protocol to ensure compatibility between apps
More than two UI form elements
Should properly manage the android lifecycle

High Priority

List of clients online on start screen
Should be able to send message to other online clients
Should be able to receive a message regardless of whether or not app is in focus/running
Notification created when messages come in
Change display name (“Handle”)
Ability to scan and rescan for online users

Low Priority

Display previous chat messages in a conversation
When a message is received and a previous conversation with that handle has not been started, prompt user to begin this conversation
Lateral slide screen with online users list
Show online status of clients (i.e., red lights for offline clients, green light for online clients)

Section 2: Required Activities and Services

1. Splash Scan Activity
First thing that is run when the app is started
Displays a splash screen while it scans the network for online clients
It loads and updates the app database with user and possibly message information
It will handle the announcement to the network that the user is online
2. Main Activity
This activity is the ‘home’ activity for this app
Displays a list of online clients (and possibly offline clients from the database)
Will implement an onClickListener
When the user taps on a client in the list, a new intent will be created to start a chat activity
3. Chat Activity
One chat activity for each conversation taking place
Will display a list of messages to and from the client
i. Organized by time sent/received
ii. Similar in appearance/functionality to android text messaging app
Will contain a text box with send button to permit user to send messages
4. Network Message Service
Is responsible for receiving messages and sending messages
Will be started when app first starts, and will continue to run until app is explicitly quit
Will run in background regardless of whether app is in focus
Will update databases directly when message is sent/received
5. Network User Service
Is responsible for scanning for new online users, and updating the users database
Only needs to run at startup, and when Main Activity is running
6. Preferences Activity
Provides a means by which the user can edit their username, among other preferences
Preferences edited here will be visible to all activities via a SharedPreferences object

Section 3: Testing Considerations

Below is a list of all the features we intend to test, and how we plan to test them:

1. Adding a name to the online user list
What is Being Tested: The ability to dynamically update the list of online users
Specific Items Under Test: Adding/removing new elements to a listView when new users become online/offline.
How We Will Test It: To simulate users coming online, we added buttons. Whenever those buttons are clicked, we add/remove one entry, as if it was someone just became online offline.

2. Updating a preference
What is Being Tested: The functionality of the PreferenceActivity in our app
Specific Items Under Test: The sharedPreference listener on the preference activity, as well as the preference manager objects. The persistence of these changes is also being tested
How We Will Test It: We have added toast messages that are displayed when the listener for a given preference hears a change on the preference. Each toast message is unique to the preference being changed, so that we can confirm the correct listener is triggered for each preference. We will kill and restart the app several times to assure that the changes do persist in the app.

3. Sending a message
What is Being Tested: The ability to dynamically update the list of messages when a new message is sent.
Specific Items Under Test: Adding new elements to a listView when new messages are sent (the process is the same for incoming messages).
How We Will Test It: As we are not working with a network yet on this stage of the project, when the users click the Send Button, we just add the user message to the sequence of displayed messages on the list.


