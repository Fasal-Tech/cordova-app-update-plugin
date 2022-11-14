# cordova-in-app-update
This pluging enabels [In app update](https://developer.android.com/guide/playcore/in-app-updates) For cordova.

## Supports
* Flexible update
* Immeidiate update
* Stalneess days For both type of updates
* Priority updates

## Configs

* **type** : Provides option to user which strategy to use *IMMEDIATE* or *FLEXIBLE* or *MIXED* (_Required_)
* **stallDays** : Provides stalness day configuration for type *IMMEDIATE* and *FLEXIBLE*      (_Required for type IMMEDIATE and FLEXIBLE_)
* **flexibleUpdateStalenessDays** : Provide stalness days for flexible update in case of type *MIXED*  (_Required for type MIXED_)
* **immediateUpdateStalenessDays** : Provide stalness days for immeidiate update in case of type *MIXED*  (_Required for type MIXED_)
* **alertTitle (only applicable in ios config)** : Provides custom title for IOS alert box (default : "New Version")
* **alertMessage (only applicable in ios config)** : Provides custom message for IOS alert box (defualt: "version <__version__> of <__appName__> is available on the AppStore.")
* **alertCancelButtonTitle (only applicable in ios config)** : Provides custom cancel button title for IOS alert box (default: "Not Now")
*  **alertUpdateButtonTitle (only applicable in ios config)** :  Provides custom update button title for IOS alert box (default: "Update")
## Examples

### Flexible update with 5 staleness days in both android and ios
```javascript
window.plugins.updatePlugin.update(()=>{
//success callback
},()=>{
//error callback
},{
   IOS: {
       type: "FLEXIBLE",
       stallDays: 5
   },
   ANDROID: {
        type: "FLEXIBLE",
        stallDays: 5
    }
});
```
### Immediate update with 5 staleness days in both android and ios

```javascript
window.plugins.updatePlugin.update(()=>{
//success callback
},()=>{
//error callback
},{
   IOS: {
       type: "IMMEDIATE",
       stallDays: 5
   },
   ANDROID: {
        type: "IMMEDIATE",
        stallDays: 5
    }
});
```
### Flexible update with 2 staleness days and Immediate update with 5 stalness days in both android and ios
```javascript
window.plugins.updatePlugin.update(()=>{
//success callback
},()=>{
//error callback
},{
   IOS: {
        type: "MIXED",
        flexibleUpdateStalenessDays: 2,
        immediateUpdateStalenessDays: 5
   },
   ANDROID: {
        type: "MIXED",
        flexibleUpdateStalenessDays: 2,
        immediateUpdateStalenessDays: 5
    }
});
```

### Flexible update with 2 staleness days and Immediate update with 5 stalness days in both android and ios and custom messages for ios
```javascript
window.plugins.updatePlugin.update(()=>{
//success callback
},()=>{
//error callback
},{
   IOS: {
        type: "MIXED",
        flexibleUpdateStalenessDays: 2,
        immediateUpdateStalenessDays: 5,
        alertTitle: "Hola new update!",
        alertMessage: "Please update your app for new Features!",
        alertCancelButtonTitle: "Nope",
        alertUpdateButtonTitle: "Go ahead!"
   },
   ANDROID: {
        type: "MIXED",
        flexibleUpdateStalenessDays: 2,
        immediateUpdateStalenessDays: 5
    }
});
```

### Priority (_only applicable for android_)
_stalness will be ignored in this case_

* If priority of released app is >= 3 it will trigger *Immediate update*
* If priority of released app is >= 1 it will trigger *Flexible update*

_**Note:** To determine priority, Google Play uses an integer value between 0 and 5, with 0 being the default, and 5 being the highest priority. To set the priority for an update, use `inAppUpdatePriority` field under `Edits.tracks.releases` in the Google Play Developer API. Priority can only be set when rolling out a new release, and cannot be changed later._