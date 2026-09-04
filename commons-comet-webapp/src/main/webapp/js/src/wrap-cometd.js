const fs = require("fs");

const input = "target/cometd/js/lib/cometd-bundle.js";
const output = input;

let bundle = fs.readFileSync(input, "utf8");
// Convert: var CometDBundle = (function () { ... })();
// into: (function () { ... })()
bundle = bundle
  .replace(/^var CometDBundle\s*=\s*/, "")
  .replace(/;\s*$/, "");
fs.writeFileSync(output, bundle);