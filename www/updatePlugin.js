// Empty constructor
var exec = require("cordova/exec");

function validateConfig(config, platform) {
  if (!config)
    config = {
      type: "FLEXIBLE",
      stallDays: 0,
    };

  const defaultsMixed = {
    flexibleUpdateStalenessDays: 0,
    immediateUpdateStalenessDays: 0,
  };
  if (config.type === "MIXED") config = { ...defaultsMixed, ...config };
  else if (["FLEXIBLE", "IMMEDIATE"].includes(config.type))
    config = { stallDays: 0, ...config };
  else {
    const error = new Error(
      `Unknown type ${config.type} for platform ${platform} in config`
    );
    error.prototype.name = "UNKNOWN_TYPE_ERROR";
    throw error;
  }

  // alert options only for ios
  if (platform === "IOS") {
    const alertTitle = "New Version";
    const alertMessage =
      "version __version__ of __appName__ is available on the AppStore.";
    const alertUpdateButtonTitle = "Update";
    const alertCancelButtonTitle = "Not Now";
    config = {
      alertTitle,
      alertMessage,
      alertCancelButtonTitle,
      alertUpdateButtonTitle,
      ...config,
    };
  }
  return config;
}

function UpdatePlugin() {}

UpdatePlugin.prototype.update = function (
  successCallback,
  errorCallback,
  config
) {
  config = config || {};
  try {
    config.ANDROID = validateConfig(config.ANDROID, "ANDROID");
    config.IOS = validateConfig(config.IOS, "IOS");
    config = { ANDROID: config.ANDROID, IOS: config.IOS };
  } catch (err) {
    errorCallback(err);
  }

  exec(successCallback, errorCallback, "UpdatePlugin", "update", [config]);
};

// Installation constructor that binds updatePlugin to window
UpdatePlugin.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.updatePlugin = new UpdatePlugin();
  return window.plugins.updatePlugin;
};

cordova.addConstructor(UpdatePlugin.install);
