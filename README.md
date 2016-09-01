# MinecraftSmartHomeServer
Minecraft Server (Bukkit/Spigot) SmartHome Extension for the use with Eclipse SmartHome, openHAB, and QIVICON via REST and SSE (Server Sent Events).

-- Preparing Raspberry Pi

(see https://www.raspberrypi.org/documentation/)

1. Get Raspberry 2 or higher and a fast SD-Card with min. 8GB
2. Download "Raspbian Jessie Lite" archive https://www.raspberrypi.org/downloads/raspbian/
3. Unzip/Extrace image
4. Get an SD Card writer like "Win32 Disk Imager" (http://sourceforge.net/projects/win32diskimager/files/latest/download)
5. Write image to SD Card
6. INsert SD Card to Raspberry and start it
7. Connect to Raspberry either via telnet (Putty) or directly
8. Login is pi raspberry

-- Configure Raspberry Pi

(see https://www.raspberrypi.org/documentation/)

1. Start and Connect to Raspberry Login as pi raspberry
2. sudo raspi-config
3. Expand Filesystem
4. Advanced Options -> Memory Split (Assign the minimum amount of memory to the GPU)
4. Advanced Options -> Update
5. Advanced Options -> Hostname (e.g. mcserver)
6. Overclock 1000Mhz

-- Wifi

(see https://www.raspberrypi.org/documentation/configuration/wireless/wireless-cli.md)

-- Installing Minecraft Server Spigot

(see https://jankarres.de/2013/04/raspberry-pi-minecraft-server-bukkitspigot-installieren/ and http://lemire.me/blog/2016/04/02/setting-up-a-robust-minecraft-server-on-a-raspberry-pi/)

1. sudo apt-get install git netatalk screen
2. sudo apt-get install oracle-java8-jdk
3. mkdir minecraft, cd minecraft
4. wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
5. java -Xmx1G -jar BuildTools.jar --rev 1.8.8
6. start via java -jar -Xms512M -Xmx1008M spigot-1.8.8.jar nogui
7. edit generated eula.txt: eula=true
8. finally start again java -jar -Xms512M -Xmx1008M spigot-1.8.8.jar nogui

-- Optimizing Minecraft Server Spigot

1. See slides for server.properties
   gamemode=1
   online-mode=false
   server-port=25565
2. spigot.yml: view-distance: 5
3. /etc/default/tmpfs: RAMTMP=yes   
4. minecaft.sh (with chmod +x)
   
if ! screen -list | grep -q "minecraft"; then
  cd /home/pi/minecraft
  while true; do
    screen -S minecraft -d -m java -jar  -Xms512M -Xmx1008M spigot-1.9.jar nogui  && break
  done
fi

5. start via ./minecraft.sh
6. access shell screen -r minecraft (exit via ctrl-a d)
7. add to rc.local

sudo nano /etc/rc.local

add right before the exit command

su -l pi -c /home/pi/minecraft/minecraft.sh

8. stopping the server
screen -r minecraft
stop
sudo shutdown -h now

-- Plug-ins
1. Usual way





