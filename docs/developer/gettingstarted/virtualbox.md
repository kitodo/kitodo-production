# Create VirtualBox appliance for Kitodo 3.x

## Download Debian 9.5 ISO file

Download `debian-9.5.0-amd64-netinst.iso` from <https://cdimage.debian.org/debian-cd/9.5.0/amd64/iso-cd/>

## Create Virtual Machine

* Name: `kitodo 3.0.0-beta.1`
* Type: `Linux`
* Version: `Debian (64-bit)`
* Memory size: `4096 MB`
* Hard disk: `VDI` / `dynamically allocated` / `20 GB`

## Virtual Machine settings

* General/Advanced/Shared clipboard: `Bidirectional`
* System/Processor/Processor(s): `2`
* System/Processor/Extended Features: `Enable PAE/NX`
* Display/Screen/Video Memory: `128 MB`
* Network/Adapter 1/Advanced/Port Forwarding/+
  * Host Port: `8080`
  * Guest Port: `8080`

## Start Virtual Machine

Select downloaded file `debian-9.5.0-amd64-netinst.iso`

## Debian install screen

* Select `Graphical install`
* Language: `English`
* Location: `United States`
* Keyboard: `German`
* Hostname: `kitodo`
* Domain: ` ` (blank)
* Root password: `kitodo`
* Full name: `kitodo`
* User name: `kitodo`
* User password: `kitodo`
* Time zone: `Eastern`
* Partitioning method: `Guided - use entire disk`
* Partitioning scheme: `All files in one partition`
* Mirror: `Germany/ftp.de.debian.org`
* Proxy: ` ` (blank)
* Software: deselect `print server`

## Install VirtualBox guest additions (shared clipboard) and reboot

```
su -c 'echo "deb http://ftp.debian.org/debian stretch-backports main contrib" > /etc/apt/sources.list.d/stretch-backports.list && apt update && apt install -y virtualbox-guest-dkms virtualbox-guest-x11 linux-headers-$(uname -r) && reboot'
```

## Install Kitodo

Follow the installation instructions in <https://github.com/kitodo/kitodo-production/wiki/Installationsanleitung-f%C3%BCr-Kitodo.Production-3.x>

Make sure to install Java 11 when using Kitodo.Production v3.4 and above, see [instructions](./development-version.md#install-openjdk-11).

## Create shortcuts

* Website link
```
echo '[Desktop Entry]
Encoding=UTF-8
Name=Kitodo.Production
Type=Link
URL=http://localhost:8080/kitodo/
Icon=text-html' >> ~/Desktop/Kitodo.Production.desktop
```
* Symlink to folders
```
ln -s /usr/local/kitodo ~/Desktop/kitodo-config
ln -s /var/lib/tomcat8/webapps/ ~/Desktop/kitodo-app
```

## Save password in Firefox

* Login at <http://localhost:8080/kitodo/> with user `testAdmin` and password `test
* Click `save` in popup dialog to let Firefox save this login

## Export Appliance

VirtualBox Manager / File / Export Appliance

* File: `kitodo-production-3.0.0-beta.1.ova`
* Product: `Kitodo Production`
* Product-URL: `http://www.kitodo.org`
* Version: `3.0.0-beta.1`
* Description:
```
This VirtualBox appliance is intended for development and tests in local networks. Do not use it in production mode!

The Kitodo.production webapp should be available from guest and host system (via NAT Port Forwarding) at:
* http://localhost:8080/kitodo/
* user: testAdmin
* pass: test

The appliance is based on debian 9.4, openjdk-8, tomcat8, mysql 5.7 and elasticsearch 5.x
* system user: kitodo, root
* system user password: kitodo
* system root password: kitodo
* mysql user: kitodo
* mysql user password: kitodo
* mysql root password: (blank)
```
* License: `GPLv3 https://www.gnu.org/licenses/gpl-3.0.en.html`
