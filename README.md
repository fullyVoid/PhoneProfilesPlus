<img src="art/ic_launcher-web.png" width="100" height="100">  

PhoneProfilesPlus (aka PPP)
===========================

[![version](https://img.shields.io/badge/version-6.1-blue)](https://github.com/henrichg/PhoneProfilesPlus/releases/tag/6.1)
[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/henrichg/PhoneProfilesPlus/blob/master/LICENSE)
[![Crowdin](https://badges.crowdin.net/phoneprofilesplus/localized.svg)](https://crowdin.com/project/phoneprofilesplus)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=AF5QK49DMAL2U&currency_code=EUR)

__[Google Play release](https://play.google.com/store/apps/details?id=sk.henrichg.phoneprofilesplus)__  
Latest version is 4.2.0.3 and will never by upgraded.  
Reason: Google restrictions:  
1. Android - All Wi-Fi related functions not working since Android 10.  
2. Google Play - Google require (currently) Android 10+ for applications. In application exists gradle configuration parameter:  
   `targetSdkVersion targetSdk`  
   and in PPP must be target sdk = 28. Android 10 is 29.

In stores, in which is currently deployed PPP, restriction about target sdk does not apply.

### Another sources of PhoneProfilesPlus:

__[GitHub PPP release (direct download)](https://github.com/henrichg/PhoneProfilesPlus/releases/latest/download/PhoneProfilesPlus.apk)__

Use keyword "PhoneProfilesPlus" for search this application in these stores.

__[Galaxy Store PPP release (for Samsung devices only)](https://galaxystore.samsung.com/detail/sk.henrichg.phoneprofilesplus)__

__[Huawei AppGallery PPP release](https://appgallery.cloud.huawei.com/ag/n/app/C104501059?channelId=PhoneProfilesPlus+application&id=957ced9f0ca648df8f253a3d1460051e&s=79376612D7DD2C824692C162FB2F957A7AEE81EE1471CDC58034CD5106DAB009&detailType=0&v=&callType=AGDLINK&installType=0000)__  
__[Huawei AppGallery application (download)](https://consumer.huawei.com/en/mobileservices/appgallery/)__

__[APKPure PPP release](https://apkpure.com/p/sk.henrichg.phoneprofilesplus)__  
__[APKPure application (download)](https://apkpure.com/apkpure/com.apkpure.aegon)__

__[F-Droid PPP release](https://apt.izzysoft.de/fdroid/index/apk/sk.henrichg.phoneprofilesplus)__
&nbsp;&nbsp;&nbsp;_[How to add IzzyOnDroid repository to F-Droid application](https://apt.izzysoft.de/fdroid/index/info)_  
__[F-Droid application (download)](https://www.f-droid.org/)__

__Droid-ify (F-Droid alternative)__  
__[Droid-ify PPP release](https://apt.izzysoft.de/fdroid/index/apk/sk.henrichg.phoneprofilesplus)__  
__[Droid-ify applicaion (download)](https://apt.izzysoft.de/fdroid/index/apk/com.looker.droidify)__
&nbsp;&nbsp;&nbsp;_IzzyOnDroid repository is included_

---

__What is PhoneProfilesPlus:__

Android application - manually and by event triggered change of device settings like ringer mode, sounds, Wifi, Bluetooth, launcher wallpaper, ...  
This application is for configuration of device for life situations (at home, at work, in car, sleep, outside, ...) using Profiles.  
In it is also possibility to automatically activate Profiles by Events.  

[Privacy Policy](https://henrichg.github.io/PhoneProfilesPlus/privacy_policy.html)

_**** Please report me bugs, comments and suggestions to my e-mail: <henrich.gron@gmail.com>. Speed up the especially bug fixes. Thank you very much. ****_

_*** Please help me with translation, thank you: <https://crowdin.com/project/phoneprofilesplus> ***_


##### (HELP) How to grant (G1) permission - for profile parameters that require this permission
- [Show it](docs/grant_g1_permission.md)

##### (HELP) How to disable Wi-Fi scan throttling - useful for Wi-Fi scanning
- [Show it](docs/wifi_scan_throttling.md)

##### (HELP) How to configure airplane mode radios - useful for profile parameter "Airplane mode"
- [Show it](docs/airplane_mode_radios_config.md)

##### Features
- [Show it](docs/ppp_features.md)

##### Screenshots
- [[1]](art/phoneScreenshots/01.png),
[[2]](art/phoneScreenshots/02.png),
[[3]](art/phoneScreenshots/03.png),
[[4]](art/phoneScreenshots/04.png),
[[5]](art/phoneScreenshots/05.png),
[[6]](art/phoneScreenshots/06.png),
[[7]](art/phoneScreenshots/07.png),
[[8]](art/phoneScreenshots/08.png),
[[9]](art/phoneScreenshots/09.png),
[[10]](art/phoneScreenshots/10.png),
[[11]](art/phoneScreenshots/11.png),
[[12]](art/phoneScreenshots/12.png),
[[13]](art/phoneScreenshots/13.png)

##### Supported Android versions

- From Android 7.0
- minSdkVersion = 24
- targetSdkVersion = 28
- compiledSdkVersion = 33

##### Required external libs - open-source

- AndroidX library: appcompat, preferences, gridlayout, cardview, recyclerview, viewpager2, constraintlayout, workmanager - https://developer.android.com/jetpack/androidx/versions
- Google Material components - https://github.com/material-components/material-components-android
- google-gson - https://code.google.com/p/google-gson/
- ACRA - https://github.com/ACRA/acra
- guava - https://github.com/google/guava
- osmdroid - https://github.com/osmdroid/osmdroid
- TapTargetView - https://github.com/KeepSafe/TapTargetView
- doki - https://github.com/DoubleDotLabs/doki
- dashclock - https://github.com/romannurik/dashclock
- DexMaker - https://github.com/linkedin/dexmaker
- volley - https://github.com/google/volley
- ExpandableLayout - https://github.com/skydoves/ExpandableLayout
- SmoothBottomBar - https://github.com/ibrahimsn98/SmoothBottomBar
- RootTools (as module, code modified) - https://github.com/Stericson/RootTools
- RootShell (as module, code modified) - https://github.com/Stericson/RootShell
- time-duration-picker (as module, code modified) - https://github.com/svenwiegand/time-duration-picker
- android-betterpickers (as module, code modified) - https://github.com/code-troopers/android-betterpickers
- AndroidClearChroma (as module, code modified) - https://github.com/Kunzisoft/AndroidClearChroma
- RecyclerView-FastScroll (as module, code modified) - https://github.com/jahirfiquitiva/RecyclerView-FastScroll (original repository: https://github.com/timusus/RecyclerView-FastScroll)
- RelativePopupWindow (only modified class RelativePopupWindow) - https://github.com/kakajika/RelativePopupWindow
- SunriseSunset (only modified class SunriseSunset) - https://github.com/caarmen/SunriseSunset
- android-hidden-api (downloaded android.jar copied into folder \<android-sdk\>/android-XX) - https://github.com/Reginer/aosp-android-jar (original repository: https://github.com/anggrayudi/android-hidden-api)
- AndroidHiddenApiBypass - https://github.com/LSPosed/AndroidHiddenApiBypass
- NoobCameraFlash (as module, code modified) - https://github.com/Abhi347/NoobCameraFlash
- AutoStarter (only modified class AutoStartPermissionHelper.kt) - https://github.com/judemanutd/AutoStarter
- ToastCompat (as module, code modified) - https://github.com/PureWriter/ToastCompat
- Advance-Android-Tutorials (only modified class ZipManager.java) - https://github.com/stacktipslab/Advance-Android-Tutorials
- Multi-language_App (only modified class LocaleHelper.java) - https://github.com/anurajr1/Multi-language_App

##### Required external libs - not open-source

- Samsung Look - http://developer.samsung.com/galaxy/edge
