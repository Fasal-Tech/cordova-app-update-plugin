# cordova-app-update-plugin
This pluging enabels [In app update](https://developer.android.com/guide/playcore/in-app-updates) For cordova.

## Supports
* Flexible update
* Immidiate update
* Stalneess days For both type of updates
* Priority updates

## Configs
**Note** : 
    Stalness days: How many times the user has already been asked to update.
    _Setting it to 0 will trigger update flow on the 1st attempt._

* **flexibleUpdateStalenessDays** : Provide stalness days for flexible update
* **immediateUpdateStalenessDays** : Provide stalness days for immidiate update

## Examples

### Priority
_stalness will be ignored in this case_

* If priority of released app is >= 3 it will trigger *Immidiate update*
* If priority of released app is >= 1 it will trigger *Flexibke update*

_**Note:** To determine priority, Google Play uses an integer value between 0 and 5, with 0 being the default, and 5 being the highest priority. To set the priority for an update, use `inAppUpdatePriority` field under `Edits.tracks.releases` in the Google Play Developer API. Priority can only be set when rolling out a new release, and cannot be changed later._

### Flexible update with out staleness days
```javascript
window.plugins.updatePlugin.update({
    flexibleUpdateStalenessDays:0,
    immediateUpdateStalenessDays: 100000
});
```
### Immidiate update with out staleness days

```javascript
window.plugins.updatePlugin.update({
    flexibleUpdateStalenessDays: 100000,
    immediateUpdateStalenessDays: 0
});
```
### Flexible update with 2 staleness days and Immidiate update with 5 stalness days
```javascript
window.plugins.updatePlugin.update({
    flexibleUpdateStalenessDays: 2,
    immediateUpdateStalenessDays: 5
});
```