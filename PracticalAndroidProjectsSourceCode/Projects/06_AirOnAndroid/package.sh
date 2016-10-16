#!/bin/bash
cd bin-debug
~/projects/cng/workspace/bocng/sdk/4.5.0/bin/adt -package -target apk-debug -storetype pkcs12 -storepass password -keystore ../cert.p12 06_AirOnAndroid.apk AndroidMain-app.xml AndroidMain.swf