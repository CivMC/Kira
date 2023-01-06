# Kira
Kira is essentially a Discord Bot that uses KiraBukkitRelay to communicate with a Minecraft server. Kira uses a PostgreSQL database to store Kira user permissions and uses RabbitMQ to communicate with KiraBukkitRelay which is a Minecraft plugin. 

Kira allows you to seamlessly communicate with your Minecraft server on Discord. Uses include role-based authentication on Discord by requiring a link to an in-game Minecraft account, relaying in-game information such as JukeAlert messages to Discord, running commands on your Minecraft server from Discord, or even running console commands as an admin from Discord. Kira empowers admins to monitor and moderate their Minecraft server from the comfort of Discord instead of their server terminal. 

Note that some features are broken and updates to Discord API's can cause further issues. Kira is also restricted by Discord's rules around gateway intents and bot permissions. 

Note that all Kotlin code is licensed under MIT, while Java code is licenced under BSD-3

## Setting up a Kira development environment

1. Install Java 11: `https://www.oracle.com/java/technologies/javase-jdk11-downloads.html` (Recommended, login necessary)

	* or OpenJDK 11: `https://adoptopenjdk.net/`

	* *other Java providers are available, but it's recommended to avoid them unless you know what you are doing.*

2. Install git: `https://git-scm.com/downloads`

3. Install Maven: `https://maven.apache.org/download.cgi`

	* You'll find it easier to install via package managers, such as:

		* Windows: `choco install maven` (Chocolatey: `https://chocolatey.org/install`)

		* macOS: `brew install maven` (Homebrew: `https://brew.sh/#install`)

		* Linux: `sudo apt install maven`

4. Install PostgreSQL: `https://www.postgresql.org/download/`

	* You may find it easier to install via package managers, such as:

		* Windows: `choco install postgresql` (Chocolatey: `https://chocolatey.org/install`)

		* macOS: `brew install postgresql@9.5` (Homebrew: `https://brew.sh/#install`)

	* You may need to manually start PostgreSQL as a service after install.

5. Install a PostgreSQL client: `https://wiki.postgresql.org/wiki/PostgreSQL_Clients`

	* DataGrip: `https://www.jetbrains.com/datagrip/`

	* pgAdmin: `https://www.pgadmin.org/download/`

6. Install RabbitMQ: `https://www.rabbitmq.com/download.html`

	* You may find it easier to install via package managers, such as:

		* Windows: `choco install rabbitmq` (Chocolatey: `https://chocolatey.org/install`)

		* macOS: `brew install rabbitmq` (Homebrew: `https://brew.sh/#install`)

	* You may need to manually start RabbitMQ as a service after install.

	* You'll need to create a user for Kira to use:

		* Navigate to RabbitMQ's Manager: `http://localhost:15672/`

		* Login with default user:

			* Username: `guest`

			* Password: `guest`

		* Select the "Admin" tab.

		* In the "Add a user" section at the bottom of the page, type in the
	      desired credentials, ensuring that the tags field contains
		  `administrator`.

		* Click the "Add user" button.

		* Click on the newly created user in the "All users" section by
	      clicking the account's name.

		* Expand the "Permissions" section and click the "Set permission"
	      button to grant the user default permissions. Do the same thing with
		  the "Topic permissions" section.

	* You'll then need to create the message queues for Kira to use:

		* Navigate to RabbitMQ's Manager: `http://localhost:15672/`

		* Login to the Kira user you created (or any other administrator user).

		* Select the "Queues" tab.

		* In the "Add a new queue", input the following information:

			* Type: `classic`

			* Name: `kira-to-gateway`

			* Durability: `transient`

			* Auto Delete: `No`

		* Add another queue but with the name: `gateway-to-kira`

7. Set up a test Minecraft server:

	* You could clone Civclassic's setup: `https://github.com/CivClassic/AnsibleSetup`

	* Or use a quick and dirty throwaway server: `https://github.com/Protonull/TestServer`

	* Ensure you have the following plugins on the server (if you're not using
	  the AnsibleSetup, you may need to restart the server multiple times to
	  generate all the default configs to fill out):

		* CivChat2

		* Citadel

		* CivModCore

		* JukeAlert

		* KiraBukkitGateway

		* LuckPerms

		* NameLayer

	  You can download these jars from: `https://github.com/CivClassic/AnsibleSetup/tree/master/jarfiles/public/plugins`

8. Create a Discord bot:

	* Navigate to `https://discord.com/developers/applications`

	* Login to your Discord account.

		* You may need to navigate back to `https://discord.com/developers/applications`
		  after login if Discord sends you to the in-browser app.

	* Click the "New Application" button and give it a name.

	* On the Application's screen, click the "Bot" link on the left-hand side.

	* Click the "Add Bot" button.

	* On the Bot screen, click the "Copy" button underneath "Click to Reveal
	  Token". Note the Token down somewhere since you'll use it later.

	* Ensure you enable "Developer Mode" for your personal Discord account by
	  going to `Settings -> Advanced` in your Discord client and toggling the
	  option to true.

9. Create a Kira execution environment:

	* Clone this repository and build it: `mvn clean package`

	* Copy the resulting `<cloned repo>/target/Kira-<version>.jar` into an empty
	  folder outside the repo. Call it `KiraSetup` for example.

	* Inside the `KiraSetup` folder, create a `config.json` file:

	```json
	{
		"db": {
			"host": "localhost",
			"port": 5432,
			"database": "kira",
			"user": "<username>",
			"password": "<password>"
		},
		"rabbitmq": {
			"host": "localhost",
			"port": 5672,
			"user": "<username>",
			"password": "<password>",
			"incomingQueue": "gateway-to-kira",
			"outgoingQueue": "kira-to-gateway"
		},
		"bot": {
			"serverid": 0,
			"authroleid": 0,
			"token": "<token>"
		},
		"api": {
			"address": "localhost",
			"port": 80,
			"sslCertPassword": null,
			"sslCertPath": null
		},
		"consoleforward": {
		"anticheat":584794001215979521,
		"bans":585524000092848129
		},
		"apirate": "500ms",
		"commandprefix": "!kira",
		"relayCategory": 0
	}
	```
	
	* Config values:

		* `bot.serverid` is the server id of the bot's primary residence, like
		  how Kira's is the Civclassic's Official Discord. You may need to
		  create a new Discord server just for this purpose. Right-click the
		  desired Discord server's icon in the server list and click "Copy ID"
		  then paste it into the `bot.serverid` value.

		* `bot.authroleid` is the id number of the role that Kira assigns people
		  when they're authenticated. Open up the server's settings and click
		  the "Roles" link on the left-hand side. Create a new cosmetic role if
		  you don't already have one. Find that roll in the list, right-click
		  it, and click "Copy ID" and then paste it into the `bot.authroleid`
		  value.

		* `bot.token` paste the bot token you copied during Part 8.

		* `db.user` should be your system account.

		* `db.password` should be your system account's password.

		* `consoleforward` are a key and the channel ID's you are relaying data to and relate to the 'key' values in KiraBukkitRelay
		
		* `relayCategory` is the ID of the category the relay channels are in, although I don't know why or for what purpose

	* Setup Kira's database:

		* Open your PostgreSQL client and login with your system account.

		* Ensure that the "kira" database exists: `CREATE DATABASE kira;`

		* Execute the Kira jar once to begin the database migration: `java -jar Kira-<version>.jar`

		* Inside your PostgreSQL client, navigate to the "kira" database and its tables.

		* Select the "user" table and add a new entry:

		  You can retrieve your own discord user id by right-clicking any message
		  you've sent, or your profile in the member's view on the right-hand side,
		  and clicking "Copy ID".

		  You can retrieve your Minecraft account's uuid by using `https://namemc.com/`
		  (use the version of the uuid with dashes).

		  id|discord_id|name|uuid
		  ---|---|---|---
		  1|\<discord user id\>|\<minecraft name\>|\<minecraft uuid\>

		* Select the "role_members" table and add yourself with the following entry:

		  user_id|role_id
		  ---|---
		  1|2
		  
		  `2` *should* be the ID of the admin role. You can verify by viewing the "roles" table.  

10. You *should* be good to go.
