;   Copyright (c) deeperbydesign, inc 2012. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns ^{:doc "Wrapper functions and utils for running shell commands with im4java."
      :author "Kevin Neaton"}
  im4clj.im4java
  (:require [im4clj.config :as config])
  (:import [org.im4java.core ImageCommand Operation]
           [org.im4java.process ProcessStarter]))

;; util fn's
(defn search-path
  "Get the system search path used by im4java."
  ([] (ProcessStarter/getGlobalSearchPath)))

(defn search-path!
  "Set the system search path used by im4java."
  ([path]
     (ProcessStarter/setGlobalSearchPath path)
     path))

;; coercian protocols
(defprotocol ICommand
  "An abstraction for building an ImageCommand."
  (command [this] "Returns a function that returns an ImageCommand instance built from whatever this is."))

(extend-protocol ICommand
  String
  (command [this] (ImageCommand. (into-array String [this])))
  clojure.lang.Named
  (command [this] (command (name this)))
  ImageCommand
  (command [this] this))

(defprotocol IArgument
  "Abstraction for building command-line arguments."
  (stringify [this] "Returns a sequence of strings representing argument(s) this"))

(extend-protocol IArgument
  String
  (stringify [this] (vector this))
  
  clojure.lang.Named
  (stringify [this] (stringify (name this)))

  nil
  (stringify [this] [])
  
  clojure.lang.IPersistentCollection
  (stringify [this] (vec (flatten (map stringify this))))
  
  Operation
  (stringify [this] (stringify (vec (.getCmdArgs this))))

  clojure.lang.IFn
  (stringify [this] (stringify (.invoke this)))
  
  Object
  (stringify [this] (stringify (str this))))

;; core fn's
(defn operation
  ""
  [& args]
  (-> (Operation.)
      (.addRawArgs (into-array String (stringify args)))))

(defn- adjust-for-gm
  "Adjusts an ImageCommand to use GraphicsMagick based on the setting of use-gm?"
  [cmd]
  (let [cmd-strs (filter #(not (= "gm" %)) (.getCommand cmd))
        cmd-strs (if (config/use-gm?) (cons "gm" cmd-strs) cmd-strs)]
    ;;(doto cmd (.setCommand (into-array String cmd-strs)))
    ;; we should be using the above, but there is a bug in im4java
    ;; where .setCommand appends to the command instead of replacing
    ;; it.
    ;; For now, we create a new ImageCommand with the adjusted command
    (ImageCommand. (into-array String cmd-strs))))

(defn run
  "Create and run an im4java ImageCommand from the arg(s) provided.

   Example Usage:

   (run \"convert\" \"input.jpg\" \"resize\" \"100\" \"output.jpg\")"
  [cmd & args]
  {:pre [(satisfies? ICommand cmd)]}
  (let [cmd (adjust-for-gm (command cmd))
        op  (operation args)]
    (.run cmd op (into-array String []))))