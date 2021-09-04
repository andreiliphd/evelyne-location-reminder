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



## Usage
Compile and run.

---