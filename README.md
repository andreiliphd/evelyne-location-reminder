# Evelyne - Location Reminder
============

Geofencing android application that reminds you when you enter area that you marked before.

---

## Features
- Location
- Geofencing
- Google Maps

---

## Setup
Clone this repo:
```
git@github.com:andreiliphd/evelyne-location-reminder.git
```
Install all the dependencies.
Copy `google-services.json` into app directory.
In `local.properties` enter the Google Maps SDK API key like in this example.
```
MAPS_API_KEY=AIzaSyAqBewn-aBdn5jc_Dd5aF8Gel8alLfZFe8
```
---

## Testing
1. In `androidTest` we mainly use Espresso to test UI and vital components of application that requires Android running.
2. In `test` we mainly use Roboelectric to unit test components that don't require Android running.
---

## Remote build
If you want to accelerate building Android application I recommend using [Mirakle](https://github.com/Adambl4/mirakle).
Create following file in `USER_HOME/.gradle/init.d/mirakle_init.gradle`:
```
initscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'io.github.adambl4:mirakle:1.4.3'
    }
}

apply plugin: Mirakle

rootProject {
    mirakle {
        host "your_remote_machine"
    }
}
```
If you are under Windows make sure to download Cygwin and use ssh and rsync from Cygwin.


---


## Usage
Compile and run.

---