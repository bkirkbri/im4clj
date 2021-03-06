;   Copyright (c) deeperbydesign, inc 2012. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns ^{:doc "Stringify and run commands via im4java."
      :author "Kevin Neaton"}
  im4clj.run
  (:require [im4clj.im4java :as im4java]))

(defmulti stringify-method
  "Method used by im4clj.core/stringify."
  type)

(defmethod stringify-method java.lang.String [s] s)
(defmethod stringify-method clojure.lang.Named [n] (.getName n))
(defmethod stringify-method :default [o] (str o))

(defn stringify
  "Convert args to a flat sequence of strings.

   TODO: define stringify method for core types and move flatten to appropriate
   methods."
  [& args]
  {:post [(coll? %)
          (every? string? %)]}
  (->> args flatten (map stringify-method)))

(defn run
  "Run a command by name with the given opts. Accepts any 'stringify-able'
   type. Does not check (use-gm?).

   Prefer pre-defined commands e.g. im4clj.core/convert.

   Example Usage:

   (run :convert \"input.jpg\" :resize 100 \"output.jpg\")
   (run [:gm :convert] \"input.jpg\" :resize 100 \"output.jpg\")
  "
  [cmd & opts]
  (apply im4java/run (stringify cmd opts)))
