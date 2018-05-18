<p align="center">
<img src="egbert.png" width="177" height="218" />
</p>

# Ecgberht

[![Build status](https://ci.appveyor.com/api/projects/status/ka5uam9blh1i8qtn?svg=true)](https://ci.appveyor.com/project/Jabbo16/ecgberht) [![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## Description

Ecgberht is a `Starcraft: Brood War` AI written in Java.
<p><img src="https://i.imgur.com/xUfYQ9H.png" width="48" height="48"/></p>

Frameworks used:

* [BWAPI4j](https://github.com/OpenBW/BWAPI4J)
* [BWAPI 4.2.0](https://github.com/bwapi/bwapi)

Main features:

* Only knows how to play Terran properly.
* Capable of executing different strategies. Prefers to play Bio oriented strategies (centered around Marines and Medics) but can also add a few mechanical units to the mix.
* Implements UCB-1 algorithm for learning best strategy to execute depending of opponent and game history.
* Can play different sounds.
* Configurable using `config.json` file. You can change some debug options, enable or disable sounds, etc.

## How to build

Requisites:

* [32-bits JDK8 (Java Development Kit)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Apache Ant (optional)](https://ant.apache.org/bindownload.cgi)

### Apache Ant

Just run the following command, recommended having Ant binaries at Path:

`ant clean jar`

Ecgberht jar file will be generated at `build/jar` folder.

### Gradle

With Gradle there is no need of extra dependencies as I included a Gradle wrapper, just run execute the following command at Ecgberht root folder:

`./gradlew clean fatjar`

Ecgberht jar file will be generated at `build/libs` folder.

## How to run

`java.exe -jar Ecgberht.jar`

Its required to run the jar using the 32-bits java.exe executable.