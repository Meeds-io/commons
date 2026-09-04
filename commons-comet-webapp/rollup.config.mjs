import path from "node:path";
import alias from "@rollup/plugin-alias";
import resolve from "@rollup/plugin-node-resolve";

export default {
  input: "src/main/webapp/js/src/cometd-entry.js",
  output: {
    file: "target/cometd/js/lib/cometd-bundle.js",
    format: "iife",
    name: "CometDBundle"
  },
  plugins: [
    alias({
      entries: [
        {
          find: "target",
          replacement: path.resolve("target")
        }
      ]
    }),
    resolve()
  ]
};