import {CometD} from "target/cometd-js/js/cometd/cometd.js";
import {AckExtension} from "target/cometd-js/js/cometd/AckExtension.js";
import {ReloadExtension} from "target/cometd-js/js/cometd/ReloadExtension.js";
import {TimeSyncExtension} from "target/cometd-js/js/cometd/TimeSyncExtension.js";

function createCometD() {
  return new CometD();
}

export default {
  CometD,
  AckExtension,
  ReloadExtension,
  TimeSyncExtension,
  createCometD
};