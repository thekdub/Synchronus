# Synchronus

## What does it do?
Synchronus is a Minecraft plugin that allows server administrators to periodically update a database with values found in .yml files on the server.

All versions of Minecraft should be supported; however this has only been tested on Minecraft 1.2.5.

Note: Synchronus only supports .yml files at this time.

### Example application:
Scrubbing Essentials user data for account balance, user connection timestamps, and nicknames; uploaded to a database for use in other applications.


## What does it not do?
Synchronus does not update server-side files from the database.


## How does Synchronus store data?
Synchronus attempts to store data in the format found when retrieving it (Integer, Long, Double, Boolean, String). If Synchronus encounters a list, the list will be converted to a JSON array, then entered into the database as text. If Synchronus encounters a configuration section, the entire section is recursively converted to a JSON object, then stored in the database as text.


## What does Synchronus require?
Synchronus requires nothing more than a Minecraft server that supports Bukkit plugins, Java 1.8+, and an appropriate MySQL database to connect to.

Note: Database table creation is not currently supported by Synchronus. Please create and configure your tables as appropriate before using Synchronus.


## Can I use Synchronus on my server?
Yes, you are welcome to use Synchronus on your server. This is at your own risk; I am not liable if Synchronus causes any data loss, performance issues, etc...


## Can I modify Synchonus?
You may modify Synchronus as long as appropriate credit is given to the original creator (myself).