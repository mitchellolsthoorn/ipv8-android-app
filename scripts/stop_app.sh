#!/bin/bash

echo Stop app
$ADB shell am start -n org.internetofmoney.android/.MainActivity -a android.intent.action.ACTION_SHUTDOWN
