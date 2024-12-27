(ns repl-kit.theme
  (:require [clojure.java.io :as io])
  (:import [org.fife.ui.rsyntaxtextarea Theme]))

(defn apply-theme [text-area res-name]
  (let [theme (Theme/load (->
               res-name
               io/resource
               .openStream)
              (.getFont text-area))]
    (.apply theme text-area)))

(defn apply-dark-theme [text-area]
  (apply-theme 
   text-area 
   "org/fife/ui/rsyntaxtextarea/themes/dark.xml"))

(comment
  (->
  "org/fife/ui/rsyntaxtextarea/themes/dark.xml" 
   io/resource
   slurp)
  ;;
  )