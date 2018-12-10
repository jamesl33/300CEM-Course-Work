# Earthquake Mapper
Earthquake Mapper is an earthquake data visualisation tool built during my third year
at Coventry University for the 300CEM module. It contains two major ways to visualise
earthquake data:
- Marker Mode - Place markers of varying sizes on the location of each earthquake.
- Heat map Mode - Generate a heat map to effectively view the concentration of earthquakes.

# Features
- Marker visualisation
    - Customisable focused/unfocused coloured markers
    - Restrict the amount of earthquakes processed
    - Focus on the current earthquake
    - Move to next earthquake (Chronological order)
    - Move to previous earthquake (Reverse chronological order)
    - Mark my current location (Using the devices' GPS)
    - Shake the device to move to a random earthquake
- Heat map visualisation
    - Mark my current location (Using the devices' GPS)
    - Restrict the amount of earthquakes processed
- Database
    - Asynchronous auto updating database (Using the USGS web api)
    - Only contains the most significant earthquakes from 1950 to the present day (Currently about 20,000)

# Testing
To run the testing:
- Import the project into Android Studio
- Right click on 'app/java/lee.james.earthquakemapper (androidTest)'
- Click run tests in this folder

# License
Earthquake Mapper is free software and you are free to modify and distribute it under the terms
of the GPLv3 license.