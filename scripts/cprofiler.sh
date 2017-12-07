#!/bin/bash

echo Clean logcat
$ADB logcat -c

echo Start twistd
$ADB shell am start -n org.internetofmoney.android/.TwistdActivity
