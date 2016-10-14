# MinecraftSmartHomeServer

"Minecraft SmartHome Server" is a plug-in for Minecraft servers like Bukkit or [Spigot](https://www.spigotmc.org/wiki/spigot/). It provides a RESTful interface which is based on HTTP POST and GET. Furthermore it supports SSE (Server Sent Events).

## Smart Home Integration

Even if you could use the "Minecraft SmartHome Server" directly via REST, we have a more convenient option using smart home solutions like Eclipse [SmartHome](http://www.eclipse.org/smarthome/), [openHAB](http://www.openhab.org/), or [QIVICON](https://www.qivicon.com/en/) called **[MinecraftSmartHomeBinding](https://github.com/wolter/MinecraftSmartHomeBinding)**

## Usage

Default port is `9998` (see [Configuration](#configure-plug-in))
* GET http://[HOST]:[PORT]/rest/hello -> String with "Hello, world!" (just for testing purpose)
* GET http://[HOST]:[PORT]/rest/things -> JSON with list of Thing including current state
* GET http://[HOST]:[PORT]/rest/things/[UID] -> JSON with Thing including current state
* GET http://[HOST]:[PORT]/rest/things/[X]/[Y]/[Z] -> JSON with Thing including current state
* POST http://[HOST]:[PORT]/rest/commands/execute/,Request Body ThingCommand
* SSE http://[HOST]:[PORT]/rest/events

## Setup

First of all you need a running server. This could be on you local machine, on your local network or in the Web. In case you do not have a server, here is a sample description for a Raspberry Pi. There's a comprehensive description on how to install and configure a Raspberry Pi at https://www.raspberrypi.org/documentation/. Anyway, here are the essential steps again...

### Preparing Raspberry Pi

1. Get Raspberry 2 or higher and a fast SD-Card with min. 8GB
2. Download ["Raspbian Jessie Lite" archive](https://www.raspberrypi.org/downloads/raspbian/)
3. Unzip/Extrace image
4. Get an SD Card writer like [Win32 Disk Imager](http://sourceforge.net/projects/win32diskimager/files/latest/download)
5. Write image to SD Card
6. INsert SD Card to Raspberry and start it
7. Connect to Raspberry either via telnet (Putty) or directly
8. Login is pi raspberry

### Configure Raspberry Pi

1. Start and Connect to Raspberry Login as pi raspberry
2. sudo raspi-config
3. Expand Filesystem
4. Advanced Options -> Memory Split (Assign the minimum amount of memory to the GPU)
4. Advanced Options -> Update
5. Advanced Options -> Hostname (e.g. mcserver)
6. Overclock 1000Mhz
7. Wireless usage is described [here](https://www.raspberrypi.org/documentation/configuration/wireless/wireless-cli.md)

### Installing Minecraft Spigot Server

There are plenty of how-tos on the installation of a Minecraft server. I.e. this English one [here](http://lemire.me/blog/2016/04/02/setting-up-a-robust-minecraft-server-on-a-raspberry-pi/) and a German one [here](https://jankarres.de/2013/04/raspberry-pi-minecraft-server-bukkitspigot-installieren/). Usually you need the follwoing steps:
1. `sudo apt-get install git netatalk screen`
2. `sudo apt-get install oracle-java8-jdk`
3. `mkdir minecraft`, `cd minecraft`
4. `wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar`
5. `java -Xmx1G -jar BuildTools.jar --rev 1.8.8`
6. start via `java -jar -Xms512M -Xmx1008M spigot-1.8.8.jar nogui`
7. edit generated eula.txt: `eula=true`
8. finally start again via `java -jar -Xms512M -Xmx1008M spigot-1.8.8.jar nogui`

### Optimizing Minecraft Spigot Server

We recommend the following sttings fpr testing and performance purpose in the `server.properties` file
* `gamemode=1`
* `online-mode=false`
* `server-port=25565`

For the `spigot.yml` yaml-file we suggest:
* `view-distance: 5`
* `/etc/default/tmpfs: RAMTMP=yes`

Finally a start file is needed. You could name it `start.sh` or `minecaft.sh` (don't forget to assign execution rights with `chmod +x`). Using Linux the content might look like this:
```Bash
if ! screen -list | grep -q "minecraft"; then
  cd /home/pi/minecraft
  while true; do
    screen -S minecraft -d -m java -jar  -Xms512M -Xmx1008M spigot-1.9.jar nogui  && break
  done
fi
```
Now, start your server via `./minecraft.sh`. You can access the shell of the server via `screen -r minecraft`. You exit the shell via _ctrl + a, d_ (please note, _ctrl + c_ kills the whole server).

If you like to start your server automatecally, add the following line to `rc.local` (via `sudo nano /etc/rc.local`) right right before the exit command.
```bash
su -l pi -c /home/pi/minecraft/minecraft.sh
```
Stopping the server will be achived through:
1. Change to the Minecraft server's shell `screen -r minecraft`
2. Stop the Minecraft server `stop`
3. Shutdown the whoel Rapsberry Pi `sudo shutdown -h now`

## Install Plug-in

Just place the generated SmartHome.jar file in your servers plug-in folder (usually `server/spigot/plugins/`). After re-starting the server, you should see "[SmartHome] Plugin enabled." on the console.

## Configure Plug-in

You find the configuration of this plug-in in the plug-in's folder (usually `server/spigot/plugins/SmartHome/`) named config.yml. The only available setting so far is the port number (default is `9998`).

## Hints

If there's something going wrong with connectivity, make sure, the server has an assigned network address before you start the Minecraft server. You can see the assigned and detected address in your Minecraft server's output on the console similar to this: "[SmartHome] IP is 192.168.178.36". 

In the rare case something is going wrong, you can always edit or delete the list of Minecraft things maintained by this plug-in. You find this list in the plug-in's folder (usually `server/spigot/plugins/SmartHome/`) named `devices.json`. Please note, you need to stop the Minecraft server first, otherwise your changes will be overwritten immediately.
