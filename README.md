# Scrcpy Controller
[![JetBrains plugins](https://img.shields.io/jetbrains/plugin/d/14565)](https://plugins.jetbrains.com/plugin/14565-scrcpy) [![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/14565)](https://plugins.jetbrains.com/plugin/14565-scrcpy) [![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/r/rating/14565)](https://plugins.jetbrains.com/plugin/14565-scrcpy)

IntelliJ/Android Studio plugin for running and managing [scrcpy](https://github.com/Genymobile/scrcpy) and adb devices (USB & WiFi both)

## Why
I am an Android Developer myself and I always use scrcpy ❤ to test my work on my phone.I feel lazy to launch scrcpy from terminal everytime. So I thought why not make a plugin for this so it would help all the Android
 Developers!

## Exclusive Features
- One Click to switch USB Device to WiFi
- Run scrcpy on multiple devices effortlessly
- Disconnect WiFi Devices
- Connect WiFi Devices
- All options from [scrcpy v1.17](https://github.com/Genymobile/scrcpy) are configurable from the UI
- Quick *Shortcuts* button for you to remember scrcpy shortcuts
- Easy installation, scrcpy does not need to be added in your system's PATH Variable
- *Shortcuts* are directly loaded from scrcpy's GitHub README if you are connected to the internet, else static shortcuts are loaded from scrcpy v1.16

## Requirements
1. Works on Android Studio 4.0+ and other IntelliJ-based IDEs with build number `193.4099.13` and above 
2. `adb` and `scrcpy` installed (need not be configured in `PATH`)

## Installation
Install the plugin in Android Studio/IntelliJ-based IDE via:
#### [JetBrains Plugins Marketplace](https://plugins.jetbrains.com/plugin/14565-scrcpy)
   - Search for 'scrcpy Controller', install, and restart IDE

#### Manually
   - Download latest plugin zip (should contain latest version in file name e.g. 0.1.0) from [releases](https://github.com/shripal17/ScrcpyController/releases/latest)
   - From Android Studio/IntelliJ-based IDE, open Settings -> Plugins -> Settings Icon -> Install plugin from disk and select the downloaded plugin zip -> Restart IDE
1. Download and extract [scrcpy](https://github.com/Genymobile/scrcpy) to any folder of your choice
2. Go to IDE Settings -> Tools -> Scrcpy Controller -> Enter/choose the location of the previously extracted scrcpy release -> Click on *Test* button to verify selected/entered path -> *Ok* button

### If you like this plugin, please don't forget to leave a rating on the [JetBrains Plugins Marketplace](https://plugins.jetbrains.com/plugin/14565-scrcpy/reviews)

## Bonus
- All options visible in the UI excluding the devices table (obviously) persist their values across IDE Restarts!
- The `To WiFi` button will automatically try to switch your USB-connected device to WiFi adb by:
    1. Restarting adbd in tcpip mode (uses the port entered in `ADB WiFi` section or `5555` (default))
    2. Extract device's local WiFi IP and run's `adb connect` on the extracted IP
- You can configure advanced, rarely-used options for scrcpy from IDE Settings -> Tools -> Scrcpy Controller
- Devices with `scrcpy` currently running have a `•` added to their serial

## Screenshots
| Main Tool Window | Settings | Shortcuts dialog |
|----------------|---------------------|-------------------|
| ![Main Tool Window](/screens/main.png?raw=true) | ![Settings](/screens/settings.png?raw=true) | ![Shortcuts Dialog](/screens/shortcuts.png?raw=true) |

##### Path Testers
| ADB Tester | scrcpy Tester |
|------------|---------------|
|![ADB Tester](/screens/adb_test.png?raw=true)| ![scrcpy Tester](/screens/scrcpy_test.png?raw=true)|
 
## Contributing
- Feel free to make a PR for feature additions/bugs
- This repository follows Google (2-space indents) coding style, so please keep this in mind while making PRs

## Known Issues
- If you use the plugin's stop button to terminate an active scrcpy recording session with `mp4` extension, the `mp4` file is not playable. This is being caused by Java's `Process` not interrupting scrcpy session
 properly. Similar issue exists with `mkv` extension, although the `mkv` file can be played properly, it is not seekable.<br>
 Workaround: Close the scrcpy session with the window's close button
- [jadb](https://github.com/vidstige/jadb) throws an exception if any connected device becomes offline, hence devices list is not refreshed

## Roadmap
- [ ] Provide better UI/UX
- [ ] Add support for [sndcpy](https://github.com/rom1v/sndcpy)
- [ ] Check and install `scrcpy` updates 

## Donate
- This project would not have been possible without scrcpy (of course)!
- I've spent endless nights making this plugin work well! If you liked my work, consider donating me via [UPI (ID: `shripal17@okaxis`, Shripal Jain)](https://kutt.it/shripal17UPI) (works only in India, open link in
 smartphone) or [PayPal
](https://paypal.me/shripaul17)

## License

      
      Copyright 2021 Shripal Jain

      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at
      
         http://www.apache.org/licenses/LICENSE-2.0
      
      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.
