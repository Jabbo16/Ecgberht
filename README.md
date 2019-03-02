<p align="center">
<img src="egbert.png" width="177" height="218" />
</p>

# Ecgberht

[![Build status](https://ci.appveyor.com/api/projects/status/ka5uam9blh1i8qtn?svg=true)](https://ci.appveyor.com/project/Jabbo16/ecgberht) [![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## Description

Ecgberht is a `Starcraft: Brood War` AI written in Java.
<p><img src="https://i.imgur.com/xUfYQ9H.png" width="48" height="48"/></p>

Currently participating in [SSCAIT](http://www.sscaitournament.com/) and [BASIL](http://basil.bytekeeper.org/ranking.html) ladders:

[Ecgberht Profile on SSCAIT](http://www.sscaitournament.com/index.php?action=botDetails&bot=Ecgberht)

Frameworks and libs used:

* [BWAPI4J](https://github.com/OpenBW/BWAPI4J)
* [BWAPI 4.2.0](https://github.com/bwapi/bwapi)
* [ASS](https://github.com/JavaBWAPI/ass)
* [Gson](https://github.com/google/gson)
* [JLayer](http://www.javazoom.net/javalayer/javalayer.html)

Main features:

* Only knows how to play Terran properly.
* Capable of executing different strategies. Prefers to play Bio oriented strategies (centered around Marines and Medics) but can also add a few mechanical units to the mix.
* Implements UCB-1 algorithm for learning best strategy to pick depending of opponent game history.
* Gaussian Mean Shift Clustering (GMS) for simulations.
* Can play different sounds.
* Configurable using `config.json` file. You can change some debug options, enable or disable sounds, etc.

## How to build

Requisites:

* [32-bits JDK8 (Java Development Kit)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

### Gradle

With Gradle there is no need of extra dependencies as a Gradle wrapper its included in the repository, just run execute the following command at Ecgberht root folder:

`./gradlew clean fatjar`

Ecgberht jar file will be generated at `build/libs` folder.

## How to run

`java.exe -jar Ecgberht.jar`

Its required to run the jar using the 32-bits java.exe executable.
